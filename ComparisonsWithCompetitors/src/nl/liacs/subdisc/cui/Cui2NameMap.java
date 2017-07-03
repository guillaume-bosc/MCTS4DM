package nl.liacs.subdisc.cui;

import java.io.*;
import java.util.*;

import nl.liacs.subdisc.*;

public class Cui2NameMap implements CuiMapInterface
{
	private final HashMap<String, String> itsCui2NameMap;

	public Cui2NameMap(File theFile)
	{
		if (theFile == null || !theFile.exists())
		{
			itsCui2NameMap = null;
			ErrorLog.log(theFile, new FileNotFoundException());
			return;
		}
		else
		{
			itsCui2NameMap =
							new HashMap<String, String>(CuiMapInterface.NR_CUI);
			parseFile(theFile);
		}
	}

	private void parseFile(File theFile)
	{
		BufferedReader aReader = null;

		try
		{
			aReader = new BufferedReader(new FileReader(theFile));
			String aLine;
			String[] aLineArray;

//			aReader.readLine();	// there is no headerLine
			while ((aLine = aReader.readLine()) != null)
			{
				aLineArray = aLine.split("\t");
				itsCui2NameMap.put(aLineArray[0], aLineArray[1]);
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
				if (aReader != null)
					aReader.close();
			}
			catch (IOException e)
			{
				ErrorLog.log(theFile, e);
			}
		}
	}

	/**
	 * Returns the <code>Map<String, String></code> for this Cui2NameMap.
	 * 
	 * @return the <code>Map<String, String></code> for this Cui2NameMap, or
	 * <code>null</code> if there is none.
	 */
	@Override
	public Map<String, String> getMap()
	{
		if (itsCui2NameMap == null)
			return null;
		else
			return Collections.unmodifiableMap(itsCui2NameMap);
	}

}
