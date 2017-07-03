/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package evolucionario;

import dp2.Avaliador;
import dp2.Const;
import dp2.D;
import dp2.Pattern;
import java.io.FileNotFoundException;
import java.util.Random;

/**
 *
 * @author TARCISIO
 */
public class Main {
    public static void main(String args[]) throws FileNotFoundException{
        //====================================================================
        //== CONFIGURATION ===================================================
        //====================================================================
        //CSV database path
        String caminho = "./data/micro/"; 
        //String nomeBase = "audiology_pn.CSV";
        String nomeBase = "alon-pn-freq-2.CSV";
        String caminhoBase = caminho + nomeBase;
       
        D.SEPARADOR = ","; //separator database
        Const.random = new Random(Const.SEEDS[0]); //Seed - 30 options
        
        //Parameters of the algorithm
        int k = 10; //number of DPs
        String tipoAvaliacao = Avaliador.TIPO_WRACC; //Fitness
         tipoAvaliacao = Avaliador.TIPO_QG; //Fitness
        tipoAvaliacao = Avaliador.TIPO_SUB; //Fitness
        D.valorAlvo = "p"; //target value of dataset
        
        
        //====================================================================
        //= END ==============================================================
        //====================================================================
        
        
        
        D.CarregarArquivo(caminhoBase, D.TIPO_CSV); //Loading database         
        Pattern.numeroIndividuosGerados = 0; //Initializing count of generated individuals by SSDP
                            
        //Rodando SSDP
        long t0 = System.currentTimeMillis(); //Initial time
        Pattern[] p = SSDP_MxC_Auto_3x3.run(k, tipoAvaliacao); //run SSDP
        double tempo = (System.currentTimeMillis() - t0)/1000.0; //time
        
        //Informations about top-k DPs:  
        System.out.println("### Base:" + D.nomeBase); //database name
        System.out.println("Average " + tipoAvaliacao + ": " + Avaliador.avaliarMedia(p, k));
        System.out.println("Time(s): " + tempo);
        System.out.println("Average size: " + Avaliador.avaliarMediaDimensoes(p,k));        
        System.out.println("Coverage of all k DPs in relation to D+: " + Avaliador.coberturaPositivo(p, k)*100 + "%");
        System.out.println("Number of individuals generated: " + Pattern.numeroIndividuosGerados);
        System.out.println("\n### Top-k DPs:");
        Avaliador.imprimirRegras(p, k);             
    }
}
