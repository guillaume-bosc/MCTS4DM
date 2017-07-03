package de.fraunhofer.iais.ocm.core.model.utility.features;


import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.util.Pair;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * Date: 25/11/13
 */
public abstract class CalibratedFeature extends AbstractFeature {

    private AbstractFeature baseFeature;

    private List<Pair<Pattern, Pattern>> trainingData;

    private int lastTrainingSize = 0;

    public CalibratedFeature(AbstractFeature baseFeature, List<Pair<Pattern, Pattern>> trainingData) {
        super("calibrated " + baseFeature.getDescription());
        this.baseFeature = baseFeature;
        this.trainingData = trainingData;
    }

    abstract double calibrate(double value);

    @Override
    public double value(Pattern pattern) {
        return calibrate(baseFeature.value(pattern));
    }

    @Override
    public boolean isCategorical() {
        return false;
    }

    boolean checkAndUpdateTrainingSize() {
        if(trainingData.size() != lastTrainingSize) {
            lastTrainingSize = trainingData.size();
            return false;
        }

        return true;
    }

    List<Pair<Pattern, Pattern>> getTrainingData() {
        return trainingData;
    }

    AbstractFeature getBaseFeature() {
        return baseFeature;
    }

    int getLastTrainingSize() {
        return lastTrainingSize;
    }
}
