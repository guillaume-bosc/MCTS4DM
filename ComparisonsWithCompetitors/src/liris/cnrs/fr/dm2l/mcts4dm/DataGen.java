package liris.cnrs.fr.dm2l.mcts4dm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.lucene.util.OpenBitSet;

import weka.core.PropertyPath.Path;
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

	static double noise_rate;   // =0.1;
	static double out_factor;   // = 0.1;
	static int nb_patterns;     // = 5;
	static int pattern_size;    // = 200;
	static int nb_trans;        // = 50000;
	static int nb_att;          // = 5;
	static int att_domain_size; // = 10;
	public static boolean addFrequentPatterns;

	public static double compare(List<Pattern> foundPatterns, String base) throws IOException
	{
		/** ground truth **/
		List<String> l = Files.readAllLines(Paths.get("./data/" + base +"/patterns.txt"));
		List<OpenBitSet> patternsHidden = new ArrayList<OpenBitSet>();
		for (int i = 1; i < l.size(); i+=2) patternsHidden.add(list2bitset(l.get(i)));

		List<Double> scores = new ArrayList<Double>();
		for (int i = 0; i < patternsHidden.size(); i++) {
			double bestXScore = 0;
			for (int j = 0; j < foundPatterns.size(); j++) {
				double x = jaccard(patternsHidden.get(i), foundPatterns.get(j).extent);
				if (x>bestXScore) bestXScore = x;
			}
			scores.add(bestXScore);
		}
		System.out.println(scores);
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
	private static double jaccard(OpenBitSet a, OpenBitSet b) {
		return ((double) OpenBitSet.intersectionCount(a, b)  / (double) OpenBitSet.unionCount(a, b) );
	}

	public static OpenBitSet list2bitset(String s)
	{
		OpenBitSet result = new OpenBitSet();
		String[] a = s.split(" ");
		for (int i = 0; i < a.length; i++) 
			if (!a[i].equals("")) 
				result.set(Integer.parseInt(a[i]));
		return result;
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
		String base = "gen"+nb_patterns+"P"+pattern_size+"S"+"D"+noise_rate +"N"+out_factor +"O"+nb_trans +"t"+nb_att +"a"+att_domain_size +"c";
		String filename1 = filename + "/all.csv";
		String filename2 = filename + "/properties.csv";
		String filename3 = filename + "/qualities.csv";
		String filename4 = filename + "/patterns.txt";

		new File("data").mkdir();
		if (Files.exists(Paths.get(filename))) {
			System.out.println("Data already exists !");
			return base;
		} else {
			System.out.println("Creating new arficial data...");
		}
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

					support += nb_gen_tuples + " "; 

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
				support += nb_gen_tuples + " ";
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
		return base;
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

}
