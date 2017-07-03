package nl.liacs.subdisc;

import java.io.*;
import java.util.*;

import javax.swing.*;

import nl.liacs.subdisc.FileHandler.Action;

import org.w3c.dom.*;

public class Table implements XMLNodeInterface
{
	// NOTE when adding members, update constructors AND Table.select()
	// all but Random can be made final
	private String itsSource;
	private String itsName;
	private int itsNrRows;
	private int itsNrColumns;
	private ArrayList<Column> itsColumns = new ArrayList<Column>();
//	private int itsNrNominals = 0;
//	private int itsNrNumerics = 0;
//	private int itsNrOrdinals = 0;
//	private int itsNrBinaries = 0;
	private Random itsRandomNumber = new Random(System.currentTimeMillis());
	private List<String> itsDomains;
	private List<Integer> itsDomainIndices; //allows for much faster removal

	public String getName() { return itsName; }
	public String getSource() { return itsSource; }

	// NOTE itsNrColumns is not tied to itsColumns.size()
	public int getNrRows() { 
		return itsNrRows; 
		}
	public int getNrColumns() {
		return itsNrColumns;
		} //just the descriptors

	public Column getColumn(int theIndex) { return itsColumns.get(theIndex); }

	public ArrayList<Column> getColumns() { return itsColumns; };

	// Empty table, meant for creating a copy with a subset of data. See select().
	public Table(String theTableName)
	{
		itsName = theTableName;
	}

	// FileLoaderARFF
	public Table(File theSource, String theTableName)
	{
		itsSource = theSource.getName();
		itsName = theTableName;
	}

	public Table(File theSource, String theTableName, int theNrRows, int theNrColumns)
	{
		itsSource = theSource.getName();
		itsName = theTableName;
		itsNrRows = theNrRows;
		itsNrColumns = theNrColumns;
		itsColumns.ensureCapacity(theNrColumns);
	}

	// FileLoaderTXT / DataLoaderTXT
	public Table(File theSource, int theNrRows, int theNrColumns)
	{
		itsSource = theSource.getName();
		itsName = FileType.removeExtension(theSource);
		itsNrRows = theNrRows;
		itsNrColumns = theNrColumns;
		itsColumns.ensureCapacity(theNrColumns);
	}

	// FileLoaderXML
	public Table(Node theTableNode, String theXMLFileDirectory)
	{
		NodeList aChildren = theTableNode.getChildNodes();
		for (int i = 0, j = aChildren.getLength(); i < j; ++i)
		{
			Node aSetting = aChildren.item(i);
			String aNodeName = aSetting.getNodeName();
			if ("table_name".equalsIgnoreCase(aNodeName))
				itsName = aSetting.getTextContent();
			else if ("source".equalsIgnoreCase(aNodeName))
				itsSource = aSetting.getTextContent();
			else if ("column".equalsIgnoreCase(aNodeName))
				itsColumns.add(new Column(aSetting));
		}
		/*
		 * now all columns are know, check if data (Columns) is valid by
		 * loading actual data from itsSource
		 */
		new FileHandler(new File(theXMLFileDirectory + "/" + itsSource), this);
	}

	/*
	 * TODO maintaining itsNrColumns becomes more difficult now
	 * complete new Column functionality to be implemented later
	 * TODO throw dialog on duplicate domain?
	 * add CUI Domain, relies on caller for Table.update()
	 * FileHandler.printLoadingInfo calls Table.update();
	 */
	boolean addDomain(String theDomainName)
	{
//not public
//		if (theDomainName == null)
//			return false;

		if (itsDomains == null)
		{
			itsDomains = new ArrayList<String>();
			itsDomainIndices = new ArrayList<Integer>();
		}

		if (itsDomains.contains(theDomainName))
		{
			Log.logCommandLine(
				String.format(
					"A domain with the name '%s' is already present.",
					theDomainName));
			return false;
		}
		else
		{
			itsDomains.add(theDomainName);

			/*
			 * a rank column will be inserted if it is not present
			 * offset ensures it will not be removed when the first
			 * domain is removed from the Table
			 */
			if (itsDomainIndices.size() == 0)
			{
				int offset = 1;
				for (Column c : itsColumns)
				{
					if (c.getName().equalsIgnoreCase("rank"))
					{
						--offset;
						break;
					}
				}
				itsDomainIndices.add(itsColumns.size() + offset);
			}
			else
				itsDomainIndices.add(itsColumns.size());

			return true;
		}
	}

