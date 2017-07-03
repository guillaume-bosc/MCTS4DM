package liris.cnrs.fr.dm2l.mcts4dm;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The task to launch the exhaustive search SD-Map* implemented in the VIKAMINE
 * tool
 * 
 * @author guillaume
 *
 */
public class TaskVikamine extends Task {
	private final int algo;

	public TaskVikamine(int algo, String base, int minSupp) {
		this.algo = algo;
		this.base = base;
		this.minSupp = minSupp;
		if (algo == Main.SD_MAP)
			this.name = "SDMAP";
		else
			this.name = "BeamSearch";
	}

	@Override
	public List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> call() {
		System.out.println("**Launch " + this.name);
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> res = null;
		try {
			long start = System.currentTimeMillis();
			res = Main.vikamine(base, "Class", algo, minSupp);
			this.runtime = System.currentTimeMillis() - start;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public String toString() {
		return "TaskVikamine [minSupp=" + minSupp + ", runtime=" + runtime + "]";
	}

	@Override
	public Set<String> getTimeout() {
		Set<String> res = new HashSet<String>();
		res.add(this.name);
		return res;
	}

}