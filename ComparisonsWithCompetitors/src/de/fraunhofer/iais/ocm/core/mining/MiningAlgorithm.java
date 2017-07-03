package de.fraunhofer.iais.ocm.core.mining;

import java.util.Collection;

import de.fraunhofer.iais.ocm.core.model.data.DataTable;
import de.fraunhofer.iais.ocm.core.model.pattern.Pattern;
import de.fraunhofer.iais.ocm.core.model.utility.PatternUtilityModel;

/**
 * User: paveltokmakov Date: 31/10/13
 */
public interface MiningAlgorithm {

	/**
	 * Initiates mining process. Mining results have to be converted to the
	 * PatternBean format and accumulated in an internal data structure.
	 * 
	 * @param dataTable
	 *            current state of the user's analysis session
	 * @throws Exception
	 *             exception
	 */
	public void mine(DataTable dataTable, PatternUtilityModel patternUtilityModel) throws Exception;

	/**
	 * Sets stop status of the mining algorithm. If status is set to true mining
	 * should be stopped as fast as possible.
	 * 
	 * @param stop
	 *            stop status
	 */
	public void setStop(boolean stop);

	/**
	 * Returns stop status of the algorithm
	 * 
	 * @return stop status
	 */
	public boolean isStop();

	/**
	 * Returns mining results accumulated during the last mining call. This
	 * method can only be called if the stop status is true. Otherwise an
	 * exception should be thrown.
	 * 
	 * @return Accumulated mining results
	 * @throws IllegalStateException
	 *             in case it is called in stop == false state
	 */
	public Collection<Pattern> getResults() throws IllegalStateException;

}
