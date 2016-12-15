import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
/**
 * Artificial data generator to hide subgroups with two labels.
 *  
 * <ul>
 * <li>Generates an object/attribute CSV file with positive and negative examples
 * such that a controlled number of subgroups are hidden in noise and frequent patterns. 
 * </li><li>
 * Used in the article <i>Anytime diverse subgroup discovery with Monte Carlo Tree Search</i>
 * submitted to DAMI.
 *  </li><li>
 * Adapted from the artificial data generator for <i>Exceptional Contextual Subgraph Mining</i>
 * taken from <a href="https://github.com/mehdi-kaytoue/contextual-exceptional-subgraph-mining">
 * https://github.com/mehdi-kaytoue/contextual-exceptional-subgraph-mining</a>
 * </li>
 * 
 * @author Mehdi Kaytoue
 */
public class DataGen {

	static double noise_rate;// =0.1;   //-N
	static double out_factor;// = 0.1; //-O
	static int nb_patterns;// = 5;     //-P
	static int pattern_size;// = 200;   //-S
	static int nb_trans;// = 50000;     // -t
	static int nb_att;// = 5;          //-a
	static int att_domain_size;// = 10;  // -c

	public static int TIMEOUT = 999000;
	public static int REPLICATES = 1; 
	public static int iterMax = 10000;
	public static int nbIter;
	public static int minSup;
	public static int evalMeasure;

	public static boolean addFrequentPatterns;

	public static final int PRECISION = 1;
	public static final int RECALL = 2;
	public static final int FMEASURE = 3;
	public static final int JACCARD = 4;


	public static int XP = 1;
	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int LARGE = 3;
	public static final int LARGE_CORTANA = 1111;

	public static void setDefaultParameters()
	{
		if (XP == SMALL) {
			noise_rate =0.1;   //-N
			out_factor = 0.1; //-O
			nb_patterns = 3;     //-P
			pattern_size = 100;   //-S
			nb_trans = 2000;     // -t
			nb_att = 5;          //-a
			att_domain_size = 10;  // -c
			iterMax = 100000;
			minSup = 1; 
			evalMeasure = JACCARD; // RECALL   F-MEASURE JACCARD
			addFrequentPatterns = true;
			REPLICATES = 5;
		}
		
		if (XP == MEDIUM) {
			noise_rate = 0.1;   //-N
			out_factor = 0.1; //-O
			nb_patterns = 5;     //-P
			pattern_size = 100;   //-S
			nb_trans = 20000;     // -t
			nb_att = 5;          //-a
			att_domain_size = 20;  // -c
			iterMax = 100000;
			minSup = 5; 
			evalMeasure = JACCARD; // RECALL   F-MEASURE JACCARD
			addFrequentPatterns = true;
		}
		
		if (XP == LARGE) {
			TIMEOUT = 9990000;
			REPLICATES = 1;
			noise_rate =0.1;   //-N
			out_factor = 0.1; //-O
			nb_patterns = 25;     //-P
			pattern_size = 100;   //-S
			nb_trans = 50000;     // -t
			nb_att = 25;          //-a
			att_domain_size = 50;  // -c
			iterMax = 1000000;
			minSup = 10; 
			evalMeasure = JACCARD; // RECALL   F-MEASURE JACCARD
			addFrequentPatterns = true;
		}
		
		
		if (XP == LARGE_CORTANA) {
			TIMEOUT = 9990000;
			REPLICATES = 3;
			noise_rate =0.2;   //-N
			out_factor = 0.15; //-O
			nb_patterns = 25;     //-P
			pattern_size = 100;   //-S
			nb_trans = 50000;     // -t
			nb_att = 25;          //-a
			att_domain_size = 1000;  // -c
			iterMax = 1000000;
			minSup = 20; 
			evalMeasure = JACCARD; // RECALL   F-MEASURE JACCARD
			addFrequentPatterns = true;
		}
		
	}


