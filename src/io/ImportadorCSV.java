package io;

import entidade.Filme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Calendar;

public class ImportadorCSV {

    public static void processarArquivo(String caminhoCSV, String caminhoBinario) {

        File arquivo = new File(caminhoBinario);
        if (arquivo.exists()) {
            arquivo.delete();
        }

        int contadorId = 1;
        
        // Prepara o arquivo binário
        ArquivoBinario arqBin = new ArquivoBinario(caminhoBinario);
        arqBin.inicializar();

        try {
            BufferedReader br = new BufferedReader(new FileReader(caminhoCSV));
            String linha = br.readLine(); // Pula o cabeçalho
            linha = br.readLine();

            System.out.println("Lendo o CSV e gravando no arquivo binário...");

            // Vamos importar todos os registros agora (removi a limitação de 10 linhas)
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
                
                // Grava fisicamente no arquivo .bin
                arqBin.inserir(filme);
                
                contadorId++;
                linha = br.readLine();
            }
            br.close();
            System.out.println("Carga da base de dados concluída com sucesso! " + (contadorId - 1) + " registros gravados.");
            
        } catch (Exception e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    // Separa as colunas do CSV lidando com aspas duplas, usando apenas charAt
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

        // Preenche nulls com string vazia
        for(int i = 0; i < campos.length; i++) {
            if(campos[i] == null) campos[i] = "";
        }
        return campos;
    }

    // Separa a string "Drama, Action" num array, ignorando os espaços após a vírgula
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
                // Pula o espaço se estiver no começo do nome do gênero
                continue;
            } else {
                atual += c;
            }
        }
        generos[indice] = atual;
        return generos;
    }

    // Converte a string MM/DD/YYYY para um long de milissegundos lidando com espaços manuais
    public static long converterDataManual(String dataStr) {
        if (dataStr.length() == 0) return 0;
        
        String mesStr = "";
        String diaStr = "";
        String anoStr = "";
        int parte = 0; // 0 = mes, 1 = dia, 2 = ano

        for (int i = 0; i < dataStr.length(); i++) {
            char c = dataStr.charAt(i);
            if (c == '/') {
                parte++;
            } 
            // Pega estritamente os caracteres numéricos usando a tabela ASCII
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
            cal.set(ano, mes - 1, dia, 0, 0, 0); // Mês no Calendar começa em 0
            return cal.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}