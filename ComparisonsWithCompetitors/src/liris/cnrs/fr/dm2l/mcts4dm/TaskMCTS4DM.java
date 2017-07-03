package liris.cnrs.fr.dm2l.mcts4dm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Data.Subgroup;
import Process.MCTS4DM;

public class TaskMCTS4DM extends Task {
	private final String confFile = "MCTS4DM.conf";
	private final Data.Enum.Measure measure;
	private final int nbIterations;
	private final Data.DataType dataType;
	private final int nbLabels;

	public TaskMCTS4DM(String d, Data.Enum.Measure m, int i, int ms, Data.DataType dt, int nl) {
		this.base = d;
		this.measure = m;
		this.nbIterations = i;
		this.minSupp = ms;
		this.dataType = dt;
		this.nbLabels = nl;
		this.name = "MCTS4DM-"+i;
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
		List<Subgroup> res2 = MCTS4DM.mcts4dm(confFile);
		stop = System.currentTimeMillis();
		
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resMcts = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();

		this.runtime = stop - start;
		for (Subgroup s : res2)
			resMcts.add(Main.convertSubgroupToPattern(s));
		return resMcts;
	}

	@Override
	public String toString() {
		return "TaskMCTS4DM [measure=" + measure + ", nbIterations=" + nbIterations + ", minSupp=" + minSupp
				+ ", runtime=" + runtime + "]";
	}
	
	@Override
	public Set<String> getTimeout() {
		Set<String> res = new HashSet<String>();
		for (int i = 0 ; i < Main.NB_ITERATIONS.length ; i++) {
			if ( Main.NB_ITERATIONS[i] >= nbIterations) {
				res.add("MCTS4DM-"+ Main.NB_ITERATIONS[i]);
			}
		}
		return res;
	}


}