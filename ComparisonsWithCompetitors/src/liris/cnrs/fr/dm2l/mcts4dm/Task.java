package liris.cnrs.fr.dm2l.mcts4dm;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class Task implements Callable<List<liris.cnrs.fr.dm2l.mcts4dm.Pattern>> {
	public long runtime;
	public String name;
	public int minSupp;
	public  String base;
	
	public abstract Set<String> getTimeout();
}
