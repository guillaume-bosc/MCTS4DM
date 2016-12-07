
public class Enum {
	public enum AttrType {
		Numeric, Boolean, Nominal
	}

	public enum UCB {
		UCB1, UCT, UCBSP, UCBTuned, DFSUCT
	}

	public enum RefineExpand {
		Direct, Generator, TunedGenerator
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
		WRAcc, F1, WKL, FBeta
	}
}
