package nl.liacs.subdisc;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

public class DAGView extends JPanel implements Serializable, MouseListener
{
	private static final long serialVersionUID = 1L;

	private static final String PROP_SAMPLE_PROPERTY = "SampleProperty";
	private String sampleProperty;
	private PropertyChangeSupport propertySupport;
	private ArrayList<VisualNode> itsComponentSet = new ArrayList<VisualNode>();
	private ArrayList<VisualArc> itsConnectorSet = new ArrayList<VisualArc>();
	private DAG itsDAG;
	private int itsDAGSize;
	private int itsDAGWidth;
	private int itsDAGHeight;
	Random itsRandom;

	public DAGView(DAG theDAG)
	{
		super();
		itsDAG = theDAG;
		itsDAGSize = itsDAG.getSize();
		itsRandom = new Random(System.currentTimeMillis()); // truly random
//		itsRandom = new Random(12345); // random, but always the same
		propertySupport = new PropertyChangeSupport(this);
		setBackground(Color.white);
		addMouseListener(this);
	}

	public void setDAGArea(int theWidth, int theHeight)
	{
		itsDAGWidth = theWidth;
		itsDAGHeight = theHeight;
	}

	public void drawDAG()
	{
		for(int i = 0; i < itsDAGSize; i++)
		{
			int anX = itsDAGWidth/2 + itsRandom.nextInt(20);
			int aY = itsDAGHeight/2 + itsRandom.nextInt(20);
			VisualNode aNode = new VisualNode(anX, aY, itsDAG.getName(i));
			addNodeComponent(aNode);
		}

		for(int i = 0; i < itsDAGSize; i++)
		{
			for(int j = 0; j < i ; j++)
			{
				switch(itsDAG.getNode(j).isConnected(i))
				{
					case 0: //we have i-/-j (not connected)
					{
						break;
					}
					case 1: //we have i-->j
					{
						connect(i, j);
						break;
					}
					case 2: //we have i<--j
					{
						connect(j, i);
						break;
					}
				}
			}
		}

		ApplySOM();
		updateConnectors();
	}

	// draw DAG, using the layout of another DAG in theDAGView
	// the drag events will no longer work, because the components of another DAGView are being used
	// you can drag the nodes in the original window, and then double click this window to see the changes in layout
	public void drawDAG(DAGView theDAGView)
	{
		itsComponentSet = theDAGView.itsComponentSet; // no deep copy
		for(int i = 0; i < itsDAGSize; i++)
		{
			for(int j = 0; j < i ; j++)
			{
				switch(itsDAG.getNode(j).isConnected(i))
				{
					case 0: //we have i-/-j (not connected)
					{
						break;
					}
					case 1: //we have i-->j
					{
						connect(i, j);
						break;
					}
					case 2: //we have i<--j
					{
						connect(j, i);
						break;
					}
				}
			}
		}
		updateConnectors();
	}

	public void ApplySOM()
	{
		VisualNode aVisualNode;

		for(float aNeighbourhood = 1f; aNeighbourhood > 0f; aNeighbourhood -= 0.05f)
		{
			for(int i = 0; i < 1000; i++)
			{
				int anX = itsRandom.nextInt(itsDAGWidth - 30)-50;
				int aY = itsRandom.nextInt(itsDAGHeight - 20)-50;

				int aWinner = 0;
				aVisualNode = itsComponentSet.get(0);
				float aMinDistance = (aVisualNode.getX() - anX)*(aVisualNode.getX() - anX) + (aVisualNode.getY() - aY)*(aVisualNode.getY() - aY);

				//find closest
				for(int j = 1; j < itsDAGSize; j++)
				{
					aVisualNode = itsComponentSet.get(j);
					float aDistance = (aVisualNode.getX() - anX)*(aVisualNode.getX() - anX) + (aVisualNode.getY() - aY)*(aVisualNode.getY() - aY);
					if(aDistance < aMinDistance)
					{
						aMinDistance = aDistance;
						aWinner = j;
					}
				}

				aVisualNode = itsComponentSet.get(aWinner);
				NetworkNode aNode = itsDAG.getNode(aWinner);
				aVisualNode.shift((int) (0.1f * (anX - aVisualNode.getX())),
							(int) (0.1f * (aY - aVisualNode.getY())));

				for(int j = 0; j < itsDAGSize; j++)
				{
					if(aNode.isConnected(j) > 0)
					{
						aVisualNode = itsComponentSet.get(j);
						aVisualNode.shift((int) (aNeighbourhood * 0.1f * (anX - aVisualNode.getX())),
									(int) (aNeighbourhood * 0.1f * (aY - aVisualNode.getY())));
					}
				}
			}
		}
	}

	public void addNodeComponent(VisualNode theNode)
	{
		addMouseListener(theNode);
		addMouseMotionListener(theNode);
		itsComponentSet.add(theNode);
	}

	public void paint(Graphics g)
	{
		super.paint(g);

		for(VisualArc c : itsConnectorSet)
			c.paint(g);

		for(VisualNode c : itsComponentSet)
			c.paint(g);
	}

	public void updateConnectors()
	{
		for(VisualArc c : itsConnectorSet)
			c.calcBounds();
	}

	public void connect(int theID1, int theID2)
	{
		VisualNode aNode1 = itsComponentSet.get(theID1);
		VisualNode aNode2 = itsComponentSet.get(theID2);
		VisualArc anArc = new VisualArc(aNode1, aNode2);

		if(!itsConnectorSet.contains(anArc))
			itsConnectorSet.add(anArc);
	}

	public Dimension getMinimumSize()
	{
		Rectangle r = getBounds();
		return new Dimension(r.width, r.height);
	}

	public Dimension getPreferredSize()
	{
		Rectangle r = getBounds();
		return new Dimension(r.width, r.height);
	}

	public String getSampleProperty()
	{
		return sampleProperty;
	}

	public void setSampleProperty(String value)
	{
		String oldValue = sampleProperty;
		sampleProperty = value;
		propertySupport.firePropertyChange(PROP_SAMPLE_PROPERTY, oldValue, sampleProperty);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		propertySupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertySupport.removePropertyChangeListener(listener);
	}

	public void mouseExited(java.awt.event.MouseEvent mouseEvent) { }

	public void mouseReleased(java.awt.event.MouseEvent mouseEvent)
	{
		updateConnectors();
		repaint();
	}

	public void mousePressed(java.awt.event.MouseEvent mouseEvent) { }

	public void mouseClicked(java.awt.event.MouseEvent mouseEvent) { }

	public void mouseEntered(java.awt.event.MouseEvent mouseEvent) { }
}
