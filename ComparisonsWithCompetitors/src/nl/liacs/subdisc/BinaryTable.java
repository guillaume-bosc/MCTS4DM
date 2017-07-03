package nl.liacs.subdisc;

import java.util.*;

/*
 * In most cases it would be useful for BinaryTable to remember the Columns it
 * represents. However, not all BinaryTables are build from Table Columns. For
 * example when the Subgroups are turned into binary Columns.
 */
public class BinaryTable
{
	private List<BitSet> itsColumns;
	private int itsNrRecords; //Nr. of examples

	//From Table
	public BinaryTable(Table theTable, List<Column> theColumns)
	{
		itsColumns = new ArrayList<BitSet>(theColumns.size());
		for (Column aColumn : theColumns)
			itsColumns.add(aColumn.getBinaries());
		itsNrRecords = theTable.getNrRows();
	}

	//turn subgroups into binary columns
	public BinaryTable(Table theTable, SubgroupSet theSubgroups)
	{
		itsColumns = new ArrayList<BitSet>(theSubgroups.size());
		itsNrRecords = theTable.getNrRows();

		for (Subgroup aSubgroup : theSubgroups)
		{
			BitSet aColumn = theTable.evaluate(aSubgroup.getConditions());
			itsColumns.add(aColumn);
		}
	}

	//this assumes that theTargets is an ArrayList<BitSet>
	// internal use only: #selectColumns(ItemSet) and #selectRows(BitSet)
	private BinaryTable(ArrayList<BitSet> theTargets, int theNrRecords)
	{
		itsColumns = theTargets;
		itsNrRecords = theNrRecords;
	}

	public BitSet getRow(int theIndex)
	{
		int itsColumnsSize = itsColumns.size();
		BitSet aBitSet = new BitSet(itsColumnsSize);
		for (int i = 0; i < itsColumnsSize; i++)
			aBitSet.set(i, itsColumns.get(i).get(theIndex));

		return aBitSet;
	}

	public BinaryTable selectColumns(ItemSet theItemSet)
	{
		ArrayList<BitSet> aNewTargets = new ArrayList<BitSet>(theItemSet.getItemCount());

		for (int i = 0; i < theItemSet.getDimensions(); i++)
			if (theItemSet.get(i))
				aNewTargets.add(itsColumns.get(i));

		return new BinaryTable(aNewTargets, itsNrRecords);
	}

	public BinaryTable selectRows(BitSet theMembers)
	{
		int aNrMembers = theMembers.cardinality();
		ArrayList<BitSet> aNewTargets = new ArrayList<BitSet>(getNrColumns());

		// single loop to get all indices of all set bits
//		final int[] setBits = new int[aNrMembers];
//		for (int i = theMembers.nextSetBit(0), j = -1; i >=0; i = theMembers.nextSetBit(i+1))
//			setBits[++j] = i;
//	
//		for (BitSet aColumn : itsColumns)
//		{
//			// create BitSet with same bits set as theMembers
//			BitSet aSmallerTarget = (BitSet) theMembers.clone();
//			// clear bits at indices that are not set for aColumn
//			for (int i = 0, j = setBits.length; i < j; ++i)
//				if (!aColumn.get(setBits[i]))
//					aSmallerTarget.clear(i);
//	
//			aNewTargets.add(aSmallerTarget);
//		}

		//copy targets
		for (BitSet aColumn : itsColumns)
		{
			BitSet aSmallerTarget = new BitSet(aNrMembers);
			int k=0;
			for (int j=0; j<getNrRecords(); j++)
				if (theMembers.get(j))
				{
					if (aColumn.get(j))
						aSmallerTarget.set(k);
					k++;
				}
			aNewTargets.add(aSmallerTarget);
		}

		return new BinaryTable(aNewTargets, aNrMembers);
	}

	public CrossCube countCrossCube()
	{
		CrossCube aCrossCube = new CrossCube(itsColumns.size());
		BitSet aBitSet = new BitSet(itsColumns.size());

		for (int i = 0; i < itsNrRecords ; i++)
		{
			aBitSet.clear();
			for (int j = 0; j < itsColumns.size(); j++)
				aBitSet.set(j, itsColumns.get(j).get(i));

			aCrossCube.incrementCount(aBitSet);
		}

		return aCrossCube;
	}

