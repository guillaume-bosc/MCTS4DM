package nl.liacs.subdisc;

/*
 * Known implementing enums: AttributeType,NumericOperators, NumericStrategy,
 * QM, SearchStrategy, TargetType.
 */
public interface EnumInterface
{
	// public static EnumInterface fromString();

	/**
	 * Returns a friendly <code>String<String> to show in the GUI.
	 * 
	 * @return the text <code>String</code> presented to the end user.
	 */
	@Override
	public String toString();
	// TODO enforce uniform handling and reporting of unknown and null enums
}
