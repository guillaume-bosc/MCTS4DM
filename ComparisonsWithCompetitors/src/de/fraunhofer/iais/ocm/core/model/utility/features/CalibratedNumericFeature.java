package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.util.Pair;

import java.util.HashSet;
import java.util.List;


/**
 * User: paveltokmakov
 * Date: 25/11/13
 */
public class CalibratedNumericFeature extends CalibratedFeature {

	private double mean = 0.;
	private double standardDeviation = 0.;
	
    public CalibratedNumericFeature(AbstractFeature baseFeature, List<Pair<Pattern, Pattern>> trainingData) {
        super(baseFeature, trainingData);
    }

    @Override
    double calibrate(double value) {
    	 if(getTrainingData().size() < 5) {
             return value;
         }

         if(!checkAndUpdateTrainingSize()) {
        	 updateMeanAndStandardDeviation();
         }

         return (value - mean) / standardDeviation;
    }
    
    private void updateMeanAndStandardDeviation() {
    	mean = 0.;
    	standardDeviation = 0.;
    	
    	HashSet<Pattern> trainingData = removeDuplication(getTrainingData());
    	
    	for (Pattern example : trainingData) {
    		mean += getBaseFeature().value(example);
    	}
    	mean = mean / trainingData.size();
    	
    	for (Pattern example : trainingData) {
    		standardDeviation += Math.pow((getBaseFeature().value(example) - mean), 2.);
    	}
    	standardDeviation = Math.sqrt(standardDeviation / trainingData.size());
    }
    private HashSet <Pattern> removeDuplication(List<Pair<Pattern, Pattern>> trainingData) {
    	HashSet<Pattern> patternSet = new HashSet<Pattern>();
    	for (Pair<Pattern, Pattern> example : trainingData) {
    		patternSet.add(example.getElement0());
    		patternSet.add(example.getElement1());
    	}
    	return patternSet;
    	
    }
    
}
