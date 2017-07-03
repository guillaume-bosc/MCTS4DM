package de.fraunhofer.iais.ocm.core.model;

import java.util.*;


/**
 * User: paveltokmakov
 * Date: 02/12/13
 */
public abstract class AbstractAlgorithmPreferenceModel implements AlgorithmPreferenceModel {

    private int numOfAlgorithms;

    public AbstractAlgorithmPreferenceModel() {
    }

    public AbstractAlgorithmPreferenceModel(int numOfAlgorithms) {
        resetModel(numOfAlgorithms);
    }

    public void resetModel(int numAlgorithms) {
        this.numOfAlgorithms = numAlgorithms;
    }

//    public String getPreferencesAsString() {
//        return getPreferences().toString();
//    }

    int getNumOfAlgorithms() {
        return numOfAlgorithms;
    }

    void setNumOfAlgorithms(int numOfAlgorithms) {
        this.numOfAlgorithms = numOfAlgorithms;
    }
}
