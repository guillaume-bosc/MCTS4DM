/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolucionario;

import dp2.Const;
import dp2.Pattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;


/**
 *
 * @author Tarcísio Pontes
 * @since 27/01/2013
 * @version 1.0 27/01/2013
 */
public class CRUZAMENTO {
        
    /**Reliza cruzamento do tipo AND entre indivíduos de duas populações distintas
     *@author Tarcísio Pontes
     * @param P1 Pattern[] - população 1 
     * @param P2 Pattern[] - população 2
     * @param tipoAvaliacao int - tipo de função de avaliação
     * @return Pattern[] - nova população
     */
    public static Pattern[] ANDduasPopulacoes(Pattern[] P1, Pattern[] P2, String tipoAvaliacao){
        int tamanhoPopulacao = P1.length;       
        Pattern[] Pnovo = new Pattern[tamanhoPopulacao];
        //int[] indicesP1 = SELECAO.proporcao25_75(tamanhoPopulacao);
        //int[] indicesP2 = SELECAO.proporcao25_75(tamanhoPopulacao);
        int[] indicesP1 = SELECAO.torneioBinario(tamanhoPopulacao, P1);
        int[] indicesP2 = SELECAO.torneioBinario(tamanhoPopulacao, P2);
        
        for(int i = 0; i < tamanhoPopulacao; i++){
            Pattern p1 = P1[indicesP1[i]];       
            Pattern p2 = P2[indicesP2[i]];
            Pnovo[i] = CRUZAMENTO.AND(p1, p2, tipoAvaliacao);
        }        
        return Pnovo;
    }
    
    
    
    /**Reliza cruzamento do tipo AND entre dois indivíduos
     *@author Tarcísio Pontes
     * @param p1 Pattern - indivíduo 1 
     * @param p2 Pattern - indivíduo 2
     * @param tipoAvaliacao int - tipo de função de avaliação
     * @return Pattern - novo indivíduo
     * @since 27/01/2016     * 
     */
    public static Pattern AND(Pattern p1, Pattern p2, String tipoAvaliacao){
        HashSet<Integer> novoitens = new HashSet<>();
        novoitens.addAll(p1.getItens());
        novoitens.addAll(p2.getItens());
        
        return new Pattern(novoitens, tipoAvaliacao);
    }
    
       
    
    /**Reliza cruzamento do uniforme, gerando 2 indivíduos e AND gernado mais um
     *@author Tarcísio Pontes
     * @param p1 Pattern[] - indivíduo 1 
     * @param p2 Pattern[] - indivíduo 2 
     * @param tipoAvaliacao int - tipo de função de avaliação
     * @return Pattern[] - novos indivíduos
     */
    public static Pattern[] uniforme2AND(Pattern p1, Pattern p2, String tipoAvaliacao){
        Pattern[] novosPattern = new Pattern[3];
        HashSet<Integer> novoitens1 = new HashSet<>();
        HashSet<Integer> novoitens2 = new HashSet<>();
        HashSet<Integer> novoitens3 = new HashSet<>();
        
        novoitens3.addAll(p1.getItens());
        novoitens3.addAll(p2.getItens());
        
        Iterator iterator = novoitens3.iterator();
        while(iterator.hasNext()){
            if(Const.random.nextBoolean()){
                novoitens1.add((Integer)iterator.next());
            }else{          
                novoitens2.add((Integer)iterator.next());
            }
        }
               
        novosPattern[0] = new Pattern(novoitens1, tipoAvaliacao);
        novosPattern[1] = new Pattern(novoitens2, tipoAvaliacao);
        novosPattern[2] = new Pattern(novoitens3, tipoAvaliacao);
        return novosPattern;           
    }
    
