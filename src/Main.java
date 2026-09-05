import entidade.Filme;
import io.*;
import menu.Menu;
import ordenacao.OrdenacaoExterna;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcao;

        do {
            opcao = Menu.principal(scanner);
            System.out.println();
            switch (opcao) {
                case 0:{
                    System.out.println("Saindo...");
                    break;
                }
                case 1:{
                    System.out.println("Iniciando carga de dados...");
                    ImportadorCSV.processarArquivo("dados/imdb_movies.csv", "dados/dados.bin");
                    break;
                }
                case 2:{
                    try {
                        System.out.print("Digite o ID do filme que deseja ler: ");
                        int idBusca = Integer.parseInt(scanner.nextLine());
                        ArquivoBinario arqBin = new ArquivoBinario("dados/dados.bin");
                        Filme encontrado = arqBin.ler(idBusca);

                        if (encontrado != null) {
                            System.out.println("\n--- Filme Encontrado ---");
                            System.out.println(encontrado);
                        } else {
                            System.out.println("\nFilme com ID " + idBusca + " não encontrado ou foi deletado.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("\nErro: ID inválido.");
                    }
                    break;
                }
                case 3:{
                    System.out.print("Digite o ID do filme que deseja atualizar: ");
                    String idStr = scanner.nextLine();

                    try {
                        int idAtualiza = Integer.parseInt(idStr);
                        ArquivoBinario arqBin = new ArquivoBinario("dados/dados.bin");
                        Filme filmeExistente = arqBin.ler(idAtualiza);

                        if (filmeExistente != null) {
                            System.out.println("\n--- Atualizando Filme (ID " + idAtualiza + ") ---");
                            System.out.println("Deixe em branco e aperte Enter para manter o valor atual.");

                            System.out.print("Nome atual (" + filmeExistente.getNome() + "): ");
                            String novoNome = scanner.nextLine();
                            if (!novoNome.isEmpty()) filmeExistente.setNome(novoNome);

                            System.out.print("Data atual (" + filmeExistente.getDataLancamento() + " ms) - Digite no formato MM/DD/YYYY: ");
                            String novaData = scanner.nextLine();
                            if (!novaData.isEmpty()) filmeExistente.setDataLancamento(ImportadorCSV.converterDataManual(novaData));

                            System.out.print("Score atual (" + filmeExistente.getScore() + "): ");
                            String novoScoreStr = scanner.nextLine();
                            if (!novoScoreStr.isEmpty()) filmeExistente.setScore(Float.parseFloat(novoScoreStr));

                            // --- CORREÇÃO: Formatando e mostrando os gêneros atuais ---
                            String strGen = "[";
                            String[] genAtuais = filmeExistente.getGeneros();
                            for (int i = 0; i < genAtuais.length; i++) {
                                strGen += genAtuais[i];
                                if (i < genAtuais.length - 1) strGen += ", ";
                            }
                            strGen += "]";

                            System.out.print("Gêneros atuais " + strGen + " - Digite separados por vírgula: ");
                            String novosGeneros = scanner.nextLine();
                            if (!novosGeneros.isEmpty()) filmeExistente.setGeneros(ImportadorCSV.separarGenerosManual(novosGeneros));
                            // -----------------------------------------------------------

                            System.out.print("País atual (" + filmeExistente.getPais() + ") - Sigla de 2 letras: ");
                            String novoPais = scanner.nextLine();
                            if (!novoPais.isEmpty()) filmeExistente.setPais(novoPais);

                            boolean sucesso = arqBin.atualizar(filmeExistente);
                            if (sucesso) {
                                System.out.println("\nFilme atualizado com sucesso no arquivo binário!");
                                // --- CORREÇÃO: Mostrando como o registro ficou ---
                                System.out.println("Como ficou: " + filmeExistente.toString());
                            }
                        } else {
                            System.out.println("\nFilme não encontrado para atualização.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("\nErro: Digite um ID numérico válido.");
                    }
                    break;
                }
                case 4:{
                    System.out.print("Digite o ID do filme que deseja deletar: ");
                    String idDeletaStr = scanner.nextLine();
                    try {
                        int idDeleta = Integer.parseInt(idDeletaStr);
                        ArquivoBinario arqBin = new ArquivoBinario("dados/dados.bin");
                        boolean sucesso = arqBin.deletar(idDeleta);

                        if (sucesso) {
                            System.out.println("\nFilme deletado com sucesso!");
                        } else {
                            System.out.println("\nFalha ao deletar: Filme não encontrado.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("\nErro: ID inválido.");
                    }
                    break;
                }
                case 5:{
                    System.out.println("--- Ordenação Externa ---");
                    System.out.print("Digite o número de caminhos (arquivos temporários, ex: 2 ou 3): ");
                    int caminhos = Integer.parseInt(scanner.nextLine());

                    System.out.print("Digite o limite de registros em memória primária (ex: 20000): ");
                    int registrosMemoria = Integer.parseInt(scanner.nextLine());

                    OrdenacaoExterna ordenacao = new OrdenacaoExterna("dados/dados.bin", caminhos, registrosMemoria);

                    System.out.println("Iniciando Fase 1: Distribuição (limpando excluídos e ordenando)...");
                    int totalArquivos = ordenacao.distribuir();
                    System.out.println("Distribuição concluída! " + totalArquivos + " arquivos temporários gerados.");

                    System.out.println("Iniciando Fase 2: Intercalação (Merge dos caminhos)...");
                    ordenacao.intercalar();
                    System.out.println("\nOrdenação Externa concluída com sucesso!");
                    System.out.println("O arquivo 'dados.bin' agora está limpo e 100% ordenado.");

                    break;
                }
                case 6:{
                    System.out.println("\n--- Inserir Novo Filme (Create) ---");
                    try {
                        // Lê o cabeçalho para descobrir qual foi o último ID gerado e soma 1
                        int novoId = 1;
                        try {
                            java.io.RandomAccessFile raf = new java.io.RandomAccessFile("dados/dados.bin", "r");
                            raf.seek(0);
                            novoId = raf.readInt() + 1;
                            raf.close();
                        } catch (Exception e) {
                            System.out.println("Arquivo binário não encontrado. Faça a carga primeiro.");
                            break;
                        }

                        System.out.println("Novo ID gerado: " + novoId);
                        
                        System.out.print("Nome do filme: ");
                        String novoNome = scanner.nextLine();

                        System.out.print("Data de lançamento (MM/DD/YYYY): ");
                        String novaData = scanner.nextLine();
                        long dataConvertida = ImportadorCSV.converterDataManual(novaData);

                        System.out.print("Score (ex: 85.5): ");
                        float novoScore = Float.parseFloat(scanner.nextLine());

                        System.out.print("Gêneros (separados por vírgula): ");
                        String novosGeneros = scanner.nextLine();
                        String[] arrayGeneros = ImportadorCSV.separarGenerosManual(novosGeneros);

                        System.out.print("País (Sigla de 2 letras, ex: US): ");
                        String novoPais = scanner.nextLine();

                        Filme novoFilme = new Filme(novoId, novoNome, dataConvertida, novoScore, arrayGeneros, novoPais);
                        
                        ArquivoBinario arqBin = new ArquivoBinario("dados/dados.bin");
                        arqBin.inserir(novoFilme);
                        
                        System.out.println("\nFilme criado com sucesso no arquivo binário!");

                    } catch (NumberFormatException e) {
                        System.out.println("\nErro de formatação nos números digitados.");
                    }
                    break;
                }
                default:{
                    System.out.println("Opção inválida, tente novamente");
                    break;
                }
            }
            System.out.println();
        } while (opcao != 0);

        scanner.close();
    }
}