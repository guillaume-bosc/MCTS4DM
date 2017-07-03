package liris.cnrs.fr.dm2l.mcts4dm;

import org.apache.lucene.util.OpenBitSet;

/**
 * This classes represents a patterns,
 * it's comparable so that we can't sort pattern set easily
 * with Collections.sort(.) before filtering redundant patterns.
 * 
 * @author Mehdi Kaytoue
 *
 */
public class Pattern implements Comparable<Pattern> {
	double quality;
	OpenBitSet extent;

	public Pattern(double quality, OpenBitSet extent) {
		this.quality = quality;
		this.extent = extent;
	}
	public String displayExtent(){
		String res = "";
		for (int id = extent.nextSetBit(0); id > -1 ; id = extent.nextSetBit(id+1)){
			if (!res.isEmpty())
				res +=",";
			res+=id;
		}
		return "[" + res + "]";

	}
	@Override
	public int compareTo(Pattern o) {
		if (this.quality < o.quality)
			return 1;
		else if (this.quality > o.quality)
			return -1;

		/*if (o.extent.cardinality() < this.extent.cardinality())
			return 1;
		else if (o.extent.cardinality() > this.extent.cardinality())
			return -1;
		*/
		
		return 0;
	}
	@Override
	public String toString()
	{
		return this.quality + " - " + this.displayExtent();
	}
}