    /**Cruzamento gera população a partir de cruzamentos do tipo uniforme e de mutações
     *@author Tarcísio Pontes
     * @param P Pattern[] - população antiga 
     * @param taxaMutacao double - taxa de indivúduos que terão um gene modificado
     * @param tipoAvaliacao int - tipo de função de avaliação utilizada
     * @return Pattern[] - nova população
     */
    public static Pattern[] uniforme2Pop(Pattern[] P, double taxaMutacao, String tipoAvaliacao){
        int tamanhoPopulacao = P.length;
        Pattern[] Pnovo = new Pattern[tamanhoPopulacao];
        
        //int[] selecao = SELECAO.proporcao25_75(tamanhoPopulacao);
        int[] selecao = SELECAO.torneioBinario(tamanhoPopulacao, P);           
        
        int indiceSelecao = 0;
        int indicePnovo = 0;
        while(indicePnovo < Pnovo.length-1){//Cuidado para não acessar índices maiores que o tamanho do array                
            if(Const.random.nextDouble() > taxaMutacao){                    
                Pattern[] novos = CRUZAMENTO.uniforme2Individuos(P[selecao[indiceSelecao]], P[selecao[indiceSelecao+1]], tipoAvaliacao);
                indiceSelecao += 2;
                Pnovo[indicePnovo++] = novos[0];                    
                if(indicePnovo < Pnovo.length){
                    Pnovo[indicePnovo++] = novos[1];                                                        
                }
                
            }else{
                Pnovo[indicePnovo++] = MUTACAO.unGeneTrocaOuAdicionaOuExclui(P[selecao[indiceSelecao++]], tipoAvaliacao);                                                       
            }         
        
        //Imprimir itens nos idivíduos gerados via cruzamento
//        DPinfo.imprimirItens(P[selecao[indiceSelecao-2]]);
//        DPinfo.imprimirItens(P[selecao[indiceSelecao-1]]);
//        System.out.print("->");
//        DPinfo.imprimirItens(Pnovo[indicePnovo-2]);
//        DPinfo.imprimirItens(Pnovo[indicePnovo-1]);
//        System.out.println();
        }
        
        if(indicePnovo < Pnovo.length){
            Pnovo[indicePnovo] = MUTACAO.unGeneTrocaOuAdicionaOuExclui(P[selecao[indiceSelecao++]], tipoAvaliacao);                                                                   
        }
                     
        return Pnovo;
        
    }
    
    
    
    /**Cruzamento gera dois indivíduos a partir do método uniforme
     *@author Tarcísio Pontes
     * @param p1 Pattern[] - indivíduo 1 
     * @param p2 Pattern[] - indivíduo 2
     * @param tipoAvaliacao int - tipo de função de avaliação utilizada
     * @return Pattern[] - vetor com dois novos indivíduos
     */
    public static Pattern[] uniforme2Individuos(Pattern p1, Pattern p2, String tipoAvaliacao){
        Pattern[] novosPattern = new Pattern[2];
        HashSet<Integer> novoItens1 = new HashSet<>();
        HashSet<Integer> novoItens2 = new HashSet<>();

        Iterator iterator = p1.getItens().iterator();
        while(iterator.hasNext()){
            if(Const.random.nextBoolean()){
                novoItens1.add((Integer)iterator.next());
            }else{          
                novoItens2.add((Integer)iterator.next());
            }
        }
        iterator = p2.getItens().iterator();
        while(iterator.hasNext()){
            if(Const.random.nextBoolean()){
                novoItens1.add((Integer)iterator.next());
            }else{          
                novoItens2.add((Integer)iterator.next());
            }
        }
        novosPattern[0] = new Pattern(novoItens1, tipoAvaliacao);
        novosPattern[1] = new Pattern(novoItens2, tipoAvaliacao);
        return novosPattern;           
    }

