package de.fraunhofer.iais.ocm.core.model.pattern.emm;

public class TheilSenLinearRegressionModelDistanceFunction implements
		ModelDistanceFunction {

	@Override
	public double distance(AbstractModel globalModel, AbstractModel localModel) {
		Double gSlope = ((TheilSenLinearRegressionModel) globalModel)
				.getSlope();
		Double lSlope = ((TheilSenLinearRegressionModel) localModel).getSlope();
		if (gSlope == null || lSlope == null) {
			return 0d;
		}
		
		// the vector is (1, gSlope * 1)
		double globalVectorNorm = Math.sqrt(1 + Math.pow(gSlope, 2));
		// the vector is (1, lSlope * 1)
		double localVectorNorm  = Math.sqrt(1 + Math.pow(lSlope, 2));
		
		// return the cosine between the two vectors
		double cosineSimilarity = (1 + gSlope * lSlope)
				/ (globalVectorNorm * localVectorNorm);
		return 1 - Math.abs(cosineSimilarity);
	}
	
	private double euclideanDistance(AbstractModel globalModel, AbstractModel localModel) {
		Double gSlope = ((TheilSenLinearRegressionModel) globalModel)
				.getSlope();
		Double gIntercept = ((TheilSenLinearRegressionModel) globalModel)
				.getIntercept();
		Double lSlope = ((TheilSenLinearRegressionModel) localModel).getSlope();
		Double lIntercept = ((TheilSenLinearRegressionModel) localModel)
				.getIntercept();
		
		/*
		 * when there is no two different
		 * covariants at least in given subgroup then 
		 * slope can not be estimated
		 */
		if (lSlope == null || lIntercept == null) {
			return 0d;
		}

		return Math.sqrt(Math.pow(gSlope - lSlope, 2)
				+ Math.pow(gIntercept - lIntercept, 2));
	}

}
