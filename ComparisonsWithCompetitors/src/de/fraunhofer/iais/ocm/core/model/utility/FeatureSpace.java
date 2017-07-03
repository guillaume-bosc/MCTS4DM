package de.fraunhofer.iais.ocm.core.model.utility;

import java.util.List;

import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.features.AbstractFeature;

/**
 * Interface to be implemented by classes that represent vector spaces of
 * patterns, which in turn can be used by pattern utility models. We require
 * vector spaces to be equipped with an inner product; consequently the
 * interface also includes the derived functions norm and cosine as convenience
 * for clients and in order to allow efficient implementations
 * 
 * @author Mario Boley
 * 
 */
public interface FeatureSpace {

	// public double innerProduct(Pattern p1, Pattern p2);
	//
	// /**
	// * must implement the norm induced by the inner product, i.e., square root
	// * of <p,p>. After Java 8 update we can give default implementation on
	// * interface level
	// */
	// public double norm(Pattern p);

	/**
	 * must implement cosine induced by inner product, i.e., <p1,p2> divided by
	 * norms of p1 and p2, again using the norm induced by inner product
	 */
	public double cosine(Pattern p1, Pattern p2);

	/**
	 * distance induced by the inner product (norm of p1-p2)
	 */
	public double distance(Pattern p1, Pattern p2);


}