	//FileHandler.printLoadingInfo calls Table.update();
	//TODO removeDomains(int[] theDomainIndices)
	public void removeDomain(int theDomainIndex)
	{
		if (itsDomains == null || theDomainIndex < 0 || theDomainIndex > itsDomains.size() - 1)
		{
			Log.logCommandLine(String.format("Domain '%s' not found."));
			return;
		}

		itsDomains.remove(theDomainIndex);
		int aStartIndex = itsDomainIndices.remove(theDomainIndex).intValue();
		int anEndIndex; //check whether it is the last domain

		if (theDomainIndex == itsDomainIndices.size())
			anEndIndex = itsColumns.size();
		else
			anEndIndex = itsDomainIndices.get(theDomainIndex);

		/*
		 * removing from ArrayList backwards avoids expensive arrayCopy
		 * if domain is at the end
		 */
		for (int i = anEndIndex - 1; i >= aStartIndex ; --i)
			itsColumns.remove(i);

		int aNrDeletedColumns = anEndIndex - aStartIndex;
		for (int i = theDomainIndex, j = itsDomainIndices.size(); i < j; ++i)
			itsDomainIndices.set(i, itsDomainIndices.get(i) - aNrDeletedColumns);

		itsNrColumns = itsColumns.size();
		itsColumns.trimToSize();

		//reset indices for all Columns after removed domain
		//inefficient if multiple domains are removed at once
		if (aStartIndex < itsNrColumns)
			for (int i = aStartIndex; i < itsNrColumns; ++i)
				itsColumns.get(i).setIndex(i);

		if (itsDomains.isEmpty())
		{
			itsDomains = null;
			itsDomainIndices = null;
		}
	}

	public JList getDomainList()
	{
		/*
		 * MiningWindow should guarantee 'Remove' is only available when
		 * itsDomains is not null/empty
		 */
		if (itsDomains == null)
			return null;
		else
			return new JList(itsDomains.toArray());
	}

	/*
	 * TODO change this method, goal is to create a lock() function that 'locks'
	 * the table. itsNrRows/itsNrColumn and itsAttributes/itsColumns.size() do
	 * not change anymore. Update is expensive right now. If itsAttributes would
	 * be implemented as a HashSet/TreeSet adding would be less of a problem.
	 */
	/**
	 * Updates this Table. This means the number of rows and columns are set,
	 * and this Tables' list of {@link Column Column}s is updated.
	 */
	public void update()
	{
		itsNrRows = itsColumns.size() > 0 ? itsColumns.get(0).size() : 0;
		itsNrColumns = itsColumns.size();	// needed for MiningWindow

		for (Column c : itsColumns)
		{
			c.close();
			c.getCardinality();
		}
	}

	/**
	 * Retrieves an array of <code>int[]</code>s, containing the number of
	 * {@link Column}s for each {@link AttributeType}, and the number of
	 * those Columns that are enabled.
	 * The <code>int[]</code>s are for AttributeTypes:
	 * {@link AttributeType#NOMINAL}, {@link AttributeType#NUMERIC},
	 * {@link AttributeType#ORDINAL} and {@link AttributeType#BINARY},
	 * respectively.
	 * 
	 * @return an array of <code>int[]</code>s, containing for each
	 * AttributeType the number of Columns of that type, and the number of
	 * those Columns that is enabled.
	 */
	public int[][] getTypeCounts()
	{
		int[][] aCounts = new int[4][2];
		for(Column c : itsColumns)
		{
			switch(c.getType())
			{
				case NOMINAL :
				{
					++aCounts[0][0];
					if (c.getIsEnabled())
						++aCounts[0][1];
					break;
				}
				case NUMERIC :
				{
					++aCounts[1][0];
					if (c.getIsEnabled())
						++aCounts[1][1];
					break;
				}
				case ORDINAL :
				{
					++aCounts[2][0];
					if (c.getIsEnabled())
						++aCounts[2][1];
					break;
				}
				case BINARY :
				{
					++aCounts[3][0];
					if (c.getIsEnabled())
						++aCounts[3][1];
					break;
				}
			}
		}
		return aCounts;
	}



