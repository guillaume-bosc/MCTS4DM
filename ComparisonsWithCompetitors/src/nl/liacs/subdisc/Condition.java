package nl.liacs.subdisc;

import java.util.*;

/*
 * TODO
 * The general nextOperator looping-strategy should be changed (in tandem with
 * Table.getNextCondition()).
 * FIRST/ LAST_OPERATORS should be defined in terms of (unmodifiable) EnumSets.
 * Condition Objects should have a boolean member indicating whether a
 * value is set for it.
 * Most LogTypeError calls should be changed to new AssertionError().
 */
public class Condition implements Comparable<Condition>
{
	// Operator Constants
//	public static final int ELEMENT_OF		= 1;
//	public static final int EQUALS			= 2;
//	public static final int LESS_THAN_OR_EQUAL	= 3;
//	public static final int GREATER_THAN_OR_EQUAL	= 4;
//	public static final int BETWEEN = 5;
//	public static final int NOT_AN_OPERATOR		= 99;

	private static final Set<Operator> OPERATORS = Operator.set();
	/*
	 * FIXME these should be defined in terms of EnumSets
	 */
	// Binary Operator Constants
//	public static final int FIRST_BINARY_OPERATOR	= EQUALS;
//	public static final int LAST_BINARY_OPERATOR	= EQUALS;
	public static final Operator FIRST_BINARY_OPERATOR	= Operator.EQUALS;
	public static final Operator LAST_BINARY_OPERATOR	= Operator.EQUALS;

	// Nominal Operator  Constants
//	public static final int FIRST_NOMINAL_OPERATOR	= ELEMENT_OF;
//	public static final int LAST_NOMINAL_OPERATOR	= EQUALS;
	public static final Operator FIRST_NOMINAL_OPERATOR	= Operator.ELEMENT_OF;
	public static final Operator LAST_NOMINAL_OPERATOR	= Operator.EQUALS;

	// Numeric Operator  Constants
	//this allows =, <= and >=
//	public static final int FIRST_NUMERIC_OPERATOR	= EQUALS;
//	public static final int LAST_NUMERIC_OPERATOR	= BETWEEN;
	public static final Operator FIRST_NUMERIC_OPERATOR	= Operator.EQUALS;
	public static final Operator LAST_NUMERIC_OPERATOR	= Operator.BETWEEN;

	private final Column itsColumn;
//	private final int itsOperator;
	private final Operator itsOperator;

	private String itsNominalValue = null;		// ColumnType = NOMINAL
	private ValueSet itsNominalValueSet = null;	// ColumnType = NOMINAL
	private float itsNumericValue = Float.NaN;	// ColumnType = NUMERIC
	private Interval itsInterval = null;		// ColumnType = NUMERIC
	private boolean itsBinaryValue = false;		// ColumnType = BINARY

	/**
	 * Default initialisation values for {@link Column}} of
	 * {@link AttributeType}:<br>
	 * {@link AttributeType#NOMINAL} = <code>null</code>,<br>
	 * {@link AttributeType#NUMERIC} = Float.NaN,<br>
	 * {@link AttributeType#BINARY} = <code>false</code>.
	 *
	 * @param theColumn The Column on which this Condition will be defined.
	 * 
	 * @throws {@link NullPointerException} if the parameter is
	 * <code>null</code>.
	 */
	public Condition(Column theColumn)
	{
		itsColumn = theColumn;

		// causes NullPointerException if (theColumn == null)
		switch (itsColumn.getType())
		{
			case NOMINAL : itsOperator = FIRST_NOMINAL_OPERATOR; return;
			case NUMERIC : itsOperator = FIRST_NUMERIC_OPERATOR; return;
			case ORDINAL : itsOperator = FIRST_NUMERIC_OPERATOR; return;
			case BINARY : itsOperator = FIRST_BINARY_OPERATOR; return;
			default :
			{
				itsOperator = FIRST_NOMINAL_OPERATOR;
				logTypeError("<init>");
				return;
			}
		}
	}

