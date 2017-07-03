package nl.liacs.subdisc;

public class NetworkNode
{
	private CrossCube nodeParameters; //node parameters
	private ItemSet nodeParents; //parents
	private ItemSet nodeChildren; //children
	private double itsQuality;
	private String itsName;

	public NetworkNode(CrossCube para, int theSize, String theName)
	{
		nodeParameters = para;
		nodeChildren = new ItemSet(theSize);
		nodeParents = new ItemSet(theSize);
		itsQuality = -100000;
		itsName = theName;
	}

	public void addChild(int theChild)
	{
		nodeChildren.set(theChild);
	}

	public void removeChild(int theChild)
	{
		nodeChildren.clear(theChild);
	}

	public void removeParent(int theParent)
	{
		nodeParents.clear(theParent);
	}

	public void addParent(int theParent)
	{
		nodeParents.set(theParent);
	}

	public boolean isThisMyChild(int childNode)
	{
		return (nodeChildren.get(childNode));
	}

	public boolean isThisMyParent(int parentNode)
	{
		return (nodeParents.get(parentNode));
	}

	public int isConnected(int nd)
	{
		if (isThisMyParent(nd))
			return 1;
		else if (isThisMyChild(nd))
			return 2;
		else
			return 0;
	}

	int getNrParents() {return nodeParents.getItemCount();}
	ItemSet getParents() {return nodeParents;}
	int getNrChildren() {return nodeChildren.getItemCount();}
	ItemSet getChildren() {return nodeChildren;}
	double getQuality() {return itsQuality;}
	void setQuality(double theQuality) {itsQuality = theQuality;}
	CrossCube getParameters() {return nodeParameters;}
	void setParameters(CrossCube theParameters) {nodeParameters = theParameters;}
	String getName() {return itsName;}
}