	/*
	 * This method is too expensive in most cases,
	 * Validation.randomConditions and SubgroupDiscovery.makeNewSubgroup use
	 * it.
	 * Actually, when making a refinement (in makeNewSubgroup), the new
	 * BitSet can be created from
	 * old_Subgroup.getMembers().and(new_condition.getAttribute.evaluate(new_condition))
	 * This makes the algorithm much faster, as only a single loop over the
	 * new_condition.getAttribute() is needed + 1 and(), instead of the
	 * current conditionList.size() loops + and()'s.
	 * Also it prevents the need to reload all Columns that occur in the
	 * Condition that form the ConditionList.
	 */
	// as above but only checks type once
	public BitSet evaluate(ConditionList theList)
	{
		// Validation.randomConditions does not check for null
		if (theList == null || theList.isEmpty())
			return new BitSet(0);

		// these 3 lines replace all code below, slightly less efficient
		BitSet aSet = theList.get(0).getColumn().evaluate(theList.get(0));
		for (int i = 1, j =  theList.size(); i < j; ++i)
			aSet.and(theList.get(i).getColumn().evaluate(theList.get(i)));

		return aSet;
	}

	//Data Model ===========================================================================

	// CHANGE CHECKED AND APPROVED
	public Column getColumn(String theName)
	{
		for (Column c : itsColumns)
			if (c.getName().equals(theName))
				return c;

		return null; //not found
	}

	public int getIndex(String theName)
	{
		for (Column c : itsColumns)
			if (c.getName().equals(theName))
				return c.getIndex();

		return -1; // not found (causes ArrayIndexOutOfBounds)
	}

	public Condition getFirstCondition()
	{
		return new Condition(itsColumns.get(0));
	}

	//TODO this code is overly complex, due to its Safarii-background
	//Arno needs to fix this, at some point.
	public Condition getNextCondition(Condition theCurrentCondition)
	{
		if (theCurrentCondition.hasNextOperator())
			return new Condition(theCurrentCondition.getColumn(), theCurrentCondition.getNextOperator());
		else
		{
			int anIndex = theCurrentCondition.getColumn().getIndex();
			if (anIndex == itsNrColumns-1) // No more attributes
				return null;
			else
				return new Condition(itsColumns.get(anIndex + 1));
		}
	}


	// Misc ===============================

	public Subgroup getRandomSubgroup(int theSize)
	{
		BitSet aSample = new BitSet(itsNrRows);
		int m = 0;
		int t = 0;

		for (int i = 0; i < itsNrRows; i++)
		{
			double aThresholdValue1 = theSize - m;
			double aThresholdValue2 = itsNrRows - t;

			if ((aThresholdValue2 * itsRandomNumber.nextDouble()) < aThresholdValue1)
			{
				aSample.set(i);
				m++;
				t++;
				if (m >= theSize)
					break;
			}
			else
				t++;
		}

		return new Subgroup(null, aSample, null);
	}

	/**
	 * Creates a new Table, where records are selected based on the bits set
	 * in the {@link BitSet BitSet} passed in as argument.
	 * <p>
	 * NOTE the new Table is not a true deep-copy.
	 * 
	 * @param theSet BitSet indicating which records to use.
	 * 
	 * @return a new Table consisting of a selection of the original one.
	 */
	public Table select(BitSet theSet)
	{
		Table aResult = new Table(itsName);
		aResult.itsSource = itsSource;
		aResult.itsNrColumns = itsNrColumns;
		aResult.itsNrRows = theSet.cardinality();

		aResult.itsDomains = this.itsDomains;
		aResult.itsDomainIndices = this.itsDomainIndices;

		//copy each column, while leaving out some of the data
		for (Column aColumn : itsColumns)
			aResult.itsColumns.add(aColumn.select(theSet));

		aResult.update();

		return aResult;
	}