	public double computeBDeuFaster()
	{
		int aDimensions = itsColumns.size();

		// Init crosscube
		int aSize = (int)Math.pow(2, aDimensions);
		int[] aCounts = new int[aSize];
		int aTotalCount = 0;

		// Cache powers
		int[] aPowers = new int[aDimensions];
		for (int j = 0; j < aDimensions; j++)
			aPowers[j] = (int)Math.pow(2, aDimensions-j-1);

		// Fill crosscube
		for (int i = 0; i < itsNrRecords ; i++)
		{
			int anIndex = 0;
			for (int j = 0; j < aDimensions; j++)
				if(itsColumns.get(j).get(i))
					anIndex += aPowers[j];
			aCounts[anIndex]++;
			aTotalCount++;
		}

		// Compute BDeu
		if (aTotalCount == 0)
			return 0;

		double aQuality = 0.0;
		int q_i = aSize / 2;
		double alpha_ijk = 1.0 / (double) aSize;
		double alpha_ij  = 1.0 / (double) q_i;
		double LogGam_alpha_ijk = Function.logGamma(alpha_ijk); //uniform prior BDeu metric
		double LogGam_alpha_ij = Function.logGamma(alpha_ij);

		for (int j=0; j<q_i; j++)
		{
			double aSum = 0.0;
			double aPost = 0.0;

			//child = 0;
			aPost += Function.logGamma(alpha_ijk + aCounts[j*2]) - LogGam_alpha_ijk;
			aSum += aCounts[j*2];
			//child = 1;
			aPost += Function.logGamma(alpha_ijk + aCounts[j*2 + 1]) - LogGam_alpha_ijk;
			aSum += aCounts[j*2 + 1];

			aQuality += LogGam_alpha_ij - Function.logGamma(alpha_ij + aSum) + aPost;
		}
		return aQuality;
	}

	public ItemSet getApproximateMiki(int k)
	{
		long aCount = 0;
		ItemSet aMaximallyInformativeItemSet = new ItemSet(getNrColumns(), 0);
		double aMaximalEntropy = 0;

		Log.logCommandLine("finding approximate " + k + "-itemsets");
		for (int i=1; i<=k; i++)
		{
			ItemSet aTempItemSet = aMaximallyInformativeItemSet;
			for (int j=0; j<getNrColumns(); j++)
			{
				if (!aMaximallyInformativeItemSet.get(j))
				{
					aCount++;
					ItemSet anItemSet = aMaximallyInformativeItemSet.getExtension(j);
					BinaryTable aTable = selectColumns(anItemSet);
					CrossCube aCube = aTable.countCrossCube();
					double anEntropy = aCube.getEntropy();

					if (aMaximalEntropy < anEntropy)
					{
						aTempItemSet = anItemSet;
						aMaximalEntropy = anEntropy;
						Log.logCommandLine("found a new maximum: " + anItemSet + ": " + aMaximalEntropy);
					}
				}
			}
			aMaximallyInformativeItemSet = aTempItemSet;
		}
		aMaximallyInformativeItemSet.setJointEntropy(aMaximalEntropy);

		Log.logCommandLine("nr of column scans: " + aCount);
		return aMaximallyInformativeItemSet;
	}

	public void print()
	{
		int nrColumns = getNrColumns();
		StringBuilder aStringBuilder;

		for (int i = 0, j = getNrRecords(); i < j; i++)
		{
			aStringBuilder = new StringBuilder(nrColumns);
			for (BitSet b : itsColumns)
				aStringBuilder.append(b.get(i) ? "1" : "0");
			Log.logCommandLine(aStringBuilder.toString());
		}
	}

	public int getNrRecords() { return itsNrRecords; }
	public int getNrColumns() { return itsColumns.size(); }
	public void addColumn(BitSet theBitSet) { itsColumns.add(theBitSet); }
	public BitSet getColumn(int theIndex) { return itsColumns.get(theIndex);}
	//public void removeColumn(BitSet theBitSet) {itsColumns.remove(theBitSet);}
	//public void removeColumn(int theIndex) {itsColumns.remove(theIndex);}
	//public void setColumn(BitSet theBitSet, int theIndex) {itsColumns.set(theIndex, theBitSet);}
}
