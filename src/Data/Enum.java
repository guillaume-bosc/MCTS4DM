package Data;

public class Enum {
	public enum UCB {
		UCB1, UCT, UCBSP, UCBTuned, DFSUCT
	}

	public enum RefineExpand {
		Direct, Generator, TunedGenerator, Prefix
	}

	public enum DuplicatesExpand {
		None, AMAF, Order
	}

	public enum RefineRollOut {
		Direct, Large
	}

	public enum RewardPolicy {
		Terminal, RandomPick, MeanPath, MaxPath, MeanTopK
	}

	public enum MemoryPolicy {
		None, AllEvaluated, TopK
	}

	public enum UpdatePolicy {
		Mean, Max, MeanTopK
	}

	public enum Measure {
		WRAcc, F1, RelativeF1, WeightedRelativeF1, WKL, FBeta, RelativeFBeta, WeightedRelativeFBeta, RAcc, Acc, HammingLoss, ZeroOneLoss, ContingencyTable, Jaccard, Entropy, MutualInformation
	}

	public enum MctsType {
		Independant, Unique, Subset
	}

	public enum Redundancy {
		JaccardSupportDescription, JaccardSupportDescriptionTarget, SumJaccard
	}
}
