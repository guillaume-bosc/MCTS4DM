package nl.liacs.subdisc;

import java.util.*;

public class DAG
{
	private double itsQuality;
	@SuppressWarnings("unused")
	private int noArcs; // debug counter
	private int itsSize;
	private List<NetworkNode> dagNode; //index is nodeId, value is Node

	private int[] visitation; //cyclic/order check stuff

	public DAG(int theSize)
	{
		itsQuality = 0;
		itsSize = theSize;
		dagNode = new ArrayList<NetworkNode>(theSize);
		for(int i=0; i<itsSize; i++)
		{
			dagNode.add(new NetworkNode(null, theSize, new String("node " + (i+1))));
			itsQuality += getNode(i).getQuality();
		}

		noArcs = 0;
		visitation = new int[theSize];
	}

	public DAG(List<Column> theTargets)
	{
		itsQuality = 0;
		itsSize = theTargets.size();
		dagNode = new ArrayList<NetworkNode>(itsSize);
		for(Column c : theTargets)
		{
			NetworkNode aNetworkNode = new NetworkNode(null, itsSize, c.getName());
			dagNode.add(aNetworkNode);
			itsQuality += aNetworkNode.getQuality();
		}

		noArcs = 0;
		visitation = new int[itsSize];
	}

	//nodes are printed from 1 to n instead of the regular 0 to n-1
	public void print()
	{
		for(int i=0; i<itsSize; ++i)
			for(int j=0; j<i ; ++j)
			{
				switch (getNode(j).isConnected(i))
				{
					case 0: //we have i-/-j (not connected)
					{
						break;
					}
					case 1: //we have i-->j
					{
						Log.logCommandLine("" + (i+1) + " -> " + (j+1));
						break;
					}
					case 2: //we have i<--j
					{
						Log.logCommandLine("" + (j+1) + " -> " + (i+1));
						break;
					}
				}
			}
	}

	public int getSize() {return itsSize;}
	public NetworkNode getNode(int nd) {return dagNode.get(nd);}
	public String getName(int aNode) {return dagNode.get(aNode).getName();}

	public void addArc(int fromNode, int toNode, boolean onlyAdd)
	{
		getNode(fromNode).addChild(toNode);
		getNode(toNode).addParent(fromNode);

		noArcs++;
		if (!onlyAdd)
			fixDimensions(toNode);
	}

	public boolean removeArc(int fromNode, int toNode, boolean onlyRemove)
	{
		if (!getNode(fromNode).isThisMyChild(toNode))
			return false;
		getNode(fromNode).removeChild(toNode);
		getNode(toNode).removeParent(fromNode);

		noArcs--;
		if (!onlyRemove)
			fixDimensions(toNode);
		return true;
	}

	public boolean addArcAcyclic(int fromNode, int toNode, boolean onlyAdd)
	{
		getNode(fromNode).addChild(toNode);
		getNode(toNode).addParent(fromNode);

		Arrays.fill(visitation, 0);
		visitation[fromNode] = 1;
		boolean tst = cyclicTst(getNode(fromNode).getChildren()); //returns true if cyclic
		if (tst) //is cyclic?
		{
			getNode(fromNode).removeChild(toNode);
			getNode(toNode).removeParent(fromNode);
		}
		else
		{
			noArcs++;
			if (!onlyAdd)
				fixDimensions(toNode);
		}
		return !tst;
	}

	private boolean cyclicTst(ItemSet ch)
	{
		int aSize = ch.size();

		for(int l=0; l<aSize; l++)
			if (ch.get(l))
			{
				if (visitation[l] == 2)
					continue; //node already closed
				else if (visitation[l] == 1)
					return true; //already visited implies cycle
				visitation[l] = 1;
				if (cyclicTst(getNode(l).getChildren()))
					return true;
				visitation[l] = 2; //close node
			}
		return false;
	}

/*
	public boolean remainsAcyclic(int fromNode, int toNode)
	{
		std::pair<itNodes, boolean> l=getNode(fromNode)->children().insert(toNode);
		//std::pair<itNodes, boolean> m=getNode(toNode)->parents().insert(fromNode);

		visitation.assign(dagNode.size(),0);
		visitation[fromNode]=1;
		boolean tst = cyclicTst(getNode(fromNode)->children()); //returns true if cyclic

		getNode(fromNode)->children().erase(l.first);
		//getNode(toNode)->parents().erase(m.first);

		return !tst;
	}

	*/

	double getQuality() { return itsQuality; }
	double getQuality(int theNode) { return getNode(theNode).getQuality(); }
	void setQuality(int theNode, double theQuality)
	{
		NetworkNode aNode = getNode(theNode);
		itsQuality = itsQuality - aNode.getQuality() + theQuality; // update total
		aNode.setQuality(theQuality);
	}

	public void fixDimensions(int nd)
	{
		CrossCube aCube = new CrossCube(getNode(nd).getNrParents());
		getNode(nd).setParameters(aCube); //fix
	}

	public boolean[][] determineVStructures()
	{	// Note: this function determines the v-structures, but we only store the edge that is missing in the v-structure.
		// So we cannot reconstruct the v-structures from solely the resulting boolean[][], but this is good enough for our purposes.
		boolean[][] result = new boolean[itsSize][itsSize];

		for(int x=0; x<itsSize; x++)
			for(int y=x+1; y<itsSize; y++)
				switch (getNode(y).isConnected(x))
				{
					case 0: //we have x-/-y (not connected)
					{
						for (int z=y+1; z<itsSize; z++)
							if (getNode(z).isConnected(x)==1 && getNode(z).isConnected(y)==1)
								result[x][y]=true;
						break;
					}
					case 1: //we have x-->y
					{
						for (int z=y+1; z<itsSize; z++)
							if (getNode(z).isConnected(x)==0 && getNode(z).isConnected(y)==2)
								result[x][z]=true;
						break;
					}
					case 2: //we have x<--y
					{
						for (int z=y+1; z<itsSize; z++)
							if (getNode(z).isConnected(x)==2 && getNode(z).isConnected(y)==0)
								result[y][z]=true;
						break;
					}
				}
		return result;
	}

	public boolean testVStructure ( int x, int y )
	{
		switch (getNode(y).isConnected(x))
		{
			case 0:
			{
				for (int z=0; z<itsSize; z++)
					if (getNode(z).isConnected(x)==1 && getNode(z).isConnected(y)==1)
						return true;
				break;
			}
			case 1:
			{
				for (int z=0; z<itsSize; z++)
					if (getNode(z).isConnected(x)==0 && getNode(z).isConnected(y)==2)
						return true;
				break;
			}
			case 2:
			{
				for (int z=y+1; z<itsSize; z++)
					if (getNode(z).isConnected(x)==2 && getNode(z).isConnected(y)==0)
						return true;
				break;
			}
		}
		return false;
	}
}
