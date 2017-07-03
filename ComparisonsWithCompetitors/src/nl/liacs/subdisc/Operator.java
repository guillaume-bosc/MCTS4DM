package nl.liacs.subdisc;

import java.util.*;

public enum Operator
{
//	public static final int ELEMENT_OF		= 1;
//	public static final int EQUALS			= 2;
//	public static final int LESS_THAN_OR_EQUAL	= 3;
//	public static final int GREATER_THAN_OR_EQUAL	= 4;
//	public static final int BETWEEN = 5;
//	public static final int NOT_AN_OPERATOR		= 99;
//
//	// Binary Operator Constants
//	public static final int FIRST_BINARY_OPERATOR	= EQUALS;
//	public static final int LAST_BINARY_OPERATOR	= EQUALS;
//
//	// Nominal Operator  Constants
//	public static final int FIRST_NOMINAL_OPERATOR	= ELEMENT_OF;
//	public static final int LAST_NOMINAL_OPERATOR	= EQUALS;
//
//	// Numeric Operator  Constants
//	//this allows =, <= and >=
//	public static final int FIRST_NUMERIC_OPERATOR	= EQUALS;
//	public static final int LAST_NUMERIC_OPERATOR	= BETWEEN;

//	public boolean hasNextOperator()
//	{
//		if (itsOperator == LAST_BINARY_OPERATOR && itsColumn.isBinaryType())
//			return false;
//		if (itsOperator == LAST_NOMINAL_OPERATOR && itsColumn.isNominalType())
//			return false;
//		if (itsOperator == LAST_NUMERIC_OPERATOR && itsColumn.isNumericType())
//			return false;
//		return true;
//	}
//
//	public int getNextOperator()
//	{
//		return hasNextOperator() ? itsOperator+1 : NOT_AN_OPERATOR;
//	}

//	private String getOperatorString()
//	{
//		switch(itsOperator)
//		{
//			case ELEMENT_OF		: return "in";
//			case EQUALS			: return "=";
//			case LESS_THAN_OR_EQUAL		: return "<=";
//			case GREATER_THAN_OR_EQUAL	: return ">=";
//			case BETWEEN	: return "in";
//			default : return "";
//		}
//	}

	ELEMENT_OF("in"),
	EQUALS("="),
	LESS_THAN_OR_EQUAL("<="),
	GREATER_THAN_OR_EQUAL(">="),
	BETWEEN("in"),
	NOT_AN_OPERATOR("");	// may not be needed anymore

	public final String GUI_TEXT;

	private Operator(String theGuiText)
	{
		GUI_TEXT = theGuiText;
	}

	@Override
	public String toString()
	{
		return GUI_TEXT;
	}

	/**
	 * Returns the <code>Operator</code> for the supplied argument, or
	 * <code>Operator,NOT_AN_OPERATOR</code> if the argument can not be
	 * mapped to an <code>Operator</code>.
	 * <p>
	 * 
	 * @param theText The <code>GUI_TEXT</code> of an <code>Operator</code>.
	 * 
	 * @return An <code>Operator</code>.
	 */
	public static Operator fromString(String theText)
	{
		for (Operator o : Operator.values())
			if (o.GUI_TEXT.equals(theText))
				return o;

		/*
		 * theText cannot be resolved to an Operator. Log error and
		 * return Operator.NOT_AN_OPERATOR.
		 */
		Log.logCommandLine(
			String.format("'%s' is not a valid Operator. Returning '%s'.",
					theText,
					Operator.NOT_AN_OPERATOR.name()));
		return Operator.NOT_AN_OPERATOR;
	}

	/**
	 * Returns all <code>Operators</code> as an immutable
	 * <code>EnumSet</code>.
	 * 
	 * @return An <code>EnumSet</code> of all <code>Operators</code>.
	 */
	public static Set<Operator> set()
	{
		final EnumSet<Operator> set = EnumSet.noneOf(Operator.class);
		for (Operator o : Operator.values())
			set.add(o);
		return Collections.unmodifiableSet(set);
	}
}
