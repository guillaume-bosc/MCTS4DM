package Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;

import org.apache.lucene.util.OpenBitSet;

import Process.Global;

public class Subgroup {
	/*
	 * ########################################################################
	 * Declaration of the attributes of the class
	 * ########################################################################
	 */
	public Pattern description; // Description of the subgroup
	public Pattern target; // Target of the subgroup

	public double measure; // Measure of this subgroup
	double e11; // Size of intersection between supportProp and supportTarg

	public Map<Integer, Subgroup> children; // Array of children
	public List<Subgroup> parent; // The parent of this subgroup

	double nbVisits; // Number of visits for this subgroup
	double totValue; // Average measure for children of this subgroup
	boolean fullExpanded; // If this subgroup is fully expanded
	public boolean fullTerminated; // If this subgroup is totally expanded (its
	// children, etc...)

	static double epsilon = 1e-6;
	static Random r = new Random();

	int lastUpdate;
	PriorityQueue<Double> topkUpdateQueue;
	double sumRewards = 0;
	double variance = 0;

	double rhoNorm = 1;

	/*
	 * ########################################################################
	 * Declaration of the methods of the class
	 * ########################################################################
	 */
	public Subgroup() {
		this.description = null;
		this.target = new PatternBoolean(true);

		switch (Global.propType) {
		case NUMERIC:
			this.description = new PatternNumerical();
			break;
		case BOOLEAN:
			this.description = new PatternBoolean(false);
			break;
		case SEQUENCE:
			this.description = new PatternSequence();
			break;
		case GRAPH:
			break;
		case NOMINAL:
			this.description = new PatternNominal();
			break;
		default:
			break;
		}

		this.measure = 0.0;

		this.children = null;
		this.parent = null;

		this.nbVisits = 1;
		this.totValue = 0;

		this.fullExpanded = true;
		this.fullTerminated = false;
		this.lastUpdate = -1;

		rhoNorm = 1;

	}

	public Subgroup(Subgroup subgroup) {
		switch (Global.propType) {
		case NUMERIC:
			this.description = new PatternNumerical((PatternNumerical) subgroup.description);
			break;
		case BOOLEAN:
			this.description = new PatternBoolean((PatternBoolean) subgroup.description);
			break;
		case NOMINAL:
			this.description = new PatternNominal((PatternNominal) subgroup.description);
			break;
		case SEQUENCE:
			this.description = new PatternSequence((PatternSequence) subgroup.description);
			break;
		case GRAPH:
			break;
		default:
			break;
		}
		this.target = new PatternBoolean((PatternBoolean) subgroup.target);
		this.measure = 0.0;
		this.e11 = 0.0;

		this.children = null;
		this.parent = new ArrayList<Subgroup>();
		this.parent.add(subgroup);

		this.nbVisits = 0;
		this.totValue = 0;

		this.fullExpanded = false;
		this.fullTerminated = false;
		this.lastUpdate = -1;
	}

	public Subgroup(Subgroup subgroup, PatternBoolean thePattern) {
		if (thePattern.isTarget) {
			this.target = thePattern;

			switch (Global.propType) {
			case NUMERIC:
				this.description = new PatternNumerical((PatternNumerical) subgroup.description);
				break;
			case NOMINAL:
				this.description = new PatternNominal((PatternNominal) subgroup.description);
				break;
			case BOOLEAN:
				this.description = new PatternBoolean((PatternBoolean) subgroup.description);
				break;
			case SEQUENCE:
				this.description = new PatternSequence((PatternSequence) subgroup.description);
				break;
			case GRAPH:
				break;
			default:
				break;
			}
		} else {
			this.target = new PatternBoolean((PatternBoolean) subgroup.target);
			this.description = thePattern;
		}

		this.children = null;

		this.nbVisits = 0;
		this.totValue = 0;

		this.fullExpanded = false;
		this.fullTerminated = false;

		this.lastUpdate = -1;

	}

	/**
	 * Expands the literal related to idTarget. Creates at most 2 new subgroups
	 * : one for increasing the lower bound and the other one for decreasing the
	 * upper bound.
	 * 
	 * Required : we suppose that the target is a boolean attribute.
	 * 
	 * @param idTarget
	 */
	public Subgroup[] startWithTargets() {
		// Create the array of children for the root subgroup :
		// one child per target
		Subgroup[] tab = new Subgroup[Global.targets.length];

		if (!Global.extendsWithLabels) {
			Global.attrSupport = new HashMap<Integer, OpenBitSet>();
			Global.attrSupportSize = new int[Global.targets.length];
		}

		for (int idTarget = 0; idTarget < Global.targets.length; idTarget++) {
			PatternBoolean theTarget = new PatternBoolean(this.target, idTarget);

			if (!Global.extendsWithLabels) {
				Global.attrSupport.put(idTarget, theTarget.support);
				Global.attrSupportSize[idTarget] = theTarget.supportSize;
			}

			// Create this child of the current subgroup containing this target
			Subgroup childSub = new Subgroup(this, theTarget);
			childSub.description.performCompleteCopy();

			// childSub.computeMeasure();
			childSub.parent = null;
			childSub.target.candidates.clear(idTarget);
			childSub.nbVisits = 0;
			childSub.totValue = 0;
			tab[idTarget] = childSub;
		}

		for (int idTarget = 0; idTarget < Global.targets.length; idTarget++) {
			tab[idTarget].computeMeasure();
		}

		return tab;
	}

