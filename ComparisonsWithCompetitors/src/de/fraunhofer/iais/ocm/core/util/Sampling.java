package de.fraunhofer.iais.ocm.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Sampling {

	/** 
	 * Exhaustive sampling based on a list of positive weights.
	 * 
	 * @param weights list of positive potential weights
	 * @return index of input list drawn with a probability according to its weight
	 */
	public static int exhaustiveSamplingFromWeights(List<Double> weights) {
		List<Double> cumulativeWeights=new ArrayList<Double>(weights.size());
		double sum=0;
		for (int i=0; i<weights.size(); i++) {
			sum+=weights.get(i);
			cumulativeWeights.add(sum);
		}
		Random random=new Random();
		Double value=random.nextDouble()*sum;
		for (int i=0; i<cumulativeWeights.size(); i++) {
			if (value<=cumulativeWeights.get(i)) {
				return i;
			}
		}
		
		//this may never happen
		return -1;
	}
	
	/**
	 * returns random permutation of ints from 0 (inclusive) to n (exclusive)
	 */
	public static List<Integer> getPermutation(int n) {
		List<Integer> init=new ArrayList<Integer>(n);
		for (int i=0; i<n; i++) {
			init.add(i);
		}
		Random random=new Random();
		List<Integer> result=new ArrayList<Integer>(n);
		for (int i=0; i<n; i++) {
			int nextIndex=random.nextInt(init.size());
			result.add(init.get(nextIndex));
			init.remove(nextIndex);
		}
		return result;
	}
	
}
