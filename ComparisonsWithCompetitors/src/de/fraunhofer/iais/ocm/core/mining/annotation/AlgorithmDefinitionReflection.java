package de.fraunhofer.iais.ocm.core.mining.annotation;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.ocm.core.mining.AlgorithmCategory;

/**
 * User: bjacobs
 * Date: 11.03.14
 * Time: 15:33
 */

public class AlgorithmDefinitionReflection {
    
    private List<AlgorithmDefinition> annotatedAlgorithms;

    public AlgorithmDefinitionReflection() {
    }

    public List<AlgorithmDefinition> getAnnotatedAlgorithms() {
        return annotatedAlgorithms;
    }

    public void setAnnotatedAlgorithms(List<AlgorithmDefinition> annotatedAlgorithms) {
      this.annotatedAlgorithms = annotatedAlgorithms;
    }

    public List<AlgorithmDefinition> getAlgorithmsByCategory(AlgorithmCategory cat) {
        List<AlgorithmDefinition> result = new ArrayList<AlgorithmDefinition>();

        for (AlgorithmDefinition algo : annotatedAlgorithms) {
            if (algo.getCategory() == cat) {
                result.add(algo);
            }
        }
        return result;
    }
}
