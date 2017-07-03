package nl.liacs.subdisc.cui;

import java.util.*;

public interface CuiMapInterface
{
	// TODO for now
	final static String CUI_DIR = "CUI/";
	final static String CUI2NAME = "CUI/conceptID2name.txt";
	final static String GENE_IDENTIFIER_CUIS = "CUI/gene_identifier_cuis.txt";
	final static String ENTREZ2CUI = "CUI/entrezGeneToConceptID.txt";
	final static String GO2CUI = "CUI/GO2conceptID.txt";
//	final static String ENSEMBL2CUI = "CUI/ensembl2cui.txt";
	final static String DOMAIN_FILE_PREFIX = "expr2";

	final static int NR_DOMAINS = 28;
	final static int NR_CUI = 314519;
	final static int NR_EXPRESSION_CUI = 23218;	// many entrez/go map to one cui
	final static int NR_ENTREZ_CUI = 64870;
	final static int NR_GO_CUI = 15879;

	public Map<String, ? extends Object> getMap();
}