	/**
	 * Choose a subgroup that is not yet full expanded based on the UCB measure
	 * 
	 * @return true if the iteration correctly adds a subgroup, false otherwise
	 */
	public boolean iterateOnce() {
		Subgroup cur = this;
		long startTime, stopTime;

		// Explores the tree structure of subgroups until reach a leaf
		startTime = System.currentTimeMillis();
		while (cur.fullExpanded) {// || !cur.isExpandable()) {
			cur = cur.select();

			// this branch is fully expanded
			if (cur == null)
				return false;
		}
		stopTime = System.currentTimeMillis();
		Global.timeSelect += stopTime - startTime;

		// Expands the leaf that is found
		startTime = System.currentTimeMillis();
		Subgroup newNode = null;
		if (cur.target.getDescriptionSize() == Global.maxLabel)
			newNode = cur.expandOnlyDescr();
		else
			newNode = cur.expand();
		stopTime = System.currentTimeMillis();
		Global.timeExpand += stopTime - startTime;

		// If cur cannot be expanded
		if (newNode == null || newNode.fullExpanded) {
			return false;
		}

		// Adds this new subgroup to the set of subgroups
		if (newNode.measure > 0. && Global.wasNotInAmaf)
			Global.addToResultSet(newNode);

		// Statistics mean measure
		computeMeanMeasure(newNode.measure);

		// Performs a simulation
		startTime = System.currentTimeMillis();
		double value = 0.;
		if (Global.pathLength >= 0)
			value = newNode.rollOutRandomPath();
		else
			value = newNode.rollOutRandomFrequentPath();

		stopTime = System.currentTimeMillis();
		Global.timeRollOut += stopTime - startTime;

		if (Global.measure == Enum.Measure.WRAcc && value < 0)
			value = 0;

		if (value > Global.maxMeasure)
			Global.maxMeasure = value;

		// Update stats of visited subgroups
		startTime = System.currentTimeMillis();
		List<Subgroup> parentNodes = new ArrayList<Subgroup>();
		parentNodes.add(newNode);
		updateParents(parentNodes, value);
		stopTime = System.currentTimeMillis();
		Global.timeUpdate += stopTime - startTime;

		Global.indexIteration++;
		return true;
	}

	/**
	 * Selects an subgroup wrt the uct value
	 * 
	 * @return the child of this subgroup with the highest uct value
	 */
	public Subgroup select() {
		Subgroup selected = null;
		double bestValue = Double.MIN_VALUE;
		//System.out.println(this + " - " + this.nbVisits);
		for (Subgroup c : children.values()) {
			// If the child is null or has been totally expanded
			if (c == null || c.fullTerminated)
				continue;

			// long startTime = System.currentTimeMillis();
			double ucb = 0;
			if (Global.UCB == Enum.UCB.UCB1)
				ucb = c.computeUCB1(this);
			else if (Global.UCB == Enum.UCB.UCT)
				ucb = c.computeUCT(this);
			else if (Global.UCB == Enum.UCB.UCBSP)
				ucb = c.computeUCBSP(this);
			else if (Global.UCB == Enum.UCB.UCBTuned)
				ucb = c.computeUCBTuned(this);
			else if (Global.UCB == Enum.UCB.DFSUCT)
				ucb = c.computeDFSUCT(this);
			// long stopTime = System.currentTimeMillis();
			// Global.timeUCT += stopTime - startTime;
			if (ucb > bestValue) {
				selected = c;
				bestValue = ucb;
			}
		}
		// System.out.println("Returning: " + selected);
		if (selected == null)
			this.fullTerminated = true;
		return selected;
	}

	/**
	 * Expands a subgroup by creating ONLY ONE child
	 * 
	 * @return
	 */
	public Subgroup expand() {
		// Create the array of children if it is not created yet:
		// 2 children for a single attribute, one child per target
		if (this.children == null) {
			this.children = new HashMap<Integer, Subgroup>();
		}

		long nPossibilities = this.description.candidates.cardinality() + this.target.candidates.cardinality();
		int idChild = -1;
		Pattern expandedPattern;
		while (this.children.get(idChild) == null && nPossibilities > 0) {
			int n = r.nextInt((int) nPossibilities);
			idChild = -1;

			if (n < this.description.candidates.cardinality()) {
				// Expand description side
				while (n >= 0) {
					idChild = this.description.candidates.nextSetBit(idChild + 1);
					if (this.children.get(idChild) == null)
						n--;
				}

				expandedPattern = this.description.expand(idChild, this.target.support);
				if (expandedPattern == null) {
					nPossibilities = this.description.candidates.cardinality() + this.target.candidates.cardinality();
					continue;
				}
				Subgroup childSub = new Subgroup(this);
				childSub.description = expandedPattern;
				childSub.computeMeasure();

				// Take into account the AMAF policy
				if (Global.duplicatesExpand == Enum.DuplicatesExpand.AMAF) {
					Subgroup uniqueOne = Global.amaf.get(childSub);
					if (uniqueOne == null) {
						Global.amaf.put(childSub, childSub);
						Global.wasNotInAmaf = true;
					} else {
						uniqueOne.parent.add(this);
						childSub = uniqueOne;
						Global.numberDuplicates++;
						Global.wasNotInAmaf = false;
					}
				}

				this.children.put(idChild, childSub);

				if (Global.UCB == Enum.UCB.DFSUCT)
					childSub.computeRhoNorm(idChild);

			} else {
				// Expand target side
				n -= this.description.candidates.cardinality();
				while (n >= 0) {
					idChild = this.target.candidates.nextSetBit(idChild + 1);
					if (this.children.get(Global.nbChild + idChild) == null)
						n--;
				}

				expandedPattern = this.target.expand(idChild, this.description.support);
				if (expandedPattern == null) {
					nPossibilities = this.description.candidates.cardinality() + this.target.candidates.cardinality();
					continue;
				}

				idChild = Global.nbChild + idChild;

				Subgroup childSub = new Subgroup(this);
				childSub.target = expandedPattern;
				childSub.computeMeasure();

				// Take into account the AMAF policy
				if (Global.duplicatesExpand == Enum.DuplicatesExpand.AMAF) {
					Subgroup uniqueOne = Global.amaf.get(childSub);
					if (uniqueOne == null) {
						Global.amaf.put(childSub, childSub);
						Global.wasNotInAmaf = true;
					} else {
						uniqueOne.parent.add(this);
						childSub = uniqueOne;
						Global.numberDuplicates++;
						Global.wasNotInAmaf = false;
					}
				}

				this.children.put(idChild, childSub);

				if (Global.UCB == Enum.UCB.DFSUCT)
					childSub.computeRhoNorm(childSub.description.lastIdAttr);
			}

			nPossibilities = this.description.candidates.cardinality() + this.target.candidates.cardinality();
		}

		// If this subgroup is fully expanded
		if (nPossibilities == 0) {
			this.fullExpanded = true;

			// Free memory
			this.target.candidates = null;
			this.description.candidates = null;
		}

		return this.children.get(idChild);
	}

