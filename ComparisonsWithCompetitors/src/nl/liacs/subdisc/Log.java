package nl.liacs.subdisc;

import java.io.*;

public class Log
{
	// outputfile constants
	public static boolean DEBUG = true;		// Logs debug in file < LOGPATH + "debug" + timeStamp + ".log" >
	public static boolean ERROR = true;		// logs error in file < LOGPATH + "error" + timeStamp + ".log" >

	public static boolean SQLLOG = false;		// Logs all sql-queries in file < LOGPATH + "sql.log" >
	public static boolean SQLRESULTLOG = false;	// If (SQLLOG = true) : Logs all sql-query results same file
	public static boolean REFINEMENTLOG = false;	// Logs all refinements in file < LOGPATH + "refinement.log" >
	public static boolean COMMANDLINELOG = false;	// Logs a few things to dos commandline

	public static boolean LOG = false;			// Basket of all the (...LOG = false) log attempts
	public static boolean FORCECOMMANDLINELOG = false;	// If (LOG = false) all its content is put on the commandline

	private static String LOGPATH = new String("../log/");	// NB: this path of direcories will be created

	static java.io.OutputStream debugStream = System.out;
	static java.io.OutputStream errorStream = System.err;
	static java.io.OutputStream logStream = System.out;
	static java.io.OutputStream sqlLogStream = System.out;
	static java.io.OutputStream refinementLogStream = System.out;

	public static void openFileOutputStreams()
	{
		boolean errmade = false;

		FileOutputStream debug = null;
		FileOutputStream error = null;
		FileOutputStream log = null;
		FileOutputStream sqlLog = null;
		FileOutputStream refinementLog = null;

		if ((SQLLOG)||((REFINEMENTLOG)||(ERROR))||((LOG)||(DEBUG))) {
			try
			{
				java.io.File aLogPath = new java.io.File(LOGPATH);
				aLogPath.mkdirs();
				Log.logCommandLine("creating files in directory: " + LOGPATH);

				if (DEBUG) debug = new java.io.FileOutputStream(LOGPATH + "debug.wri");
				if (ERROR) error = new java.io.FileOutputStream(LOGPATH + "error.wri");
				if (LOG) log = new java.io.FileOutputStream(LOGPATH + "log.wri");
				if (SQLLOG) sqlLog = new java.io.FileOutputStream(LOGPATH + "sql.wri");
				if (REFINEMENTLOG) refinementLog = new java.io.FileOutputStream(LOGPATH + "refinement.wri");
			} catch (Exception ex) {
				errmade = true;
				error(ex.toString()); }

			if (!errmade)
			{
				if (DEBUG) debugStream = debug;
				if (ERROR) errorStream = error;
				if (LOG) logStream = log;
				if (SQLLOG) sqlLogStream = sqlLog;
				if (REFINEMENTLOG) refinementLogStream = refinementLog;
			}
		}
	}

	public static void closeFileOutputStreams()
	{
		try
		{
			debugStream.flush(); debugStream.close();
			logStream.flush(); logStream.close();
			errorStream.flush(); errorStream.close();
			sqlLogStream.flush(); sqlLogStream.close();
			refinementLogStream.flush(); refinementLogStream.close();
		}
		catch (Exception ex) { }
	}

	public static void error(String s)
	{
		try { errorStream.write(charsToBytes(s.toCharArray())); errorStream.write('\n');
			} catch (Exception ex) { }
	}

	public static void debug(String s)
	{
		try {
			// debugStream.write(charsToBytes(s.toCharArray())); debugStream.write('\n');
		} catch (Exception ex) { }
	}

	public static void logSql(String s)
	{
		if (SQLLOG)
			try {
				sqlLogStream.write(charsToBytes(s.toCharArray())); sqlLogStream.write('\n');
			} catch (Exception ex) { }
		logCommandLine(s);
	}

	public static void logRefinement(String s)
	{
		if (REFINEMENTLOG)
			try {
				refinementLogStream.write(charsToBytes(s.toCharArray())); refinementLogStream.write('\n');
			} catch (Exception ex) { log(s);}
	}

	public static void logCommandLine(String s)
	{
		if (COMMANDLINELOG)
			System.out.println(s);
	}

	public static void toUniqueFile(String theFileName, String theContent) {

		boolean errorMade = false;
		java.io.OutputStream aFileStream = System.out;
		FileOutputStream aFile = null;

		try {
			java.io.File alogPath = new java.io.File(LOGPATH);
			alogPath.mkdirs(); // create directories if not already there

			String aUniqueID = "" + System.currentTimeMillis();
			aFile = new java.io.FileOutputStream(LOGPATH + theFileName + aUniqueID + ".wri");
		} catch (Exception ex) {
			errorMade = true;
			error(ex.toString()); }

		if (!errorMade) {
			aFileStream = aFile;
	try {
				aFileStream.write(charsToBytes(theContent.toCharArray()));
				aFileStream.write('\n');
			} catch (Exception ex) { }
		}
		try {
			aFileStream.flush(); aFileStream.close();
		} catch (Exception ex) { }
	}

	private static void log(String s) {
		if (LOG)

	        try {
				logStream.write(charsToBytes(s.toCharArray())); logStream.write('\n');
	        	}
			catch (Exception ex){}
   else
    	if (FORCECOMMANDLINELOG)

			try {
				System.out.println("  " + s);
			}
			catch (Exception ex){}
    }


	private static byte[] charsToBytes(char[] ca)
	{
		byte[] ba = new byte[ca.length];
		for (int i = 0; i < ca.length; i++)
			ba[i] = (byte)ca[i];
		return ba;
	}
}
