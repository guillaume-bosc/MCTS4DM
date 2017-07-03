package nl.liacs.subdisc;

import java.io.*;
import java.util.*;

public class ExternalKnowledgeFileLoader
{
	private final List<ConditionList> externalInfoLocal;
	private final List<ConditionList> externalInfoGlobal;
	private final List<String> linesLocal;
	private final List<String> linesGlobal;

	public ExternalKnowledgeFileLoader(String theStringF)
	{
		externalInfoLocal = new ArrayList<ConditionList>();
		externalInfoGlobal = new ArrayList<ConditionList>();
		linesGlobal = new ArrayList<String>();
		linesLocal = new ArrayList<String>();

		File f = new File(theStringF);

		// load global knowledge file
		readFiles(f.listFiles(new OnlyExt("gkf")), linesGlobal);

		// load local knowledge file
		readFiles(f.listFiles(new OnlyExt("lkf")), linesLocal);

		print();
	}

	private static void readFiles(File[] theFiles, List<String> theLines)
	{
		if (theFiles.length == 0)
			return;

		// only one file is loaded for each type of knowledge
		// change to (File f : theFiles) or (i < j)
		for (int i = 0, j = theFiles.length; i < 1; ++i)
			addLinesFromFile(theFiles[i], theLines);
	}

	private static void addLinesFromFile(File theFile, List<String> theLines)
	{
		BufferedReader br = null;

		try
		{
			br = new BufferedReader(new FileReader(theFile));

			String aLine;
			while ((aLine = br.readLine()) != null)
				theLines.add(aLine);
		}
		catch (IOException e)
		{
			Log.logCommandLine("Error while reading File: " + theFile);
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e1)
				{
					Log.logCommandLine("Error while closing File: " + theFile);
				}
			}
		}
	}

	private void print()
	{
		Log.logCommandLine("\nGlobal External Knowledge:");
		for (String s : linesGlobal)
			Log.logCommandLine(s);

		Log.logCommandLine("\nLocal External Knowledge:");
		for (String s : linesLocal)
			Log.logCommandLine(s);

		Log.logCommandLine("");
	}

	public void createConditionListLocal(Table theTable)
	{
		if (externalInfoLocal.size() == 0)
			knowledgeToConditions(linesLocal, externalInfoLocal, theTable);
	}

	public void createConditionListGlobal(Table theTable)
	{
		if (externalInfoGlobal.size() == 0)
			knowledgeToConditions(linesGlobal, externalInfoGlobal, theTable);
	}

	private static void knowledgeToConditions(List<String> theKnowledge, List<ConditionList> theConditionLists, Table theTable)
	{
		for (String aLine : theKnowledge)
		{
			ConditionList aConditionList = new ConditionList();

			// add every conjunct to the ConditionList
			for (String conjunct : getConjuncts(aLine))
			{
				String[] sa = disect(conjunct);
				Column col = theTable.getColumn(sa[0]);
				Operator op = Operator.fromString(sa[1]);

				// create Condition using Column and Operator
				Condition aCondition = new Condition(col, op);
				// set Condition value
				aCondition.setValue(sa[2]);
				aConditionList.add(aCondition);
			}

			theConditionLists.add(aConditionList);
			Log.logCommandLine(aConditionList.toString());
		}
	}

	private static String[] getConjuncts(String conjunction)
	{
		// assume ' AND ' does not appear in column names
		return conjunction.split(" AND ", -1);
	}

	// " in ", " = ", " <= ", " >= ", 
	private static final String[] OPERATORS = getOperatorStrings();

	// keep in sync with 'official' Operator-string-values 
	private static String[] getOperatorStrings()
	{
		final List<String> aList = new ArrayList<String>();
		for (Operator o : Operator.set())
		{
			if (o == Operator.NOT_AN_OPERATOR)
				continue;

			final String s = new StringBuilder(4)
							.append(" ")
							.append(o.GUI_TEXT)
							.append(" ")
							.toString();
			if (!aList.contains(s))
				aList.add(s);
		}

		return aList.toArray(new String[0]);
	}

	// TODO mapping a Condition back to its constituents should be made a
	// Condition.method().
	private static String[] disect(String condition)
	{
		// assume OPERATORS do not appear in column name
		for (String s : OPERATORS)
		{
			if (condition.contains(s))
			{
				final String[] tmp = condition.split(s);
				// remove outer quotes from column name
//				tmp[0] = tmp[0].substring(1, tmp[0].length()-1);
				if (tmp[1].startsWith("'") && tmp[1].endsWith("'"))
					tmp[1] = tmp[1].substring(1, tmp[1].length()-1);
				return new String[] { tmp[0] , s.trim(), tmp[1] };
			}
		}

		return null; // throw Exception
	}

	public List<ConditionList> getLocal()
	{
		return externalInfoLocal;
	}

	public List<ConditionList> getGlobal()
	{
		return externalInfoGlobal;
	}
}
