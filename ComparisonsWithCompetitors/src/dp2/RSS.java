/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dp2;


/**
 *
 * @author Marianna
 */
public class RSS {
    public static Pattern[] run(Pattern[] p, int k){
        
        //Pesos dos exemplos positivos
        int[] pesos = new int[D.numeroExemplosPositivo];
        
        //Atribuindo pesos iniciais
        for(int i = 0; i < pesos.length; i++){
            pesos[i] = 1;
        }
        
        //PA: revisão!
        Pattern[] S = p.clone();
        Pattern[] SS = new Pattern[k];//Inicializando array com os melhores patterns
        for(int i = 0; i < SS.length; i++){
            //Identificando o maior pontuador
            int indiceMaximoPontuador = RSS.indiceMaiorPontuador(S, pesos);
            //Atribuindo maior pontuador ao array com os k melhores
            SS[i] = p[indiceMaximoPontuador];
            //Atualizando vetor de pesos com base no maior pontuador
            RSS.atualizaPesos(p[indiceMaximoPontuador].getVrP(), pesos);
            //Excluir exemplo com maior pontuação
            S[indiceMaximoPontuador] = null;
        }        
        return SS;
    }
    
    private static double pontuacao(boolean[] vetorResultantePattern, int[] pesos){
        double pontuacao = 0.0;
        
        for(int i = 0; i < vetorResultantePattern.length; i++){
            if(vetorResultantePattern[i]){
                pontuacao += 1.0/(double)pesos[i];
            }
        }
        
        return pontuacao;
    }

    private static int indiceMaiorPontuador(Pattern[] S, int[] pesos){
        double pontuacaoMaxima = 0.0;
        int indiceMaiorPontuador = 0;
        for(int i = 0; i < S.length; i++){
            if(S[i] != null){
                double pontuacao = RSS.pontuacao(S[i].getVrP(), pesos);
                if(pontuacao > pontuacaoMaxima){
                    pontuacaoMaxima = pontuacao;
                    indiceMaiorPontuador = i;
                }
            }
        }
        return indiceMaiorPontuador;
    }
    
    private static void atualizaPesos(boolean[] vetorResultantePositivo, int[] vetorPesosPositivo){
        for(int i = 0; i < vetorPesosPositivo.length; i++){
            if(vetorResultantePositivo[i]){
                vetorPesosPositivo[i]++;
            }
        }
    }
}
