package liris.cnrs.fr.dm2l.mcts4dm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.liacs.subdisc.AttributeType;

/**
 * The task to launch a beam search with Cortana
 * @author guillaume
 *
 */
public class TaskCortana extends Task {
	private final int beamwidth;
	private AttributeType dataType;

	public TaskCortana(String base, int minSupp, int width, Data.DataType type) {
		this.base = base;
		this.minSupp = minSupp;
		this.name = "BeamSearch-" + width;
		this.beamwidth = width;
		this.dataType = AttributeType.NUMERIC;
		if (type == Data.DataType.BOOLEAN)
			this.dataType = AttributeType.BINARY;
		else if (type == Data.DataType.NOMINAL)
			this.dataType = AttributeType.NOMINAL;
		else if (type == Data.DataType.NUMERIC)
			this.dataType = AttributeType.NUMERIC;
	}

	@Override
	public List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> call() {
		System.out.println("**Launch " + this.name);
		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> res = null;
		try {
			long start = System.currentTimeMillis();
			res = Main.cortanaBeamSearch(base, dataType, minSupp, beamwidth);
			this.runtime = System.currentTimeMillis() - start;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public String toString() {
		return "TaskCortana [beamwidth=" + beamwidth + ", dataType=" + dataType + ", runtime=" + runtime + ", name="
				+ name + ", minSupp=" + minSupp + ", base=" + base + "]";
	}

	@Override
	public Set<String> getTimeout() {
		Set<String> res = new HashSet<String>();
		for (int i = 0; i < Main.BEAM_WIDTH.length; i++) {
			if (Main.BEAM_WIDTH[i] >= beamwidth) {
				res.add("BeamSearch-" + Main.BEAM_WIDTH[i]);
			}
		}
		return res;
	}
}
