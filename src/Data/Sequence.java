package Data;

import java.util.Comparator;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

public class Sequence {
	public List<OpenBitSet> sequence;
	public int length; // nb items
	public int size; // nb itemsets
	public int id;

	public Sequence(List<OpenBitSet> sequence, int id) {
		this.sequence = sequence;
		this.size = sequence.size();
		this.length = 0;
		for (OpenBitSet bs : sequence) {
			this.length += bs.cardinality();
		}
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String res = "";
		for (int i = 0; i < sequence.size(); i++) {
			if (!res.isEmpty())
				res += ") ";

			res += "(";
			OpenBitSet bs = sequence.get(i);
			String itemset = "";
			for (int idItem = bs.nextSetBit(0); idItem >= 0; idItem = bs.nextSetBit(idItem + 1)) {
				if (!itemset.isEmpty())
					itemset += " ";

				itemset += idItem;
			}
			res += itemset;
		}
		if (!res.isEmpty())
			res += ")";

		return res;
	}

	/**
	 * The comparator for Sequence wrt the measure
	 */
	public static Comparator<Sequence> sequenceComparator = new Comparator<Sequence>() {

		@Override
		public int compare(Sequence c1, Sequence c2) {
			if (c1.length - c2.length < 0) {
				return -1;
			}
			if (c1.length - c2.length > 0) {
				return 1;
			}
			if (c1.size - c2.size < 0) {
				return -1;
			}
			if (c1.size - c2.size > 0) {
				return 1;
			}
			return 0;
		}
	};

}
