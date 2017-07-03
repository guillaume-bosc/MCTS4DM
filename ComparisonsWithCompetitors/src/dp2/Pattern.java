/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dp2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Tarcísio Pontes
 * @since 27/01/2016
 * @version 1.0
 * OK revisão!
 */
public class Pattern implements Comparable<Pattern>, Serializable{
    private HashSet<Integer> itens;
    
    private String tipoAvaliacao;
    private boolean[] vrP;
    private boolean[] vrN;
    private int TP;
    private int FP;
    private double qualidade;
    
    //Atributos para dar mais opções ao cliente
    private ArrayList<Pattern> sinonimos;
    private ArrayList<Pattern> subPatterns;
        
    //Atributos estáticos
    public static int numeroIndividuosGerados = 0;
    public static int[] vrPCount;
    public static int[] vrNCount;
    
    public Pattern(HashSet<Integer> itens, String tipoAvaliacao){
        this.itens = itens;
        this.tipoAvaliacao = tipoAvaliacao;            
        this.vrP = Avaliador.vetorResultantePositivo(itens); //Saber se somina ou é dominado. Isso ajuda!
        this.vrN = Avaliador.vetorResultanteNegativo(itens); //Saber se somina ou é dominado. Isso ajuda!
        this.TP = Avaliador.TP(this.vrP);
        this.FP = Avaliador.FP(this.vrN);
        this.qualidade = Avaliador.avaliar(this.TP, this.FP, this.tipoAvaliacao);
        this.sinonimos = new ArrayList<>();
        this.subPatterns = new ArrayList<>();
        Pattern.numeroIndividuosGerados++;
    }

      
    public HashSet<Integer> getItens() {
        return itens;
    }

    public String getTipoAvaliacao() {
        return tipoAvaliacao;
    }

    public boolean[] getVrP() {
        return vrP;
    }

    public boolean[] getVrN() {
        return vrN;
    }

    public int getTP() {
        return TP;
    }

    public int getFP() {
        return FP;
    }

    public double getQualidade() {
        return qualidade;
    }

    public ArrayList<Pattern> getSinonimos() {
        return sinonimos;
    }

    public ArrayList<Pattern> getSubPatterns() {
        return subPatterns;
    }

    public void addSinonimo(Pattern sinonimo){
        this.sinonimos.add(sinonimo);
    }
    
    public void addSub(Pattern subPattern){
        this.subPatterns.add(subPattern);
    }
    
    /**
     * Retorna se indivíduo this sobrescreve o passado como parâmetro
     * Para um pattern sobrescrever outro é necessário possuir:
     * (1)todos os exemplos positivos do patternParâmetro e 
     * (2) não possuir nenhum dos exemplos negativos ausentes do patternParâmetros. 
     *@author Tarcísio Pontes
     * @param Pattern p
     * @return int (-1): não sobrescreve, (1): sovrescreve, (0): são sinônimos.
     * @since 27/01/2016
     * @version 1.0
     */
    public int sobrescreve(Pattern p){
       if(this.sobrescreveP(p) && this.sobrescreveN(p)){
           if(this.equivalente(p)){
                 return 0;
           }else{
               return 1;
           }
       }
       else{
           return -1;
       }
    }
    
