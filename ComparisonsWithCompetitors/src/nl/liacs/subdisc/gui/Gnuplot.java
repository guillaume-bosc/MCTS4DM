package nl.liacs.subdisc.gui;

import java.io.*;
import java.util.*;

import nl.liacs.subdisc.*;

public class Gnuplot
{
	private static final String DELIMITER = "\t";
	private static final String COLUMN_HEADER =
		"threshold" + DELIMITER + "FPR" + DELIMITER + "TPR"; // x y z
	private static final String DATA_EXT = ".dat";
	private static final String SCRIPT_EXT = ".gp";

	// uninstantiable
	private Gnuplot() {};


	// TODO null checks + theStatistics.size() > 0
	public static void writeSplotSccript(Column theColumn, List<List<Float>> theStatistics)
	{
		final String aBaseName = String.format("%s_%d",
						theColumn.getName(),
						System.currentTimeMillis());

		writeData(aBaseName, theStatistics);
		writeSplotScript(theColumn.getName(), aBaseName, theStatistics);
	}

	private static void writeData(String theBaseName, List<List<Float>> theStatistics)
	{
		BufferedWriter br = null;

		try
		{
			final File f  = new File(theBaseName + DATA_EXT);
			br = new BufferedWriter(new FileWriter(f));

			int aSize = theStatistics.size();
			br.write(getColumnNumber(aSize));
			br.write(getHeader(theStatistics));
			br.write(getHeader2(aSize));
			// 3 > threshold FPR TPR ...
			for (int i = 3, j = getMax(theStatistics); i < j; i+=2)
				br.write(getDataLine(i, j, theStatistics));
			log("data", f);
		}
		catch (IOException e) {}
		finally
		{
			if (br != null)
			{
				try { br.close(); }
				catch (IOException e) {}
			}
		}
	}

	private static String getColumnNumber(int theSize)
	{
		StringBuilder sb = new StringBuilder(theSize*6);
		sb.append("#"); // commented header line
		sb.append(1);
		// *3 = threshold fpr tpr
		for (int i = 1, j = theSize*3; i < j; )
		{
			sb.append(DELIMITER);
			sb.append(++i);
		}
		return sb.append("\n").toString();
	}

	private static String getHeader(List<List<Float>> theStatistics)
	{
		StringBuilder sb = new StringBuilder(theStatistics.size()*64);
		sb.append("#"); // commented header line
		sb.append(makeTitle(theStatistics.get(0)));
		for (int i = 1, j = theStatistics.size(); i < j; ++i)
		{
			sb.append(DELIMITER);
			sb.append(DELIMITER);
			sb.append(DELIMITER);
			sb.append(makeTitle(theStatistics.get(i)));
		}
		return sb.append("\n").toString();
	}

	private static String makeTitle(List<Float> theStats)
	{
		return String.format("t=%f_n=%f_auc=%f",
					theStats.get(0),
					theStats.get(1),
					theStats.get(2));
	}

	private static String getHeader2(int theSize)
	{
		StringBuilder sb = new StringBuilder(theSize*20);
		sb.append("#"); // commented header line
		sb.append(COLUMN_HEADER);
		for (int i = 1, j = theSize; i < j; ++i)
		{
			sb.append(DELIMITER);
			sb.append(COLUMN_HEADER);
		}
		return sb.append("\n").toString();
	}

	private static int getMax(List<List<Float>> theStatistics)
	{
		int aMax = 0;
		for (List<Float> l : theStatistics)
			if (l.size() > aMax)
				aMax = l.size();
		return aMax;
	}

	private static String getDataLine(int theIndex, int theMaxSize, List<List<Float>> theStatistics)
	{
		StringBuilder sb = new StringBuilder(theStatistics.size()*20);
		sb.append(getDatum(theIndex, theStatistics.get(0)));
		for (int i = 1, j = theStatistics.size(); i < j; ++i)
		{
			sb.append(DELIMITER);
			sb.append(getDatum(theIndex, theStatistics.get(i)));
		}
		return sb.append("\n").toString();
	}

	// NOTE script could use the less portable: 'set datafile missing = "?"'
	private static String getDatum(int theIndex, List<Float> theStats)
	{
		if (theStats.size() <= theIndex)
			return getDatum(theStats.get(0), 1.0f, 1.0f);
		else
			return getDatum(theStats.get(0),
					theStats.get(theIndex),
					theStats.get(++theIndex));
	}

	private static String getDatum(float theThreshold, float theFPR, float theTPR)
	{
		return String.format("%f%s%f%s%f",
			theThreshold, DELIMITER, theFPR, DELIMITER, theTPR);
	}

