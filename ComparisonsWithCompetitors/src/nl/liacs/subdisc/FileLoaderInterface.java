package nl.liacs.subdisc;

public interface FileLoaderInterface
{
	public static final String DEFAULT_SEPARATOR = ",";

	// TODO use different separator in FileLoaders?
	// TODO all FileLoaders should check if the File passed to its constructor
	// is a valid type for them to handle
//	void setSeparator(String theNewSeparator);

	/**
	 * Returns the {@link Table Table} created by the DataLoader. It may be
	 * <code>null</code> or empty (containing no data), in case errors occur
	 * during loading.
	 */
	Table getTable();
}
