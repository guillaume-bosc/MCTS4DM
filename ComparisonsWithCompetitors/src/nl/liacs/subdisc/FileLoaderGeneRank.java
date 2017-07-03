/*
 * NOTE: this class may need a lot of memory. Use the virtual machine command
 * line parameter '-Xms1600m' and/or '-Xmx1600m' (or another suitable amount of
 * memory).
 * TODO split foo() into multiple methods and rename
 */
package nl.liacs.subdisc;

import java.io.*;
import java.util.*;

import nl.liacs.subdisc.cui.*;
import nl.liacs.subdisc.gui.*;

// TODO rename this class to generic FileLoaderRank
public class FileLoaderGeneRank implements FileLoaderInterface
{
	// TODO should come from GUI, values below threshold are set to 0.0f
	public static final Float THRESHOLD = new Float(0.01);

	private static Map<String, Integer> itsCui2LineNrMap;	// TODO generify
	private static Map<String, String> itsCui2NameMap;	// TODO generify

	// TODO make static
	private Map<String, String> itsGene2CuiMap;

	private Table itsTable;
	private File itsEnrichmentSource = null;

	public FileLoaderGeneRank(Table theTable, EnrichmentType theType)
	{
		if (theTable == null || theType == null)
		{
			Log.logCommandLine(
			"FileLoaderRank Constructor: parameter(s) can not be 'null'.");
			return;
		}
		else
		{
			itsTable = theTable;
			checkEnrichmentType(theType);

			if (itsEnrichmentSource == null)
			{
				Log.logCommandLine("No enrichment source file selected.");
				return;
			}
			else if (!itsEnrichmentSource.exists())
			{
				ErrorLog.log(itsEnrichmentSource, new FileNotFoundException());
				return;
			}
			else if (itsEnrichmentSource.length() == 0)
			{
				Log.logCommandLine("Empty file: " + itsEnrichmentSource);
				return;
			}
			else
			{
				// TODO remove debug only
				System.gc();
				// NOTE extra memory can be claimed at runtime
				Runtime r = Runtime.getRuntime();
				long usedPre = r.totalMemory() - r.freeMemory();
				long start = System.currentTimeMillis();
				foo();
				System.gc();
				System.out.println(((r.totalMemory()-r.freeMemory()) - usedPre) / 1048576 + "MB used after adding domain info.");
				System.out.println((System.currentTimeMillis() - start)/1000 + "s. for loading of domain file.");
			}
		}
	}

	private void checkEnrichmentType(EnrichmentType theType)
	{
		switch (theType)
		{
			case CUI : setupCuiMaps(); break;
			case GO : setupGoMaps() ; break;
			case CUSTOM : setupCustomMaps() ; break;
			default :
				Log.logCommandLine("Unknown EnrichmentType: " + theType); break;
		}
	}

	private void setupCuiMaps()
	{
		File aCui2LineNrFile = new File(CuiMapInterface.GENE_IDENTIFIER_CUIS);
		File aCui2NameFile = new File(CuiMapInterface.CUI2NAME);

		if (!aCui2LineNrFile.exists())
		{
			ErrorLog.log(aCui2LineNrFile, new FileNotFoundException());
		}
		else if (!aCui2NameFile.exists())
		{
			ErrorLog.log(aCui2NameFile, new FileNotFoundException());
		}
		else
		{
			itsCui2LineNrMap = new Cui2LineNrMap(aCui2LineNrFile).getMap();
			itsCui2NameMap = new Cui2NameMap(aCui2NameFile).getMap();
		}

		/*
		 * Note: the CuiMapInterface.GENE_IDENTIFIER_CUIS file is not strictly
		 * needed to create a mapping. Instead, the same map can be build by
		 * reading the selected cui-domain file twice.
		 * Once to build the cui2lineNrMap, using the first column only (while
		 * avoiding duplicates).
		 * Another time to populate Table.
		 */
		if (itsCui2LineNrMap == null)
		{
			Log.logCommandLine(String.format("File: '%s' not found.", 
										CuiMapInterface.GENE_IDENTIFIER_CUIS));
			return;
		}

		itsEnrichmentSource = new CuiDomainChooser().getFile();
	}

	private void setupGoMaps()
	{
		
	}

	private void setupCustomMaps()
	{
		
	}

