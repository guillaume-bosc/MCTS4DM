/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dp2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

/**
 *
 * @author Marianna
 */
public class D {
   
    public static String nomeBase;
    public static String caminho;
    public static int numeroExemplos;
    public static int numeroExemplosPositivo;
    public static int numeroExemplosNegativo;
    
    public static int numeroAtributos;
    public static int numeroItens;
    public static String[] nomeVariaveis;   
    
    public static String SEPARADOR = ","; 
    
    public static int[] itemAtributo;
    public static int[] itemValor;
    public static String[] itemAtributoStr;
    public static String[] itemValorStr;
    
    public static int[][] Dp;
    public static int[][] Dn;
    
    public static int[] itensUtilizados;
    public static int numeroItensUtilizados;
    
    public static final int TIPO_CSV = 0;
    
    public static String tipoDiscretizacao;
    
    public static String valorAlvo = "";
    
    public static void CarregarArquivo(String caminho, int tipoArquivo) throws FileNotFoundException{
          
        if(caminho.contains("freq_2")){
            D.tipoDiscretizacao = Const.TIPO_BASE_FREQ2;
        }else if(caminho.contains("freq_4")){
            D.tipoDiscretizacao = Const.TIPO_BASE_FREQ4;
        }else if(caminho.contains("freq_8")){
            D.tipoDiscretizacao = Const.TIPO_BASE_FREQ8;
        }else if(caminho.contains("width_2")){
            D.tipoDiscretizacao = Const.TIPO_BASE_WIDTH2;
        }else if(caminho.contains("width_4")){
            D.tipoDiscretizacao = Const.TIPO_BASE_WIDTH4;
        }else if(caminho.contains("width_8")){    
            D.tipoDiscretizacao = Const.TIPO_BASE_WIDTH8;
        }else{
            D.tipoDiscretizacao = "Não discretizada";
        }
        
        String[][] dadosStr = null;
        switch(tipoArquivo){
            case D.TIPO_CSV:
                dadosStr = D.CVStoDadosStr(caminho);
        }            
        D.dadosStrToD(dadosStr);
        
        D.numeroItensUtilizados = D.numeroItens;
        D.itensUtilizados = new int[D.numeroItensUtilizados];
        for(int l = 0; l < D.numeroItensUtilizados; l++){
            D.itensUtilizados[l] = l;
        }
        Pattern.vrPCount = new int[D.numeroExemplosPositivo];
        Pattern.vrNCount = new int[D.numeroExemplosNegativo];
    }
    
    //Densidade é a quantidade
    public static double densidade(){        
        return 0.0;
    }
    

    
    //Diferente para cada tipo de arquivo, recebe caminho de arquivo.CSV e realiza as seuguintes tarefas:
    //(1) Salva os exemplos numa matriz de dados nomeada dadosStr do tipo String[numeroExemplo][numeroAtributos]
    //(2) Salva nome da base em D.nomeBase
    //(3) Salva os nomes dos atributos e do rótulo em D.nomeVariaveis
    //(4) Salva número de exemplos e de atributos
    //(5) Salva caminho da base em D.caminho
    private static String[][] CVStoDadosStr(String caminho) throws FileNotFoundException{
        //Lendo arquivo no formato padrão
        D.caminho = caminho;
        Scanner scanner = new Scanner(new FileReader(D.caminho))
                       .useDelimiter("\\n");
        ArrayList<String[]> dadosString = new ArrayList<>();        
              
        
        String[] palavras = D.caminho.split("\\\\");
        if(palavras.length == 1){
            palavras = D.caminho.split("/");//Caso separador de pastas seja / e  não \\
        }
        
        D.nomeBase = palavras[palavras.length-1].replace(".CSV", "");//Nome do arquivo é a última palavra
        
        
        D.nomeVariaveis = scanner.next().split(D.SEPARADOR); //1º linha: nome das variáveis
        D.numeroAtributos = D.nomeVariaveis.length-1; //último atributo é o rótulo
        while (scanner.hasNext()) {
            dadosString.add(scanner.next().split(D.SEPARADOR));
        }
        D.numeroExemplos = dadosString.size();
        
        String[][] dadosStr = new String[D.numeroExemplos][D.numeroAtributos+1];
        for(int i = 0; i < dadosString.size(); i++){
            String[] exemploBase = dadosString.get(i);//recebe linha de dados
            for(int j = 0; j < exemploBase.length; j++){
                dadosStr[i][j] = exemploBase[j];
            }            
        }       
        
        return dadosStr;
    }
    
