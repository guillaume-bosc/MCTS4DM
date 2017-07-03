package nl.liacs.subdisc;

import java.io.*;

import org.w3c.dom.*;

public class FileLoaderXML implements FileLoaderInterface
{
	private Table itsTable;
	private SearchParameters itsSearchParameters;

	public FileLoaderXML(File theFile)
	{
		if (theFile == null || !theFile.exists())
		{
			Log.logCommandLine(
				String.format(
					"FileLoaderXML: can not open File '%s'",
					theFile.getAbsolutePath()));
			return;
		}
		else
			loadFile(theFile);
	}

	private void loadFile(File theFile)
	{
		NodeList aSettings = XMLDocument.parseXMLFile(theFile)
						.getLastChild()
						.getFirstChild()
						.getChildNodes();

		// order of nodes is known but use fail-safe checking for now
		// also order is important (Table, SearchParameters, TargetConcept)
		for(int i = aSettings.getLength() - 1; i >= 0; i--)
		{
			String aNodeName = aSettings.item(i).getNodeName();
			if ("table".equalsIgnoreCase(aNodeName))
				itsTable = new Table(aSettings.item(i), theFile.getParent());
			else if ("search_parameters".equalsIgnoreCase(aNodeName))
				itsSearchParameters = new SearchParameters(aSettings.item(i));
			// NOTE order sensitive, SearchParameters must be set first
			else if ("target_concept".equalsIgnoreCase(aNodeName))
				itsSearchParameters.setTargetConcept(new TargetConcept(aSettings.item(i),
											itsTable));
		}
	}

	@Override
	public Table getTable() { return itsTable; }

	public SearchParameters getSearchParameters()
	{
		return itsSearchParameters;
	}
}