	/**
	 * NOTE this method is destructive to the {@link TargetConcept} passed
	 * in as parameter. If the TargetConcept needs to be restored to its
	 * original state, be sure to back it up before calling this method.
	 * 
	 * @param theTC the TargetConcept to swapRandomize.
	 * 
	 * @see Column#permute(int[])
	 * @see Validation#swapRandomization(int)
	 */
	public void swapRandomizeTarget(TargetConcept theTC)
	{
		List<Column> aTargets = new ArrayList<Column>(2);
		TargetType aType = theTC.getTargetType();

		//find all targets
		switch (aType)
		{
			case DOUBLE_REGRESSION : // deliberate fall-through
			case DOUBLE_CORRELATION :
				aTargets.add(theTC.getSecondaryTarget());
				//no break
			case SINGLE_NOMINAL : // deliberate fall-through
			case SINGLE_NUMERIC :
				aTargets.add(theTC.getPrimaryTarget());
				break;
			case MULTI_LABEL :
				aTargets = theTC.getMultiTargets();
				break;

			// unimplemented TargetTypes
			case SINGLE_ORDINAL :
			case MULTI_BINARY_CLASSIFICATION :
			default :
			{
				throw new AssertionError(
						String.format("%s.swapRandomizeTarget(): unimplemented %s '%s'",
								this.getClass().getSimpleName(),
								TargetType.class.getSimpleName(),
								aType));
			}
		}

		int n = getNrRows();
		//start with regular order
		int[] aPermutation = new int[n];
		for (int i=0; i<n; i++)
			aPermutation[i] = i;

		//randomize
		for (int i=0; i<n-1; i++)
		{
			int aFirst = i;
			int aSecond = i+itsRandomNumber.nextInt(n-i);

			//swap first and second
			int aSwap = aPermutation[aFirst];
			aPermutation[aFirst] = aPermutation[aSecond];
			aPermutation[aSecond] = aSwap;
		}

		//execute permutation on all targets
		for (Column aColumn : aTargets)
		{
			Log.logCommandLine("permuting \"" + aColumn.getName() + "\"");
			aColumn.permute(aPermutation);
		}
	}

	public void print()
	{
		Log.logCommandLine("Types ===========================================");
		for (Column c : itsColumns)
			c.print();
		Log.logCommandLine("Table ===========================================");
		for (int i = 0, j = itsColumns.get(0).size(); i < j; i++)
		{
			StringBuilder aRows = new StringBuilder("Row ");
			aRows.append(i + 1);
			aRows.append(": ");
			for (Column aColumn : itsColumns)
			{
				aRows.append(aColumn.getString(i));
				aRows.append(", ");
			}
			Log.logCommandLine(aRows
								.substring(0, aRows.length() - 2)
								.toString());
		}
		Log.logCommandLine("=================================================");
	}

	// TODO arff-writer, AttributeType inclusion makes is load faster/safer
	/**
	 * Write this Table to <code>File</code>.
	 *
	 * @param theMembers the indices of the set bits in this <BitSet>
	 * correspond to the row numbers of this Table that should be written to
	 * the output <code>File</code>. If the parameter is <code>null</code>,
	 * the whole Table will be written.
	 */
	public void toFile(BitSet theMembers)
	{
		BufferedWriter aWriter = null;

		File aFile = new FileHandler(Action.SAVE).getFile();

		if (aFile == null)
			return;
		else if (theMembers == null)
			toFile(aFile);
		else
		{
			try
			{
				aWriter = new BufferedWriter(new FileWriter(aFile));
				int aNrColumnsMinusOne = itsNrColumns - 1;

				for (int i = 0; i < aNrColumnsMinusOne; ++i)
				{
					aWriter.write(itsColumns.get(i).getName());
					aWriter.write(",");
				}
				aWriter.write(itsColumns.get(aNrColumnsMinusOne).getName());
				aWriter.write("\n");

				// could return if (theMembers.cardinality == 0)
				// lookup columnTypes only once
				AttributeType[] aTypes = new AttributeType[itsNrColumns];
				for (int i = 0, j = itsNrColumns; i < j; ++i)
					aTypes[i] = itsColumns.get(i).getType();

				// itsNrRows for safety
				for (int i = theMembers.nextSetBit(0); i >= 0 && i < itsNrRows; i = theMembers.nextSetBit(i + 1))
				{
					for (int k = 0; k < aNrColumnsMinusOne; ++k)
					{
						switch(aTypes[k])
						{
							case NOMINAL : aWriter.write(itsColumns.get(k).getNominal(i)); break;
							case NUMERIC : aWriter.write(String.valueOf(itsColumns.get(k).getFloat(i))); break;
							case ORDINAL : aWriter.write(String.valueOf(itsColumns.get(k).getFloat(i))); break;
							case BINARY : aWriter.write(itsColumns.get(k).getBinary(i) ? "1" : "0"); break;
							default : Log.logCommandLine("Unknown AttributeType: " + aTypes[k]); break;
						}
						aWriter.write(",");
					}
					switch(aTypes[aNrColumnsMinusOne])
					{
						case NOMINAL : aWriter.write(itsColumns.get(aNrColumnsMinusOne).getNominal(i)); break;
						case NUMERIC : aWriter.write(String.valueOf(itsColumns.get(aNrColumnsMinusOne).getFloat(i))); break;
						case ORDINAL : aWriter.write(String.valueOf(itsColumns.get(aNrColumnsMinusOne).getFloat(i))); break;
						case BINARY : aWriter.write(itsColumns.get(aNrColumnsMinusOne).getBinary(i) ? "1" : "0"); break;
						default : Log.logCommandLine("Unknown AttributeType: " + aTypes[aNrColumnsMinusOne]); break;
					}
					aWriter.write("\n");
				}
				aWriter.write("\n");
			}
			catch (IOException e)
			{
				Log.logCommandLine("Error while writing: " + aFile);
			}
			finally
			{
				try
				{
					if (aWriter != null)
						aWriter.close();
				}
				catch (IOException e)
				{
					Log.logCommandLine("Error while writing: " + aFile);
				}
			}
		}
	}

