/*
 * TODO this class does not remove quotes: 'value'
 * TODO skip empty lines before/within/after data
 */
package nl.liacs.subdisc;

import java.io.*;
import java.util.*;

@Deprecated
public class FileLoaderTXT implements FileLoaderInterface
{
	private Table itsTable = null;
	private boolean checkDataWithXMLTable = false;
	private String itsSeparator = FileLoaderInterface.DEFAULT_SEPARATOR;

	public FileLoaderTXT(File theFile)
	{
		if (theFile == null || !theFile.exists())
		{
			// TODO new ErrorDialog(e, ErrorDialog.noSuchFileError);
			Log.logCommandLine(
					String.format("FileLoaderTXT: can not open File '%s'",
									theFile.getAbsoluteFile()));
			return;
		}
		else
			loadFile(theFile);
	}

	public FileLoaderTXT(File theFile, Table theTable)
	{
		if (theFile == null || !theFile.exists())
		{
			// TODO new ErrorDialog(e, ErrorDialog.noSuchFileError);
			Log.logCommandLine(
					String.format("FileLoaderTXT: can not open File '%s'",
									theFile.getAbsolutePath()));
			return;
		}
		else if (theTable == null)
			// TODO warning, try normal loading
			loadFile(theFile);
		else
		{
			checkDataWithXMLTable = true;
			itsTable = theTable;
			loadFile(theFile);
		}
	}

