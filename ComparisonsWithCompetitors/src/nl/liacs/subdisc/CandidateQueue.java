package nl.liacs.subdisc;

import java.util.*;

/*
 * NOTE As it stands, this class has deadlock potential.
 * Always obtain lock in fixed order: itsQueue -> itsNextQueue -> itsTempQueue.
 * 
 * TODO Queue classes in Concurrency framework allow for better concurrency. Eg.
 * Higher concurrency through non-locking algorithms and compareAndSwap methods.
 */
/**
 * A CandidateQueue holds a collection of {@link Candidate Candidate}s for
 * future processing. These are ordered as dictated by Candidate's
 * {@link Candidate#compareTo(Candidate) compareTo(Candidate)} method.
 * 
 * This class is thread save.
 * 
 * @see Candidate
 */
public class CandidateQueue
{
	private SearchStrategy itsSearchStrategy;
	private TreeSet<Candidate> itsQueue;
	private TreeSet<Candidate> itsNextQueue;
	private TreeSet<Candidate> itsTempQueue;
	private final int itsMaximumQueueSize;

	public CandidateQueue(SearchParameters theSearchParameters, Candidate theRootCandidate)
	{
		itsSearchStrategy = theSearchParameters.getSearchStrategy();
		if (itsSearchStrategy == SearchStrategy.BEAM)
			itsNextQueue = new TreeSet<Candidate>();
		if (itsSearchStrategy == SearchStrategy.COVER_BASED_BEAM_SELECTION)
		{
			// initialise now, avoids nullPointerException later
			itsNextQueue = new TreeSet<Candidate>();
			itsTempQueue = new TreeSet<Candidate>();
		}
		itsQueue = new TreeSet<Candidate>();
		itsQueue.add(theRootCandidate);

		itsMaximumQueueSize = theSearchParameters.getSearchStrategyWidth();
	}

	/**
	 * Adds a {@link Candidate Candidate} to this CandidateQueue.
	 * The add() and removeFirst() methods are thread save.
	 * 
	 * @param theCandidate the Candidate to add.
	 * 
	 * @return <code>true</code> if Candidate is added, <code>false</code>
	 * otherwise.
	 * 
	 * @see CandidateQueue#removeFirst()
	 * @see Candidate
	 */
	public boolean add(Candidate theCandidate)
	{
		if (itsSearchStrategy == SearchStrategy.BEAM)
			return addToQueue(itsNextQueue, theCandidate);
		else if (itsSearchStrategy == SearchStrategy.COVER_BASED_BEAM_SELECTION)
			//simply add candidate, regardless of the current size of itsTempQueue
			synchronized (itsTempQueue) { return itsTempQueue.add(theCandidate); }
		else
			return addToQueue(itsQueue, theCandidate);
	}

	//add candidate and trim queue to specified size itsMaximumQueueSize
	private boolean addToQueue(TreeSet<Candidate> theQueue, Candidate theCandidate)
	{
		boolean isAdded;
		synchronized (theQueue)
		{
			isAdded = theQueue.add(theCandidate);

			if (isAdded && (theQueue.size() > itsMaximumQueueSize))
				theQueue.pollLast();
		}
		return isAdded;
	}

	 /**
	 * Retrieves first {@link Candidate Candidate} from this CandidateQueue,
	 * and moves to next level if required.
	 * The add() and removeFirst() methods are thread save.
	 * 
	 * @return the Candidate at the head of this CandidateQueue.
	 * 
	 * @see CandidateQueue#add(Candidate)
	 * @see Candidate
	 */
	public Candidate removeFirst()
	{
		synchronized (itsQueue)
		{
			if (itsSearchStrategy.isBeam() && itsQueue.size() == 0)
				moveToNextLevel();

			return itsQueue.pollFirst();
		}
	}

