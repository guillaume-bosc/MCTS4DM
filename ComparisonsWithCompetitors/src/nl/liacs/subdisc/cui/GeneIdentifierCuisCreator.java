package nl.liacs.subdisc.cui;

import java.io.*;
import java.util.*;

import nl.liacs.subdisc.*;

/**
 * This class is not part of the public API, as it relies on a correct memory
 * setting, and correct CUI files.
 * This class creates an 'gene_identifier_cuis.txt' <code>File</code> for the
 * CUI-domains. It does not do any checking, as the structure of these files is
 * known. No line number is written to the resulting <code>File</code>, this
 * keeps the <code>File</code> smaller, and during loading the line number for
 * each GENE_IDENTIFIER_CUI can be calculated, as each one is on a new line.
 * NOTE: all domain files use the exact same GENE_IDENTIFIER_CUIs (the first
 * Column), so only one 'gene_identifier_cuis.txt' <code>File</code> is needed.
 */
class GeneIdentifierCuisCreator
{
	// may create setters for this one day
//	private final String itsSeparator= ",";
	private final String itsLineEnd = "\n";


	// TODO for testing, all files are identical
	public static void main(String[] args)
	{
		for (File f : new File(CuiMapInterface.CUI_DIR).listFiles())
		{
			if (f.getName().startsWith(CuiMapInterface.DOMAIN_FILE_PREFIX))
			{
				System.out.print(f.getName() + " ");
				long aBegin = System.currentTimeMillis();
				new GeneIdentifierCuisCreator(f);
				System.out.println((System.currentTimeMillis() - aBegin) / 1000 + "s.");
			}
		}
	}


	// TODO return boolean indicating success?
	GeneIdentifierCuisCreator(File theFile)
	{
		if (theFile == null || !theFile.exists())
		{
			ErrorLog.log(theFile, new FileNotFoundException());
			return;
		}
		else
			parseFile(theFile);
	}

	/*
	 * Reading and writing on same disk may be relatively slow. Also, the
	 * 'gene_identifier_cuis.txt' file is small enough to completely keep in
	 * memory, and only write after whole file is read.
	 * The initial size of aList is based on CUI_20100104 expression files.
	 * TODO could use raw inputStreamReader() as the size of all fields is known
	 * (all 8 characters long).
	 */
	private void parseFile(File theFile)
	{
		BufferedReader aReader = null;
		List<String> aList =
					new ArrayList<String>(CuiMapInterface.NR_EXPRESSION_CUI);

		try
		{
			// Scanner() on file is 5x slower than BufferedFeader()
			aReader = new BufferedReader(new FileReader(theFile));
			String aLine = aReader.readLine(); // skip headerLine

			// Scanner() is 4x faster than aLine.split()[0]
			while ((aLine = aReader.readLine()) != null)
				aList.add(new Scanner(aLine).useDelimiter(",").next());
		}
		catch (IOException e)
		{
			ErrorLog.log(theFile, e);
			return;
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
				ErrorLog.log(theFile, e);
			}
		}

		writeGeneCuiFile(theFile, aList);
	}

	private void writeGeneCuiFile(File theFile, List<String> theGeneCuiList)
	{
		BufferedWriter aWriter = null;

		try
		{
			aWriter =
				new BufferedWriter(
					new FileWriter(theFile.getParent() +
							CuiMapInterface.GENE_IDENTIFIER_CUIS.substring(3)));

//			aWriter.write(itsSeparator);
//			aWriter.write((++aLineNr));
			for (String s : theGeneCuiList)
			{
				aWriter.write(s);
				aWriter.write(itsLineEnd);
			}
		}
		catch (IOException e)
		{
			ErrorLog.log(theFile, e);
			return;
		}
		finally
		{
			try
			{
				if (aWriter != null)
				{
					aWriter.flush();
					aWriter.close();
				}
			}
			catch (IOException e)
			{
				ErrorLog.log(theFile, e);
			}
		}
	}

/*
	public enum Separator { COMMA, TAB, SEMICOLON, COLON }
	public enum LineEnd { UNIX, WINDOWS, MACINTOSH, }

	public void setNewSeparator(Separator theNewSeparator)
	{
		if (theNewSeparator != null)
			itsSeparator = theNewSeparator;
	}

	public void setNewLine(LineEnd theNewLineEnd)
	{
		if (theNewLineEnd != null)
			itsLineEnd = theNewLineEnd;
	}
*/
}
