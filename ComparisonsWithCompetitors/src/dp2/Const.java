/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dp2;

import java.util.Random;

/**
 *
 * @author tarcisio_pontes
 */
public class Const {
    
    public final static String FUNCAO_OBJETIVO_Qg = "Qg";
    public final static String FUNCAO_OBJETIVO_SUB = "SUB";
    public final static String FUNCAO_OBJETIVO_WRACC = "WRAcc";
    
    public final static String TIPO_BASE_FREQ2 = "Freq2";
    public final static String TIPO_BASE_FREQ4 = "Freq4";
    public final static String TIPO_BASE_FREQ8 = "Freq8";
    public final static String TIPO_BASE_WIDTH2 = "Width2";
    public final static String TIPO_BASE_WIDTH4 = "Width4";
    public final static String TIPO_BASE_WIDTH8 = "Width8";
       
    public final static String CAMINHO_BASES = "../DP2/pastas/bases/";
    public final static String CAMINHO_BASES_EDITADAS = "../DP2/pastas/bases_editadas/";
    public final static String CAMINHO_RESULTADOS = "../DP2/pastas/resultados/";
    public final static String CAMINHO_RESULTADOS_OBJ = "../DP2/pastas/resultados_obj/";  
    public final static String CAMINHO_RELATORIO = "../DP2/pastas/relatorios/";
    public final static String CAMINHO_DICIONARIOS = "../DP2/pastas/dicionarios/";
    public final static String CAMINHO_INDICE = "../DP2/pastas/indice.txt";
       
    public static Random random;   
    public final static long[] SEEDS = {179424673, 125164703, 132011827, 124987441, 123979721 , 119777719, 117705823 , 112131119, 108626351, 107980007, 
        106368047, 99187427, 98976029, 97875523, 96763291, 95808337, 94847387, 87552823, 86842271 , 80650457, 78220001, 74585729, 73852469 , 68750849, 58160551 , 
        45320477, 31913771, 24096223, 16980937, 8261369};
}