	private void foo()
	{
		BufferedReader aReader = null;

		try
		{
			aReader = new BufferedReader(new FileReader(itsEnrichmentSource));
			boolean hasCuiColumn = false;
			boolean hasRankColumn = false;
			int identifierColumn = -1;

			ArrayList<Column> aColumns = itsTable.getColumns();
			int aNrDataColumns = aColumns.size();	// safer than itsTable.getNrColumns()
			//int aNrDataColumns = itsTable.getNrColumns(); // relies on correct itsNrColumn/update
			int aNrRows = itsTable.getNrRows();
			// file non-empty checked by constructor
			String[] aCUIHeaderArray = aReader.readLine().split("\t", -1);
			int aNrCUIColumns = aCUIHeaderArray.length;
			String aString = null;

			// TODO only when it is not already known (previous domain addition)
			// get essential information about the existing Table
			// if a CUI column already exist, still create new ones
			for (int i = 0; i < aNrDataColumns; ++i)
			{
				aString = aColumns.get(i).getName().toLowerCase();

				if ("rank".equals(aString))
					hasRankColumn = true;
				else if ("entrez".equals(aString) || "go".equals(aString))
				{
					createGene2CuiMap(aString);
					identifierColumn = i;
				}
				// no mapping needed
				else if ("cui".equals(aString))
				{
					hasCuiColumn = true;
					identifierColumn = i;
				}

				if (hasRankColumn && identifierColumn != -1)
					break;
			}

			if (identifierColumn == -1)
			{
				IdentifierChooser aChooser = new IdentifierChooser(aColumns);
				identifierColumn = aChooser.getIdentifierColumnIndex();
				if ((identifierColumn == -1) || (aChooser.getIdentifierType() == null))
					return;
				else
					createGene2CuiMap(aChooser.getIdentifierType());
			}

			String aDomainName = FileType.removeExtension(itsEnrichmentSource);
			if (!itsTable.addDomain(aDomainName))
				return;

			// disable by default
			aColumns.get(identifierColumn).setIsEnabled(false);

			aColumns.ensureCapacity(aNrDataColumns + aNrCUIColumns + (hasRankColumn ? 0 : 1));

			if (!hasRankColumn)
			{
				Column aRankColumn = new Column("RANK",
										null,
										AttributeType.NUMERIC,
										aNrDataColumns++,
								aNrRows);

				for (float f = 1.0f, nrRows = (float)aNrRows; f <= nrRows; ++f)
					aRankColumn.add(f);	// relatively expensive, see TODO XXX

				aRankColumn.setIsEnabled(false);	// XXX for now disable by default
				aColumns.add(aRankColumn);
			}

			aColumns.add(new Column(("Domain: " + aDomainName),
									null,
									AttributeType.NOMINAL,
									aNrDataColumns++,
									aNrRows));

			// disable CUI column
			aColumns.get(aNrDataColumns - 1).setIsEnabled(false);

			for (int i = 1; i < aNrCUIColumns; i++)
			{
				// Note: Browse~/ReasultWindow use normal name, not short name
				aColumns.add(new Column(itsCui2NameMap.get(aCUIHeaderArray[i]),
										aCUIHeaderArray[i],
										AttributeType.NUMERIC,
										aNrDataColumns++,
										aNrRows));
			}

			// for each identifier in itsTable determine Domain file lineNr
			// itsCui2LineNrMap<K, V> uses String as Key
			/*
			 * aLineNrMap is used to read the CUI-Domain file effectively, that
			 * is, from begin to end in one pass.
			 */
			/*
			 * theLineNrSet is sorted. All lineNumbers are looked up in the CUI-Domain
			 * file in order, and the information is added to the relevant gene in
			 * itsGeneRank.
			 */
			Map<Integer, Integer> aLineNrMap = new TreeMap<Integer, Integer>();
			Column idColumn = aColumns.get(identifierColumn);
			int aNrColumns = itsTable.getColumns().size();
			Column aDomainColumn = itsTable.getColumn(aNrColumns - aNrCUIColumns);

			for (int i = 0; i < aNrRows; i++)
			{
				// always set all association scores to 0.0f, update CUIs later
				// TODO XXX new constructor/method using name(ArrayList<Float>().nCopies(anrRows, 0.0f))
				for (int j = aNrCUIColumns - 1, k = aNrColumns; j > 0; --j)
					itsTable.getColumn(--k).add(0.0f);

				// if CUI exists, put it in Map<cuiLineNr, identifierRowNr>
				String aCui;
				if (hasCuiColumn)
					aCui = String.valueOf(Float.valueOf(idColumn.getString(i)).intValue()); // XXX removes '.0' :)
				else
					aCui = itsGene2CuiMap.get(String.valueOf(Float.valueOf(idColumn.getString(i)).intValue())); // XXX removes '.0' :)

				Integer aLineNr = itsCui2LineNrMap.get(aCui);
				aDomainColumn.add(itsCui2NameMap.get(aCui));

				// no mapping may exist in some ENTREZ/GO cases
				if (aLineNr != null)
					aLineNrMap.put(aLineNr, i);
			}

			// populate the new CUI columns for identifiers mapped to CUIs
			int aCurrentLineNr = 1;	// headerLine read already
			int aNeededLineNr = 0;
			for (Map.Entry<Integer, Integer> anEntry: aLineNrMap.entrySet())
			{
				aNeededLineNr = anEntry.getKey().intValue();
				// no EOF check, lineNrs should always be in file
				while (++aCurrentLineNr <= aNeededLineNr)
					aReader.readLine();

				/*
				 * For long lines aLine.split() is much faster then Scanner().
				 * To reduce memory usage values are directly stored as Float.
				 * Does not test for NumberFormatException, assumes well-formed
				 * CUI-Domain files.
				 */
				String[] anArray = aReader.readLine().split("\t", -1);
				for (int j = aNrCUIColumns - 1, k = aNrColumns; j > 0; --j)
					// TODO THIS IS THE CRUCIAL THRESHOLD CHECK, THRESHOLD SHOULD COME FROM GUI
					if (Float.valueOf(anArray[j]) > THRESHOLD)
						itsTable.getColumn(--k).set(anEntry.getValue(), Float.valueOf(anArray[j]));
			}
		}
		// TODO
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
	}

	private void createGene2CuiMap(String theIdentifierType)
	{
		if ("entrez".equals(theIdentifierType))
			itsGene2CuiMap = Gene2CuiMap.ENTREZ2CUI.getMap();
		else if ("go".equalsIgnoreCase(theIdentifierType))
			itsGene2CuiMap = Gene2CuiMap.GO2CUI.getMap();
	}

	@Override
	public Table getTable()
	{
		return itsTable;
	}
}
