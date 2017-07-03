package de.fraunhofer.iais.ocm.core.mining.utility;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Double.parseDouble;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mime.plain.PlainTransaction;
import mime.plain.PlainTransactionDB;
import mime.plain.weighting.PosNegTransactionDb;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.InverseOfDominantPosNegDecider;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.data.NumericAttribute;

public interface RowWeightComputer {

	public static class UniformRowWeightComputer implements RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			for (PlainTransaction transaction : db.getTransactions()) {
				weights.put(transaction, 1.0);
			}
			return weights;
		}
	}

	public static class MeanDeviationRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {
			PosNegTransactionDb d = (PosNegTransactionDb) db;

			Map<PlainTransaction, Double> weights = newHashMap();
			Attribute targetAttribute = targets.get(0);
			if (!targetAttribute.isNumeric()) {
				return weights;
			}

			double mean = ((NumericAttribute) targetAttribute).getMean();
			double totalPos = Math.abs(((NumericAttribute) targetAttribute)
					.getMax() - mean);
			double totalNeg = Math.abs(mean
					- ((NumericAttribute) targetAttribute).getMin());
			// System.out.println("mean: " + mean + "["
			// + ((NumericAttribute) targetAttribute).getMin() + ":"
			// + ((NumericAttribute) targetAttribute).getMax() + "]"
			// + "totPos: " + totalPos + " totNeg: " + totalNeg);

			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			for (String value : targetAttribute.getValues()) {
				PlainTransaction transaction = it.next();

				double parseDouble = parseDouble(value);
				if (parseDouble < mean) {
					weights.put(transaction, 1. * Math.abs(parseDouble - mean)
							/ totalNeg);
				} else {
					weights.put(transaction, 1. * Math.abs(parseDouble - mean)
							/ totalPos);
				}
				// System.out.println(parseDouble + " " + mean);
				// System.out.println("IsNeg: "
				// + d.getTransactionsNeg().contains(transaction)
				// + ", IsPos: "
				// + d.getTransactionsPos().contains(transaction));
				// System.out.println(weights.get(transaction));
			}

			return weights;
		}
	}

	public static class PositiveMeanDeviationRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			Attribute targetAttribute = targets.get(0);
			if (!targetAttribute.isNumeric()) {
				return weights;
			}

			double mean = ((NumericAttribute) targetAttribute).getMean();

			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			for (String value : targetAttribute.getValues()) {
				weights.put(it.next(), max(parseDouble(value) - mean, 0));
			}

			return weights;
		}
	}

	public static class MultiplicatingMeanDeviationRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {
			Map<PlainTransaction, Double> weights = newHashMap();
			for (Attribute attribute : targets) {
				if (!attribute.isNumeric()) {
					return weights;
				}
			}

			DataTable dataTable = targets.get(0).getDataTable();
			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			for (List<String> row : dataTable.getDataTable()) {
				weights.put(it.next(), computeWeight(row, targets));
			}

			return weights;
		}

		private double computeWeight(List<String> row, List<Attribute> targets) {
			double weight = 1;
			for (Attribute attribute : targets) {
				double value = parseDouble(row.get(attribute.getIndexInTable()));
				double mean = ((NumericAttribute) attribute).getMean();
				weight *= Math.abs(value - mean);
			}
			return weight;
		}
	}

	public static class SquaredSumMeanDeviationRowWeightComputer implements
			RowWeightComputer {

		PosNegDecider pnd;

		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {


			Map<PlainTransaction, Double> weights = newHashMap();
			for (Attribute attribute : targets) {
				if (!attribute.isNumeric()) {
					return weights;
				}
			}

			DataTable dataTable = targets.get(0).getDataTable();
			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			for (List<String> row : dataTable.getDataTable()) {
				PlainTransaction next = it.next();
				weights.put(next, computeWeight(row, targets));
			}

			return weights;
		}

		private double computeWeight(List<String> row, List<Attribute> targets) {
			double weight = 0;
			for (Attribute attribute : targets) {
				double value = parseDouble(row.get(attribute.getIndexInTable()));
				double mean = ((NumericAttribute) attribute).getMean();
				double totalPos = Math.abs(((NumericAttribute) attribute)
						.getMax() - mean);
				double totalNeg = Math.abs(mean
						- ((NumericAttribute) attribute).getMin());
				
				double z = value >= mean ? totalPos : totalNeg;
				double v = (value - mean) / z;
				weight += v*v;
//				if (value >= mean) {
//					weight += 1. * Math.abs(value - mean) / totalPos;
//				} else {
//					weight += 1. * Math.abs(value - mean) / totalNeg;
//				}
			}
			return sqrt(weight);
		}
	}

	public static class TestRowWeightComputer implements RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {
			PosNegDecider pnd = new InverseOfDominantPosNegDecider(targets);
			Map<PlainTransaction, Double> weights = newHashMap();
			for (Attribute attribute : targets) {
				if (!attribute.isNumeric()) {
					return weights;
				}
			}

			DataTable dataTable = targets.get(0).getDataTable();
			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			int i = 0;
			for (List<String> row : dataTable.getDataTable()) {
				PlainTransaction next = it.next();
				if (pnd.isPos(i)) {
					weights.put(next, computePosWeight(row, targets));
				} else {
					weights.put(next, computeNegWeight(row, targets));
				}
			}

			return weights;
		}

		private double computePosWeight(List<String> row,
				List<Attribute> targets) {
			double weight = 0;
			for (Attribute attribute : targets) {
				double value = parseDouble(row.get(attribute.getIndexInTable()));
				double mean = ((NumericAttribute) attribute).getMean();
				double totalPos = Math.abs(((NumericAttribute) attribute)
						.getMax() - mean);
				double totalNeg = Math.abs(mean
						- ((NumericAttribute) attribute).getMin());
				if (value >= mean) {
					weight += 1. * Math.abs(value - mean) / totalPos;
				} else {
					weight += 1. * Math.abs(value - mean) / totalNeg;
				}
			}
			return weight;
		}

		private double computeNegWeight(List<String> row,
				List<Attribute> targets) {
			double weight = 0;
			for (Attribute attribute : targets) {
				double value = parseDouble(row.get(attribute.getIndexInTable()));
				double mean = ((NumericAttribute) attribute).getMean();
				double max = ((NumericAttribute) attribute).getMax();
				double totalPos = Math.abs(max - mean);
				double min = ((NumericAttribute) attribute).getMin();
				double totalNeg = Math.abs(mean - min);
				if (value >= mean) {
					weight += 1. * Math.abs(value - max) / totalPos;
				} else {
					weight += 1. * Math.abs(value - min) / totalNeg;
				}
			}
			return weight;
		}
	}

	public static class PCABasedRowWeightComputer implements RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {
			PCAEvaluator pcaEvaluator = new PCAEvaluator(targets);

			Map<PlainTransaction, Double> weights = newHashMap();
			for (Attribute attribute : targets) {
				if (!attribute.isNumeric()) {
					return weights;
				}
			}

			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			for (int i = 0; i < db.getTransactions().size(); i++) {
				PlainTransaction next = it.next();
				weights.put(next,
						Math.abs(pcaEvaluator.getDevFirstDimension(i)));
			}

			return weights;
		}
	}

	public static class PCAMULBasedRowWeightComputer implements
			RowWeightComputer {
		@Override
		public Map<PlainTransaction, Double> getRowWeights(
				List<Attribute> targets, PlainTransactionDB db) {
			PCAEvaluator pcaEvaluator = new PCAEvaluator(targets);

			Map<PlainTransaction, Double> weights = newHashMap();
			for (Attribute attribute : targets) {
				if (!attribute.isNumeric()) {
					return weights;
				}
			}

			Iterator<PlainTransaction> it = db.getTransactions().iterator();
			for (int i = 0; i < db.getTransactions().size(); i++) {
				PlainTransaction next = it.next();
				double weight = 1;
				for (int j = 0; j < Math.max(1,
						pcaEvaluator.getNumberOfComponents() - 1); j++) {
					weight *= Math.abs(pcaEvaluator.getDevForDimension(i, j));
				}
				weights.put(next, weight);
			}

			return weights;
		}
	}

	public Map<PlainTransaction, Double> getRowWeights(List<Attribute> targets,
			PlainTransactionDB db);

}
