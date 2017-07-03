package nl.liacs.subdisc;

import javax.swing.*;

/* Note: Write a parent class for DataModelWindow, SelectionGraphWindow and DAGWindow */
public class DAGWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private DAGView itsDAGView;
	private boolean itsExitBehaviour;

	public DAGWindow(DAG theDAG,int theDAGWidth, int theDAGHeight, boolean theExitBehaviour)
	{
		initComponents();
		itsExitBehaviour = theExitBehaviour;
		itsDAGView = new DAGView(theDAG);
		itsDAGView.setDAGArea(theDAGWidth, theDAGHeight);
		itsDAGView.drawDAG();
		jScrollPaneCenter.setViewportView(itsDAGView);
//		setIconImage(MiningWindow.ICON);
		pack();
	}

	//create window with the same layout of nodes as the one in theDAGView
	public DAGWindow(DAG theDAG,int theDAGWidth, int theDAGHeight, boolean theExitBehaviour, DAGView theDAGView)
	{
		initComponents();
		itsExitBehaviour = theExitBehaviour;
		itsDAGView = new DAGView(theDAG);
		itsDAGView.setDAGArea(theDAGWidth, theDAGHeight);
		itsDAGView.drawDAG(theDAGView);
		jScrollPaneCenter.setViewportView(itsDAGView);
//		setIconImage(MiningWindow.ICON);
		pack();
	}

	private void initComponents()
	{
		jPanel = new javax.swing.JPanel();
		jButton = new javax.swing.JButton();
		jScrollPaneCenter = new javax.swing.JScrollPane();
		addWindowListener(
				new java.awt.event.WindowAdapter()
				{
					public void windowClosing(java.awt.event.WindowEvent evt)
					{
						exitForm(evt);
					}
				}
		);

		jButton.setPreferredSize(new java.awt.Dimension(80, 25));
		jButton.setBorder(new javax.swing.border.BevelBorder(0));
		jButton.setMaximumSize(new java.awt.Dimension(80, 25));
		jButton.setFont(new java.awt.Font ("Dialog", 1, 11));
		jButton.setText("Close");
		jButton.setMnemonic('C');
		jButton.setMinimumSize(new java.awt.Dimension(80, 25));
		jButton.addActionListener(
				new java.awt.event.ActionListener()
				{
					public void actionPerformed(java.awt.event.ActionEvent evt)
					{
						jButtonActionPerformed(evt);
					}
				}
		);

		jPanel.add(jButton);
		getContentPane().add(jScrollPaneCenter, java.awt.BorderLayout.CENTER);
		getContentPane().add(jPanel, java.awt.BorderLayout.SOUTH);
	}

	private void jButtonActionPerformed(java.awt.event.ActionEvent evt)
	{
		if (itsExitBehaviour) { dispose(); }
		else { System.exit(0); }
	}

	private void exitForm(java.awt.event.WindowEvent evt)
	{
		if (itsExitBehaviour) { dispose(); }
		else { System.exit(0); }
	}

	public DAGView getDAGView() { return itsDAGView; }

	private javax.swing.JPanel jPanel;
	private javax.swing.JButton jButton;
	private javax.swing.JScrollPane jScrollPaneCenter;
}
