/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolucionario;

import dp2.Const;
import dp2.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;


/**
 *
 * @author Tarcísio Pontes
 * @since 27/01/2016
 * @version 1.0
 * Revisão ok!
 */
public class SELECAO {
        
    /**
     * Os 75% primeiros índices são selecionados dentre os 25% melhores,
     * considerando que os indivíduoes estão em ordenados do melhor para o pior.
     * Os 25% restantes são índices aleatórios entre zero e o tamanho da população
     * A quantidade de índies retornado é sempre do mesmo do tamanho da população.
     *@author Tarcísio Pontes
     * @param tamanhoPopulacao
     * @return int[]
     * @since 14/01/2016
     * @version 1.0
     */
    public static int[] proporcao25_75(int tamanhoPopulacao){
        int[] indices = new int[tamanhoPopulacao];
        int i = 0;
        for(; i < tamanhoPopulacao*0.75; i++){
            indices[i] = Const.random.nextInt(tamanhoPopulacao*1/4);
        }
        for(; i < tamanhoPopulacao; i++){
            indices[i] = Const.random.nextInt(tamanhoPopulacao);
        }
        return indices;
    }
    
    /**
     * Retorna índices vencedores em torneios binários (entre dois indivíduos)
     *@author Tarcísio Pontes
     * @param tamanhoPopulacao
     * @param  P população
     * @return int[] - índices vencedores de P
     * @since 27/01/2016
     * @version 1.0
     */
    public static int[] torneioBinario(int tamanhoPopulacao, Pattern[] P){
        int[] indices = new int[tamanhoPopulacao];
        for(int i = 0; i < indices.length; i++){
            int indiceP1 = Const.random.nextInt(P.length);
            int indiceP2 = Const.random.nextInt(P.length);
            if(P[indiceP1].getQualidade() > P[indiceP2].getQualidade()){
                indices[i] = indiceP1;
            }else{
                indices[i] = indiceP2;         
            }
        }
        return indices;
    }
    
    /**
     * Retorna índice vencedor em torneio binário (entre dois indivíduos aleatórios)
     *@author Tarcísio Pontes
     * @param  P população
     * @return int - índice vencedore de P
     * @since 27/01/2016
     * @version 1.0
     */
    public static int torneioBinario(Pattern[] P){
        int indiceP1 = Const.random.nextInt(P.length);
        int indiceP2 = Const.random.nextInt(P.length);
        if(P[indiceP1].getQualidade() > P[indiceP2].getQualidade()){
            return indiceP1;
        }else{
            return indiceP2;                     
        }        
    }
    
    
    /**
     * Recebe duas populações de mesmo tamanho T e retorna os T melhores
     * indivíduos distintos.
     *@author Tarcísio Pontes
     * @param P PAtterns[]
     * @param Pnovo PAtterns[]
     * @return Pattern[]
     * @since 27/01/2016
     * @version 1.0
     */
    public static Pattern[] selecionarMelhoresDistintos(Pattern[] P, Pattern[] Pnovo){
        int tamanhoPopulacao = P.length;
        Pattern[] PAsterisco = new Pattern[tamanhoPopulacao];        
        ArrayList<Pattern> patternAux = new ArrayList<>();
        
        //System.out.println("\tAdicioando P");
        patternAux.addAll(Arrays.asList(P));
        
        //System.out.println("\tAdicioando Pnovos");
        for (Pattern pnovo : Pnovo) {
            if (SELECAO.ehInedito(pnovo, patternAux)) {
                patternAux.add(pnovo);                
            }           
        }
        //int numeroIneditosNovo = patternAux.size() - P.length;
        
        //System.out.println("\tOrdenando...");
        Collections.sort(patternAux);
        //System.out.println("\tCopiando |P| melhores...");
        for(int i = 0; i < PAsterisco.length; i++){
            PAsterisco[i] = patternAux.get(i);
        }        
        //System.out.println("|Pnovo| = " + numeroIneditosNovo);
        return PAsterisco;
    }
    
    
    /**
     * Recebe 03 populações de mesmo tamanho T e retorna os T melhores
     * indivíduos distintos.
     *@author Tarcísio Pontes
     * @param P1 PAtterns[]
     * @param P2 PAtterns[]
     * @param P3 PAtterns[]
     * @return Pattern[]
     * @since 29/01/2016
     * @version 1.0
     */
    public static Pattern[] selecionarMelhoresDistintos(Pattern[] P1, Pattern[] P2, Pattern[] P3){
        int tamanhoPopulacao = P1.length;
        Pattern[] PAsterisco = new Pattern[tamanhoPopulacao];        
        ArrayList<Pattern> patternAux = new ArrayList<>();
        
        //System.out.println("\tAdicioando P");
        patternAux.addAll(Arrays.asList(P1));
        
        //System.out.println("\tAdicioando Pnovos");
        for (Pattern p2 : P2) {
            if (SELECAO.ehInedito(p2, patternAux)) {
                patternAux.add(p2);                
            }           
        }
        
        //System.out.println("\tAdicioando Pnovos");
        for (Pattern p3 : P3) {
            if (SELECAO.ehInedito(p3, patternAux)) {
                patternAux.add(p3);                
            }           
        }
        //int numeroIneditosNovo = patternAux.size() - P.length;
        
        //System.out.println("\tOrdenando...");
        Collections.sort(patternAux);
        //System.out.println("\tCopiando |P| melhores...");
        for(int i = 0; i < PAsterisco.length; i++){
            PAsterisco[i] = patternAux.get(i);
        }        
        //System.out.println("|Pnovo| = " + numeroIneditosNovo);
        return PAsterisco;
    }
       
