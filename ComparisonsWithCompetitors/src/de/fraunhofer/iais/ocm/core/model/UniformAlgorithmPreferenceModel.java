package de.fraunhofer.iais.ocm.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


/**
 * User: paveltokmakov
 * Date: 02/12/13
 */
public class UniformAlgorithmPreferenceModel extends AbstractAlgorithmPreferenceModel {

    public int selectAlgorithm() {
        Random random = new Random();

        return random.nextInt(getNumOfAlgorithms());
    }

    public void tellReward(int algIndex, double reward) {
    }

    public synchronized Collection<Double> getProbabilities() {
        List<Double> preferences = new ArrayList<Double>();

        for(int i = 0; i < getNumOfAlgorithms(); i++) {
            preferences.add(1. / getNumOfAlgorithms());
        }

        return preferences;
    }
}
