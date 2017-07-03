package nl.liacs.subdisc;

import java.util.*;

public enum NumericOperatorSetting implements EnumInterface
{
	NUMERIC_NORMAL("<html>&#8804;, &#8805;</html>"),
	NUMERIC_LEQ("<html>&#8804;</html>"),
	NUMERIC_GEQ("<html>&#8805;</html>"),
	NUMERIC_ALL("<html>&#8804;, &#8805;, =</html>"),
	NUMERIC_EQ("="),
	NUMERIC_INTERVALS("in");

	/**
	 * For each NumericOperatorSetting this is the text that will be used in
	 * the GUI.
	 * This is also the <code>String</code> that will be returned by the
	 * {@link #toString()} method.
	 */
	public final String GUI_TEXT;

	private NumericOperatorSetting(String theGuiText)
	{
		GUI_TEXT = theGuiText;
	}

	/**
	 * Returns the NumericOperatorSetting corresponding to the
	 * <code>String</code> argument, this method is case insensitive.
	 *
	 * @param theText The <code>String</code> corresponding to a
	 * NumericOperatorSetting.
	 *
	 * @return The NumericOperatorSetting corresponding to the
	 * <code>String</code> argument, or the default NumericOperatorSetting
	 * <code>NUMERIC_NORMAL</code> if no corresponding
	 * NumericOperatorSetting can not be found.
	 * 
	 * @see #getDefault()
	 */
	public static NumericOperatorSetting fromString(String theText)
	{
		for (NumericOperatorSetting n : NumericOperatorSetting.values())
			if (n.GUI_TEXT.equalsIgnoreCase(theText))
				return n;

		/*
		 * theType cannot be resolved to a NumericOperators. Log error and
		 * return default.
		 */
		Log.logCommandLine(
			String.format("'%s' is not a valid NumericOperators. Returning '%s'.",
					theText,
					NumericOperatorSetting.getDefault().GUI_TEXT));
		return NumericOperatorSetting.getDefault();
	}

	// EnumSet
	public static List<NumericOperatorSetting> getNormalValues()
	{
		List<NumericOperatorSetting> aResult = new ArrayList<NumericOperatorSetting>(5);
		aResult.add(NUMERIC_NORMAL);
		aResult.add(NUMERIC_LEQ);
		aResult.add(NUMERIC_GEQ);
		aResult.add(NUMERIC_ALL);
		aResult.add(NUMERIC_EQ);
		//no intervals!
		return aResult;
	}

	/**
	 * Returns the default NumericOperatorSetting.
	 *
	 * @return the default NumericOperatorSetting.
	 */
	public static NumericOperatorSetting getDefault()
	{
		return NumericOperatorSetting.NUMERIC_NORMAL;
	}

	// uses Javadoc from EnumInterface
	@Override
	public String toString()
	{
		return GUI_TEXT;
	}

	public static boolean check(NumericOperatorSetting theNO, Operator theOperator)
	{
		if (theNO == NUMERIC_NORMAL && (theOperator == Operator.LESS_THAN_OR_EQUAL || theOperator == Operator.GREATER_THAN_OR_EQUAL))
			return true;
		if (theNO == NUMERIC_LEQ && theOperator == Operator.LESS_THAN_OR_EQUAL)
			return true;
		if (theNO == NUMERIC_GEQ && theOperator == Operator.GREATER_THAN_OR_EQUAL)
			return true;
		if (theNO == NUMERIC_EQ && theOperator == Operator.EQUALS)
			return true;
		if (theNO == NUMERIC_ALL && (theOperator == Operator.LESS_THAN_OR_EQUAL || theOperator == Operator.GREATER_THAN_OR_EQUAL || theOperator == Operator.EQUALS))
			return true;
		if (theNO == NUMERIC_INTERVALS && theOperator == Operator.BETWEEN)
			return true;

		return false;
	}
}
