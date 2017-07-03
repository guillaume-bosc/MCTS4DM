package de.fraunhofer.iais.ocm.core.model;

import java.util.Collection;

/**
 * User: paveltokmakov
 * Date: 02/12/13
 */
public interface AlgorithmPreferenceModel {

    public int selectAlgorithm();

    public void resetModel(int numAlgorithms);

    public void tellReward(int algIndex, double reward);

    public Collection<Double> getProbabilities() throws UnsupportedOperationException;

//    public String getPreferencesAsString();
}
