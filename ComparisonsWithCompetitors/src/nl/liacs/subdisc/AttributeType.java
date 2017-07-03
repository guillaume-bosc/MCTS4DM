package nl.liacs.subdisc;

import java.util.*;

/**
 * AttributeType contains all available AttributeTypes.
 */
public enum AttributeType implements EnumInterface
{
	NOMINAL("?"),
	NUMERIC("0.0"),
	ORDINAL("0.0"),
	BINARY("0");

	// used for FileLoading/Column setMissingValue
	private static final Set<String> BOOLEAN_POSITIVES =
		new HashSet<String>(Arrays.asList(new String[] { "1", "true", "t", "yes" }));
	private static final Set<String> BOOLEAN_NEGATIVES =
		new HashSet<String>(Arrays.asList(new String[] { "0", "false", "f", "no" }));

	/*
	 * NOTE if DEFAULT_MISSING_VALUE is changed for NUMERIC/ORDINAL, check
	 * the switch() code for the Column constructor:
	 * public Column(Attribute theAttribute, int theNrRows)
	 */
	/**
	 * The default missing value for each AttributeType. To set a different
	 * missing value use {@link Column#setNewMissingValue(String theNewValue)
	 * Column.setNewMissingValue()}.
	 */
	public final String DEFAULT_MISSING_VALUE;

	private AttributeType(String theDefaultMissingValue)
	{
		DEFAULT_MISSING_VALUE = theDefaultMissingValue;
	}

	/**
	 * Returns the AttributeType corresponding to the <code>String</code>
	 * parameter. If the corresponding AttributeType can not be found, the
	 * default AttributeType NOMINAL is returned. This method is case
	 * insensitive.
	 * 
	 * @param theText the <code>String</code> corresponding to an
	 * AtrtibuteType.
	 * 
	 * @return the AttributeType corresponding to the <code>String</code>
	 * parameter, or AttributeType NOMINAL if no corresponding AttributeType
	 * is found.
	 */
	public static AttributeType fromString(String theText)
	{
		for (AttributeType at : AttributeType.values())
			if (at.toString().equalsIgnoreCase(theText))
				return at;

		/*
		 * theType cannot be resolved to an AttibuteType. Log error and
		 * return default.
		 */
		Log.logCommandLine(
			String.format("'%s' is not a valid AttributeType. Returning '%s'.",
					theText,
					AttributeType.getDefault()));
		return AttributeType.getDefault();
	}

	/**
	 * Returns whether the <code>String</code> parameter is considered to
	 * represent a valid <code>boolean</code> value of <code>true<code>. This
	 * method is case insensitive.
	 * 
	 * @param theBooleanValue <code>String</code> to check.
	 * 
	 * @return <code>true</code> if the <code>String</code> parameter is
	 * considered to represent a valid <code>boolean</code> value of
	 * <code>true<code>, <code>false</code> otherwise.
	 */
	public static boolean isValidBinaryTrueValue(String theBooleanValue)
	{
		return BOOLEAN_POSITIVES.contains(theBooleanValue.toLowerCase().trim());
	}

	/**
	 * Returns whether the <code>String</code> parameter is considered to
	 * represent a valid <code>boolean</code> value of <code>false<code>. This
	 * method is case insensitive.
	 * 
	 * @param theBooleanValue <code>String</code> to check.
	 * 
	 * @return <code>true</code> if the <code>String</code> parameter is
	 * considered to represent a valid <code>boolean</code> value of
	 * <code>false<code>, <code>false</code> otherwise.
	 */
	public static boolean isValidBinaryFalseValue(String theBooleanValue)
	{
		return BOOLEAN_NEGATIVES.contains(theBooleanValue.toLowerCase().trim());
	}

	public static boolean isValidBinaryValue(String theBooleanValue)
	{
		return BOOLEAN_POSITIVES.contains(theBooleanValue.toLowerCase().trim()) ||
			BOOLEAN_NEGATIVES.contains(theBooleanValue.toLowerCase().trim());
	}

	/**
	 * Returns the default AttributeType.
	 * 
	 * @return the default AttributeType.
	 */
	public static AttributeType getDefault()
	{
		return AttributeType.NOMINAL;
	}

	// uses Javadoc from EnumInterface
	@Override
	public String toString()
	{
		return name().toLowerCase();
	}
/*
	private static void unknownType(String theSource, String theType)
	{
		Log.logCommandLine(
					String.format("AttributeType.%s(): unknown AttributeType '%s'",
									theSource,
									theType));
	}
*/
}
