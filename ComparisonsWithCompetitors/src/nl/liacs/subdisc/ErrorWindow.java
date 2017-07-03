package nl.liacs.subdisc;

import javax.swing.*;

public class ErrorWindow extends javax.swing.JFrame
{
	private static final long serialVersionUID = 1L;

	public ErrorWindow(Exception theException)
	{
			initComponents(theException);
			this.getToolkit();
//			setIconImage(MiningWindow.ICON);
			setTitle("Error");
			pack();
	}

	public void initComponents(Exception theException)
	{
		JTextArea aTextArea = new JTextArea(20,30);
		aTextArea.setWrapStyleWord(true);
		aTextArea.setLineWrap(true);
		aTextArea.setEditable(false);
		aTextArea.append(theException.getMessage());
		aTextArea.append("\n" + "\n");

		//FREEZE
		//this code should be commented out if this is a production version
/*		if (!(theException instanceof org.xml.sax.SAXException ||
			theException instanceof java.sql.SQLException))
		{
			StackTraceElement[] aStackTraceElementArray = theException.getStackTrace();
			for (int i = 0; i < aStackTraceElementArray.length; i++)
			{
				aTextArea.append(aStackTraceElementArray[i].toString());
				aTextArea.append("\n");
			}
		}
*/

		JScrollPane aScrollPane = new JScrollPane();
		aScrollPane.add(aTextArea);
		aScrollPane.setViewportView(aTextArea);

		JPanel aJPanelSouth = new JPanel();
		JButton jButtonCloseWindow = new javax.swing.JButton();
		jButtonCloseWindow.setPreferredSize(new java.awt.Dimension(142, 25));
		jButtonCloseWindow.setBorder(new javax.swing.border.BevelBorder(0));
		jButtonCloseWindow.setMaximumSize(new java.awt.Dimension(142, 25));
		jButtonCloseWindow.setFont(new java.awt.Font ("Dialog", 1, 11));
		jButtonCloseWindow.setText("Close");
		jButtonCloseWindow.setMnemonic('C');
		jButtonCloseWindow.setMinimumSize(new java.awt.Dimension(142, 25));
		jButtonCloseWindow.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dispose();
			}
		}
		);
		aJPanelSouth.add(jButtonCloseWindow);

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				dispose();
			}
		}
		);

		getContentPane().add(aScrollPane, java.awt.BorderLayout.CENTER);
		getContentPane().add(aJPanelSouth, java.awt.BorderLayout.SOUTH);
	}
}