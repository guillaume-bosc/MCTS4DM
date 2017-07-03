package nl.liacs.subdisc;

import java.io.*;

public class LoaderFraunhofer
{
	private int itsNrLines = 0;

	// default file loader
	public LoaderFraunhofer(File theFile)
	{
		BufferedReader aReader = null;
		try
		{
			aReader = new BufferedReader(new FileReader(theFile));
			String aHeaderLine = null;
			String aLine;
			int aLineNr = 0;

			while ((aLine = aReader.readLine()) != null)
			{
				++aLineNr;

				//do stuff

				if (aLineNr % 100 == 0)
					Log.logCommandLine("" + aLineNr + " lines read");
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
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
			}
		}
	}
}