    /**
     * Recebe duas populações de mesmo tamanho T e retorna os T melhores
     * indivíduos NÃO distintos! Ou seja, não controla se indivíduos são
     * distintos.
     *@author Tarcísio Pontes
     * @param P PAtterns[]
     * @param Pnovo PAtterns[]
     * @return Pattern[]
     * @since 27/01/2016
     * @version 1.0
     */
    public static Pattern[] selecionarMelhores(Pattern[] P, Pattern[] Pnovo){
        int tamanhoPopulacao = P.length;
        Pattern[] PAsterisco = new Pattern[tamanhoPopulacao];        
        Pattern[] PAuxiliar = new Pattern[2*tamanhoPopulacao];        
        System.arraycopy(P, 0, PAuxiliar, 0, P.length);        
        System.arraycopy(Pnovo, 0, PAuxiliar, P.length, Pnovo.length);        
        Arrays.sort(PAuxiliar);                
        System.arraycopy(PAuxiliar, 0, PAsterisco, 0, PAsterisco.length);                
        return PAsterisco;
    }
    
    /**
     * Recebe duas populações de mesmo tamanho T e retorna os T melhores
     * indivíduos NÃO distintos! Ou seja, não controla se indivíduos são
     * distintos.
     *@author Tarcísio Pontes
     * @param P PAtterns[]
     * @param Pnovo PAtterns[]
     * @return Pattern[]
     * @since 27/01/2016
     * @version 1.0
     */
    public static Pattern[] selecionarMelhores(Pattern[] P1, Pattern[] P2, Pattern[] P3){
        int tamanhoPopulacao = P1.length;
        Pattern[] PAsterisco = new Pattern[tamanhoPopulacao];        
        Pattern[] PAuxiliar = new Pattern[3*tamanhoPopulacao];        
        System.arraycopy(P1, 0, PAuxiliar, 0, tamanhoPopulacao);        
        System.arraycopy(P2, 0, PAuxiliar, tamanhoPopulacao, tamanhoPopulacao);
        System.arraycopy(P3, 0, PAuxiliar, tamanhoPopulacao*2, tamanhoPopulacao);        
        Arrays.sort(PAuxiliar);                
        System.arraycopy(PAuxiliar, 0, PAsterisco, 0, PAsterisco.length);                
        return PAsterisco;
    }
    
    /**Atualiza Pk com os indivíduos de melhor qualidade presentes em PAsterísco. 
     * Retorna o número de substituições realizadas em Pk.
     *@author Tarcísio Pontes
     * @param Pk Pattern[] - top-k indivíduos ordenados e distintos
     * @param PAsterisco Pattern[] - população de indivíduos ordenados
     * @return Pattern[] - número de novos indivíduoes inseridos em Pk
     */
    public static int salvandoRelevantes(Pattern[] Pk, Pattern[] PAsterisco){
        int indiceP = 0;
        int novosk10 = 0;
        while(indiceP < PAsterisco.length && (PAsterisco[indiceP].getQualidade() > Pk[Pk.length-1].getQualidade())){
            if(SELECAO.ehRelevante(PAsterisco[indiceP], Pk)){
                Pk[Pk.length-1] = PAsterisco[indiceP];
                Arrays.sort(Pk);                                    
                novosk10++;
            }
            indiceP++;
        }
        return novosk10;
    }

