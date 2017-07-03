package nl.liacs.subdisc;

/**
 * TargetType contains all available TargetTypes.
 */
public enum TargetType implements EnumInterface
{
	/*
	 * When implementing/adding TargetTypes, all static methods should be
	 * checked and updated.
	 */
	SINGLE_NOMINAL("single nominal"),
	SINGLE_NUMERIC("single numeric"),
	SINGLE_ORDINAL("single ordinal"),
	DOUBLE_REGRESSION("double regression"),
	DOUBLE_CORRELATION("double correlation"),
	MULTI_LABEL("multi-label"),
	MULTI_BINARY_CLASSIFICATION("multi binary classification");

	/**
	 * For each TargetType, this is the text that will be used in the GUI.
	 * This is also the <code>String</code> that will be returned by the
	 * toString() method.
	 */
	public final String GUI_TEXT;

	private TargetType(String theGuiText)
	{
		GUI_TEXT = theGuiText;
	}

	/**
	 * Returns the TargetType corresponding to the <code>String</code>
	 * parameter. This method is case insensitive.
	 *
	 * @param theType the <code>String</code> corresponding to a TargetType.
	 *
	 * @return the TargetType corresponding to the <code>String</code>
	 * parameter, or the default TargetType <code>SINGLE_NOMINAL</code> if no
	 * corresponding TargetType can not be found.
	 */
	public static TargetType fromString(String theType)
	{
		for (TargetType t : TargetType.values())
			if (t.GUI_TEXT.equalsIgnoreCase(theType))
				return t;

		/*
		 * theType cannot be resolved to a TargetType. Log error and return
		 * default.
		 */
		Log.logCommandLine(
			String.format("'%s' is not a valid TargetType. Returning '%s'.",
					theType,
					TargetType.getDefault().GUI_TEXT));
		return TargetType.getDefault();
	}

	/**
	 * Returns the default TargetType.
	 *
	 * @return the default TargetType.
	 */
	public static TargetType getDefault()
	{
		return TargetType.SINGLE_NOMINAL;
	}

	// uses Javadoc from EnumInterface
	@Override
	public String toString()
	{
		return GUI_TEXT;
	}

	public static boolean isImplemented(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return true;
			case SINGLE_NUMERIC		: return true;
			case SINGLE_ORDINAL		: return false;
			case DOUBLE_REGRESSION		: return true;
			case DOUBLE_CORRELATION		: return true;
			case MULTI_LABEL		: return true;
			case MULTI_BINARY_CLASSIFICATION: return false;
			default :
			{
				unknownTargetType("isImplemented", theType);
				return false;
			}
		}
	}

	// used by XMLNodeTargetConcept
	public static boolean hasSecondaryTarget(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return false;
			case SINGLE_NUMERIC		: return false;
			case SINGLE_ORDINAL		: return false;
			case DOUBLE_REGRESSION		: return true;
			case DOUBLE_CORRELATION		: return true;
			case MULTI_LABEL		: return false;
			case MULTI_BINARY_CLASSIFICATION: return false;
			default :
			{
				unknownTargetType("hasSecondaryTarget", theType);
				return false;
			}
		}
	}

	// used by MiningWindow.jComboBoxTargetAttributeActionPerformed()
	public static boolean hasMultiTargets(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return false;
			case SINGLE_NUMERIC		: return false;
			case SINGLE_ORDINAL		: return false;
			case DOUBLE_REGRESSION		: return false;
			case DOUBLE_CORRELATION		: return false;
			case MULTI_LABEL		: return true;
			case MULTI_BINARY_CLASSIFICATION: return true;
			default :
			{
				unknownTargetType("hasMultiTargets", theType);
				return false;
			}
		}
	}

	// used by MiningWindow.jComboBoxTargetAttributeActionPerformed()
	public static boolean hasMultiRegressionTargets(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return false;
			case SINGLE_NUMERIC		: return false;
			case SINGLE_ORDINAL		: return false;
			case DOUBLE_REGRESSION		: return true;
			case DOUBLE_CORRELATION		: return false;
			case MULTI_LABEL		: return false;
			case MULTI_BINARY_CLASSIFICATION: return false;
			default :
			{
				unknownTargetType("hasMultiRegressionTargets", theType);
				return false;
			}
		}
	}

	// used by MiningWindow.jComboBoxTargetAttributeActionPerformed()
	public static boolean hasMiscField(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return true;
			case SINGLE_NUMERIC		: return false;
			case SINGLE_ORDINAL		: return false;
			case DOUBLE_REGRESSION		: return true;
			case DOUBLE_CORRELATION		: return true;
			case MULTI_LABEL		: return false;
			case MULTI_BINARY_CLASSIFICATION: return true;
			default :
			{
				unknownTargetType("hasMiscField", theType);
				return false;
			}
		}
	}

	// used by MiningWindow.jComboBoxTargetAttributeActionPerformed()
	public static boolean hasTargetAttribute(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return true;
			case SINGLE_NUMERIC		: return true;
			case SINGLE_ORDINAL		: return true;
			case DOUBLE_REGRESSION		: return true;
			case DOUBLE_CORRELATION		: return true;
			case MULTI_LABEL		: return false;
			case MULTI_BINARY_CLASSIFICATION: return false;	// TODO true?
			default :
			{
				unknownTargetType("hasTargetAttribute", theType);
				return false;
			}
		}
	}

	// used by MiningWindow initGuiComponentsFromFile()
	// used by ResultWindow.setTitle()
	public static boolean hasTargetValue(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return true;
			case SINGLE_NUMERIC		: return false;
			case SINGLE_ORDINAL		: return false;
			case DOUBLE_REGRESSION		: return true;
			case DOUBLE_CORRELATION		: return true;
			case MULTI_LABEL		: return false;
			case MULTI_BINARY_CLASSIFICATION: return false;	// TODO true?
			default :
			{
				unknownTargetType("hasTargetValue", theType);
				return false;
			}
		}
	}

	// used by MiningWindow.jComboBoxTargetAttributeActionPerformed()
	// used by ResultWindow.initComponents()
	public static boolean hasBaseModel(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL		: return false;
			case SINGLE_NUMERIC		: return true;
			case SINGLE_ORDINAL		: return false;
			case DOUBLE_REGRESSION		: return true;
			case DOUBLE_CORRELATION		: return true;
			case MULTI_LABEL		: return true;
			case MULTI_BINARY_CLASSIFICATION: return false;	// TODO true?
			default :
			{
				unknownTargetType("hasBaseModel", theType);
				return false;
			}
		}
	}
/*
	public static boolean isEMM(TargetType theType)
	{
		switch (theType)
		{
			case SINGLE_NOMINAL			: return false;
			case SINGLE_NUMERIC			: return false;
			case SINGLE_ORDINAL			: return false;
			case DOUBLE_REGRESSION			: return true;
			case DOUBLE_CORRELATION			: return true;
			case MULTI_LABEL			: return true;
			case MULTI_BINARY_CLASSIFICATION	: return true;
			default :
			{
				unknownTargetType("isEMM", theType);
				return false;
			}
		}
	}
*/

	// TODO this should actually throw an AssertionError
	private static void unknownTargetType(String theSource, TargetType theType)
	{
		Log.logCommandLine(String.format("%s.%s(): unknown type '%s'",
							theType.getClass().getSimpleName(),
							theSource,
							theType));
	}
}
