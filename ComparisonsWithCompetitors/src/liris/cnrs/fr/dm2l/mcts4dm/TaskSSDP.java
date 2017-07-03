package liris.cnrs.fr.dm2l.mcts4dm;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.trees.j48.NBTreeClassifierTree;

public class TaskSSDP extends Task{
	private final int nbOutput;

	public TaskSSDP(String d, int ms, int count) {
		this.base = d;
		this.minSupp = ms;
		this.nbOutput = count;
		this.name = "SSDP-" + count;
	}

	@Override
	public List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> call() {
		long start, stop;
		System.out.println("**Launch " + this.name);
		start = System.currentTimeMillis();
		List<Pattern> res = null;
		try {
			res = Main.ssdp(base, nbOutput, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		stop = System.currentTimeMillis();
		this.runtime = stop-start;

		return res;
	}

	@Override
	public String toString() {
		return "TaskSSDP [nbOutput=" + nbOutput + ", runtime=" + runtime + ", name=" + name + ", minSupp=" + minSupp
				+ ", base=" + base + "]";
	}

	@Override
	public Set<String> getTimeout() {
		Set<String> res = new HashSet<String>();
		
		for (int i = 0 ; i < Main.NB_ITERATIONS.length ; i++) {
			int nbCount = (Main.NB_ITERATIONS[i] / 10);
			if ( nbCount >= nbOutput) {
				res.add("SSDP-"+ nbCount);
			}
		}
		
		return res;
	}

	
}
