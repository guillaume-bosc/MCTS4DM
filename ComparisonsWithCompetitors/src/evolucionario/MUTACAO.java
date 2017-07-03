/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolucionario;

import dp2.Const;
import dp2.D;
import dp2.Pattern;
import java.util.HashSet;
import java.util.Iterator;


/**
 *
 * @author Marianna
 */
public class MUTACAO {
            
    /**Adiciona ou troca gene de forma aleatória
     * 33% das vezes troca (objetivo: explorar aleatoriamente espaço D)
     * 33% das vezes adiciona (objetivo: explorar aleatoriamente espaço D+1)
     * 33% das vezes exclui (objetivo: explorar aleatoriamente espaço D-1)
     *@author Tarcísio Pontes
     * @param p Pattern - indivíduo a ser mutado
     * @param tipoAvaliacao int - tipo de função de avaliação utilizada
     * @return Pattern - indivíduo mutado
     */
    public static Pattern unGeneTrocaOuAdicionaOuExclui(Pattern p, String tipoAvaliacao){
        HashSet<Integer> itens = p.getItens();
        
        if(itens.isEmpty()){//Se indivíduo não tiver gene, retorne um novo aleatório de 1D
            itens.add(D.itensUtilizados[Const.random.nextInt(D.numeroItensUtilizados)]);            
            return new Pattern(itens, tipoAvaliacao);
        }
        
        HashSet<Integer> novoItens = new HashSet<>();
        double r = Const.random.nextDouble();
        if(r < 0.33){//Excluir gene
            
            int indiceExcluir = Const.random.nextInt(itens.size());
            Iterator iterator = itens.iterator();
            for(int i = 0; iterator.hasNext(); i++){
                if(i != indiceExcluir){
                    novoItens.add((Integer)iterator.next());
                }else{
                    iterator.next();
                }
            }
            
        }else if(r > 0.66){//Troca gene por outro aleatório
            //Excluir gene
            int indiceExcluir = Const.random.nextInt(itens.size());
            Iterator iterator = itens.iterator();
            for(int i = 0; iterator.hasNext(); i++){
                if(i != indiceExcluir){
                    novoItens.add((Integer)iterator.next());
                }else{
                    iterator.next();
                }
            }
                   
            //Adiciona novo gene
            while(novoItens.size() < itens.size()){
                novoItens.add(D.itensUtilizados[Const.random.nextInt(D.numeroItensUtilizados)]);            
            }
        }else{//Adiciona gene aleatoriamente
            //Adiciona novo gene
            novoItens.addAll(itens);
            while(novoItens.size() < itens.size() + 1){
                novoItens.add(D.itensUtilizados[Const.random.nextInt(D.numeroItensUtilizados)]);            
            }
        }                          
              
        
        Pattern pNovo = new Pattern(novoItens, tipoAvaliacao);
        
        //Imprimir itens nos idivíduos gerados via cruzamento
        //DPinfo.imprimirItens(p);
        //System.out.print(r + "->");
        //DPinfo.imprimirItens(pNovo);
        //System.out.println();
        
        return pNovo;
    }
 
    /**Gerar uma população a partir de mutações unGeneTrocaOuAdicionaOuExclui
     * 33% das vezes troca (objetivo: explorar aleatoriamente espaço D)
     * 33% das vezes adiciona (objetivo: explorar aleatoriamente espaço D+1)
     * 33% das vezes exclui (objetivo: explorar aleatoriamente espaço D-1)
    *@author Tarcísio Pontes
     * @param P Pattern[] - população
     * @param tamanhoPopulacao - número de indivíduoes a serem gerados
     * @param tipoAvaliacao int - tipo de função de avaliação utilizada
     * @return Pattern - indivíduo mutado
     */
    public static Pattern[] unGeneTrocaOuAdicionaOuExcluiPop(Pattern[] P, int tamanhoPopulacao, String tipoAvaliacao){
        Pattern[] Pm = new Pattern[tamanhoPopulacao];       
        
        for(int i = 0; i < Pm.length; i++){
            Pm[i] = MUTACAO.unGeneTrocaOuAdicionaOuExclui(P[i], tipoAvaliacao);
        }
        return Pm;
    }
    
    
    public static Pattern unGeneD(Pattern p, String tipoAvaliacao){
        HashSet<Integer> itens = (HashSet<Integer>) p.getItens().clone();
        HashSet<Integer> novoItens = new HashSet<>();
                    
        int indiceExcluir = Const.random.nextInt(itens.size());
        Iterator iterator = itens.iterator();
        for(int i = 0; iterator.hasNext(); i++){
            if(i != indiceExcluir){
                novoItens.add((Integer)iterator.next());
            }
        }          
        
        while(novoItens.size() < itens.size()){
            novoItens.add(D.itensUtilizados[Const.random.nextInt(D.numeroItensUtilizados)]);            
        }        
        return new Pattern(novoItens, tipoAvaliacao);
    }

}
