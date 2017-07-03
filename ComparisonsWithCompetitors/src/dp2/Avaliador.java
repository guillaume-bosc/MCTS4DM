/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dp2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.lucene.util.OpenBitSet;

/**
 *
 * @author Marianna
 */
public class Avaliador {

	public static final String TIPO_QG = "Qg";
	public static final String TIPO_SUB = "Sub";
	public static final String TIPO_WRACC = "WRAcc";

	// Avaliação individual
	public static double avaliar(int TP, int FP, String tipo) {
		double qualidade = 0.0;

		switch (tipo) {
		case Avaliador.TIPO_QG:
			qualidade = Avaliador.Qg(TP, FP);
			break;
		case Avaliador.TIPO_SUB:
			qualidade = Avaliador.sub(TP, FP);
			break;
		case Avaliador.TIPO_WRACC:
			qualidade = Avaliador.WRAcc(TP, FP);
			break;
		}
		return qualidade;
	}

	private static double WRAcc(int TP, int FP) {
		if (TP == 0) {
			return 0.0;
		}
		double sup = (double) (TP + FP) / (double) D.numeroExemplos;
		double conf = (double) TP / (double) (TP + FP);
		double confD = (double) D.numeroExemplosPositivo / (double) D.numeroExemplos;
		double wracc = sup * (conf - confD);

		return wracc;
	}

	private static double Qg(int TP, int FP) {
		double qg = (double) TP / (double) (FP + 1);
		return qg;
	}

	private static double sub(int TP, int FP) {
		double sub = TP - FP;
		return sub;
	}

	private static double chi_quad(int TP, int FP) {
		// Só é preciso isso para calcular via função pronta!
		long[][] n = new long[2][2];
		n[0][0] = TP;
		n[1][0] = D.numeroExemplosPositivo - n[0][0];
		n[0][1] = FP;
		n[1][1] = D.numeroExemplosNegativo - n[0][1];

		ChiSquareTest chiTest = new ChiSquareTest();
		double chi_quad = chiTest.chiSquare(n);
		// System.out.println("Chi_quad: " + chi + "/" + chi_quad);
		// System.out.println("chi_quad: " + chi_quad);

		return chi_quad;
	}

	private static double p_value(int TP, int FP) {
		// Só é preciso isso para calcular via função pronta!
		long[][] n = new long[2][2];
		n[0][0] = TP;
		n[1][0] = D.numeroExemplosPositivo - n[0][0];
		n[0][1] = FP;
		n[1][1] = D.numeroExemplosNegativo - n[0][1];
		ChiSquareTest chiTest = new ChiSquareTest();
		// Returns the observed significance level, or p-value, associated with
		// a chi-square test of independence based on the input counts array,
		// viewed as a two-way table.
		double p_value = chiTest.chiSquareTest(n);
		// System.out.println("pvalue: " + p_value);
		return p_value;
	}

	public static int TP(boolean[] vrP) {
		int TP = 0;
		for (int i = 0; i < vrP.length; i++) {
			if (vrP[i]) {
				TP++;
			}
		}
		return TP;
	}

	public static int FP(boolean[] vrN) {
		int FP = 0;
		for (int i = 0; i < vrN.length; i++) {
			if (vrN[i]) {
				FP++;
			}
		}
		return FP;
	}

	// Vetor resultante
	public static boolean[] vetorResultantePositivo(HashSet<Integer> itens) {
		boolean[] vetorResultantePositivo = new boolean[D.numeroExemplosPositivo];

		for (int i = 0; i < D.numeroExemplosPositivo; i++) {
			vetorResultantePositivo[i] = Avaliador.patternContemplaExemplo(itens, D.Dp[i]);
			if (vetorResultantePositivo[i]) {
				Pattern.vrPCount[i]++;
			}
		}

		return vetorResultantePositivo;
	}

	public static boolean[] vetorResultanteNegativo(HashSet<Integer> itens) {
		boolean[] vetorResultanteNegativo = new boolean[D.numeroExemplosNegativo];

		for (int i = 0; i < D.numeroExemplosNegativo; i++) {
			vetorResultanteNegativo[i] = Avaliador.patternContemplaExemplo(itens, D.Dn[i]);
			if (vetorResultanteNegativo[i]) {
				Pattern.vrNCount[i]++;
			}
		}

		return vetorResultanteNegativo;
	}

	private static boolean patternContemplaExemplo(HashSet<Integer> itens, int[] exemplo) {
		Iterator iterator = itens.iterator();
		while (iterator.hasNext()) {
			int item = (int) iterator.next();
			int itemAtributo = D.itemAtributo[item];
			int itemValor = D.itemValor[item];
			if (exemplo[itemAtributo] != itemValor) {
				return false;
			}
		}
		return true;
	}

