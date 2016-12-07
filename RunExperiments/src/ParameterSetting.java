public class ParameterSetting {
	// The folder containing the results
	String resultFolderName;
	String attrFile;
	String targetFile;
	Enum.AttrType attrType;

	// Default parameters
	int minSupp = 30;
	int nbIter = 5000;
	int maxOutput = 50;
	double maxRedundancy = 1.4;
	int maxLength = 5;
	Enum.Measure measure;
	int xBeta = 100;
	int lBeta = 70;
	Enum.UCB UCB = Enum.UCB.UCBSP;
	Enum.RefineExpand refineExpand = Enum.RefineExpand.TunedGenerator;
	Enum.DuplicatesExpand duplicatesExpand = Enum.DuplicatesExpand.AMAF;
	int pathLength = -1;
	Enum.RefineRollOut refineRollOut = Enum.RefineRollOut.Large;
	int jumpingLarge = 30;
	Enum.RewardPolicy rewardPolicy = Enum.RewardPolicy.MaxPath;
	int topKRollOut = 1;
	Enum.MemoryPolicy memoryPolicy = Enum.MemoryPolicy.TopK;
	int topKMemory = 1;
	Enum.UpdatePolicy updatePolicy = Enum.UpdatePolicy.Max;
	int topKUpdate = 3;
	@Override
	public String toString() {
		return "resultFolderName=" + resultFolderName + ",\tattrFile=" + attrFile + ",\ttargetFile="
				+ targetFile + ",\tattrType=" + attrType + ",\tminSupp=" + minSupp + ",\tnbIter=" + nbIter + ",\tmaxOutput="
				+ maxOutput + ",\tmaxRedundancy=" + maxRedundancy + ",\tmaxLength=" + maxLength + ",\tmeasure=" + measure
				+ ",\txBeta=" + xBeta + ",\tlBeta=" + lBeta + ",\tUCB=" + UCB + ",\trefineExpand=" + refineExpand
				+ ",\tduplicatesExpand=" + duplicatesExpand + ",\tpathLength=" + pathLength + ",\trefineRollOut="
				+ refineRollOut + ",\tjumpingLarge=" + jumpingLarge + ",\trewardPolicy=" + rewardPolicy + ",\ttopKRollOut="
				+ topKRollOut + ",\tmemoryPolicy=" + memoryPolicy + ",\ttopKMemory=" + topKMemory + ",\tupdatePolicy="
				+ updatePolicy + ",\ttopKUpdate=" + topKUpdate;
	}

	

}