	/**
	 * Expands a subgroup by creating ONLY ONE child
	 * 
	 * @return
	 */
	public Subgroup expandOnlyDescr() {
		// Create the array of children if it is not created yet:
		// 2 children for a single attribute, one child per target
		if (this.children == null) {
			this.children = new HashMap<Integer, Subgroup>();
		}

		long nPossibilities = this.description.candidates.cardinality();
		int idChild = -1;
		Pattern expandedPattern;
		while (this.children.get(idChild) == null && nPossibilities > 0) {
			int n = r.nextInt((int) nPossibilities);
			idChild = -1;

			// Expand description side
			while (n >= 0) {
				idChild = this.description.candidates.nextSetBit(idChild + 1);
				if (this.children.get(idChild) == null)
					n--;
			}

			expandedPattern = this.description.expand(idChild, this.target.support);
			if (expandedPattern == null) {
				nPossibilities = this.description.candidates.cardinality();
				continue;
			}
			Subgroup childSub = new Subgroup(this);
			childSub.description = expandedPattern;
			childSub.computeMeasure();

			// Take into account the AMAF policy
			if (Global.duplicatesExpand == Enum.DuplicatesExpand.AMAF) {
				Subgroup uniqueOne = Global.amaf.get(childSub);
				if (uniqueOne == null) {
					Global.amaf.put(childSub, childSub);
					Global.wasNotInAmaf = true;
				} else {
					uniqueOne.parent.add(this);
					childSub = uniqueOne;
					Global.numberDuplicates++;
					Global.wasNotInAmaf = false;
				}
			}

			this.children.put(idChild, childSub);

			if (Global.UCB == Enum.UCB.DFSUCT)
				childSub.computeRhoNorm(idChild);

			nPossibilities = this.description.candidates.cardinality();
		}

		// If this subgroup is fully expanded
		if (nPossibilities == 0) {
			this.fullExpanded = true;

			if (Global.propType == DataType.SEQUENCE) {
				for (int idOfChild = 0; idOfChild < Global.nbChild; idOfChild++) {
					if (!this.children.containsKey(idOfChild)) {
						if (this.description.getDescriptionSize() > 1 || (this.description.getDescriptionSize() == 1
								&& ((PatternSequence) this.description).description.get(0).cardinality() > 1))
							for (Subgroup existingChild : children.values()) {
								if (!existingChild.fullExpanded)
									((PatternSequence) existingChild.description).notAChildOfParent(idOfChild);
							}
					}
				}

				// System.out.println(this + " clear");
				((PatternSequence) this.description).projectedBase.clear();
				((PatternSequence) this.description).projectedBase = null;
			}

			// Free memory
			this.target.candidates = null;
			this.description.candidates = null;
		}

		return this.children.get(idChild);
	}

	/**
	 * Emulates the exploration of the tree of subgroup. Stops when it comes to
	 * a final state.
	 * 
	 * @param tn
	 * @return
	 */
	public double rollOutRandomFrequentPath() {
		long startTime, stopTime;
		startTime = System.currentTimeMillis();

		double reward = Double.MIN_VALUE;
		Subgroup subExpanded = new Subgroup(this);
		subExpanded.description.performCompleteCopy();

		if (Global.propType == DataType.SEQUENCE && Global.refineExpand == Enum.RefineExpand.Direct) {
			((PatternSequence) (subExpanded.description)).generatePrefixCandidates();
		}

		OpenBitSet attrSet = new OpenBitSet(Global.nbAttr);
		if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order) {
			if (subExpanded.description.lastIdAttr != -1)
				attrSet.set(subExpanded.description.lastIdAttr, Global.nbAttr);
			else
				attrSet.set(0, Global.nbAttr);
		} else {
			attrSet.set(0, Global.nbAttr);
		}

		List<Subgroup> path = new ArrayList<Subgroup>();
		List<Subgroup> memoryList = new ArrayList<Subgroup>();

		Subgroup firstSubgroup = new Subgroup(this);
		firstSubgroup.description.performCompleteCopy();
		path.add(firstSubgroup);

		while (true) {
			Random r = new Random();

			// Only roll out on the attributes and not on the targets
			int n = r.nextInt((int) attrSet.cardinality());
			int idProp = -1;
			if (Global.duplicatesExpand == Enum.DuplicatesExpand.Order) {
				if (subExpanded.description.lastIdAttr != -1)
					idProp = subExpanded.description.lastIdAttr - 1;
				else
					idProp = -1;
			}
			while (n >= 0) {
				idProp = attrSet.nextSetBit(idProp + 1);
				n--;
			}

			long startTime1, stopTime1;
			startTime1 = System.currentTimeMillis();
			boolean rollOutCorrect = false;
			if (Global.refineRollOut == Enum.RefineRollOut.Direct)
				rollOutCorrect = subExpanded.description.rollOutDirect(idProp);
			else
				rollOutCorrect = subExpanded.description.rollOutLarge(attrSet);

			stopTime1 = System.currentTimeMillis();
			Global.timeCreatePathRoll += stopTime1 - startTime1;

			if (!rollOutCorrect) {
				attrSet.clear(idProp);
				if (attrSet.cardinality() == 0)
					break;
				continue;
			}

			// Keep the greatest value of roll out subgroups
			if (subExpanded.description.supportSize >= Global.minSup
					&& subExpanded.description.getDescriptionSize() <= Global.maxLength) {
				// Computes measure of this expanded subgroup
				// long startTime = System.currentTimeMillis();
				Subgroup nextSubgroup = new Subgroup(subExpanded);
				nextSubgroup.description.performCompleteCopy();
				path.add(nextSubgroup);
			} else {
				// End of the path
				break;
			}

		}
		stopTime = System.currentTimeMillis();
		Global.timeCreatePath += stopTime - startTime;

