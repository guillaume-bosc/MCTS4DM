package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.util.Pair;

import java.util.List;


/**
 * User: paveltokmakov
 * Date: 25/11/13
 */
public class CalibratedCategoricalFeature extends CalibratedFeature {

    private int countPos = 1;

    private int countNeg = 1;

    public CalibratedCategoricalFeature(AbstractFeature baseFeature, List<Pair<Pattern, Pattern>> trainingData) {
        super(baseFeature, trainingData);
    }

    @Override
    double calibrate(double value) {
        if(getTrainingData().size() == 0) {
            return value;
        }

        if(!checkAndUpdateTrainingSize()) {
            updateCountPos();
            updateCountNeg();
        }

        if(value == 1) {
            return Math.log(getProbabilityNonNegative() / getProbabilityNonPositive());
        } else {
            return Math.log(getProbabilityNonPositive() / getProbabilityNonNegative());
        }
    }

    private double getProbabilityNonPositive() {
        return 1 - ((double) countPos) / (getTrainingData().size() + 4);
    }

    private void updateCountPos() {
        countPos = 1;

        for(Pair<Pattern, Pattern> example: getTrainingData()) {
            if(getBaseFeature().value(example.getElement0()) == 1 && getBaseFeature().value(example.getElement1()) == 0) {
                countPos++;
            }
        }
    }

    private double getProbabilityNonNegative() {
        return 1 - ((double) countNeg) / (getTrainingData().size() + 4);
    }

    private void updateCountNeg() {
        countNeg = 1;

        for(Pair<Pattern, Pattern> example: getTrainingData()) {
            if(getBaseFeature().value(example.getElement0()) == 0 && getBaseFeature().value(example.getElement1()) == 1) {
                countNeg++;
            }
        }
    }
}
