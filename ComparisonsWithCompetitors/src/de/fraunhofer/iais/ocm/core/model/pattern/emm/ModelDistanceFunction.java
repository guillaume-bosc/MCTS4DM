package de.fraunhofer.iais.ocm.core.model.pattern.emm;


public interface ModelDistanceFunction {

	public double distance(AbstractModel globalModel, AbstractModel localModel);
}