	/**
	 * Default initialisation values for {@link Column}} of
	 * {@link AttributeType}:<br>
	 * {@link AttributeType#NOMINAL} = <code>null</code>,<br>
	 * {@link AttributeType#NUMERIC} = Float.NaN,<br>
	 * {@link AttributeType#BINARY} = <code>false</code>.
	 *
	 * @param theColumn The Column on which this Condition will be defined.
	 * 
	 * @throws {@link NullPointerException} if the parameter is
	 * <code>null</code>.
	 */
	public Condition(Column theColumn, Operator theOperator)
	{
		if (theColumn == null)
			throw new NullPointerException();

		itsColumn = theColumn;
		// TODO check if operator is valid for ColumnType
		itsOperator = theOperator;
	}

	// obviously does not deep-copy itsColumn
	// itsOperator is primitive type, no need for deep-copy
	// itsValue new String not really needed, as none of current code ever
	// changes it, beside it can be overridden through setValue anyway.
	public Condition copy()
	{
		Condition aCopy = new Condition(itsColumn, itsOperator);
		// new for deep-copy? not strictly needed for code
		if (itsNominalValue != null)
			aCopy.itsNominalValue = new String(itsNominalValue);
		aCopy.itsNominalValueSet = this.itsNominalValueSet; //shallow copy!
		aCopy.itsNumericValue = this.itsNumericValue;
		aCopy.itsInterval = this.itsInterval; //shallow copy!
		aCopy.itsBinaryValue = this.itsBinaryValue;
		return aCopy;
	}

	public Column getColumn() { return itsColumn; }

	public Operator getOperator() { return itsOperator; }

	public boolean isElementOf() { return itsOperator == Operator.ELEMENT_OF; }
	public boolean isEquals() { return itsOperator == Operator.EQUALS; }

	// see class comment on valueIsSet-boolean indicating (non)-virgin state
	private String getValue()
	{
		switch (itsColumn.getType())
		{
			case NOMINAL :
				if (itsNominalValue != null) //single value?
					return itsNominalValue;
				else if (itsNominalValueSet != null) //value set?
					return itsNominalValueSet.toString();
				else
					return null; // TODO no value is set yet

			case NUMERIC :
				if (!Float.isNaN(itsNumericValue)) //single value?
					return Float.toString(itsNumericValue);
				else if (itsInterval != null) //interval?
					return itsInterval.toString();
				else
					return null; // TODO no value is set yet

			/*
			 * TODO a "NaN" return may mean that no value is set yet
			 * or that the value Float.NaN is set deliberately
			 */
			case ORDINAL : return Float.toString(itsNumericValue);

			/*
			 * TODO a "0" return may mean that no value is set yet
			 * or that the value "0" is set deliberately
			 */
			case BINARY : return itsBinaryValue ? "1" : "0";
			default : logTypeError("getValue"); return "";
		}
	}

	/**
	 * Set the value for this Condition, use:<br>
	 * Floats.toString(theFloatValue) for a <code>float</code>,<br>
	 * "0" or "1" for <code>false</code> and <code>true</code> respectively.
	 */
	/*
	 * Setting the value using a (parsed) String is still sub-optimal, but
	 * unlikely to be a performance drawback. It is done only once per
	 * condition, contrary to subgroup.size()-calls to evaluate().
	 *
	 * Method is called by:
	 * Refinement getRefinedSubgroup
	 * SubgroupDiscovery single nominal constructor
	 * Validation getRandomConditionList randomConditions randomSubgroups
	 * 
	 * TODO
	 * check if supplied value is correct and update valueIsSet
	 * only allow value to be set once, so it can never be changed
	 */
	public void setValue(String theValue)
	{
		switch (itsColumn.getType())
		{
			case NOMINAL : itsNominalValue = theValue; return;
			case NUMERIC : // deliberate fall-through to ORDINAL
			case ORDINAL :
			{
				try { itsNumericValue = Float.parseFloat(theValue); }
				catch (NumberFormatException e) {} // remains NaN
				return;
			}
			case BINARY :
			{
				itsBinaryValue = theValue.equals("1");
				return;
			}
			default : logTypeError("setValue"); return;
		}
	}