	// Valor médio de qualidade
	public static double avaliarMedia(Pattern[] p, int k) {
		double total = 0.0;
		int i = 0;
		for (; i < k; i++) {
			total += p[i].getQualidade();
		}
		return total / (double) i;
	}

	// Valor médio de dimensões
	public static double avaliarMediaDimensoes(Pattern[] p, int k) {
		int total = 0;
		int i = 0;
		for (; i < k; i++) {
			total += p[i].getItens().size();
		}
		return (double) total / (double) i;
	}

	public static double coberturaPositivo(Pattern[] p, int k) {
		double coberturaP = 0.0;
		boolean[] vrpGrupo = new boolean[D.numeroExemplosPositivo];

		for (int i = 0; i < k; i++) {
			boolean[] vrpItem = p[i].getVrP();
			for (int j = 0; j < vrpItem.length; j++) {
				if (vrpItem[j]) {
					vrpGrupo[j] = true;
				}
			}
		}

		for (int i = 0; i < vrpGrupo.length; i++) {
			if (vrpGrupo[i]) {
				coberturaP = coberturaP + 1;
			}
		}
		// System.out.println("Numero P: " + coberturaP);
		coberturaP = coberturaP / (double) vrpGrupo.length;
		return coberturaP;
	}

	public static void imprimir(Pattern[] p, int kPrimeiros) {
		for (int i = 0; i < kPrimeiros; i++) {
			System.out.println(p[i].toString());
		}
	}

	// Imprime regras em texto
	public static List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> imprimirRegras(Pattern[] p, int kPrimeiros) {

		List<liris.cnrs.fr.dm2l.mcts4dm.Pattern> resultPatternSet = new ArrayList<liris.cnrs.fr.dm2l.mcts4dm.Pattern>();

		for (int i = 0; i < kPrimeiros; i++) {
			// System.out.println(p[i].toString2());
			// System.out.println(p[i].getQualidade());

			OpenBitSet extent = new OpenBitSet();

			for (int j = 0; j < p[i].getVrP().length; j++) {
				if (p[i].getVrP()[j])
					extent.set(j);
			}
			for (int j = 0; j < p[i].getVrN().length; j++) {
				if (p[i].getVrN()[j])
					extent.set(j);
			}
			resultPatternSet.add(new liris.cnrs.fr.dm2l.mcts4dm.Pattern(p[i].getQualidade(), extent));
		}
		return resultPatternSet;
	}

	public static void imprimirDimensaoQuantidade(Pattern[] p, int kPrimeiros, int dDimensoes) {
		int[] dimensaoQuantidade = new int[dDimensoes]; // Até dimensão 10
		for (int i = 0; i < kPrimeiros; i++) {
			int dimensao = p[i].getItens().size();
			dimensaoQuantidade[dimensao]++;
		}

		for (int i = 0; i < dDimensoes; i++) {
			System.out.println(/* "D" + i + ":" + */ dimensaoQuantidade[i]/* + ", " */);
		}

		System.out.println();
	}

	// Compactar dados: foco em tirar redundância
	public static Pattern[] comprimir(Pattern[] pOrdenado) {

		int numeroSinonimos = 0;
		int numeroSub = 0;
		for (int i = 0; i < pOrdenado.length; i++) {
			Pattern p = pOrdenado[i];
			if (p == null) {
				continue;
			}
			for (int j = i + 1; j < pOrdenado.length; j++) {
				// System.out.println("i:" + i + ",j:" + j);
				if (pOrdenado[j] == null) {
					continue;
				}
				int resultadoSobrescreve = p.sobrescreve(pOrdenado[j]);
				if (resultadoSobrescreve == -1) {

				} else if (resultadoSobrescreve == 1) {
					p.addSub(pOrdenado[j]);
					numeroSub++;
					System.out.println("Sub:" + i + "/" + j);
					// System.out.println(p.toString());
					// System.out.println(pOrdenado[j].toString());

					pOrdenado[j] = null;
				} else {
					p.addSinonimo(pOrdenado[j]);
					numeroSinonimos++;

					System.out.println("Sin:" + i + "/" + j);
					System.out.println(p.toString());
					System.out.println(pOrdenado[j].toString());
					pOrdenado[j] = null;
				}
			}
		}

		Pattern[] pComprimido = new Pattern[pOrdenado.length - numeroSinonimos - numeroSub];
		int indice = 0;
		for (int i = 0; i < pOrdenado.length; i++) {
			if (pOrdenado[i] != null) {
				pComprimido[indice++] = pOrdenado[i];
			}
		}
		System.out.println("\n\nSinonimos: " + numeroSinonimos + ", Sub: " + numeroSub);
		return pComprimido;
	}

}
