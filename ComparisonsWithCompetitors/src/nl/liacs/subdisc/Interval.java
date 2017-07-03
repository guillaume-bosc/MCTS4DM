package nl.liacs.subdisc;

public class Interval
{
	private final float itsLower;
	private final float itsUpper;

	public Interval(float theLower, float theUpper)
	{
		itsLower = theLower;
		itsUpper = theUpper;
	}

	public boolean between(float theValue)
	{
		return (itsLower < theValue) && (theValue <= itsUpper);
	}

	public String toString()
	{
		String aLeft = (itsLower == Float.NEGATIVE_INFINITY) ? "-inf" : Float.toString(itsLower);
		String aRight = (itsUpper == Float.POSITIVE_INFINITY) ? "inf)" : (Float.toString(itsUpper) + "]");
		return new StringBuilder(32).append("(").append(aLeft).append(", ").append(aRight).toString();
	}
}