		// Process the path
		startTime = System.currentTimeMillis();
		int size = path.size();
		if (Global.rewardPolicy == Enum.RewardPolicy.RandomPick) {
			int index = new Random().nextInt(size);
			Subgroup res = path.get(index);
			res.computeMeasure();
			reward = res.measure;
			memoryList.add(res);

			if (res.measure > Global.maxMeasure)
				Global.maxMeasure = res.measure;

		} else if (Global.rewardPolicy == Enum.RewardPolicy.MeanPath) {
			reward = 0;
			for (Subgroup aSub : path) {
				aSub.computeMeasure();
				reward += aSub.measure;

				if (aSub.measure > Global.maxMeasure)
					Global.maxMeasure = aSub.measure;
			}
			reward /= size;
			memoryList.addAll(path);
		} else if (Global.rewardPolicy == Enum.RewardPolicy.MaxPath) {
			for (Subgroup aSub : path) {
				aSub.computeMeasure();
				if (reward < aSub.measure)
					reward = aSub.measure;

				if (aSub.measure > Global.maxMeasure)
					Global.maxMeasure = aSub.measure;
			}
			memoryList.addAll(path);
		} else if (Global.rewardPolicy == Enum.RewardPolicy.MeanTopK) {
			PriorityQueue<Subgroup> rankedPath = new PriorityQueue<Subgroup>(Global.topKRollOut + 1,
					Subgroup.subgroupComparatorMeasure);
			for (Subgroup aSub : path) {
				aSub.computeMeasure();

				if (aSub.measure > Global.maxMeasure)
					Global.maxMeasure = aSub.measure;

				rankedPath.add(aSub);
				if (rankedPath.size() > Global.topKRollOut) {
					rankedPath.poll();
				}
			}
			int sizeQueue = 0;
			reward = 0;
			while (true) {
				Subgroup aSub = rankedPath.poll();
				if (aSub == null)
					break;

				sizeQueue++;
				reward += aSub.measure;
			}
			reward /= sizeQueue;
			memoryList.addAll(path);
		}
		stopTime = System.currentTimeMillis();
		Global.timeHandlePath += stopTime - startTime;

		// Memory policy
		startTime = System.currentTimeMillis();
		if (Global.memoryPolicy == Enum.MemoryPolicy.AllEvaluated) {
			Global.addToResultSet(memoryList);

			// Statistic mean measure
			for (Subgroup aSub : memoryList) {
				computeMeanMeasure(aSub.measure);
			}
		} else if (Global.memoryPolicy == Enum.MemoryPolicy.TopK) {
			PriorityQueue<Subgroup> rankedMemory = new PriorityQueue<Subgroup>(Global.topKMemory + 1,
					Subgroup.subgroupComparatorMeasure);
			for (Subgroup aSub : memoryList) {
				rankedMemory.add(aSub);
				if (rankedMemory.size() > Global.topKRollOut)
					rankedMemory.poll();
			}
			// Statistic mean measure
			int s = rankedMemory.size();
			for (Subgroup aSub : rankedMemory) {
				computeMeanMeasure(aSub.measure);
			}
			if (s != rankedMemory.size())
				System.err.println("PB !!!");

			Global.addToResultSet(rankedMemory);
		}
		stopTime = System.currentTimeMillis();
		Global.timeMemory += stopTime - startTime;

