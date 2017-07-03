package de.fraunhofer.iais.ocm.core.mining.parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: bjacobs
 * Date: 25.03.14
 * Time: 15:47
 */

public class ExecutionParameterSetup {
    private String algorithmName;
    private List<ParameterSetup> parameters = new ArrayList<ParameterSetup>();

    public ExecutionParameterSetup(String algorithmName, List<ParameterSetup> parameters) {
        this.algorithmName = algorithmName;
        this.parameters = parameters;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public List<ParameterSetup> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterSetup> parameters) {
        this.parameters = parameters;
    }

    public class ParameterSetup {
        private final String parameterType;
        private final String id;
        private final String value;

        public String getParameterType() {
            return parameterType;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        private ParameterSetup(String parameterType, String id, String value) {
            this.parameterType = parameterType;
            this.id = id;
            this.value = value;
        }
    }
}