	private static void writeSplotScript(String theTitle, String theBaseName, List<List<Float>> theStatistics)
	{
		BufferedWriter br = null;

		try
		{
			File f = new File(theBaseName + SCRIPT_EXT);
			br = new BufferedWriter(new FileWriter(f));

			br.write(parameterise(theTitle,
						theBaseName,
						theStatistics));
			log("script", f);
		}
		catch (IOException e) {}
		finally
		{
			if (br != null)
			{
				try { br.close(); }
				catch (IOException e) {}
			}
		}
	}

	private static String parameterise(String theTitle, String theBaseName, List<List<Float>> theStatistics)
	{
		int aSize = theStatistics.size();
		return String.format(PLOT_CODE,
					theBaseName + DATA_EXT, // tricky
					1, // 0 for plot, !0 for splot
					aSize*3,
					theStatistics.get(0).get(0),
					theStatistics.get(aSize-1).get(0),
					theTitle);
	}

	private static void log(String theType, File theFile)
	{
		Log.logCommandLine(String.format("Gnuplot %s written: '%s'",
						theType,
						theFile.getAbsolutePath()));
	}

	// YES I want this in code
	public static final String PLOT_CODE =
		"##### DECLARATION OF DEFAULTS #####\n" +
		"\n" +
		// based on input parameters
		"INPUT_FILE = '%s'\n" +
		"SPLOT = %d\n" + 
		"OUTPUT_FILE = SPLOT ? INPUT_FILE.'.3d.' : INPUT_FILE.'.2d.'\n" +
		"\n" +
		"NR_COLUMNS = %d\n" +
		"THRESHOLD_MIN = %f\n" +
		"THRESHOLD_MAX = %f\n" +
		"\n" +
		"TITLE = '%s'\n" +
		// end based on input parameters
		"\n" +
		"FONT = 'Helvetica'\n" +
		"FONT_SIZE = 14\n" +
		"\n" +
		"XLABEL = SPLOT ? 'Threshold' : 'FPR'\n" +
		"YLABEL = SPLOT ? 'FPR' : 'TPR'\n" +
		"ZLABEL = 'TPR'\n" +
		"\n" +
		"TIC_SIZE = 0.2\n" +
		"USE_MINOR_TICS = 0\n" +
		"\n" +
		"LINE_STYLE = 1\n" +
		"LINE_WIDTH = 1\n" +
		"\n" +
		"\n" +
		"\n" +
		"##### SETUP #####\n" +
		"\n" +
		"set terminal postscript eps enhanced FONT FONT_SIZE\n" +
		"set output OUTPUT_FILE.'eps'\n" +
		"\n" +
		"set title TITLE\n" +
		"\n" +
		"set xlabel XLABEL\n" +
		"set ylabel YLABEL\n" +
		"set zlabel ZLABEL\n" +
		"\n" +
		"if (!SPLOT) set xtics 0 TIC_SIZE\n" +
		"set ytics 0 TIC_SIZE\n" +
		"set ztics 0 TIC_SIZE\n" +
		"\n" +
		"# NOTE will also set mxtics\n" +
		"if (USE_MINOR_TICS) set mxtics; set mytics; set mztics;\n" +
		"\n" +
		"if (!SPLOT) set xrange [0 : 1]; else set xrange [THRESHOLD_MIN : THRESHOLD_MAX]\n" +
		"set yrange [0 : 1]\n" +
		"set zrange [0 : 1]\n" +
		"\n" +
		"# forces ground plain to height 0.0\n" +
		"set ticslevel 0\n" +
		"\n" +
		"\n" +
		"\n" +
		"##### PLOTTING #####\n" +
		"\n" +
		"if (SPLOT) \\\n" +
		"	splot for [i=1 : NR_COLUMNS-2 : 3] \\\n" +
		"		INPUT_FILE u i : i+1 : i+2 w l ls LINE_STYLE lw LINE_WIDTH notitle; \\\n" +
		"else \\\n" +
		"	plot for [i=1 : NR_COLUMNS-2 : 3] \\\n" +
		"		INPUT_FILE u i+1 : i+2 w l ls LINE_STYLE lw LINE_WIDTH notitle\n" +
		"\n" +
		"#set terminal pslatex\n" +
		"#set output OUTPUT_FILE.'tex'\n" +
		"#replot\n" +
		"\n" +
		"#set terminal postscript landscape enhanced FONT 8\n" +
		"#set output OUTPUT_FILE.'ps'\n" +
		"#replot\n" +
		"\n" +
		"#set terminal postscript eps FONT FONT_SZIE\n" +
		"#set output OUTPUT_FILE.'eps'\n" +
		"#replot\n" +
		"\n" +
		"#set terminal svg\n" +
		"#set output OUTPUT_FILE.'svg'\n" +
		"\n" +
		"#replot\n" +
		"set output\n" +
		"#set terminal windows\n" +
		"#platform-independent way of restoring terminal by push/pop\n" +
		"set terminal pop\n" +
		"#set size 1,1\n\n";
}