	// as above, but writes whole Table (no row inclusion test)
	private void toFile(File theFile)
	{
		BufferedWriter aWriter = null;

		try
		{
			aWriter = new BufferedWriter(new FileWriter(theFile));
			int aNrColumnsMinusOne = itsNrColumns - 1;

			for (int i = 0; i < aNrColumnsMinusOne; ++i)
			{
				aWriter.write(itsColumns.get(i).getName());
				aWriter.write(",");
			}
			aWriter.write(itsColumns.get(aNrColumnsMinusOne).getName());
			aWriter.write("\n");

			// lookup columnTypes only once
			AttributeType[] aTypes = new AttributeType[itsNrColumns];
			for (int i = 0, j = itsNrColumns; i < j; ++i)
				aTypes[i] = itsColumns.get(i).getType();

			for (int i = 0, j = itsNrRows; i < j; ++i)
			{
				for (int k = 0; k < aNrColumnsMinusOne; ++k)
				{
					switch(aTypes[k])
					{
						case NOMINAL : aWriter.write(itsColumns.get(k).getNominal(i)); break;
						case NUMERIC : aWriter.write(String.valueOf(itsColumns.get(k).getFloat(i))); break;
						case ORDINAL : aWriter.write(String.valueOf(itsColumns.get(k).getFloat(i))); break;
						case BINARY : aWriter.write(itsColumns.get(k).getBinary(i) ? "1" : "0"); break;
						default : Log.logCommandLine("Unknown AttributeType: " + aTypes[k]); break;
					}
					aWriter.write(",");
				}
				switch(aTypes[aNrColumnsMinusOne])
				{
					case NOMINAL : aWriter.write(itsColumns.get(aNrColumnsMinusOne).getNominal(i)); break;
					case NUMERIC : aWriter.write(String.valueOf(itsColumns.get(aNrColumnsMinusOne).getFloat(i))); break;
					case ORDINAL : aWriter.write(String.valueOf(itsColumns.get(aNrColumnsMinusOne).getFloat(i))); break;
					case BINARY : aWriter.write(itsColumns.get(aNrColumnsMinusOne).getBinary(i) ? "1" : "0"); break;
					default : Log.logCommandLine("Unknown AttributeType: " + aTypes[aNrColumnsMinusOne]); break;
				}
				aWriter.write("\n");
			}
			aWriter.write("\n");
		}
		catch (IOException e)
		{
			Log.logCommandLine("Error while writing: " + theFile);
		}
		finally
		{
			try
			{
				if (aWriter != null)
					aWriter.close();
			}
			catch (IOException e)
			{
				Log.logCommandLine("Error while writing: " + theFile);
			}
		}
	}

	@Override
	public void addNodeTo(Node theParentNode)
	{
		Node aNode = XMLNode.addNodeTo(theParentNode, "table");
		XMLNode.addNodeTo(aNode, "table_name", itsName);
		XMLNode.addNodeTo(aNode, "source", itsSource);

		for (int i = 0, j = itsColumns.size(); i < j; ++i)
			itsColumns.get(i).addNodeTo(aNode);
	}
}