    /**Cruzamento gera um indivíduo a partir do método uniforme
     *@author Tarcísio Pontes
     * @param p1 Pattern - indivíduo 1 
     * @param p2 Pattern - indivíduo 2
     * @param tipoAvaliacao int - tipo de função de avaliação utilizada
     * @return Pattern - novo indivíduo
     */
    public static Pattern uniforme1(Pattern p1, Pattern p2, String tipoAvaliacao){

        HashSet<Integer> itens = new HashSet<>();
        itens.addAll(p1.getItens());
        itens.addAll(p2.getItens());

        HashSet<Integer> novoItens = new HashSet<>();                  

        while(novoItens.size() == 0){
            Iterator iterator = itens.iterator();
            while(iterator.hasNext()){
                if(Const.random.nextBoolean()){
                    novoItens.add((Integer)iterator.next());
                }
            }
        }     

        return new Pattern(novoItens, tipoAvaliacao);
    }
     
    
    /**Dois indivíduos de tamanho d geram outros dois do mesmo tamanho d
     * pelo método uniforme
     *@author Tarcísio Pontes
     * @param p1 Pattern[] - indivíduo 1 
     * @param p2 Pattern[] - indivíduo 2
     * @param tipoAvaliacao int - tipo de função de avaliação utilizada
     * @return Pattern[] - novos indivíduos
     */
    public static Pattern[] uniforme2D(Pattern p1, Pattern p2, String tipoAvaliacao){
        Pattern[] p = new Pattern[2];
        int d = p1.getItens().size();
        ArrayList<Integer> itensTodos = new ArrayList<>();
        itensTodos.addAll(p1.getItens());
        itensTodos.addAll(p2.getItens());
        
        HashSet<Integer> itens = new HashSet<>();
        while(itens.size() < d){
            itens.add(itensTodos.get(Const.random.nextInt(itensTodos.size())));
        }
        p[0] = new Pattern(itens, tipoAvaliacao);
        
        itens = new HashSet<>();
        while(itens.size() < d){
            itens.add(itensTodos.get(Const.random.nextInt(itensTodos.size())));
        }
        p[1] = new Pattern(itens, tipoAvaliacao);       
        
        return p;
    }
    
    
    /**Cruzamento gera população a partir de cruzamentos do tipo uniforme2D 
     * e mutações
     *@author Tarcísio Pontes
     * @param P Pattern[] - população antiga 
     * @param taxaMutacao double - taxa de indivúduos que terão um gene modificado
     * @param tipoAvaliacao int - tipo de função de avaliação utilizada
     * @return Pattern[] - nova população
     */
    public static Pattern[] uniforme2DPop(Pattern[] P, double taxaMutacao, String tipoAvaliacao){
        int tamanhoPopulacao = P.length;        
        Pattern[] Pnovo = new Pattern[tamanhoPopulacao];
               
        int indicePnovo = 0;
        int indiceP1 = 0;
        int indiceP2 = 0;
        while(indicePnovo < Pnovo.length){//Cuidado para não acessar índices maiores que o tamanho do array                     
            if(Const.random.nextDouble() > taxaMutacao){                                    
                //Obtendo índices do indivíduos que serão cruzados
//                if(random.nextDouble() > 0.75){//75% de chanses de ser selecionado um dos 25% mais relevantes
//                    indiceP1 = random.nextInt(P.length*1/4);
//                    indiceP2 = random.nextInt(P.length*1/4);
//                }else{//25% de chanses de ser selecionado totalmente aleatório
//                    indiceP1 = random.nextInt(P.length);
//                    indiceP2 = random.nextInt(P.length);
//                }
                indiceP1 = SELECAO.torneioBinario(P);
                indiceP2 = SELECAO.torneioBinario(P);
                
                Pattern[] novos = CRUZAMENTO.uniforme2D(P[indiceP1], P[indiceP2], tipoAvaliacao);
                Pnovo[indicePnovo++] = novos[0];
                if(indicePnovo < Pnovo.length){
                    Pnovo[indicePnovo++] = novos[1];                    
                }                                                                      
            }else{
//                if(random.nextDouble() > 0.75){//75% de chanses de ser selecionado um dos 25% mais relevantes
//                    indiceP1 = random.nextInt(P.length*1/4);                    
//                }else{//25% de chanses de ser selecionado totalmente aleatório
//                    indiceP1 = random.nextInt(P.length);                    
//                }
                indiceP1 = SELECAO.torneioBinario(P);                    
                Pnovo[indicePnovo++] = MUTACAO.unGeneD(P[indiceP1], tipoAvaliacao);                                                       
            }
        }

        return Pnovo;
    }
    
    
    
    
    //    public static Pattern[] ANDpopVRP(Pattern[] P, int tipoAvaliacao, boolean[] vrP){
//        Pattern[] Pnovo = new Pattern[P.length];        
//        
//        Pattern[][] P1P2 = SELECAO.separarP(P, vrP);
//        
//        Pattern[] P1novo = CRUZAMENTO.ANDpop(P1P2[0], tipoAvaliacao);
//        Pattern[] P2novo = CRUZAMENTO.ANDpop(P1P2[1], tipoAvaliacao);
//        
//        System.arraycopy(P1novo, 0, Pnovo, 0, P1novo.length);
//        System.arraycopy(P2novo, 0, Pnovo, P1novo.length, P2novo.length);
//        return Pnovo;      
//    }
    
//    public static Pattern[] ANDpopVRN(Pattern[] P, int tipoAvaliacao){
//        boolean[] vrN = SELECAO.geraVetorComparacaoN();
//        Pattern[][] P1P2 = SELECAO.separarN(P, vrN);        
//        return CRUZAMENTO.ANDpop2(P1P2[0], P1P2[1], tipoAvaliacao);
//    }    
//    
//    public static Pattern[] ANDpop2(Pattern[] P1, Pattern[] P2, int tipoAvaliacao){
//        int tamanhoPopulacao1 = P1.length;
//        int tamanhoPopulacao2 = P2.length;
//        int tamanhoPopulacao = tamanhoPopulacao1 + tamanhoPopulacao2;
//        Pattern[] Pnovo = new Pattern[tamanhoPopulacao];
//               
//        for(int i = 0; i < tamanhoPopulacao; i++){
//            int indiceP1 = SELECAO.proporcao25_75(tamanhoPopulacao1);
//            int indiceP2 = SELECAO.proporcao25_75(tamanhoPopulacao2);
//            Pattern p1 = P1[indiceP1];       
//            Pattern p2 = P2[indiceP2];
//            Pnovo[i] = CRUZAMENTO.AND(p1, p2, tipoAvaliacao);
//        }        
//        return Pnovo;
//    }    

