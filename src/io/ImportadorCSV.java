package io;

import arvore.ArvoreBMais;
import entidade.Filme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Calendar;

public class ImportadorCSV {

    public static void processarArquivo(String caminhoCSV, String caminhoBinario, ArvoreBMais arvore) {

        File arquivo = new File(caminhoBinario);
        if (arquivo.exists()) {
            arquivo.delete();
        }
        
        File arqArvore = new File("dados/indice_arvore.bin");
        if (arqArvore.exists()) {
            arqArvore.delete();
        }

        int contadorId = 1;
        
        ArquivoBinario arqBin = new ArquivoBinario(caminhoBinario);
        arqBin.inicializar();
        arvore.inicializar();

        try {
            BufferedReader br = new BufferedReader(new FileReader(caminhoCSV));
            String linha = br.readLine(); 
            linha = br.readLine();

            System.out.println("A ler o CSV e a gravar no ficheiro binário e na Árvore B+...");

            while (linha != null) {
                String[] campos = separarColunasCSV(linha);
                
                String nome = campos[0];
                long dataLancamento = converterDataManual(campos[1]);
                float score = 0.0f;
                if (!campos[2].isEmpty()) {
                    score = Float.parseFloat(campos[2]);
                }
                String[] generos = separarGenerosManual(campos[3]);
                String pais = campos[11];

                Filme filme = new Filme(contadorId, nome, dataLancamento, score, generos, pais);
                
                long posicao = arqBin.inserir(filme);
                
                if (posicao != -1) {
                    arvore.inserir(contadorId, posicao);
                }
                
                contadorId++;
                linha = br.readLine();
            }
            br.close();
            System.out.println("Carga da base de dados concluída! " + (contadorId - 1) + " registos gravados e indexados.");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String[] separarColunasCSV(String linha) {
        String[] campos = new String[15]; 
        int indice = 0;
        String atual = "";
        boolean emAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (emAspas) emAspas = false;
                else emAspas = true;
            } else if (c == ',' && !emAspas) {
                campos[indice] = atual;
                indice++;
                atual = "";
            } else {
                atual += c;
            }
        }
        campos[indice] = atual;

        for(int i = 0; i < campos.length; i++) {
            if(campos[i] == null) campos[i] = "";
        }
        return campos;
    }

    public static String[] separarGenerosManual(String bruta) {
        int qtd = 1;
        for (int i = 0; i < bruta.length(); i++) {
            if (bruta.charAt(i) == ',') qtd++;
        }

        String[] generos = new String[qtd];
        int indice = 0;
        String atual = "";

        for (int i = 0; i < bruta.length(); i++) {
            char c = bruta.charAt(i);
            if (c == ',') {
                generos[indice] = atual;
                indice++;
                atual = "";
            } else if (c == ' ' && atual.length() == 0) {
                continue;
            } else {
                atual += c;
            }
        }
        generos[indice] = atual;
        return generos;
    }

    public static long converterDataManual(String dataStr) {
        if (dataStr.length() == 0) return 0;
        
        String mesStr = "";
        String diaStr = "";
        String anoStr = "";
        int parte = 0; 

        for (int i = 0; i < dataStr.length(); i++) {
            char c = dataStr.charAt(i);
            if (c == '/') {
                parte++;
            } 
            else if (c >= '0' && c <= '9') { 
                if (parte == 0) mesStr += c;
                else if (parte == 1) diaStr += c;
                else if (parte == 2) anoStr += c;
            }
        }

        try {
            int mes = Integer.parseInt(mesStr);
            int dia = Integer.parseInt(diaStr);
            int ano = Integer.parseInt(anoStr);

            Calendar cal = Calendar.getInstance();
            cal.set(ano, mes - 1, dia, 0, 0, 0); 
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}