    /**
     * Retorna se indivíduo this sobrescreve o passado como parâmetro em relação
     * aos exemplos positivos.
     * Lógica: Se meu vetorResultantePositivo interno contém todos os exemplos 
     * do vetorResultantePositivo passado como parâmetro, significa que ele
     * está contido dentro de mim e portanto, para alguns casos, ele é irrelevante.
     *@author Tarcísio Pontes
     * @param Pattern p
     * @return boolean se this sobrescreve em relação a exemplos positivos
     * @since 27/01/2016
     * @version 1.0
     */
    private boolean sobrescreveP(Pattern p){
        boolean[] vrPParametro = p.getVrP();
        for(int i = 0; i < vrPParametro.length; i++){
            if(vrPParametro[i] == true && this.vrP[i]!=true){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Retorna se indivíduo this sobrescreve o passado como parâmetro em relação
     * aos exemplos negativos.
     * Lógica: Se para todo exemplo negativo, sempre que o vrN=false 
     * this.vrs=false significa que eu sou um superset do vetor passado como
     * parâmetro, logo, o sobrescrevo sem prejuízo.     * 
     *@author Tarcísio Pontes
     * @param Pattern p
     * @return boolean se this sobrescreve em relação a exemplos negativos
     * @since 27/01/2016
     * @version 1.0
     */
    private boolean sobrescreveN(Pattern p){
        boolean[] vrNParametro = p.getVrN();
        for(int i = 0; i < vrNParametro.length; i++){
            if(vrNParametro[i] == false && this.vrN[i]!=false){
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * Retorna se indivíduo this cobre exatamente os mesmos exemplos positivos
     * e negativos do PAtterns passado como parâmetro
     * Lógica: Se tiverem os mesmos vetores resultantes Positivo e Negativo
     *@author Tarcísio Pontes
     * @param Pattern p
     * @return boolean se são equivalentes
     * @since 27/01/2016
     * @version 1.0
     */
    private boolean equivalente(Pattern p){
        boolean[] vrPParametro = p.vrP;
        boolean[] vrNParametro = p.vrN;
        
        for(int i = 0; i < vrPParametro.length; i++){
            if(vrPParametro[i] != this.vrP[i]){
                return false;
            }
        }
        
        for(int i = 0; i < vrNParametro.length; i++){
           if(vrNParametro[i] != this.vrN[i]){
               return false;
           } 
        }
        return true;
    }
    
    /**
     * Retonra de Pattern p é maior, igual ou menor que this considerando o
     * atributo qualidade.
     * Lógica: (1): se this maior que p, (0) se iguais e (-1) se this menos que p (confirmar!)
     * Utilizaod pela função Arrays.sort(Pattern[])
     *@author Tarcísio Pontes
     * @param Pattern p
     * @return int
     * @since 27/01/2016
     * @version 1.0
     */
    @Override
    public int compareTo(Pattern p) {
        double compareQuantity = p.getQualidade();
        double sub = compareQuantity - this.qualidade;
        
        if(sub > 0){
            return 1;
        }else if(sub == 0){
            return 0;
        }else{
            return -1;
        }        
    }
    
    //@Override
//    public int compareTo2(Pattern p) {//Esse cara é maior que eu???
//        double sub1 = p.getQualidade() - this.qualidade;
//        
//         
//        if(sub1 > 0){
//            return 1;
//        }else if(sub1 < 0){
//            return -1;
//        }else{//Desempate com tamanho!
//            double sub2 = this.itens.size() - p.getItens().size(); //Quanto menor melhor!
//            if(sub2 > 0){
//                return 1;
//            }else if(sub2 < 0){
//                return -1;
//            }else{
//                int totalItensP = 0;
//                Iterator ip = p.getItens().iterator();
//                while(ip.hasNext()){
//                    totalItensP+= (int)ip.next();
//                }            
//
//                int totalItensThis = 0;
//                Iterator iThis = this.itens.iterator();
//                while(iThis.hasNext()){
//                    totalItensThis+= (int)iThis.next();
//                }
//            
//                double sub3 = totalItensP - totalItensThis;
//                
//                if(sub3 > 0){
//                    return 1;
//                }else if(sub3 < 0){
//                    return -1;
//                }else{
//                    return 0;
//                }        
//            }        
//        }            
//    }
    
//    @Override
//    public int compareTo(Pattern p) {
//        double compareQuantity = p.TP-p.FP;
//        double sub = compareQuantity - (this.TP - this.FP);
//        
//        if(sub > 0){
//            return 1;
//        }else if(sub == 0){
//            return 0;
//        }else{
//            return -1;
//        }        
//    }
    
    @Override
    public String toString() {
        //Capturando e ordenando conteúdo
        Iterator iterator = itens.iterator();
        int[] itensArray = new int[itens.size()];
        int indice = 0;
        while(iterator.hasNext()){
            itensArray[indice++] = (int)iterator.next();
        }           
        Arrays.sort(itensArray);

        //Salvando em string
        StringBuilder str = new StringBuilder("{");
        int j = 0;
        for(; j < itensArray.length-1; j++){
            str.append(itensArray[j]);
            str.append(",");
        }
        str.append(itensArray[j]);
        str.append("} -> ");
        str.append(this.qualidade);
        str.append("(");
        str.append(this.TP);
        str.append("p,");
        str.append(this.FP);
        str.append("n)");       
        
        str.append("\n{");       
        
        for(int i = 0; i < this.vrP.length; i++){
            
            if(this.vrP[i]){
                str.append("1");
            }else{
                str.append("0");
            }
            if(i != this.vrP.length-1){
                str.append(",");
            }else{
                str.append("} ");
            }
        }
        
        str.append("{");    
        for(int i = 0; i < this.vrN.length; i++){
            
            if(this.vrN[i]){
                str.append("1");
            }else{
                str.append("0");
            }
            if(i != this.vrN.length-1){
                str.append(",");
            }else{
                str.append("} ");
            }
        }
        return str.toString();
    }
    
    //Imprime regras em texto.
    public String toString2() {
        //Capturando e ordenando conteúdo
        Iterator iterator = itens.iterator();
        int[] itensArray = new int[itens.size()];
        int indice = 0;
        while(iterator.hasNext()){
            itensArray[indice++] = (int)iterator.next();
        }           
        Arrays.sort(itensArray);

        //Salvando em string
        StringBuilder str = new StringBuilder("{");
        int j = 0;
        for(; j < itensArray.length-1; j++){
            str.append(D.itemAtributoStr[ itensArray[j] ] + " = " + D.itemValorStr[ itensArray[j] ]);
            str.append(",");
        }
        str.append(D.itemAtributoStr[ itensArray[j] ] + " = " + D.itemValorStr[ itensArray[j] ]);
        
        str.append("}");
      /**  str.append("} -> ");
        str.append(this.qualidade);
        str.append("(");
        str.append(this.TP);
        str.append("p,");
        str.append(this.FP);
        str.append("n)");   **/ // comment mehdi    
        
//        str.append("\n{");       
//        x
//        for(int i = 0; i < this.vrP.length; i++){
//            
//            if(this.vrP[i]){
//                str.append("1");
//            }else{
//                str.append("0");
//            }
//            if(i != this.vrP.length-1){
//                str.append(",");
//            }else{
//                str.append("} ");
//            }
//        }
//        
//        str.append("{");    
//        for(int i = 0; i < this.vrN.length; i++){
//            
//            if(this.vrN[i]){
//                str.append("1");
//            }else{
//                str.append("0");
//            }
//            if(i != this.vrN.length-1){
//                str.append(",");
//            }else{
//                str.append("} ");
//            }
//        }
        return str.toString();
    }

}
