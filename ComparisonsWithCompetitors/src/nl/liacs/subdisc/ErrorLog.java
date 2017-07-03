package nl.liacs.subdisc;

import java.io.*;

public class ErrorLog
{
	public enum Error
	{
		ATTRIBUTE_TYPE_UNKNOWN("AttributeType unknown"),
		TARGET_TYPE_UNKNOWN("TargetType unknown"),
		FILE_NOT_FOUND("File not found"),
		IOEXCEPTION("IOException");

		public final String TEXT;

		private Error(String theDescription) { TEXT = theDescription; }

		public static String getErrorDescription(Throwable theThrowable)
		{
			if (theThrowable == null)
				return "";
			else if (theThrowable instanceof FileNotFoundException)
				return FILE_NOT_FOUND.TEXT;
			else if (theThrowable instanceof IOException)
				return IOEXCEPTION.TEXT;
			else
				return "Unknown Error";
		}
	}

	// uninstatiable
	private ErrorLog() {};

	// TODO maybe remove getMessage()
	/**
	 * Logs an Error message to the <code>command line</code>. This
	 * includes the fully qualified class name and the method in which the Error
	 * occurred, and the <code>getMessage()</code> <code>String</code> if it is
	 * available for the <code>Throwable</code>. Always call this method
	 * immediately after the Error occurs, as it relies on the
	 * <code>StackTrace</code>.
	 * 
	 * @param theFile the <code>File</code> that caused the Error.
	 * @param theThrowable the <code>Throwable</code> indicating the cause of
	 * this Error.
	 */
	public static void log(File theFile, Throwable theThrowable)
	{
		String anError;
		String aMessage;

		if (theThrowable != null)
		{
			anError = (" " + Error.getErrorDescription(theThrowable));
			aMessage =
				(theThrowable.getMessage() == null ? "" :
													theThrowable.getMessage());
			if (aMessage.length() > 0)
				aMessage += "/n";
		}
		else
		{
			anError = "";
			aMessage = "";
		}

		Log.logCommandLine(
				String.format("%s%s: '%s'.%n%s",
					getClassAndMethod(Thread.currentThread().getStackTrace()),
					anError,
					(theFile == null ? "null" : theFile.getAbsolutePath()),
					aMessage));
	}

	// e[0] = this method, e[1] = ErrorLog.log(), e[2] = source method
	private static String getClassAndMethod(StackTraceElement e[])
	{
		if (e.length > 2)
		{
			StackTraceElement aTrace = e[2];
			return aTrace.getClassName() + "." + aTrace.getMethodName();
		}
		else
			return "No StackTrace available.";
	}
}
