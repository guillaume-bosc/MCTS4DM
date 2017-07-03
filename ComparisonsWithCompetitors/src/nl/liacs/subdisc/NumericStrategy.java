package nl.liacs.subdisc;

import java.util.*;

/**
 * NumericStrategy contains all available NumericStrategies.
 */
public enum NumericStrategy implements EnumInterface
{
	NUMERIC_BINS("bins"),
	NUMERIC_BEST("best"),
	NUMERIC_ALL("all"),
	NUMERIC_INTERVALS("intervals");

	/**
	 * For each NumericStrategy, this is the text that will be used in the GUI.
	 * This is also the <code>String</code> that will be returned by the
	 * toString() method.
	 */
	public final String GUI_TEXT;

	private NumericStrategy(String theGuiText)
	{
		GUI_TEXT = theGuiText;
	}

	/**
	 * Returns the NumericStrategy corresponding to the <code>String</code>
	 * parameter. This method is case insensitive.
	 *
	 * @param theText the <code>String</code> corresponding to a
	 * NumericStrategy.
	 *
	 * @return the NumericStrategy corresponding to the <code>String</code>
	 * parameter, or the default NumericStrategy <code>NUMERIC_BINS</code>
	 * if no corresponding NumericStrategy can not be found.
	 */
	public static NumericStrategy fromString(String theText)
	{
		for (NumericStrategy n : NumericStrategy.values())
			if (n.GUI_TEXT.equalsIgnoreCase(theText))
				return n;

		/*
		 * theType cannot be resolved to a NumericStrategy. Log error and
		 * return default.
		 */
		Log.logCommandLine(
			String.format("'%s' is not a valid NumericStrategy. Returning '%s'.",
					theText,
					NumericStrategy.getDefault().GUI_TEXT));
		return NumericStrategy.getDefault();
	}

	public static ArrayList<NumericStrategy> getNormalValues()
	{
		ArrayList<NumericStrategy> aResult = new ArrayList<NumericStrategy>(3);
		aResult.add(NUMERIC_BINS);
		aResult.add(NUMERIC_BEST);
		aResult.add(NUMERIC_ALL);
		//no intervals!
		return aResult;
	}

	/**
	 * Returns the default NumericStrategy.
	 *
	 * @return the default NumericStrategy.
	 */
	public static NumericStrategy getDefault()
	{
		return NumericStrategy.NUMERIC_BINS;
	}

	// uses Javadoc from EnumInterface
	@Override
	public String toString()
	{
		return GUI_TEXT;
	}
}