	/*
	 * removeFirst locks itsQueue, additional locks are acquired here
	 * NOTE to avoid potential deadlock always obtain locks in fixed order:
	 * itsQueue -> itsNextQueue -> itsTempQueue
	 */
	private void moveToNextLevel()
	{
		Log.logCommandLine("\nLevel finished --------------------------------------------\n");
		if (itsSearchStrategy == SearchStrategy.BEAM) //make next level current
		{
			itsQueue = itsNextQueue;
			synchronized (itsNextQueue) { itsNextQueue = new TreeSet<Candidate>(); }
		}
		else // COVER_BASED_BEAM_SELECTION
		{
		// lock in fixed order to avoid deadlock, excuse indenting
		synchronized (itsNextQueue) {
		synchronized (itsTempQueue) {
			Log.logCommandLine("candidates: " + itsTempQueue.size());
			int aLoopSize = Math.min(itsMaximumQueueSize, itsTempQueue.size());
			BitSet aUsed = new BitSet(itsTempQueue.size());
			for (int i=0; i<aLoopSize; i++) //copy candidates into itsNextQueue
			{
				Log.logCommandLine("loop " + i);
				Candidate aBestCandidate = null;
				double aMaxQuality = Float.NEGATIVE_INFINITY;
				int aCount = 0;
				int aChosen = 0;
				for (Candidate aCandidate : itsTempQueue)
				{
					if (!aUsed.get(aCount)) //is this one still available
					{
						double aQuality = computeMultiplicativeWeight(aCandidate) * aCandidate.getPriority();
						if (aQuality > aMaxQuality)
						{
							aMaxQuality = aQuality;
							aBestCandidate = aCandidate;
							aChosen = aCount;
						}
					}
					aCount++;
				}
				Log.logCommandLine("best (" + aChosen + "): " + aBestCandidate.getPriority() + ", " + computeMultiplicativeWeight(aBestCandidate) + ", " + aMaxQuality);
				aUsed.set(aChosen, true);
				aBestCandidate.setPriority(aMaxQuality);
				addToQueue(itsNextQueue, aBestCandidate);
			}
			itsQueue = itsNextQueue;

			Log.logCommandLine("========================================================");
			Log.logCommandLine("used: " + aUsed.toString());
			for (Candidate aCandidate : itsQueue)
				Log.logCommandLine("priority: " + aCandidate.getPriority());

			itsNextQueue = new TreeSet<Candidate>();
			itsTempQueue = new TreeSet<Candidate>();
		}
		}
		}
	}

	/**
	 * Returns the total number of {@link Candidate Candidate}s in this
	 * CandidateQueue.
	 * Thread save with respect to add() and removeFirst().
	 * 
	 * @return the size of the current queue level.
	 * 
	 * @see CandidateQueue#add(Candidate)
	 * @see CandidateQueue#removeFirst
	 * @see Candidate
	 */
	public int size()
	{
		synchronized (itsQueue)
		{
			if (itsSearchStrategy == SearchStrategy.BEAM)
				synchronized (itsNextQueue) { return itsQueue.size() + itsNextQueue.size(); }
			else if (itsSearchStrategy == SearchStrategy.COVER_BASED_BEAM_SELECTION)
				synchronized (itsTempQueue) { return itsQueue.size() + itsTempQueue.size(); }
			else
				return itsQueue.size();
		}
	}

	/**
	 * Returns the number of {@link Candidate Candidate}s in the current
	 * queue level of this CandidateQueue.
	 * Thread save with respect to add() and removeFirst().
	 * 
	 * @return the size of the current queue level.
	 * 
	 * @see CandidateQueue#add(Candidate)
	 * @see CandidateQueue#removeFirst
	 * @see Candidate
	 */
	public int currentLevelQueueSize()
	{
		synchronized (itsQueue) { return itsQueue.size(); }
	}

	/**
	* Computes the cover count of a particular example: the number of times this example is a member of a subgroup. \n
	* See van Leeuwen & Knobbe, ECML PKDD 2011. \n
	*/
	private int computeCoverCount(int theRow)
	{
		int aResult = 0;

		synchronized (itsNextQueue)
		{
			for (Candidate aCandidate: itsNextQueue)
				if (aCandidate.getSubgroup().covers(theRow))
					++aResult;
		}

		return aResult;
	}

	/**
	* Computes the multiplicative weight of a subgroup \n
	* See van Leeuwen & Knobbe, ECML PKDD 2011. \n
	*/
	private double computeMultiplicativeWeight(Candidate theCandidate)
	{
		double aResult = 0;
		double anAlpha = 0.9;
		Subgroup aSubgroup = theCandidate.getSubgroup();
		BitSet aMember = aSubgroup.getMembers();

		for(int i=aMember.nextSetBit(0); i>=0; i=aMember.nextSetBit(i+1))
			aResult += Math.pow(anAlpha, computeCoverCount(i));

		return aResult/aSubgroup.getCoverage();
	}
}
