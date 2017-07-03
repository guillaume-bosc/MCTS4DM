package de.fraunhofer.iais.ocm.core.model;

import java.util.*;

import de.fraunhofer.iais.ocm.core.util.Sampling;


/**
 * User: paveltokmakov
 * Date: 2/24/13
 */
public class BanditAlgorithmPreferenceModel extends AbstractAlgorithmPreferenceModel {

    private int constant = 100;

    private final double DEFAULT = 1.;

    private Map<Integer, Double> algorithmsWeights;
    
    private int miningRound = 1;
    
    public BanditAlgorithmPreferenceModel() {
        super();
    }

    public BanditAlgorithmPreferenceModel(int numOfAlgorithms) {
       super(numOfAlgorithms);
    }

    public BanditAlgorithmPreferenceModel(String rawWeights) {
        String [] weights = new String[0];
        if(rawWeights.length() != 0) {
            rawWeights = rawWeights.substring(1, rawWeights.length()-1);
            weights = rawWeights.split(",");
        }


        setNumOfAlgorithms(weights.length);
        algorithmsWeights = new HashMap<Integer, Double>();

        for(int i = 0; i < getNumOfAlgorithms(); i++) {
            algorithmsWeights.put(i, Double.parseDouble(weights[i]));
        }
    }

    public int selectAlgorithm() {
//        Random rand = new Random();
        List<Double> probabilities = getProbabilities();
        return Sampling.exhaustiveSamplingFromWeights(probabilities);
        
//        do {
//            int algIndex = rand.nextInt(probabilities.size());
//            double probability = probabilities.get(algIndex);
//
//            if(rand.nextDouble() <= probability) {
//                return algIndex;
//            }
//        } while (true);
    }

    public synchronized List<Double> getProbabilities() {
        Collection<Double> preferences = algorithmsWeights.values();
        List<Double> probabilities = new ArrayList<Double>();
        double gamma = gamma(miningRound);
        double sum = 0.0;

        for(Double preference: preferences) {
            sum += preference;
        }

        for(Double preference: preferences) {
            probabilities.add(((1 - gamma) * preference / sum) + gamma / getNumOfAlgorithms());
        }

        return probabilities;
    }

    private double eta(double miningRound) {
        return gamma(miningRound) / (2 * getNumOfAlgorithms());
    }

    private double gamma(double miningRound) {
        return 4 * getNumOfAlgorithms() * beta(miningRound) / (3 + beta(miningRound));
    }

    private double beta(double miningRound) {
    	double roundFactor=Math.max(constant, Math.pow(2,Math.log(miningRound)/Math.log(2)));
        return Math.sqrt(Math.log(10 * getNumOfAlgorithms()) / (getNumOfAlgorithms() * constant));
    }

    /**
     * Updates weights of the algorithms if the number of algorithms has been changed. In case some algorithms have
     * been discarded, their weights are deleted. If new algorithms have been introduced their weights are set accordingly
     * @param numAlgorithms new number of algorithms
     */
    public void resetModel(int numAlgorithms) {
        super.resetModel(numAlgorithms);

        algorithmsWeights = new LinkedHashMap<Integer, Double>();
        for (int i = 0; i < numAlgorithms; i++) {
            algorithmsWeights.put(i, DEFAULT);
        }
        miningRound=1;
    }

    public void tellReward(int algIndex, double reward) {
        List<Double> optimisticEstimates = getOptimisticEstimates(algIndex, reward);
        System.out.println(reward);

        for(int i = 0; i < getNumOfAlgorithms(); i++) {
            Double oldEstimate = algorithmsWeights.get(i);

            if(oldEstimate == null) {
                oldEstimate = DEFAULT;
            }

            double newEstimate = oldEstimate * Math.exp(eta(miningRound) * optimisticEstimates.get(i));
            algorithmsWeights.put(i, newEstimate);
        }
        miningRound++;
    }

    private List<Double> getOptimisticEstimates(int algIndex, double reward) {
        List<Double> probabilities = getProbabilities();
        List<Double> estimates = new ArrayList<Double>();

        for(int i = 0; i < getNumOfAlgorithms(); i++) {
            if(i == algIndex) {
                estimates.add((reward + beta(miningRound)) / probabilities.get(i));
            } else {
                estimates.add(beta(miningRound) / probabilities.get(i));
            }
        }

        return estimates;
    }

//    public Collection<Double> getPreferences() {
//        return algorithmsWeights.values();
//    }
}
