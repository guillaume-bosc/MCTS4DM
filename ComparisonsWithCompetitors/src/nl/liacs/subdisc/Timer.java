package nl.liacs.subdisc;

import java.util.*;

/**
 * @author marvin
 * Timer class for all event timings.
 */
public class Timer
{
	private final long itsStartTime;

	public Timer()
	{
		itsStartTime = System.nanoTime();
	}

	public long getElapsedTime()
	{
		return System.nanoTime() - itsStartTime;
	}

	public String getElapsedTimeString()
	{
		long theNanoSeconds = System.nanoTime() - itsStartTime;
		long minutes = theNanoSeconds / 60000000000l;
		float seconds = (theNanoSeconds % 60000000000l) / 1000000000f;

		return String.format(Locale.US,
					"%d minute%s and %3$.3f seconds.%n",
					minutes, (minutes == 1 ? "" : "s"),
					seconds);
	}
}