	/**
	 * Set the value for this Condition, specifically for nominal value sets
	 */
	public void setValue(ValueSet theValue) { itsNominalValueSet = theValue; }

	/**
	 * Set the value for this Condition, specifically for numeric intervals.
	 */
	public void setValue(Interval theValue) { itsInterval = theValue; }

	public boolean hasNextOperator()
	{
		final AttributeType aType = itsColumn.getType();
		if (itsOperator == LAST_BINARY_OPERATOR && aType == AttributeType.BINARY)
			return false;
		if (itsOperator == LAST_NOMINAL_OPERATOR && aType == AttributeType.NOMINAL)
			return false;
		if (itsOperator == LAST_NUMERIC_OPERATOR && aType == AttributeType.NUMERIC)
			return false;
		return true;
	}

	public Operator getNextOperator()
	{
		//return hasNextOperator() ? itsOperator+1 : NOT_AN_OPERATOR;
		if (hasNextOperator())
		{
			// hasNextOperator() sort of guarantees i.hasNext() 
			for (Iterator<Operator> i = OPERATORS.iterator(); i.hasNext(); )
				if (itsOperator == i.next())
					return i.next();
		}

		return Operator.NOT_AN_OPERATOR;
	}

	/**
	 * Evaluate Condition for {@link Column Column} of type
	 * {@link AttributeType#NOMINAL AttributeType.NOMINAL}.
	 * <p>
	 * The evaluation is performed using the operator and value set for this
	 * Condition, and {@link String#equals(Object) String.equals()}.
	 *
	 * @param theValue the value to compare to the value of this Condition.
	 *
	 * @return <code>true</code> if the evaluation yields <code>true</code>,
	 * <code>false</code> otherwise.
	 */
	public boolean evaluate(String theValue)
	{
		switch(itsOperator)
		{
			case ELEMENT_OF :
				return itsNominalValueSet.contains(theValue);
			case EQUALS :
				return theValue.equals(itsNominalValue);
			case LESS_THAN_OR_EQUAL : // deliberate fall-through
			case GREATER_THAN_OR_EQUAL :
			{
				logError("nominal");
				return false;
			}
			default : return false;
		}
	}

	/**
	 * Evaluate Condition for {@link Column Column} of type
	 * {@link AttributeType#NUMERIC AttributeType.NUMERIC}.
	 * <p>
	 * The evaluation is performed using the operator and value set for this
	 * Condition.
	 *
	 * @param theValue the value to compare to the value of this Condition.
	 *
	 * @return <code>true</code> if the evaluation yields <code>true</code>,
	 * <code>false</code> otherwise.
	 */
	public boolean evaluate(Float theValue)
	{
		switch(itsOperator)
		{
			case EQUALS :
				return theValue == itsNumericValue;
			case LESS_THAN_OR_EQUAL :
				return theValue <= itsNumericValue;
			case GREATER_THAN_OR_EQUAL :
				return theValue >= itsNumericValue;
			case BETWEEN:
				return itsInterval.between(theValue);
			default : return false;
		}
	}

	/**
	 * Evaluate Condition for {@link Column Column} of type
	 * {@link AttributeType#BINARY AttributeType.BINARY}.
	 * <p>
	 * The evaluation is performed using the operator and value set for this
	 * Condition.
	 *
	 * @param theValue the value to compare to the value of this Condition.
	 *
	 * @return <code>true</code> if the evaluation yields <code>true</code>,
	 * <code>false</code> otherwise.
	 */
	public boolean evaluate(boolean theValue)
	{
		if (itsOperator != Operator.EQUALS)
			logError("binary");
		return itsBinaryValue == theValue;
	}

	private void logError(String theColumnType)
	{
		Log.error(String.format("incorrect operator for %s column",
					theColumnType));
	}