    //A partir da matriz de dados D.dadosStr deixa atributo D pronto para ser utilizado por algoritmos fazendo as aeguintes tarefas:
    //(1) itemAtributo
    //(2) itemValor
    //(3) itemAtributoStr
    //(4) itemValorStr
    //(5) Dp e Dn
    private static void dadosStrToD(String[][] dadosStr){
        ArrayList<HashSet<String>> valoresDistintosAtributos = new ArrayList<>(); //Amazena os valores distintos de cada atributo em um linha
        D.numeroItens = 0;
        //Capturando os valores distintos de cada atributo
//        int n = 1;
        for(int i = 0; i < D.numeroAtributos; i++){
            HashSet<String> valoresDistintosAtributo = new HashSet<>(); //Criar HashSet para armezenar valores distintos de um atributo
            for(int j = 0; j < D.numeroExemplos; j++){
                valoresDistintosAtributo.add(dadosStr[j][i]);
            }
            D.numeroItens += valoresDistintosAtributo.size();
//            if(valoresDistintosAtributo.size() == 2){
//                System.out.println("["+ n++ +"]Atributo: " + i + "(" +valoresDistintosAtributo.size() + ")");
//            }
            
            valoresDistintosAtributos.add(valoresDistintosAtributo); //Adiciona lista de valores distintos do atributo de índice i na posição i do atributo atributosEvalores
        }
        
        //Inicializando atributos responsáveis pela matriz de mapeamento L: todos os itens disponíveis na busca.
        D.itemAtributoStr = new String[D.numeroItens];
        D.itemValorStr = new String[D.numeroItens];
        D.itemAtributo = new int[D.numeroItens];
        D.itemValor = new int[D.numeroItens];
            
        int[][] dadosInt = new int[D.numeroExemplos][D.numeroAtributos];
        int indiceItem = 0; //Indice vai de zero ao número de itens total
        for(int indiceAtributo = 0; indiceAtributo < valoresDistintosAtributos.size(); indiceAtributo++){
            Iterator valoresDistintosAtributoIterator = valoresDistintosAtributos.get(indiceAtributo).iterator(); //Capturando valores distintos do atributo de indice i
            int indiceValor = 0; //vai mapear um inteiro distinto para cada valor distinto de cada variável
            
            while(valoresDistintosAtributoIterator.hasNext()){
                D.itemAtributoStr[indiceItem] = D.nomeVariaveis[indiceAtributo]; //
                D.itemValorStr[indiceItem] = (String)valoresDistintosAtributoIterator.next();

                D.itemAtributo[indiceItem] = indiceAtributo;
                D.itemValor[indiceItem] = indiceValor;               
                
                //Preenche matrix dadosInt com inteiro que mapeia valor categórico da base
                for(int m = 0; m < D.numeroExemplos; m++){
                    if(dadosStr[m][indiceAtributo].equals(D.itemValorStr[indiceItem])){
                        dadosInt[m][indiceAtributo] = D.itemValor[indiceItem];
                    }
                }
                indiceValor++;
                indiceItem++;
            }     
        } 
        
        D.geraDpDn(dadosStr, dadosInt);
    }
    
   
    private static void geraDpDn(String[][] dadosStr, int[][] dadosInt){
        //Capturar número de exemplo positivos (y="p") e negativos (y="n")
        int indiceRotulo = D.numeroAtributos;
        D.numeroExemplosPositivo = 0;
        D.numeroExemplosNegativo = 0;
        for(int i = 0; i < D.numeroExemplos; i++){
            String y = dadosStr[i][indiceRotulo];
            //if(y.equalsIgnoreCase("\"p\"\r") || y.equalsIgnoreCase("\'p\'\r") || y.equalsIgnoreCase("p\r")){
            if(y.equals(D.valorAlvo) || y.equals("\"" + D.valorAlvo + "\"\r") || y.equals("\'" + D.valorAlvo + "\'\r") || y.equals(D.valorAlvo + "\r")){
            //if(y.equals(D.valorAlvo)){
                D.numeroExemplosPositivo++;
            }else{
                D.numeroExemplosNegativo++;
            }
        }
        
        //inicializando Dp e Dn
        D.Dp = new int[D.numeroExemplosPositivo][D.numeroAtributos];
        D.Dn = new int[D.numeroExemplosNegativo][D.numeroAtributos];
        
        int indiceDp = 0;
        int indiceDn = 0;
        for(int i = 0; i < D.numeroExemplos; i++){
            String yValue = dadosStr[i][indiceRotulo];
            //if(yValue.equals("\"p\"\r") || yValue.equals("\'p\'\r") || yValue.equals("p\r")){
            if(yValue.equals(D.valorAlvo) || yValue.equals("\"" + D.valorAlvo + "\"\r") || yValue.equals("\'" + D.valorAlvo + "\'\r") || yValue.equals(D.valorAlvo + "\r")){
            //if(yValue.equals(D.valorAlvo)){
                for(int j = 0; j < D.numeroAtributos; j++){
                    Dp[indiceDp][j] = dadosInt[i][j];
                }
                indiceDp++;
            }else{
                for(int j = 0; j < D.numeroAtributos; j++){
                    Dn[indiceDn][j] = dadosInt[i][j];
                }
                indiceDn++;            
            }
        }
    }
    
