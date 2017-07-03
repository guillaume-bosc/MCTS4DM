package nl.liacs.subdisc.gui;

import javax.swing.*;

public class RemoveDomainWindow extends BasicJListWindow// implements ActionListener//, ListSelectionListener
{
	private static final long serialVersionUID = 1L;

	public RemoveDomainWindow(JList theJList)
	{
		super(theJList);

		if (theJList == null)
			constructorWarning("RemoveDomainWindow", true);
		else if (itsJList.getModel().getSize() == 0)
			constructorWarning("RemoveDomainWindow", false);
		else
			display("Remove Enrichment Source");
	}
}