	private void loadFile(File theFile)
	{
		if (checkFormatAndType(theFile))
		{
			//does not call getNrColumns(), itsNrRows is not set yet
			int aNrColumns = itsTable.getColumns().size();
			BufferedReader aReader = null;

			try
			{
				aReader = new BufferedReader(new FileReader(theFile));
				String aLine;
				int aLineNr = 0;// TODO remove

				//skip header, make sure line is not empty/null
				while ((aLine = aReader.readLine()) != null)
					if (!aLine.isEmpty())
						break;

				while ((aLine = aReader.readLine()) != null)
				{
					if (aLine.isEmpty())
						continue;

					String[] anImportRow = aLine.split(itsSeparator, -1);
					//read fields
					for (int i = 0; i < aNrColumns; i++)
					{
						Column aColumn = itsTable.getColumns().get(i);

						switch (aColumn.getType())
						{
							case NOMINAL :
							{
								aColumn.add(anImportRow[i].trim());
								break;
							}
							case NUMERIC :
							case ORDINAL :
							{
								aColumn.add(Float.parseFloat(anImportRow[i]));
								break;
							}
							case BINARY :
							{
								aColumn.add(anImportRow[i].trim().equals("1"));
								break;
							}
							default : break; // TODO ERROR
						}
					}
					++aLineNr;
					if (aLineNr % 10000 == 0)
						System.out.println(aLineNr + " lines read");
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
//				new ErrorDialog(e, ErrorDialog.fileReaderError);
			}
			finally
			{
				try
				{
					if (aReader != null)
						aReader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
//					new ErrorDialog(e, ErrorDialog.fileReaderError);
				}
			}
		}
	}

	private boolean checkFormatAndType(File theFile)
	{
		boolean isWellFormedFile = false; //default should always be false
		BufferedReader aReader = null;

		try
		{
			aReader = new BufferedReader(new FileReader(theFile));
			BitSet aNominals = new BitSet();
			BitSet aNotZeroOne = new BitSet();
			String aLine;

			// make sure line is not empty/null
			while ((aLine = aReader.readLine()) != null)
				if (!aLine.isEmpty())
					break;
			isWellFormedFile = true;

			String[] aHeaders = aLine.split(itsSeparator, -1);
			int aNrColumns = aHeaders.length;
			int aNrRows = 0;

			/*
			 * if read from XML:
			 * check (number of) Attributes
			 * check number of columns in each line
			 */
			if (checkDataWithXMLTable)
			{
				// check if number of columns is equal in XML and File
				if (aNrColumns != itsTable.getColumns().size())
				{
					Log.logCommandLine(
						"The number of Attributes for the Table read from " +
						"XML is not the same as that for the File " +
						theFile.getName() + "'.");
					return false;
				}
				// check if AttributeNames are equal in XML and File
				else
				{
					for (int i = 0; i < aNrColumns; i++)
					{
						if (!aHeaders[i].trim().equals(itsTable
														.getColumn(i)
														.getName()))
						{
							Log.logCommandLine(
								String.format(
								"At index %d: Attribute '%s' from XML does " +
								"not match Attribute '%s' from File '%s'.",
								(i + 1),
								itsTable.getColumns().get(i).getName(),
								aHeaders[i].trim(),
								theFile.getName()));
							return false;
						}
					}
				}

				// check number of columns for each line in the File
				while ((aLine = aReader.readLine()) != null)
				{
					if (aLine.isEmpty())
						continue;

					++aNrRows;

					String[] aRow = aLine.split(itsSeparator, -1);
					int aLineNrColumns = aRow.length;

					if (aLineNrColumns != aNrColumns)
					{
						Log.logCommandLine(
							String.format(
							"Line %d has %d columns instead of the expected %d." // TODO  1 ? column(s)
							, (aNrRows + 1), aLineNrColumns, aNrColumns));
						isWellFormedFile = false;
						// continue while to inform about more malformed lines
					}
				}
			}
			/*
			 * if not read from XML:
			 * check number of columns in each line
			 * determine AttributeType of each column
			 * create Table and Columns
			 */
			else
			{
				while ((aLine = aReader.readLine()) != null)
				{
					if (aLine.isEmpty())
						continue;

					++aNrRows;

					String[] aRow = aLine.split(itsSeparator, -1);
					int aLineNrColumns = aRow.length;

					// check number of columns for each line in the File
					if (aLineNrColumns != aNrColumns)
					{
						Log.logCommandLine(
							String.format(
							"Line %d has %d columns instead of the expected %d."
							, (aNrRows + 1), aLineNrColumns, aNrColumns));
						isWellFormedFile = false;
						//continue while to inform about more malformed lines
					}

					/*
					 * determine AttributeType of each column
					 * even if !isWellFormedFile
					 * allows creation of empty Table with all Attributes set
					 * only first 'aNrColumns' columns are used
					 */
					for (int i = 0; i < aNrColumns; i++)
					{
						String aCell = aRow[i];
						try
						{
							Float.parseFloat(aCell);
							//numeric could be binary also
							if (!aCell.equals("0") && !aCell.equals("1"))
								aNotZeroOne.set(i);
						}
						// if not a float
						catch (NumberFormatException anException)
						{
							aNominals.set(i);
						}
					}
				}

				// create Table and Columns
				itsTable = new Table(theFile, aNrRows, aNrColumns);

				for (int i = 0; i < aNrColumns; i++)
				{
					if (aNominals.get(i))
						itsTable.getColumns()
						.add(new Column(aHeaders[i].trim(),
										null,
										AttributeType.NOMINAL,
										i,
										aNrRows));
					else if (aNotZeroOne.get(i))
						itsTable.getColumns()
						.add(new Column(aHeaders[i].trim(),
										null,
										AttributeType.NUMERIC,
										i,
										aNrRows));
					else
						itsTable.getColumns()
						.add(new Column(aHeaders[i].trim(),
										null,
										AttributeType.BINARY,
										i,
										aNrRows));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
//			new ErrorDialog(e, ErrorDialog.fileReaderError);
			return false;
		}
		finally
		{
			if (!isWellFormedFile)
				Log.logCommandLine(
					"File '" +
					theFile + 
					"' is not well-formed,\n i.e. not all records have the " +
					"same number of Attributes. The Table will be 'dataless'.");

			try
			{
				if (aReader != null)
					aReader.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
//				new ErrorDialog(e, ErrorDialog.fileReaderError);
			}
		}
		return isWellFormedFile;
	}

	public void setSeparator(String theNewSeparator)
	{
		itsSeparator = theNewSeparator;
	}

	@Override
	public Table getTable()
	{
		// TODO will still return a table, even if no data is loaded, change
		// MiningWindow could fall back to 'no table' if itsTable.getNrRows == 0
		return itsTable;
	}
}
