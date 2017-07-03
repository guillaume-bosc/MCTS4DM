package nl.liacs.subdisc.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.cui.*;

public class CuiDomainChooser extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final Set<String> KNOWN_DOMAINS =
				new HashSet<String>(CuiMapInterface.NR_DOMAINS);

	private List<File> itsAvailableDomains;
	private File itsDomainFile;
	private ButtonGroup itsDomainButtons = new ButtonGroup();

	// TODO discuss with CMSB to find another way (eg. release.info file)
	static
	{
		KNOWN_DOMAINS.add("Acquired Abnormality");
		KNOWN_DOMAINS.add("Anatomical Abnormality");
		KNOWN_DOMAINS.add("Anatomical Structure");
		KNOWN_DOMAINS.add("Anatomy");
		KNOWN_DOMAINS.add("Biological Process"); // GO
		KNOWN_DOMAINS.add("Body Location or Region");
		KNOWN_DOMAINS.add("Body Part, Organ, or Organ Component");
		KNOWN_DOMAINS.add("Body Space or Junction");
		KNOWN_DOMAINS.add("Body Substance");
		KNOWN_DOMAINS.add("Body System");
		KNOWN_DOMAINS.add("Cell Component");
		KNOWN_DOMAINS.add("Cell or Molecular Dysfunction");
		KNOWN_DOMAINS.add("Cell");
		KNOWN_DOMAINS.add("Cellular Component"); // GO
		KNOWN_DOMAINS.add("Congenital Abnormality");
		KNOWN_DOMAINS.add("Disease or Syndrome");
		KNOWN_DOMAINS.add("Disorders");
		KNOWN_DOMAINS.add("drugbank");
		KNOWN_DOMAINS.add("Embryonic Structure");
		KNOWN_DOMAINS.add("Fully Formed Anatomical Structure");
		KNOWN_DOMAINS.add("Genes");
		KNOWN_DOMAINS.add("GO"); // GO
		KNOWN_DOMAINS.add("Mental or Behavioral Dysfunction");
		KNOWN_DOMAINS.add("Molecular Function"); // GO
		KNOWN_DOMAINS.add("Neoplastic Process");
		KNOWN_DOMAINS.add("Pathologic Function");
		KNOWN_DOMAINS.add("Sign or Symptom");
		KNOWN_DOMAINS.add("Tissue");
	}

	public CuiDomainChooser()
	{
		super.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		super.setTitle("CUI Domain Chooser");
		super.setIconImage(MiningWindow.ICON);
		super.setLocation(100, 100);
		initComponents();
//		setSize(GUI.DEFAULT_WINDOW_DIMENSION);	// TODO
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	private void initComponents()
	{
		JPanel aMasterPanel = new JPanel();
		JPanel aRadioButtonPanel = new JPanel();
		final JPanel aButtonPanel = new JPanel();
		JRadioButton aRadioButton;

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		aMasterPanel.setLayout(new BoxLayout(aMasterPanel, BoxLayout.Y_AXIS));
		aRadioButtonPanel.setBorder(GUI.buildBorder("Select CUI Domain"));

		File aCuiDir = new File(CuiMapInterface.CUI_DIR);

		if (aCuiDir == null || !aCuiDir.exists())
		{
			ErrorLog.log(aCuiDir, new FileNotFoundException());
			aRadioButtonPanel.add(new JLabel("No Domain Files Found"));
		}
		else
		{
			itsAvailableDomains =
				new ArrayList<File>(CuiMapInterface.NR_DOMAINS);
			// TODO this will be checked recursively
			for (File f : aCuiDir.listFiles())
				if (KNOWN_DOMAINS.contains(FileType.removeExtension(f)))
					itsAvailableDomains.add(f);
			Collections.sort(itsAvailableDomains);

			aRadioButtonPanel.setLayout(new BoxLayout(aRadioButtonPanel,
									BoxLayout.Y_AXIS));

			for (File f : itsAvailableDomains)
			{
				aRadioButton = new JRadioButton(FileType.removeExtension(f));
				aRadioButton.setActionCommand(f.getAbsolutePath());
				aRadioButtonPanel.add(aRadioButton);
			}

			for (Component c : aRadioButtonPanel.getComponents())
				itsDomainButtons.add((AbstractButton) c);

			if (itsAvailableDomains.size() > 0)
				((JRadioButton) aRadioButtonPanel.getComponent(0))
				.setSelected(true);

			aButtonPanel.add(
				GUI.buildButton("Use Domain", KeyEvent.VK_U, "domain", this));
			aButtonPanel.add(
				GUI.buildButton("Cancel", KeyEvent.VK_C, "cancel", this));
	
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowOpened(WindowEvent e)
				{
					aButtonPanel.getComponent(0).requestFocusInWindow();
				}
			});
		}
		aMasterPanel.add(aRadioButtonPanel);
//		aMasterPanel.add(new JScrollPane(aRadioButtonPanel));
		aButtonPanel.setAlignmentX(LEFT_ALIGNMENT);
		aMasterPanel.add(aButtonPanel);
		getContentPane().add(aMasterPanel);
	}

	@Override
	public void actionPerformed(ActionEvent theEvent)
	{
		String aCommand = theEvent.getActionCommand();

		if ("domain".equals(aCommand))
		{
			itsDomainFile =
				new File(itsDomainButtons.getSelection().getActionCommand());
			dispose();
		}
		else if ("cancel".equals(aCommand))
			dispose();
	}

	public File getFile()
	{
		return itsDomainFile;
	}
}