    /**
     * Retorna se um Pattern P é inédito em relação a um Conjunto de patterns.
     * @author Tarcísio Pontes
     * @param p Patteern
     * @param pList ArrayList<Pattern>
     * @return boolean
     * @since 27/01/2016
     * @version 1.0
     */
    private static boolean ehInedito(Pattern p, ArrayList<Pattern> pList){
        
        for(int i = 0; i < pList.size(); i++){
            if(SELECAO.ehIgual(p, pList.get(i))){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Retorna se um Pattern P1 é igual a outro P2.
     * Lembrando com os itens num Patterns nessa implementação não são ordenados.
     * Isso dificutou um pouco a lógica desse método.
     * @author Tarcísio Pontes
     * @param p1 Patteern
     * @param p2 Patteern
     * @return boolean
     * @since 27/01/2016
     * @version 1.0
     */
    private static boolean ehIgual(Pattern p1, Pattern p2){
        if(p1.getQualidade() == p2.getQualidade()){//A maioria dos casos são resolvidos aqui.
            HashSet<Integer> itens1 = p1.getItens();
            HashSet<Integer> itens2 = p2.getItens();
        
            if(itens1.size() != itens2.size()){
                return false;
            }else{
                return itens1.containsAll(itens2);
            }
        }else{
            return false;
        }
        
        
    }
    
    /**Retorna se indivíduos candidato (fitness maior que pior indivíduo de Pk)
     * é dominado por algum indivíduo de Pk.
     * Se for igual a algum indivíduo ou cobrir subparte dos exemplos positivos
     * presentes e negativos ausêntes, ele é considerado dominado.
     *@author Tarcísio Pontes
     * @param p Pattern - indivíduo candidato a substituir o pior indivíduo de Pk.
     * @param Pk Pattern[] - população com os k melhores indivíduos
     * @return boolean - se novo indivíduo é relevante
     */
    public static boolean ehRelevante(Pattern p, Pattern[] Pk){
        for(int i = 0; i  < Pk.length; i++){
            if(Pk[i].sobrescreve(p) != -1){
                return false;
            }
        }
        return true;
    }
    
    /*
    //Separa dados em dois grupos em relação a similaridade com os exemplos Positivos
    public static Pattern[][] separarP(Pattern[] P, boolean[] vrP){
        ArrayList<Pattern> P1_list = new ArrayList<>();
        ArrayList<Pattern> P2_list = new ArrayList<>();
        
        Pattern[][] P1P2 = new Pattern[2][];
        
        for(int i = 0; i < P.length; i++){
            int pontuacaoP = 0;
            int pontuacaoP_ = 0;
            boolean[] vrPItem = P[i].getVrP();
            for(int j = 0; j < vrPItem.length; j++){
                if(vrPItem[j]){
                    if(vrP[j]){
                        pontuacaoP++;
                    }else{
                        pontuacaoP_++;
                    }
                }            
            }
            if(pontuacaoP > pontuacaoP_){
                P1_list.add(P[i]);
            }else{
                P2_list.add(P[i]);
            }
        }
        
        P1P2[0] = new Pattern[P1_list.size()];
        for(int i = 0; i < P1_list.size(); i++){
            P1P2[0][i] = P1_list.get(i);
        }
        
        P1P2[1] = new Pattern[P2_list.size()];
        for(int i = 0; i < P2_list.size(); i++){
            P1P2[1][i] = P2_list.get(i);
        }
        //System.out.println("P1: " + P1P2[0].length + ", P2: " + P1P2[1].length);
        return P1P2;
    }
    
    //Separa dados em dois grupos em relação a similaridade com os exemplos Negativos
    public static Pattern[][] separarN(Pattern[] P, boolean[] vrN){
        ArrayList<Pattern> P1_list = new ArrayList<>();
        ArrayList<Pattern> P2_list = new ArrayList<>();
        
        Pattern[][] P1P2 = new Pattern[2][];
        
        for(int i = 0; i < P.length; i++){
            int pontuacaoP = 0;
            int pontuacaoP_ = 0;
            boolean[] vrNItem = P[i].getVrN();
            for(int j = 0; j < vrNItem.length; j++){
                if(vrNItem[j]){
                    if(vrN[j]){
                        pontuacaoP++;
                    }else{
                        pontuacaoP_++;
                    }
                }            
            }
            if(pontuacaoP > pontuacaoP_){
                P1_list.add(P[i]);
            }else{
                P2_list.add(P[i]);
            }
        }
        
        P1P2[0] = new Pattern[P1_list.size()];
        for(int i = 0; i < P1_list.size(); i++){
            P1P2[0][i] = P1_list.get(i);
        }
        
        P1P2[1] = new Pattern[P2_list.size()];
        for(int i = 0; i < P2_list.size(); i++){
            P1P2[1][i] = P2_list.get(i);
        }
        //System.out.println("P1: " + P1P2[0].length + ", P2: " + P1P2[1].length);
        
        return P1P2;
    }
    
    public static boolean[] geraVetorComparacaoP(){
        boolean[] vrP = new boolean[D.numeroExemplosPositivo];
        Random random = new Random();
        
        for(int i = 0; i < vrP.length; i++){
            vrP[i] = random.nextBoolean();            
        }        
        return vrP;
    }
    
    public static boolean[] geraVetorComparacaoPponderado(){
        int total = 0;
        for(int i = 0; i < Pattern.vrPCount.length; i++){
            total += Pattern.vrPCount[i];
        }
                
        boolean[] vrP = new boolean[D.numeroExemplosPositivo];
        Random random = new Random();
        int metadeTotal = 0;
        while(metadeTotal < total/2){
            int indice = random.nextInt(D.numeroExemplosPositivo);
            if(!vrP[indice]){
                vrP[indice] = true;
                metadeTotal += Pattern.vrPCount[indice];
            }            
        }
        
                
        return vrP;
    }
    public static boolean[] geraVetorComparacaoN(){
        boolean[] vrN = new boolean[D.numeroExemplosNegativo];
        Random random = new Random();
        
        for(int i = 0; i < vrN.length; i++){
            vrN[i] = random.nextBoolean();            
        }        
        return vrN;
    }
    */
}
