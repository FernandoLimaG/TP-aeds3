package io;

import entidade.Filme;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Calendar;

public class ImportadorCSV {

    public static void processarArquivo(String caminhoCSV, ArquivoBinario arqBin) {
        int contadorId = 1;
        // Abre o CSV antes de limpar a base, para preservar os dados se faltar o CSV.
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCSV))) {
            arqBin.limpar();
            String linha = br.readLine(); 
            linha = br.readLine();

            System.out.println("Lendo o CSV e gravando os dados, a Árvore B+ e as duas listas invertidas...");

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
                
                arqBin.inserir(filme);

                contadorId++;
                linha = br.readLine();
            }
            System.out.println("Carga da base de dados concluída! " + (contadorId - 1) + " registos gravados e indexados.");
            
        } catch (Exception e) {
            throw new IllegalStateException("Erro na carga CSV: " + e.getMessage(), e);
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
                // Duas aspas dentro de um campo representam uma aspa no texto.
                if (emAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual += '"';
                    i++;
                } else {
                    emAspas = !emAspas;
                }
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
        long resultado = 0;
        if (!dataStr.trim().isEmpty()) {
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
                cal.clear(); // A data não deve herdar os milissegundos da execução.
                cal.set(ano, mes - 1, dia, 0, 0, 0);
                resultado = cal.getTimeInMillis();
                // Verifica o dia sem rejeitar ajustes de horário de verão à meia-noite.
                if (cal.get(Calendar.YEAR) != ano || cal.get(Calendar.MONTH) != mes - 1
                        || cal.get(Calendar.DAY_OF_MONTH) != dia) {
                    throw new IllegalArgumentException("Dia, mês ou ano inválido.");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Data inválida. Use MM/DD/YYYY.", e);
            }
        }
        return resultado;
    }
}