    public static void recordDicionario(String caminhoPastaSalvar) throws IOException{
        String nomeArquivo = caminhoPastaSalvar + "\\" + D.nomeBase + "Dic.txt";
        String separadorDicionario = ",";
        File file = new File(nomeArquivo);
        // creates the file
        file.createNewFile();
        // creates a FileWriter Object
        FileWriter writer = new FileWriter(file); 
        // Writes the content to the file
        
        writer.write("@Nome: " + D.nomeBase + "\r\n"); 
        writer.write("@Info: Atributos=" + D.numeroAtributos +  separadorDicionario + "|D|=" +  D.numeroExemplos + separadorDicionario + "|Dp|=" + D.numeroExemplosPositivo + separadorDicionario + "|Dn|=" + D.numeroExemplosNegativo
            + separadorDicionario + "|I|=" + D.numeroItensUtilizados + "\r\n"); 
        //writer.write(); 
        writer.write("@Dicionario:Item,Atributo,Valor" + "\r\n"); 
        for(int i = 0; i < D.numeroItensUtilizados; i++){
            writer.write(i + separadorDicionario + D.itemAtributoStr[i] + separadorDicionario + itemValorStr[i] + "\r\n");           
        }      
        writer.flush();
        writer.close();
    }
    
    public static void imprimirDicionario(){        
        System.out.println("@Nome:" + D.nomeBase);
        System.out.println("@Info:Atributos=" + D.numeroAtributos + " ; |D|=" +  D.numeroExemplos + " ; |Dp|=" + D.numeroExemplosPositivo + " ; |Dn|=" + D.numeroExemplosNegativo
            + "; |I|=" + D.numeroItensUtilizados);
        //System.out.println("@Dicionario: Item;atributoOriginal;valorOriginal;atributoInt;valorInt");
        System.out.println("@Dicionario: Item;Atributo;Valor");
        for(int i = 0; i < D.numeroItensUtilizados; i++){
            //System.out.println(i + ";" + D.itemAtributoStr[i] + ";" + itemValorStr[i] + ";" + D.itemAtributo[i] + ";" + D.itemValor[i]);
            System.out.println(i + ";" + D.itemAtributoStr[i] + ";" + itemValorStr[i]);
        }        
    }  

}