		// Return the reward
		return reward;
	}

	/**
	 * Randomly pick one subgroup and compute its measure
	 * 
	 * @return The measure of the picked subgroup
	 */
	public double rollOutRandomPath() {
		if (this.description.supportSize == Global.minSup)
			return this.measure;

		double res = Double.MIN_VALUE;

		boolean success = false;

		while (!success) {
			Subgroup subExpanded = new Subgroup(this);
			subExpanded.description.performCompleteCopy();
			subExpanded.description.rollOutRandomPath();

			// Keep the greatest value of roll out subgroups
			if (subExpanded.description.supportSize >= Global.minSup
					&& subExpanded.description.getDescriptionSize() <= Global.maxLength) {
				Global.nbRoll--;
				success = true;
				subExpanded.computeMeasure();
				return subExpanded.measure;
			}
		}

		return res;
	}

	/**
	 * The recursive update function with possible several parents
	 * 
	 * @param parentNodes
	 *            : the list of nodes to update
	 * @param reward
	 *            : the reward value to update
	 */
	public static void updateParents(List<Subgroup> parentNodes, double reward) {
		// If the root is reached, stop the recursive calls
		if (parentNodes == null)
			return;

		// Iterates over the parents
		for (Subgroup parentNode : parentNodes) {

			// If the parent is already updated
			if (parentNode.lastUpdate == Global.indexIteration)
				continue;

			// Proceeds to the update
			if (Global.updatePolicy == Enum.UpdatePolicy.Max)
				parentNode.updateStatsMax(reward);
			else if (Global.updatePolicy == Enum.UpdatePolicy.Mean)
				parentNode.updateStatsMean(reward);
			else if (Global.updatePolicy == Enum.UpdatePolicy.MeanTopK)
				parentNode.updateStatsMeanTopK(reward);
			else
				System.out.println("Erreur Update Parents");

			// Puts a flag
			parentNode.lastUpdate = Global.indexIteration;

			// Recursive call
			updateParents(parentNode.parent, reward);
		}
	}

	/**
	 * Updates this node by adding the value to totValue
	 * 
	 * @param value
	 */
	public void updateStatsMean(double value) {
		nbVisits++;
		totValue += value;

		// Update the the rewards value to compute variance if needed
		if (Global.UCB == Enum.UCB.UCBSP || Global.UCB == Enum.UCB.UCBTuned) {
			// parentNode.rewards.add(reward);
			sumRewards += value;
			if (nbVisits == 1) {
				variance = 0;
			} else {
				variance = ((nbVisits - 1) * variance
						+ (value - (sumRewards - value) / (nbVisits - 1)) * (value - sumRewards / nbVisits)) / nbVisits;
			}
		}
	}

	/**
	 * Updates this node by considering the mean of the topK rewards obtained so
	 * far
	 * 
	 * @param reward
	 *            the reward to back propagate
	 */
	public void updateStatsMeanTopK(double reward) {
		nbVisits++;

		double meanSquare = 0;
		if (topkUpdateQueue == null) {
			topkUpdateQueue = new PriorityQueue<Double>();
		}

		topkUpdateQueue.add(reward);
		if (topkUpdateQueue.size() > Global.topKUpdate)
			topkUpdateQueue.poll();

		totValue = 0;
		for (Double r : topkUpdateQueue) {
			totValue += r;
			meanSquare += r * r;
		}

		// Update the the rewards value to compute variance if needed
		if (Global.UCB == Enum.UCB.UCBSP || Global.UCB == Enum.UCB.UCBTuned) {
			variance = (meanSquare - (totValue * totValue) / topkUpdateQueue.size()) / topkUpdateQueue.size();
		}
	}

	/**
	 * Updates this node by keeping the max value in totValue
	 * 
	 * @param value
	 */
	public void updateStatsMax(double value) {
		nbVisits++;
		if (value > totValue)
			totValue = value;

		// Update the the rewards value to compute variance if needed
		if (Global.UCB == Enum.UCB.UCBSP || Global.UCB == Enum.UCB.UCBTuned) {
			// parentNode.rewards.add(reward);
			sumRewards += value;
			if (nbVisits == 1) {
				variance = 0;
			} else {
				variance = ((nbVisits - 1) * variance
						+ (value - (sumRewards - value) / (nbVisits - 1)) * (value - sumRewards / nbVisits)) / nbVisits;
			}
		}
	}

	/**
	 * Tells if the node can be expanded due to the number of visit. This is the
	 * progressive widening (Coulom 06; Rolet et al. 09)
	 * 
	 * @return
	 */
	public boolean isExpandable() {
		double b = 0.5;

		if (this.children == null) {
			return true;
		}

		return (int) (Math.pow(nbVisits, b)) != (int) (Math.pow(nbVisits + 1, b));
	}

	/**
	 * 
	 * @param listSub
	 * @return
	 */
	public boolean isRedundantWith(List<Subgroup> listSub) {
		for (Subgroup aSub : listSub) {
			if (this.isRedundantWith(aSub))
				return true;
		}

		return false;
	}

	/**
	 * Checks if this subgroup is redundant wrt another subgroup
	 * 
	 * @param aSub
	 *            : the other subgroup
	 * @return true if this subgroup is redundant, false otherwise
	 */
	public boolean isRedundantWith(Subgroup aSub) {

		// Checks if the subgroups are exactly related to the same subset of
		// labels
		if (!Global.redundancyIdenticalLabels || this.target.sameTargets(aSub.target)) {
			if (Global.redundancyStrategy == Enum.Redundancy.SumJaccard)
				return (this.description.similarityScore(aSub.description) > Global.maxRedundancy);
			else if (Global.redundancyStrategy == Enum.Redundancy.JaccardSupportDescription) {
				double jaccard = 0.;
				jaccard = (double) (OpenBitSet.intersectionCount(this.description.support, aSub.description.support))
						/ (OpenBitSet.unionCount(this.description.support, aSub.description.support));
				return (jaccard > Global.maxRedundancy);
			} else {
				// Global.redundancyStrategy ==
				// Enum.Redundancy.JaccardSupportDescriptionTarget
				double jaccard = 0.;
				OpenBitSet supp1 = (OpenBitSet) this.description.support.clone();
				supp1.and(this.target.support);

				OpenBitSet supp2 = (OpenBitSet) aSub.description.support.clone();
				supp1.and(aSub.target.support);

				jaccard = (double) (OpenBitSet.intersectionCount(supp1, supp2)) / (OpenBitSet.unionCount(supp1, supp2));

				return (jaccard > Global.maxRedundancy);
			}
		} else
			return false;
	}

	/**
	 * Computes the measure of the subgroup thanks to both the support of the
	 * attribute description and the target description.
	 */
	public void computeMeasure() {
		long startTime, stopTime;
		startTime = System.currentTimeMillis();
		if (description == null || description.support == null)
			System.out.println(this);
		OpenBitSet temp = (OpenBitSet) this.description.support.clone();
		temp.and(this.target.support);
		this.e11 = temp.cardinality();

		if (Global.measure == Enum.Measure.F1 || Global.measure == Enum.Measure.RelativeF1
				|| Global.measure == Enum.Measure.WeightedRelativeF1)
			this.computeMeasureF1();
		else if (Global.measure == Enum.Measure.FBeta || Global.measure == Enum.Measure.RelativeFBeta
				|| Global.measure == Enum.Measure.WeightedRelativeFBeta)
			this.computeMeasureFBeta();
		else if (Global.measure == Enum.Measure.WRAcc || Global.measure == Enum.Measure.RAcc
				|| Global.measure == Enum.Measure.Acc)
			this.computeMeasureWRAcc();
		else if (Global.measure == Enum.Measure.WKL)
			this.computeMeasureWKL();
		else if (Global.measure == Enum.Measure.HammingLoss)
			this.computeMeasureHLoss();
		else if (Global.measure == Enum.Measure.ZeroOneLoss)
			this.computeMeasureLossOne();
		else if (Global.measure == Enum.Measure.ContingencyTable)
			this.computeMeasureContingencyTable();
		else if (Global.measure == Enum.Measure.Jaccard)
			this.computeMeasureJaccard();
		else if (Global.measure == Enum.Measure.Entropy)
			this.computeMeasureEntropy();
		else if (Global.measure == Enum.Measure.MutualInformation)
			this.computeMeasureMI();
		else
			System.out.println("Error for the selection of measure.");

		stopTime = System.currentTimeMillis();
		Global.timeComputeRollOut += stopTime - startTime;
	}

	public void computeMeasureJaccard() {
		this.measure = 0;

		if (this.e11 != 0) {
			this.measure = ((double)(this.e11)) / OpenBitSet.unionCount(this.description.support, this.target.support);
		}
	}
	
	public void computeMeasureEntropy() {
		this.measure = 0;
		
		double pab = ((double) (this.e11))/this.description.supportSize;
		double pnab = 1.-pab;
		
		this.measure = -pab * Math.log10(pab)/Math.log10(2) - pnab * Math.log10(pnab)/Math.log10(2);
	}
	
	public void computeMeasureMI() {
		this.measure = 0;
		
		double pa =  ((double) (this.description.supportSize))/Global.objects.length;
		double pb = ((double) (this.target.supportSize))/Global.objects.length;
		double pab = ((double) (this.e11))/Global.objects.length;
		double pnab = ((double) (this.target.supportSize - this.e11))/Global.objects.length;
		double panb = ((double) (this.description.supportSize - this.e11))/Global.objects.length;
		double pnanb = 1.-pab-pnab-panb;
		double log2 = Math.log10(2.);
		
		this.measure = pab*Math.log10(pab/(pa * pb))/log2 + panb*Math.log10(panb/(pa*(1.-pb)))/log2 
				+ pnab*Math.log10(pnab/((1.-pa) * pb))/log2 + pnanb*Math.log10(pnanb/((1.-pa) * (1-pb)))/log2;
	}

	/**
	 * Compute the WKL measure
	 */
	public void computeMeasureWKL() {
		double sizeG = (double) this.description.supportSize;
		double sizeS = (double) Global.objects.length;
		double sum = 0;
		for (int idTarget = 0; idTarget < Global.targets.length; idTarget++) {
			OpenBitSet suppTarget = Global.attrSupport.get(idTarget);
			OpenBitSet tempAnd1 = (OpenBitSet) suppTarget.clone();
			double pl0 = (double) Global.attrSupportSize[idTarget] / sizeS;
			double pl02 = (sizeS - (double) Global.attrSupportSize[idTarget]) / sizeS;

			tempAnd1.and(description.support);

			double tempAnd1Size = (double) tempAnd1.cardinality();
			double pl = tempAnd1Size / sizeG;
			double pl2 = (double) (sizeG - tempAnd1Size) / sizeG;

			if (pl != 0)
				sum += pl * Math.log(pl / pl0) / Global.log2;

			if (pl2 != 0)
				sum += pl2 * Math.log(pl2 / pl02) / Global.log2;
		}

		this.measure = sum * sizeG / sizeS;

	}

	/**
	 * Compute the table contingency table
	 */
	public void computeMeasureContingencyTable() {
		double sizeG = (double) this.description.supportSize;
		double sizeS = (double) Global.objects.length;
		OpenBitSet suppTarget1 = Global.attrSupport.get(0);
		OpenBitSet suppTarget2 = Global.attrSupport.get(1);
		OpenBitSet intersection = (OpenBitSet) (suppTarget1.clone());
		intersection.and(suppTarget2);

		double intersS = (double) (intersection.cardinality()) / sizeS;
		double intersG = (double) (OpenBitSet.intersectionCount(intersection, this.description.support)) / sizeG;

		double target1NotTarget2S = (double) (Global.attrSupportSize[0]) / sizeS - intersS;
		double target1NotTarget2G = (double) (OpenBitSet.intersectionCount(suppTarget1, this.description.support))
				/ sizeG - intersG;

		double target2NotTarget1S = (double) (Global.attrSupportSize[1]) / sizeS - intersS;
		double target2NotTarget1G = (double) (OpenBitSet.intersectionCount(suppTarget2, this.description.support))
				/ sizeG - intersG;

		double notS = 1. - intersS - target1NotTarget2S - target2NotTarget1S;
		double notG = 1. - intersG - target1NotTarget2G - target2NotTarget1G;

		this.measure = (sizeG / (2 * sizeS))
				* (Math.abs(intersS - intersG) + Math.abs(target1NotTarget2S - target1NotTarget2G)
						+ Math.abs(target2NotTarget1S - target2NotTarget1G) + Math.abs(notS - notG));

	}

	/**
	 * Compute the WRAcc measure
	 */
	public void computeMeasureWRAcc() {
		double sizeG = (double) this.description.supportSize;
		double sizeS = (double) Global.objects.length;
		double fracG = e11 / sizeG;
		double fracS = ((double) (this.target.supportSize)) / sizeS;
		measure = fracG;

		if (Global.measure == Enum.Measure.RAcc || Global.measure == Enum.Measure.WRAcc) {
			measure = measure - fracS;
			if (Global.measure == Enum.Measure.WRAcc) {
				measure = measure * (sizeG / sizeS);
			}
		}

		if (measure < 0)
			measure = 0;
	}

	/**
	 * Compute the FBeta Score
	 */
	public void computeMeasureFBeta() {
		this.measure = 0;

		if (this.e11 != 0) {
			long targetSize = this.target.supportSize;
			double precision = this.e11 / (this.description.supportSize);
			double recall = this.e11 / (targetSize);

			Double beta = 0.;
			// Compute the value of Beta
			long suppL = targetSize;
			if (Global.mappingBeta.containsKey(suppL)) {
				beta = Global.mappingBeta.get(suppL);
			} else {
				beta = 0.5 * (1. + Math.tanh((Global.xBeta - (suppL)) / Global.lBeta));
				Global.mappingBeta.put((int) (suppL), new Double(beta));
			}
			beta *= beta;

			measure = (1. + beta) * precision * recall / ((beta * precision) + recall);

			if (Global.measure == Enum.Measure.RelativeFBeta || Global.measure == Enum.Measure.WeightedRelativeFBeta) {
				measure = measure - ((1. + beta) * ((double) (targetSize) / Global.objects.length)
						/ ((beta * ((double) (targetSize) / Global.objects.length)) + 1));

				if (Global.measure == Enum.Measure.WeightedRelativeFBeta) {
					double sizeG = (double) this.description.supportSize;
					double sizeS = (double) Global.objects.length;
					measure = measure * (sizeG / sizeS);
				}
			}
		}
	}

	/**
	 * Computes the classical F1 Score
	 */
	public void computeMeasureF1() {
		this.measure = 0;

		if (this.e11 != 0) {
			double precision = this.e11 / (this.description.supportSize);
			double recall = this.e11 / (this.target.supportSize);

			this.measure = 2. * precision * recall / (precision + recall);

			if (Global.measure == Enum.Measure.RelativeF1 || Global.measure == Enum.Measure.WeightedRelativeF1) {
				measure = measure - (2. * ((double) (this.target.supportSize) / Global.objects.length)
						/ ((((double) (this.target.supportSize) / Global.objects.length)) + 1));

				if (Global.measure == Enum.Measure.WeightedRelativeF1) {
					double sizeG = (double) this.description.supportSize;
					double sizeS = (double) Global.objects.length;
					measure = measure * (sizeG / sizeS);
				}
			}
		}
	}

	/**
	 * Compute the HammingLoss measure
	 */
	public void computeMeasureHLoss() {
		double sizeG = (double) this.description.supportSize;
		double nbLabels = (double) Global.targets.length;
		measure = 0.;

		for (int i = description.support.nextSetBit(0); i >= 0; i = description.support.nextSetBit(i + 1)) {
			measure += OpenBitSet.xorCount(Global.objects[i].target, ((PatternBoolean) (target)).description);
		}
		measure = measure * (1. / (sizeG * nbLabels));

		// Inverted measure because maximize
		measure = 1. - measure;
	}

	/**
	 * Compute the 01Loss measure
	 */
	public void computeMeasureLossOne() {
		double sizeG = (double) this.description.supportSize;
		measure = 0.;

		for (int i = description.support.nextSetBit(0); i >= 0; i = description.support.nextSetBit(i + 1)) {
			if (!Global.objects[i].target.equals(((PatternBoolean) (target)).description)) {
				measure += 1.;
			}
		}
		measure = measure * (1. / sizeG);

		// Inverted measure because maximize
		measure = 1. - measure;
	}

	/**
	 * Computes the UCT Value of the subgroup
	 * 
	 * @return The UCT Value
	 */
	public double computeUCT(Subgroup parentSubgroup) {
		// Take into account the initial seed of the tree (whose parent is null)
		// There is a single child (the subgroup with the label to
		// characterize, without a restriction
		if (this.parent == null) {
			return Double.MAX_VALUE;
		}

		// small random number to break ties randomly in unexpanded nodes
		double value = 0.;
		if (Global.updatePolicy == Enum.UpdatePolicy.Max) {
			value = this.totValue * nbVisits / (this.nbVisits + epsilon);
		} else if (Global.updatePolicy == Enum.UpdatePolicy.MeanTopK) {
			if (topkUpdateQueue == null)
				System.out.println("cc");
			value = this.totValue / (this.topkUpdateQueue.size() + epsilon);
		} else {
			value = this.totValue / (this.nbVisits + epsilon);
		}

		// If the measure is not in [Ø,1], normalize it with the maximum value
		// encountered so far
		if (Global.measure == Enum.Measure.WRAcc)
			value /= Global.maxMeasure;

		// Optimize log computation
		int logX = (int) parentSubgroup.nbVisits;
		double logValue;
		if (Global.logList[logX - 1] != 0) {
			logValue = Global.logList[logX - 1];
		} else {
			logValue = Math.log(logX);
			Global.logList[logX - 1] = logValue;
		}

		double uctValue = value + 2. * Math.sqrt(logValue / (this.nbVisits + epsilon)) + r.nextDouble() * epsilon;

		return uctValue;
	}

	/**
	 * Computes the UCT Value of the subgroup
	 * 
	 * @return The UCT Value
	 */
	public double computeDFSUCT(Subgroup parentSubgroup) {
		// Take into account the initial seed of the tree (whose parent is null)
		// There is a single child (the subgroup with the label to
		// characterize, without a restriction
		if (this.parent == null) {
			return Double.MAX_VALUE;
		}

		// small random number to break ties randomly in unexpanded nodes
		double value = 0.;
		if (Global.updatePolicy == Enum.UpdatePolicy.Max) {
			value = this.totValue * nbVisits / (this.nbVisits + epsilon);
		} else if (Global.updatePolicy == Enum.UpdatePolicy.MeanTopK) {
			if (topkUpdateQueue == null)
				System.out.println("cc");
			value = this.totValue / (this.topkUpdateQueue.size() + epsilon);
		} else {
			value = this.totValue / (this.nbVisits + epsilon);
		}

		// If the measure is not in [Ø,1], normalize it with the maximum value
		// encountered so far
		if (Global.measure == Enum.Measure.WRAcc)
			value /= Global.maxMeasure;

		// Optimize log computation
		double logX = parentSubgroup.nbVisits * parentSubgroup.rhoNorm;
		double logValue = Math.log(logX);
		double uctValue = value + 2. * Math.sqrt(logValue / (this.nbVisits * this.rhoNorm + epsilon))
				+ r.nextDouble() * epsilon;

		return uctValue;
	}

	/**
	 * Computes the UCB1 Value of the subgroup
	 * 
	 * @return The UCB1 Value
	 */
	public double computeUCB1(Subgroup parentSubgroup) {
		// Take into account the initial seed of the tree (whose parent is null)
		// There is a single child (the subgroup with the label to
		// characterize, without a restriction
		if (this.parent == null) {
			return Double.MAX_VALUE;
		}

		// small random number to break ties randomly in unexpanded nodes
		double value = 0.;
		if (Global.updatePolicy == Enum.UpdatePolicy.Max) {
			value = this.totValue * nbVisits / (this.nbVisits + epsilon);
		} else if (Global.updatePolicy == Enum.UpdatePolicy.MeanTopK) {
			value = this.totValue / (this.topkUpdateQueue.size() + epsilon);
		} else {
			value = this.totValue / (this.nbVisits + epsilon);
		}

		// If the measure is not in [Ø,1], normalize it with the maximum value
		// encountered so far
		if (Global.measure == Enum.Measure.WRAcc)
			value /= Global.maxMeasure;

		// Optimize log computation
		int logX = (int) parentSubgroup.nbVisits;
		double logValue;
		if (Global.logList[logX - 1] != 0) {
			logValue = Global.logList[logX - 1];
		} else {
			logValue = Math.log(logX);
			Global.logList[logX - 1] = logValue;
		}

		double uctValue = value + Math.sqrt(2. * logValue / (this.nbVisits + epsilon)) + r.nextDouble() * epsilon;

		return uctValue;
	}

	/**
	 * Computes the UCBSP Value of the subgroup
	 * 
	 * @return The UCBSP Value
	 */
	public double computeUCBSP(Subgroup parentSubgroup) {
		// Take into account the initial seed of the tree (whose parent is null)
		// There is a single child (the subgroup with the label to
		// characterize, without a restriction
		if (this.parent == null) {
			return Double.MAX_VALUE;
		}

		// small random number to break ties randomly in unexpanded nodes
		double value = 0.;
		if (Global.updatePolicy == Enum.UpdatePolicy.Max) {
			value = this.totValue * nbVisits / (this.nbVisits + epsilon);
		} else if (Global.updatePolicy == Enum.UpdatePolicy.MeanTopK) {
			value = this.totValue / (this.topkUpdateQueue.size() + epsilon);
		} else {
			value = this.totValue / (this.nbVisits + epsilon);
		}

		// If the measure is not in [Ø,1], normalize it with the maximum value
		// encountered so far
		if (Global.measure == Enum.Measure.WRAcc)
			value /= Global.maxMeasure;

		// Optimize log computation
		int logX = (int) parentSubgroup.nbVisits;
		double logValue;
		if (Global.logList[logX - 1] != 0) {
			logValue = Global.logList[logX - 1];
		} else {
			logValue = Math.log(logX);
			Global.logList[logX - 1] = logValue;
		}

		double UCBSP = value + 0.5 * Math.sqrt(logValue / (this.nbVisits + epsilon))
				+ Math.sqrt(variance + 10000 / this.nbVisits) + r.nextDouble() * epsilon;

		return UCBSP;
	}

	/**
	 * Computes the UCB1-Tuned Value of the subgroup
	 * 
	 * @return The UCUCB1-TunedBSP Value
	 */
	public double computeUCBTuned(Subgroup parentSubgroup) {
		// Take into account the initial seed of the tree (whose parent is null)
		// There is a single child (the subgroup with the label to
		// characterize, without a restriction
		if (this.parent == null) {
			return Double.MAX_VALUE;
		}

		// small random number to break ties randomly in unexpanded nodes
		double value = 0.;
		if (Global.updatePolicy == Enum.UpdatePolicy.Max) {
			value = this.totValue * nbVisits / (this.nbVisits + epsilon);
		} else if (Global.updatePolicy == Enum.UpdatePolicy.MeanTopK) {
			value = this.totValue / (this.topkUpdateQueue.size() + epsilon);
		} else {
			value = this.totValue / (this.nbVisits + epsilon);
		}

		// If the measure is not in [Ø,1], normalize it with the maximum value
		// encountered so far
		if (Global.measure == Enum.Measure.WRAcc)
			value /= Global.maxMeasure;

		// Optimize log computation
		int logX = (int) parentSubgroup.nbVisits;
		double logValue;
		if (Global.logList[logX - 1] != 0) {
			logValue = Global.logList[logX - 1];
		} else {
			logValue = Math.log(logX);
			Global.logList[logX - 1] = logValue;
		}

		double UCBSP = value
				+ Math.sqrt((logValue / (this.nbVisits + epsilon))
						* Math.min(0.25, variance + Math.sqrt(2. * logValue / this.nbVisits)))
				+ r.nextDouble() * epsilon;

		return UCBSP;
	}

	public void computeRhoNorm(int idChild) {
		if (idChild == -1) {
			rhoNorm = 1;
		} else if (Global.propType == DataType.NOMINAL || Global.propType == DataType.BOOLEAN) {
			int idValue = idChild;
			int idAttr = -1;
			for (int i = 0; i < Global.attributes.length; i++) {
				int attSize = ((AttributeNominal) Global.attributes[i]).getOrderedValues().length;
				if (idValue < attSize) { // This attribute is chosen
					idAttr = i;
					break;
				} else { // switch to the next attribute
					idValue -= attSize;
				}
			}
			rhoNorm = Math.pow(2, idAttr + 1 - (this.description).getDescriptionSize());
		} else if (Global.propType == DataType.NUMERIC) {
			rhoNorm = 1;
			int idAttr = idChild / 2;
			PatternNumerical descr = (PatternNumerical) this.description;
			if (!descr.lastRightMove) {
				Literal lit = descr.description.get(idAttr);
				int nbVal = lit.getIdMax() - lit.getIdMin() + 1;
				rhoNorm *= (nbVal + 1) / 2;
			}

			for (int idNextAttr = idAttr + 1; idNextAttr < Global.nbAttr; idNextAttr++) {
				int nbVal = ((AttributeNumerical) (Global.attributes[idNextAttr])).getOrderedValues().length;
				rhoNorm *= (nbVal + 1) / 2;
			}

		} else {
			System.out.println("[computeRhoNorm] not implemented for the type of data : " + Global.propType);
		}
	}

	/**
	 * Deletes this subgroup and frees all attributes and children of this
	 * subgroup
	 */
	public void delete() {
		this.description.delete();
		this.target.delete();
		this.deleteChildren();
		this.children = null;
		this.parent = null;
	}

	/**
	 * Deletes the children of this subgroup
	 */
	public void deleteChildren() {
		if (this.children == null) {
			return;
		}
		for (Entry<Integer, Subgroup> entry : this.children.entrySet()) {
			Integer i = entry.getKey();
			Subgroup aChild = entry.getValue();
			if (aChild == null) {
				continue;
			}

			aChild.delete();
			this.children.put(i, null);
		}
	}

	public String writeSupport() {
		String res = "";
		for (int idMol = this.description.support.nextSetBit(0); idMol >= 0; idMol = this.description.support
				.nextSetBit(idMol + 1)) {
			if (!res.isEmpty())
				res += " ";

			int actualID = idMol + 1;
			res += actualID;
		}
		return res;
	}

	public String writeSupportE11() {
		String res = "";
		OpenBitSet temp = (OpenBitSet) this.description.support.clone();
		temp.and(this.target.support);
		for (int idMol = temp.nextSetBit(0); idMol >= 0; idMol = temp.nextSetBit(idMol + 1)) {
			if (!res.isEmpty())
				res += " ";

			int actualID = idMol + 1;
			res += actualID;
		}
		return res;
	}

	private void computeMeanMeasure(double measure) {
		Global.meanMeasure = (double) ((Global.meanMeasure * Global.nbPatterns) + measure) / (Global.nbPatterns + 1);
		Global.nbPatterns++;
	}

	@Override
	public String toString() {
		return description + "\t" + target + "\t" + this.measure + "\t" + this.e11 + "\t"
				+ (this.description.supportSize - this.e11) + "\t" + (this.target.supportSize - this.e11);
	}

	/**
	 * The comparator for Subgroup wrt the measure
	 */
	public static Comparator<Subgroup> subgroupComparatorMeasureReverse = new Comparator<Subgroup>() {

		@Override
		public int compare(Subgroup c1, Subgroup c2) {
			if (c1.measure - c2.measure < 0) {
				return 1;
			}
			if (c1.measure - c2.measure > 0) {
				return -1;
			}
			return 0;
		}
	};

	/**
	 * The comparator for Subgroup wrt the measure
	 */
	public static Comparator<Subgroup> subgroupComparatorMeasure = new Comparator<Subgroup>() {

		@Override
		public int compare(Subgroup c1, Subgroup c2) {
			if (c1.measure - c2.measure > 0) {
				return 1;
			}
			if (c1.measure - c2.measure < 0) {
				return -1;
			}
			return 0;
		}
	};

	/**
	 * Return the depth in the tree which root is this subgroup
	 * 
	 * @return the depth of the tree
	 */
	public int getDepth() {
		if (this.children == null)
			return 0;

		int max = -1;
		for (Subgroup sg : this.children.values()) {
			if (sg != null) {
				int temp = sg.getDepth();
				if (temp > max)
					max = temp;
			}
		}

		return (1 + max);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(java.lang.Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subgroup other = (Subgroup) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

}
