package liris.cnrs.fr.dm2l.mcts4dm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Data.Subgroup;
import Process.Misere;

/**
 * The task to launch Misere, a sampling algorithm
 * @author guillaume
 *
 */
public class TaskMisere extends Task {
	private final String confFile = "Misere.conf";
	private final Data.Enum.Measure measure;
	private final int nbIterations;
	private final Data.DataType dataType;
	private final int nbLabels;

	public TaskMisere(String d, Data.Enum.Measure m, int i, int ms, Data.DataType dt, int nl) {
		this.base = d;
		this.measure = m;
		this.nbIterations = i;
		this.minSupp = ms;
		this.dataType = dt;
		this.nbLabels = nl;
		this.name = "Misere-" + i;
	}

	@Override
	public List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> call() {
		long start, stop;
		System.out.println("**Launch " + this.name);
		Main.updateData(base, confFile, nbLabels);
		Main.updateMeasure(measure, confFile);
		Main.updateNbIterations(nbIterations, confFile);
		Main.updateMinSupp(minSupp, confFile);
		Main.updateDataType(dataType, confFile);
		start = System.currentTimeMillis();
		List<Subgroup> res = Misere.misere(confFile);
		stop = System.currentTimeMillis();

		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resPattern = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();

		this.runtime = stop - start;
		for (Subgroup s : res)
			resPattern.add(Main.convertSubgroupToPattern(s));
		return resPattern;
	}

	@Override
	public String toString() {
		return "TaskMisere [measure=" + measure + ", nbIterations=" + nbIterations + ", minSupp=" + minSupp
				+ ", runtime=" + runtime + "]";
	}
	
	@Override
	public Set<String> getTimeout() {
		Set<String> res = new HashSet<String>();
		for (int i = 0 ; i < Main.NB_ITERATIONS.length ; i++) {
			if ( Main.NB_ITERATIONS[i] >= nbIterations) {
				res.add("Misere-"+ Main.NB_ITERATIONS[i]);
			}
		}
		return res;
	}

}