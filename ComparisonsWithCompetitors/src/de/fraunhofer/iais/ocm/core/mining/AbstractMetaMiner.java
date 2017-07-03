package de.fraunhofer.iais.ocm.core.mining;

public abstract class AbstractMetaMiner {
	
	private MiningAlgorithm entailedAlgorithm;

	public MiningAlgorithm getEntailedAlgorithm() {
		MiningAlgorithm currentAlgorithm = entailedAlgorithm;
		
		if (entailedAlgorithm instanceof AbstractMetaMiner){
			return ((AbstractMetaMiner) currentAlgorithm).getEntailedAlgorithm();
		} else {
			return currentAlgorithm;
		}
	}

}