	public static void generateExpData() throws IOException
	{
		String df;
		setDefaultParameters();
		String content = "";

	/*
		System.err.println("noise");
		int[] sups = new int[]{100, 50, 10}; //100, 50, 25, 10, 5, 1};
		for (int s = 0; s < sups.length; s++) {
			content = "";
			for (noise_rate = 0; noise_rate <= 1; noise_rate += .1) 
			{
				while ( (df= generate()).equals("-1"));
				System.out.print(noise_rate + " ");
				content += (noise_rate + " ");
				for (int nbiter = 1; nbiter <= iterMax; nbiter *= 10)   
				{
					double recall = 0;
					int nb = 0;
					for (int i = 0; i < REPLICATES; i++) {
						double exitCode =lauchMCTS4DM(df,nbiter, sups[s]);
						if (exitCode != -1)
						{
							recall+=exitCode;
							nb++;
						}
					}
					recall = recall / (double) nb;
					content += recall+" ";
					System.out.print(recall + " ");
					if (recall==1) break;

				}
				System.out.println(" ");
				content += "\n";
			}
			Files.write(Paths.get("graphs"+ XP  +"/data"+ sups[s] +".txt"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		}
		*/
		
		int[] iters = new int[]{1, 10, 100, 1000, 5000, 7500, 10000, 50000, 75000, 100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000}; //100, 50, 25, 10, 5, 1};
		
		/*
		System.err.println("out factor");
		setDefaultParameters();
		content = "";
		for (out_factor = 0; out_factor <= 1; out_factor += 0.1)
		{
			while ( (df= generate()).equals("-1"));
			System.out.print(out_factor + " ");
			content += (out_factor + " ");
			for (nbIter = 1; nbIter < iters.length; nbIter++)   
			{
				double recall = 0;
				int nb = 0;
				for (int i = 0; i < REPLICATES; i++) {
					double exitCode =lauchMCTS4DM(df, iters[nbIter], minSup);
					if (exitCode != -1)
					{
						recall+=exitCode;
						nb++;
					}
				}
				recall = recall / (double) nb;
				content += recall+" ";
				System.out.print(recall + " ");
				if (recall==1) break;
			}
			System.out.println(" ");
			content += "\n";
		}
		Files.write(Paths.get("graphs" + XP  +"/out-fact-data.txt"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	

		System.out.println("nbPatterns");
		setDefaultParameters();
		content = "";
		for (nb_patterns = 1; nb_patterns <= 51; nb_patterns+=5)
		{
			while ( (df= generate()).equals("-1")) {
				//att_domain_size+=5;
			}
			System.out.print(nb_patterns+ " ");
			content += (nb_patterns + " ");
			for (nbIter = 1; nbIter < iters.length; nbIter++)   
			{
				double recall = 0;
				int nb = 0;
				for (int i = 0; i < REPLICATES; i++) {
					double exitCode =lauchMCTS4DM(df, iters[nbIter], minSup);
					if (exitCode != -1)
					{
						recall+=exitCode;
						nb++;
					}
				}
				recall = recall / (double) nb;
				content += recall+" ";
				System.out.print(recall + " ");
				if (recall==1) break;
			}
			System.out.println(" ");
			System.out.flush();
			content += "\n";
		}
		Files.write(Paths.get("graphs" + XP  +"/nb-patterns-data.txt"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);

*/

		System.out.println("patternsize");
		setDefaultParameters();
		content = "";
		for (pattern_size = 20; pattern_size <= 200 ; pattern_size+=20) 
		{
			while ( (df= generate()).equals("-1")) {
				//nb_trans += 1000;
				//nb_att++;
				//att_domain_size+=10;
			}
			System.out.print(pattern_size+ " ");
			content += (pattern_size + " ");
			//minSup=pattern_size;
			for (nbIter = 1; nbIter < iters.length; nbIter++)   
			{
				double recall = 0;
				int nb = 0;
				for (int i = 0; i < REPLICATES; i++) {
					double exitCode =lauchMCTS4DM(df, iters[nbIter], minSup);
					if (exitCode != -1)
					{
						recall+=exitCode;
						nb++;
					}
				}
				recall = recall / (double) nb;
				content += recall+" ";
				System.out.print(recall + " ");
				if (recall==1) break;
			}
			System.out.println(" ");
			System.out.flush();
			content += "\n";
		}
		Files.write(Paths.get("graphs" + XP  +"/pattern-size-data.txt"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		

		/*System.out.println("nbtrans");
		setDefaultParameters();
		content = "";
		//addFrequentPatterns = true;
		for (nb_trans = 1000; nb_trans <= 50000; nb_trans+=5000) 
		{
			while ( (df= generate()).equals("-1"));
			System.out.print(nb_trans+ " ");
			content += (nb_trans + " ");
			for (nbIter = 1; nbIter <= iterMax; nbIter*=10)   
			{
				double recall = 0;
				int nb = 0;
				for (int i = 0; i < REPLICATES; i++) {
					double exitCode =lauchMCTS4DM(df,nbIter, minSup);
					if (exitCode != -1)
					{
						recall+=exitCode;
						nb++;
					}
				}
				recall = recall / (double) nb;
				content += recall+" ";
				System.out.print(recall + " ");
				//if (recall==1) break;
			}
			System.out.println(" ");
			System.out.flush();
			content += "\n";
		}
		Files.write(Paths.get("graphs" + XP  +"/nbtrans-data.txt"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
 */

		/*System.out.println("nbattr");
		setDefaultParameters();
		content = "";
		for (nb_att = 5; nb_att <= 50; nb_att+=15)
		{
			while ( (df= generate()).equals("-1"));
			System.out.print(nb_att+ " ");
			content += (nb_att + " ");
			for (nbIter = 1; nbIter <= iterMax; nbIter*=10)   
			{
				double recall = 0;
				int nb = 0;
				for (int i = 0; i < REPLICATES; i++) {
					double exitCode =lauchMCTS4DM(df,nbIter, minSup);
					if (exitCode != -1)
					{
						recall+=exitCode;
						nb++;
					}
				}
				recall = recall / (double) nb;
				content += recall+" ";
				System.out.print(recall + " ");
				//if (recall==1) break;
			}
			System.out.println(" ");
			System.out.flush();
			content += "\n";
		}
		Files.write(Paths.get("graphs" + XP  +"/nb_att-data.txt"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
*/


/*
		System.out.println("attdomain");
		setDefaultParameters();
		content = "";
		for (att_domain_size =10; att_domain_size <= 110; att_domain_size+=20) 
		{
			while ( (df= generate()).equals("-1"));
			System.out.print(att_domain_size+ " ");
			content += (att_domain_size + " ");
			for (nbIter = 1; nbIter <= iterMax; nbIter*=10)   
			{
				double recall = 0;
				int nb = 0;
				for (int i = 0; i < REPLICATES; i++) {
					double exitCode =lauchMCTS4DM(df,nbIter, minSup);
					if (exitCode != -1)
					{
						recall+=exitCode;
						nb++;
					}
				}
				recall = recall / (double) nb;
				content += recall+" ";
				System.out.print(recall + " ");
				//if (recall==1) break;
			}
			System.out.println(" ");
			System.out.flush();
			content += "\n";
		}
		Files.write(Paths.get("graphs" + XP  +"/attdomain-data.txt"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
*/
	}

	public static double lauchMCTS4DM(String filename, int nbIter, int minSup) throws IOException
	{
		String filename1 = filename + "/descriptors.csv";
		String filename2 = filename + "/targets.csv";

		String content = ""; 
		List<String> l = Files.readAllLines(Paths.get("debugTest.conf"));
		for (String s: l) content += s + "\n";

		content = content.replace("#{1}", filename1);
		content = content.replace("#{2}", filename2);
		content = content.replace("#{3}", "artificial/" + filename.replaceAll("./data/", ""));
		content = content.replace("#{4}", nbIter + "");
		content = content.replace("#{5}", minSup + "");
		Files.write(Paths.get("current.conf"), content.getBytes(),  StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		try {
			executeCommandLine("java -Xms16g -Xmx16g -jar MCTS4SD.jar current.conf", DataGen.TIMEOUT); // 3min
			double recall = compare(filename);
			return recall;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}


	public static double compare(String prefix) throws IOException
	{
		/*** result found **/
		File file = new File("./results/artificial/"+ prefix.replaceAll("./data/", ""));
		File[] fold = file.listFiles();
		String prefixResult = fold[fold.length-1].getAbsolutePath();
		List<String> l = Files.readAllLines(Paths.get(prefixResult+"/supportE11.log"));
		List<BitSet> patternsFound = new ArrayList<BitSet>();
		for (int i = 0; i < l.size(); i++) patternsFound.add(list2bitset(l.get(i)));

		/** ground truth **/
		l = Files.readAllLines(Paths.get(prefix+"/patterns.txt"));
		List<BitSet> patternsHidden = new ArrayList<BitSet>();
		for (int i = 1; i < l.size(); i+=2) patternsHidden.add(list2bitset(l.get(i)));

		List<Double> scores = new ArrayList<Double>();
		for (int i = 0; i < patternsHidden.size(); i++) {
			double bestXScore = 0;
			for (int j = 0; j < patternsFound.size(); j++) {
				double x = jaccard(patternsHidden.get(i), patternsFound.get(j));
				if (x>bestXScore) bestXScore = x;
			}
			scores.add(bestXScore);
		}
		return mean(scores);
	}

	private static double mean(List<Double> l) {
		double mean = 0;
		for (double d: l) mean +=d;
		return mean/(double)l.size();
	}

	/**
	 * 
	 * @param a hidden pattern
	 * @param b found pattern
	 * @return
	 */
	private static double jaccard(BitSet a, BitSet b) {
		BitSet c = (BitSet) a.clone();
		c.and(b);
		BitSet d = (BitSet) a.clone();
		d.or(b);

		double precision = ((double)c.cardinality()) / ((double)b.cardinality());
		double recall    = ((double)c.cardinality()) / ((double)a.cardinality());
		double fmeasure  = 2 * ((precision * recall) / (precision + recall));
		if (precision + recall == 0) fmeasure = 0;
		double jaccard   = ((double)c.cardinality()) / ((double)d.cardinality()); 

		if (evalMeasure == PRECISION )
			return precision;
		else if (evalMeasure == RECALL)
			return recall;
		else if (evalMeasure == FMEASURE)
			return fmeasure;
		else
			return jaccard;

	}

	public static BitSet list2bitset(String s)
	{
		BitSet result = new BitSet();
		String[] a = s.split(" ");
		for (int i = 0; i < a.length; i++) 
			if (!a[i].equals("")) 
				result.set(Integer.parseInt(a[i]));
		return result;
	}

	public static void executeCommandLine(final String commandLine, final int timeout) throws IOException {
		Process process = Runtime.getRuntime().exec(commandLine);
		ProcessWithTimeOut processWithTimeout = new ProcessWithTimeOut(process);
		int exitCode = processWithTimeout.waitForProcess(timeout);

		if (exitCode == Integer.MIN_VALUE) {
			process.destroy();
		}
	}

	public static boolean hasPattern(String tuple, List<String> generatedPatterns, List<Integer>generatedPos, int skip)
	{
		for (int i = 0; i < generatedPatterns.size(); i++) 
		{
			if (i==skip) continue;
			String pat = generatedPatterns.get(i);
			int pos = generatedPos.get(i);
			String[] patS = pat.split("\t");
			String[] tupleS = tuple.split("\t");
			boolean isIncluded=true;
			for (int j = 0; j < patS.length && isIncluded; j++) 
				if (!patS[j].equals(tupleS[j+pos]))
					isIncluded = false;
			if (isIncluded)		return true;
		}
		return false;
	}

	public static String generate() throws IOException
	{
		boolean print = false;

		int nb_C_outsidePattern = (int) (out_factor * (double) pattern_size );
		if (print) System.out.println(nb_C_outsidePattern +" tuples for C not(e)");

		String context ="";
		String context_bidon="";
		int nb_gen_tuples=0;
		int nb_gen_outside=0;
		int nb_gen_noisy=0;

		String filename="./data/gen"+nb_patterns+"P"+pattern_size+"S"+"D"+noise_rate +"N"+out_factor +"O"+nb_trans +"t"+nb_att +"a"+att_domain_size +"c";
		String filename1 = filename + "/all.csv";
		String filename2 = filename + "/descriptors.csv";
		String filename3 = filename + "/targets.csv";
		String filename4 = filename + "/patterns.txt";

		new File("data").mkdir();
		new File(filename).mkdir();

		BufferedWriter out  = new BufferedWriter(new FileWriter(filename1));
		BufferedWriter out2 = new BufferedWriter(new FileWriter(filename2));
		BufferedWriter out3 = new BufferedWriter(new FileWriter(filename3));
		BufferedWriter out4 = new BufferedWriter(new FileWriter(filename4));


		/*** Headers ***/
		for (int i = 0; i < nb_att; i++) 
		{
			out.write("att"+i+";");
			out2.write("att"+i+   ((i==nb_att-1) ? "\n":"\t") );
		}
		out.write("class\n");
		out3.write("+\t-\n");


		List<String> generatedPatterns = new ArrayList<String>();
		List<Integer> generatedPos = new ArrayList<Integer>();
		List<Integer> generatedLengths = new ArrayList<Integer>();

		/**** Generating tuples : C means covered by the pattern,  e means covered by + ***/
		for (int i = 0; i < nb_patterns ; i++) {


			// Generating a pattern
			if (print) System.out.println("Generates pattern: " + i);
			int position;
			int pattern_length;

			do {
				pattern_length =    (int)   (new Random().nextDouble()* nb_att ); 
				while(pattern_length ==0) pattern_length = (int) (new Random().nextDouble()* nb_att );
				position       = (int) (new Random().nextDouble()*(nb_att-pattern_length)-1);
				context = "";
				for (int j = 0; j < pattern_length; j++) context +=  (int) (new Random().nextDouble() * att_domain_size) +"\t" ; //((i*4)+j)+";";
				//System.out.println("retry c");
			} while(generatedPatterns.contains(context));

			//hasPattern(context, generatedPatterns, generatedPos));


			if (print) System.out.println("pattern length= " + pattern_length + " pattern start position= " + position);
			if (print) System.out.println("\tPiece of context: " + context);
			if (print) System.out.println("\tPiece of context bosc: " + csv2bosc(context, position));


			generatedPatterns.add(context.substring(0, context.length()-1)); // remove last \t
			generatedPos.add(position);
			generatedLengths.add(pattern_length);
		}

		for (int i = 0; i < generatedPatterns.size() ; i++) {
			// Generating tuples covering the pattern that is, (C,e)
			String support = "";
			String contextE = "";

			int position = generatedPos.get(i);
			int pattern_length = generatedLengths.get(i);
			context = generatedPatterns.get(i);
			out4.write(csv2bosc(context, position) + "\n"); // store the true pattern to find

			int block = 0;

			for (int count = 0; count < pattern_size; count++) {

				contextE = "";

				if(new Random().nextDouble() >= noise_rate){ 

					do {
						int p = 0;
						while(p++ < position) {
							int r = (int)(new Random().nextDouble()*att_domain_size);
							contextE += r + "\t";
						}
						contextE += context + "\t";
						p+=pattern_length-1;
						while(p++ < nb_att) {
							int r = (int)(new Random().nextDouble()*att_domain_size);
							contextE += r;
							if (p<nb_att) contextE += "\t";
						}
						if(block++ ==1000) {
							out.close();
							out2.close();
							out3.close();
							out4.close();
							System.err.println("Insatisfaction...");
							return "-1"; // satisfaction problem too hard too solve, lets restart the generation!
						}
						//System.out.println("retry a");
					}while (hasPattern(contextE,generatedPatterns,generatedPos, i));

					if (print) System.out.print(contextE + "\t+\n");
					out.write(contextE + "\t+\n") ;
					out2.write(contextE + "\n") ;
					out3.write("1\t0\n") ;

					support += nb_gen_tuples+1 + " "; 

				}
				else { // introducing noise
					// generation of (not(C),e)


					do {
						context_bidon="";// + (int)(new Random().nextDouble()*att_domain_size) +";";
						for (int k = 0; k < nb_att; k++) {
							context_bidon+= (int)(new Random().nextDouble()*att_domain_size);
							if (k<nb_att-1) context_bidon +="\t";
						}
						//System.out.println("retry b");
					} while (hasPattern(context_bidon,generatedPatterns,generatedPos,-1));


					nb_gen_noisy++;
					if (print) System.out.print(context_bidon + "\t+\n");

					out.write(context_bidon+ "\t+"+"\n");
					out2.write(context_bidon + "\n") ;
					out3.write("1\t0\n") ;
				}
				nb_gen_tuples++;
			}
			// Generating some tuples for (C,not(e))
			for (int j = 0; j < nb_C_outsidePattern; j++) {

				int p = 0;
				contextE="";
				while(p++ < position) {
					int r = (int)(new Random().nextDouble()*att_domain_size);
					contextE += r + "\t";
				}
				contextE += context+"\t";
				p+=pattern_length-1;
				while(p++ < nb_att) {
					int r = (int)(new Random().nextDouble()*att_domain_size);
					contextE += r;
					if (p<nb_att) contextE += "\t";
				}

				if (print) System.out.print(contextE + "\t-\n");

				out.write(contextE+"\t-"+"\n");  // label negative
				out2.write(contextE + "\n") ;
				out3.write("0\t1\n") ;
				//support += nb_gen_tuples + " ";
				nb_gen_outside++;
				nb_gen_tuples++;
			}
			out4.write(support + "\n");
			if (print) System.out.println("\t" + nb_gen_tuples + " tuples (cumulated)" );
			if (print) System.out.println("\t" + nb_gen_noisy + " noisy tuples (cumulated)");
			if (print) System.out.println("\t" + nb_gen_outside + " outside tuples (cumulated)");
		}

		// Adding some other transactions to have a sufficient number
		// we need some frequent pattern too!
		while(nb_gen_tuples  < nb_trans){
			context="";
			do	{
				context="" + (int)(new Random().nextDouble()*att_domain_size) +"\t";
				for (int i = 1; i < nb_att; i++) { 
					context+=(int)(new Random().nextDouble()*(att_domain_size)) ;
					if (i < nb_att-1) context+="\t";
				}
			} while (hasPattern(context,generatedPatterns,generatedPos,-1));



			if (addFrequentPatterns) 
			{
				for (int i = 0; i < pattern_size && nb_gen_tuples  < nb_trans ; i++) {
					String label = (new Random().nextDouble()<=0.5) ? "+" : "-";
					out.write(context+"\t"+label+"\n");
					out2.write(context.replaceAll(";","\t") + "\n") ;
					out3.write( (label.equals("+")?"1\t0":"0\t1") + "\n") ;
					nb_gen_tuples++;
				}
			} else {
				String label = (new Random().nextDouble()<=0.5) ? "+" : "-";
				if (print) System.out.print(context+"\t"+label+"\n");
				out.write(context+"\t"+label+"\n");
				out2.write(context.replaceAll(";","\t") + "\n") ;
				out3.write( (label.equals("+")?"1\t0":"0\t1") + "\n") ;
				nb_gen_tuples++;
			}

		}
		//System.out.println(nb_gen_tuples + " tuples in total" );
		out.close();
		out2.close();
		out3.close();
		out4.close();
		return filename;
	}


	public static String csv2bosc (String csv, int position)
	{
		String transfo="";
		String[] elems = csv.split("\t");
		for (int j = 0; j < elems.length; j++) {
			transfo += "[att" + (j+position) + " = " + elems[j] + "]";
			if (j<elems.length-1) transfo += '\t';
		}
		return transfo;
	}

	public static void main(String[] args) throws IOException {
		/*
		for(int a = 0; a < args.length; a++){
			if(args[a].charAt(0) == '-'){
				if(args[a].charAt(1) == 'P'){ // nb patterns to hide
					a++;
					nb_patterns = Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'S'){
					a++;
					pattern_size= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'N'){
					a++;
					noise_rate= Double.parseDouble(args[a]);
				}else if(args[a].charAt(1) == 'O'){
					a++;
					out_factor= Double.parseDouble(args[a]);
				}else if(args[a].charAt(1) == 't'){
					a++;
					nb_trans= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'a'){
					a++;
					nb_att= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'c'){
					a++;
					att_domain_size= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'h'){
					System.out.println("TransGenerator usage:");
					System.out.println("-------- PATTERN PARAMETERS -------- ");
					System.out.println("\t-P\t Number of patterns to be hidden (default " + nb_patterns  + ")");
					System.out.println("\t-S\t  Number of vertices involved within the pattern (default " +  pattern_size  + ")");
					System.out.println("\t-N\t  Noise rate: probability of a transaction of a patterns to be noisy (default " + noise_rate + ")");
					System.out.println("\t-O\t  Out Factor: percentage of tuples with context C outside the pattern   (default " + out_factor + ")");
					System.out.println("-------- DATA PARAMETERS -------- ");
					System.out.println("\t-t\t Number of transactions  (default " + nb_trans + ")");
					System.out.println("\t-a\t Number of attributes  (default " + nb_att  + ")");
					System.out.println("\t-c\t Cardinality of the attribute domains (default " + att_domain_size + ")");
					System.exit(0);
				}
			}
		}
		if (args.length==0) generateExpData(); 
		else generate();*/

		new File("./graphs1/").mkdir();
		new File("./graphs2/").mkdir();
		new File("./graphs3/").mkdir();
		
		XP=SMALL;
		XP = LARGE_CORTANA;
		generateExpData();
		
		XP=MEDIUM;
		//generateExpData();
		
		XP=LARGE;
		//generateExpData();
	}
}