	private void logTypeError(String theMethod)
	{
		Log.logCommandLine(String.format("%s.%s(): unknown AttributeType '%s'. Returning '%s'.",
							getClass().getSimpleName(),
							theMethod,
							itsColumn.getType(),
							itsOperator));
	}

	@Override
	public String toString()
	{
		if (itsColumn.getType() == AttributeType.NUMERIC || itsOperator == Operator.ELEMENT_OF)
			return String.format("%s %s %s", itsColumn.getName(), itsOperator, getValue());
		else
			return String.format("%s %s '%s'", itsColumn.getName(), itsOperator, getValue());
	}

	/*
	 * NOTE
	 * Never override equals() without also overriding hashCode().
	 * Some (Collection) classes use equals to determine equality, others
	 * use hashCode() (eg. java.lang.HashMap).
	 * Failing to override both methods will result in strange behaviour.
	 *
 	 * NOTE
	 * Map interface expects compareTo and equals to be consistent.
	 *
	 * Used by ConditionList.findCondition().
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

/*	@Override
	public boolean equals(Object theObject)
	{
		if (theObject == null || this.getClass() != theObject.getClass())
			return false;
		Condition aCondition = (Condition) theObject;
		if (itsColumn == aCondition.getColumn() &&
			itsOperator == aCondition.getOperator() &&
			itsValue.equals(aCondition.getValue()))
			return true;
		return false;
	}
*/
	// throws NullPointerException if theCondition is null.
	@Override
	public int compareTo(Condition theCondition)
	{
		if (this == theCondition)
			return 0;
		else if (this.itsColumn.getIndex() < theCondition.itsColumn.getIndex())
			return -1;
		else if (this.itsColumn.getIndex() > theCondition.itsColumn.getIndex())
			return 1;
		// same column, check operator
		else if (this.itsOperator.ordinal() < theCondition.itsOperator.ordinal())
			return -1;
		else if (this.itsOperator.ordinal() > theCondition.itsOperator.ordinal())
			return 1;
		// same column, same operator, check on value
		/*
		else if (this.getColumn().isNumericType())
			return (Float.valueOf(this.getValue()).compareTo(Float.valueOf(theCondition.getValue())));
		else
		{
			// String.compareTo() does not strictly return -1, 0, 1
			int aCompare = this.getValue().compareTo(theCondition.getValue());
			return (aCompare < 0 ? -1 : aCompare > 0 ? 1 : 0);
		}
		*/
		switch (itsColumn.getType())
		{
			case NOMINAL :
			{
				/*
				 * reasoning based on (itsNominalValue != null)
				 * is flawed, if setValue(null) is used to set
				 * itsNominalValue for a 'SINGLE_NOMINAL'
				 * Condition, this code erroneously assumes
				 * ValueSet, which comparison will crash with a
				 * NullPointerException.
				 * FIXME add setValue() sanity-checks
				 */
				if (itsNominalValue != null) //single value
				{
					// String.compareTo() does not strictly return -1, 0, 1
					int aCompare = itsNominalValue.compareTo(theCondition.itsNominalValue);
					return (aCompare < 0 ? -1 : aCompare > 0 ? 1 : 0);
				}
				else // assumes ValueSet
				{
					if (itsNominalValueSet != theCondition.itsNominalValueSet)
						throw new AssertionError(String.format("Multiple %ss for %s '%s'",
											itsNominalValueSet.getClass().getSimpleName(),
											itsColumn.getClass().getSimpleName(),
											itsColumn.getName()));
					return 0;
				}
			}
			case NUMERIC : // deliberate fall-through to ORDINAL
			case ORDINAL :
			{
				return Float.compare(itsNumericValue, theCondition.itsNumericValue);
			}
			case BINARY :
			{
				if (!itsBinaryValue)
					return theCondition.itsBinaryValue ? -1 : 0;
				else
					return theCondition.itsBinaryValue ? 0 : 1;
			}
			// should never happen
			default :
			{
				logTypeError("compareTo");
				return 0;
			}
		}
	}
}