    ////    //Cruzamento uniforme2AND entre dentro de subgrupos seprados com base do VRP
////    public static Pattern[] uniforme2ANDPopVRP(Pattern[] P, double taxaMutacao, int tipoAvaliacao, boolean[] vrP){
////        Pattern[] Pnovo = new Pattern[P.length];       
////        Pattern[][] P1P2 = SELECAO.separarP(P, vrP);
////        
////        Pattern[] P1novo = CRUZAMENTO.uniforme2ANDPop(P1P2[0], taxaMutacao, tipoAvaliacao);
////        Pattern[] P2novo = CRUZAMENTO.uniforme2ANDPop(P1P2[1], taxaMutacao, tipoAvaliacao);
////        
////        System.arraycopy(P1novo, 0, Pnovo, 0, P1novo.length);
////        System.arraycopy(P2novo, 0, Pnovo, P1novo.length, P2novo.length);
////        return Pnovo;
////    }
    
//    //Cruzamento uniforme2AND entre dentro de subgrupos seprados com base do VRP
//    public static Pattern[] uniforme2ANDPopVRN(Pattern[] P, double taxaMutacao, int tipoAvaliacao, boolean[] vrN){
//        Pattern[][] P1P2 = SELECAO.separarN(P, vrN);                
//        return CRUZAMENTO.uniforme2ANDPop2(P1P2[0], P1P2[1], taxaMutacao, tipoAvaliacao);
//    }
    
    
//    //Cruzamento gerando três indivíduos, dois pelo método uniforme e outro pelo AND
//    public static Pattern[] uniforme2ANDPop(Pattern[] P, double taxaMutacao, int tipoAvaliacao){
//        int tamanhoPopulacao = P.length;
//        Pattern[] Pnovo = new Pattern[tamanhoPopulacao];
//        int[] selecao = SELECAO.proporcao25_75(tamanhoPopulacao, tamanhoPopulacao+2);           
//        
//        int indiceSelecao = 0;
//        int indicePnovo = 0;
//        while(indicePnovo < Pnovo.length-2){//Cuidado para não acessar índices maiores que o tamanho do array                
//            if(random.nextDouble() > taxaMutacao){                    
//                Pattern[] novos = CRUZAMENTO.uniforme2AND(P[selecao[indiceSelecao]], P[selecao[indiceSelecao+1]], tipoAvaliacao);
//                indiceSelecao += 2;
//                Pnovo[indicePnovo++] = novos[0];                    
//                Pnovo[indicePnovo++] = novos[1];                                        
//                Pnovo[indicePnovo++] = novos[2];                                        
//            }else{
//                Pnovo[indicePnovo++] = MUTACAO.unGene(P[selecao[indiceSelecao++]], tipoAvaliacao);                                                       
//            }
//        }
//
//        //Completa os 1 ou 2 indivíduos da população com cruzamentos AND
//        while(indicePnovo < Pnovo.length){
//            //System.out.println("=========== While 2: " + indicePnovo);
//            Pnovo[indicePnovo++] = CRUZAMENTO.AND(P[selecao[indiceSelecao]], P[selecao[indiceSelecao+1]], tipoAvaliacao);
//            indiceSelecao += 2;
//        }
//        return Pnovo;
//    }
    
//    //Cruzamento gerando três indivíduos, dois pelo método uniforme e outro pelo AND
//    public static Pattern[] uniforme2ANDPop2(Pattern[] P1, Pattern[] P2, double taxaMutacao, int tipoAvaliacao){
//        int tamanhoPopulacao1 = P1.length;
//        int tamanhoPopulacao2 = P2.length;
//        int tamanhoPopulacao = tamanhoPopulacao1 + tamanhoPopulacao2;
//        Pattern[] Pnovo = new Pattern[tamanhoPopulacao];
//        
//        int indicePnovo = 0;
//        while(indicePnovo < Pnovo.length-2){//Cuidado para não acessar índices maiores que o tamanho do array                
//            if(random.nextDouble() > taxaMutacao){                    
//                int indiceP1 = SELECAO.proporcao25_75(tamanhoPopulacao1);
//                int indiceP2 = SELECAO.proporcao25_75(tamanhoPopulacao2);
//                Pattern[] novos = CRUZAMENTO.uniforme2AND(P1[indiceP1], P2[indiceP2], tipoAvaliacao);
//                Pnovo[indicePnovo++] = novos[0];                    
//                Pnovo[indicePnovo++] = novos[1];                                        
//                Pnovo[indicePnovo++] = novos[2];                                        
//            }else{
//                if(CRUZAMENTO.random.nextBoolean()){
//                    int indiceP1 = SELECAO.proporcao25_75(tamanhoPopulacao1);
//                    Pnovo[indicePnovo++] = MUTACAO.unGene(P1[indiceP1], tipoAvaliacao);                                                       
//                }else{
//                    int indiceP2 = SELECAO.proporcao25_75(tamanhoPopulacao2);
//                    Pnovo[indicePnovo++] = MUTACAO.unGene(P2[indiceP2], tipoAvaliacao);                                                       
//                }
//                
//            }
//        }
//
//        //Completa os 1 ou 2 indivíduos da população com cruzamentos AND
//        while(indicePnovo < Pnovo.length){
//            //System.out.println("=========== While 2: " + indicePnovo);
//            int indiceP1 = SELECAO.proporcao25_75(tamanhoPopulacao1);
//            int indiceP2 = SELECAO.proporcao25_75(tamanhoPopulacao2);
//            Pnovo[indicePnovo++] = CRUZAMENTO.AND(P1[indiceP1], P2[indiceP2], tipoAvaliacao);
//        }
//        return Pnovo;
//    }

    
}
