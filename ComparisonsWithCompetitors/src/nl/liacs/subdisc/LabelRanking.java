package nl.liacs.subdisc;

public class LabelRanking
{
	int[] itsRanking;
	int itsSize;

	//labels are numbered from 0 to k-1
	//rankings are provided as input with a string of letters. 'a' corresponds to rank 0
	//ties are not implemented properly yet

	public LabelRanking(String theString)
	{
		itsSize = theString.length();
		itsRanking = new int[itsSize];
		for (int i=0; i<itsSize; i++)
			itsRanking[i] = Character.getNumericValue(theString.charAt(i)) - 10; //0 means 'a'
	}

	public float KendallTau(LabelRanking anLR)
	{
		int aConcordant = 0;
		int aDiscordant = 0;

		float aNumer = 0;
		for (int i=1; i<itsSize; i++)
			for (int j=0; j<=i-1; j++)
			{
				Log.logCommandLine("rank " + getRank(i) + "," + getRank(j) + "  " + anLR.getRank(i) + "," + anLR.getRank(j));
				aNumer += Math.signum((float) getRank(i) - getRank(j)) * Math.signum((float) anLR.getRank(i) - anLR.getRank(j));
			}
		return 2 * aNumer / (itsSize*(itsSize-1));
	}

	public void print()
	{
		for (int i=0; i<itsSize; i++)
			Log.logCommandLine("--" + i + ", " + itsRanking[i]);
	}

	public int getRank(int anIndex)	{ return itsRanking[anIndex]; }
	public void setRank(int anIndex, int aRank)	{ itsRanking[anIndex] = aRank; }
	public int getSize() { return itsSize; }
}
