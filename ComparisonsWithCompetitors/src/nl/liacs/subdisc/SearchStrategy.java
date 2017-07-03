package nl.liacs.subdisc;

/**
 * SearchStrategy contains all available search strategies.
 */
public enum SearchStrategy implements EnumInterface
{
	BEAM("beam"),
	COVER_BASED_BEAM_SELECTION("cover-based beam selection"),
	BEST_FIRST("best first"),
	DEPTH_FIRST("depth first"),
	BREADTH_FIRST("breadth first");

	/**
	 * For each SearchStrategy, this is the text that will be used in the
	 * GUI.
	 * This is also the <code>String</code> that will be returned by the
	 * {@link #toString()} method.
	 */
	public final String GUI_TEXT;

	private SearchStrategy(String theGuiText)
	{
		GUI_TEXT = theGuiText;
	}

	/**
	 * Returns the SearchStartegy corresponding to the <code>String</code>
	 * parameter. This method is case insensitive.
	 *
	 * @param theText the <code>String</code>
	 * ({@link SearchStrategy#GUI_TEXT}) corresponding to a SearchStrategy.
	 *
	 * @return the SearchStrategy corresponding to the <code>String</code>
	 * parameter, or the default SearchStrategy
	 * (as per {@link SearchStrategy#getDefault()}) if no corresponding 
	 * SearchStrategy can be found.
	 */
	public static SearchStrategy fromString(String theText)
	{
		for (SearchStrategy s : SearchStrategy.values())
			if (s.GUI_TEXT.equalsIgnoreCase(theText))
				return s;

		/*
		 * theType cannot be resolved to a SearchStrategy. Log error and
		 * return default.
		 */
		Log.logCommandLine(
			String.format("'%s' is not a valid SearchStrategy. Returning '%s'.",
					theText,
					SearchStrategy.getDefault().GUI_TEXT));
		return SearchStrategy.getDefault();
	}

	/**
	 * Returns the default SearchStrategy {@link SearchStrategy#BEAM}.
	 *
	 * @return the default SearchStrategy.
	 */
	public static SearchStrategy getDefault()
	{
		return SearchStrategy.BEAM;
	}

	// uses Javadoc from EnumInterface
	@Override
	public String toString()
	{
		return GUI_TEXT;
	}

	public boolean isBeam()
	{
		return ((this == SearchStrategy.BEAM) ||
			(this == SearchStrategy.COVER_BASED_BEAM_SELECTION));
	}
}
