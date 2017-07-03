package de.fraunhofer.iais.ocm.core.util;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class Miscellaneous {

	public Properties readProperties(String file) {
		try {
			InputStream is = getClass().getResourceAsStream(file);
			Properties prop = new Properties();
			prop.load(is);
			return prop;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to read from " + file + " file.");
		}
		return null;
	}

	public static double l2Distance(List<Double> vectorA, List<Double> vectorB) {
		double prod = 0.;
		for (int i = 0; i < vectorA.size(); i++) {
			prod += Math.pow(vectorA.get(i) - vectorB.get(i), 2.);
		}
		return Math.sqrt(prod);
	}
	
    public static double cosineDistance(List<Double> vectorA, List<Double> vectorB) {
		return 1.0 - Miscellaneous.cosineSimilarity(vectorA, vectorB);
    }

	public static double cosineSimilarity(List<Double> vectorA,
			List<Double> vectorB) {
		return scalarProduct(vectorA, vectorB) / (l2Norm(vectorA) * l2Norm(vectorB));
	}

	public static double l2Norm(List<Double> vector) {
		return Math.sqrt(scalarProduct(vector, vector));
	}
	
    public static double scalarProduct(List<Double> vectorA, List<Double> vectorB) {
		double res = 0.;
		for (int i = 0; i < vectorA.size(); i++) {
			res += vectorA.get(i) * vectorB.get(i);
		}
		return res;
	}
    
    public static double logit(double x) {
    	return 1.0/(1.0+Math.exp(-1.0*x));
    }

}
