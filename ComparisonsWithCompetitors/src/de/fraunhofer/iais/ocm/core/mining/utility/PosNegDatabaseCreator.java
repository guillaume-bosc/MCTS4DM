package de.fraunhofer.iais.ocm.core.mining.utility;

import java.util.HashSet;
import java.util.List;

import mime.plain.weighting.PosNegDbInterface;
import de.fraunhofer.iais.ocm.core.mining.patternsampling.ConsaptUtils;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.InverseOfDominantPosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.InverseProbabilityPosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.PCAMULPosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.PCAPosNegDecider;
import de.fraunhofer.iais.ocm.core.mining.utility.PosNegDecider.RandomPosNegDecider;
import de.fraunhofer.iais.ocm.core.model.data.Attribute;
import de.fraunhofer.iais.ocm.core.model.data.DataTable;

public interface PosNegDatabaseCreator {

	public static class PosNegDatabaseByFirstAttribute implements
			PosNegDatabaseCreator {

		@Override
		public PosNegDbInterface createDb(DataTable dataTable,
				List<Attribute> targetAttributes) {
			return ConsaptUtils.createPosNegTransactionDBByFirstAttribute(
					dataTable, targetAttributes);
		}

	}

	public static class PosNegDatabaseByFirstTwoAttributes implements
			PosNegDatabaseCreator {

		@Override
		public PosNegDbInterface createDb(DataTable dataTable,
				List<Attribute> targetAttributes) {
			return ConsaptUtils.createPosNegTransactionDBByFirstTwoAttributes(
					dataTable, targetAttributes);
		}

	}

	public static class InverseOfDominantPosNegDatabase implements PosNegDatabaseCreator {

		@Override
		public PosNegDbInterface createDb(DataTable dataTable,
				List<Attribute> targetAttributes) {
			return ConsaptUtils.createPosNegDb(dataTable,
					new HashSet<Attribute>(targetAttributes),
					new InverseOfDominantPosNegDecider(targetAttributes));
		}
	}

	public static class InverseProbabilityPosNegDatabase implements
			PosNegDatabaseCreator {

		@Override
		public PosNegDbInterface createDb(DataTable dataTable,
				List<Attribute> targetAttributes) {
			return ConsaptUtils.createPosNegDb(dataTable,
					new HashSet<Attribute>(targetAttributes),
					new InverseProbabilityPosNegDecider(targetAttributes));
		}
	}

	public static class RandomPosNegDatabase implements PosNegDatabaseCreator {

		@Override
		public PosNegDbInterface createDb(DataTable dataTable,
				List<Attribute> targetAttributes) {
			return ConsaptUtils.createPosNegDb(dataTable,
					new HashSet<Attribute>(targetAttributes),
					new RandomPosNegDecider(targetAttributes));
		}
	}

	public static class PosNegDatabaseUsingPCA implements PosNegDatabaseCreator {

		@Override
		public PosNegDbInterface createDb(DataTable dataTable,
				List<Attribute> targetAttributes) {
			return ConsaptUtils.createPosNegDb(dataTable,
					new HashSet<Attribute>(targetAttributes),
					new PCAPosNegDecider(targetAttributes));
		}
	}

	public static class PosNegDatabaseUsingPCAMUL implements
			PosNegDatabaseCreator {

		@Override
		public PosNegDbInterface createDb(DataTable dataTable,
				List<Attribute> targetAttributes) {
			return ConsaptUtils.createPosNegDb(dataTable,
					new HashSet<Attribute>(targetAttributes),
					new PCAMULPosNegDecider(targetAttributes));
		}
	}

	public PosNegDbInterface createDb(DataTable dataTable,
			List<Attribute> targetAttributes);
}
