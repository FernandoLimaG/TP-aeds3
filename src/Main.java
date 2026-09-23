import io.*;
import menu.Menu;
import entidade.Filme;
import arvore.ArvoreBMais;
import lista.ListaInvertida;
import ordenacao.OrdenacaoExterna;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Caminhos de Arquivos
        final String caminhoCSV = "dados/imdb_movies.csv";
        final String caminhoBinario = "dados/dados.bin";
        final String indiceArvore = "dados/indice_arvore.bin";
        final String indiceGeneros = "dados/lista_generos.bin";
        final String indicePaises = "dados/lista_paises.bin";

        int opcao = -1;
        Scanner scanner = new Scanner(System.in);
        ArvoreBMais arvore = new ArvoreBMais(indiceArvore, 100); // Ordem parametrizada
        ListaInvertida generos = new ListaInvertida(indiceGeneros);
        ListaInvertida paises = new ListaInvertida(indicePaises);
        ArquivoBinario arqBin = new ArquivoBinario(caminhoBinario, arvore, generos, paises);

        // Inicializar Estruturas
        arvore.inicializar();
        generos.inicializar();
        paises.inicializar();
        arqBin.inicializar();


        // Interface do usuário
        do {
            try {
                opcao = Menu.principal(scanner);
                System.out.println();
                switch (opcao) {
                    case 0:{
                        System.out.println("Saindo...");
                        break;
                    }
                    case 1:{
                        System.out.println("Iniciando carga de dados...");
                        ImportadorCSV.processarArquivo(caminhoCSV, arqBin);
                        break;
                    }
                    case 2:{
                        crudSequencial(caminhoBinario, arqBin, scanner);
                        break;
                    }
                    case 3:{
                        crudIndexado(caminhoBinario, arqBin, arvore, generos, paises, scanner);
                        break;
                    }
                    case 4:{
                        System.out.println("--- Ordenação Externa ---");
                        System.out.print("Digite o número de caminhos (ex: 2 ou 3): ");
                        int caminhos = Integer.parseInt(scanner.nextLine());

                        System.out.print("Digite o limite de registros em memória primária (ex: 20000): ");
                        int registrosMemoria = Integer.parseInt(scanner.nextLine());

                        OrdenacaoExterna ordenacao = new OrdenacaoExterna(caminhoBinario, caminhos, registrosMemoria);

                        System.out.println("\nDistribuição (limpando excluídos e ordenando)...");
                        int totalArquivos = ordenacao.distribuir();
                        System.out.println("Distribuição concluída! " + totalArquivos + " segmentos ordenados gerados.");

                        System.out.println("Intercalação (Merge dos caminhos)...");
                        ordenacao.intercalar(totalArquivos);
                        arqBin.reconstruirIndices();
                        System.out.println("\nOrdenação Externa concluída com sucesso!");

                        break;
                    }
                    default:{
                        System.out.println("Opção inválida, tente novamente");
                        break;
                    }
                }
            } catch (java.util.NoSuchElementException e) {
                opcao = 0;
            } catch (RuntimeException e) {
                System.out.println("Operação não concluída: " + e.getMessage());
            }
            System.out.println();
        } while (opcao != 0);

        scanner.close();
    }

    // TP1: CRUD sequencial
    static void crudSequencial(String caminhoBinario, ArquivoBinario arqBin, Scanner scanner) {
        int opcao;

        do {
            opcao = Menu.sequencial(scanner);
            System.out.println();
            switch (opcao) {
                case 0:{
                    System.out.println("Voltando ao menu principal...");
                    break;
                }
                case 1:{
                    try {
                        System.out.print("Digite o ID do filme que deseja ler: ");
                        int idBusca = Integer.parseInt(scanner.nextLine());
                        Filme encontrado = arqBin.ler(idBusca);

                        if (encontrado != null) {
                            System.out.println("\n--- Filme Encontrado ---");
                            System.out.println(encontrado);
                        } else {
                            System.out.println("\nFilme com ID " + idBusca + " não encontrado.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("\nErro: ID inválido.");
                    }
                    break;
                }
                case 2:{
                    System.out.print("Digite o ID do filme que deseja atualizar: ");
                    String idStr = scanner.nextLine();

                    try {
                        int idAtualiza = Integer.parseInt(idStr);
                        Filme filmeExistente = arqBin.ler(idAtualiza);

                        if (filmeExistente != null) {
                            System.out.println("\n--- Atualizando Filme (ID " + idAtualiza + ") ---");
                            System.out.println("Aperte Enter para manter o valor atual.");

                            System.out.print("Nome atual (" + filmeExistente.getNome() + "): ");
                            String novoNome = scanner.nextLine();
                            if (!novoNome.isEmpty()) filmeExistente.setNome(novoNome);

                            System.out.print("Data atual (" + filmeExistente.getDataLancamento() + " ms) - Digite no formato MM/DD/YYYY: ");
                            String novaData = scanner.nextLine();
                            if (!novaData.isEmpty()) filmeExistente.setDataLancamento(ImportadorCSV.converterDataManual(novaData));

                            System.out.print("Score atual (" + filmeExistente.getScore() + "): ");
                            String novoScoreStr = scanner.nextLine();
                            if (!novoScoreStr.isEmpty()) filmeExistente.setScore(Float.parseFloat(novoScoreStr));

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

                            System.out.print("País atual (" + filmeExistente.getPais() + ") - Sigla de 2 letras: ");
                            String novoPais = scanner.nextLine();
                            if (!novoPais.isEmpty()) filmeExistente.setPais(novoPais);

                            boolean sucesso = arqBin.atualizar(filmeExistente);
                            if (sucesso) {
                                System.out.println("\nFilme atualizado com sucesso no arquivo binário!");
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
                case 3:{
                    System.out.print("Digite o ID do filme que deseja deletar: ");
                    String idDeletaStr = scanner.nextLine();
                    try {
                        int idDeleta = Integer.parseInt(idDeletaStr);
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
                case 4:{
                    System.out.println("\n--- Inserir Novo Filme (Create) ---");
                    try {
                        // Lê o cabeçalho para descobrir qual foi o último ID gerado e soma 1
                        int novoId = 1;
                        try {
                            RandomAccessFile raf = new RandomAccessFile(caminhoBinario, "r");
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
    }

    // TP2: CRUD indexado: Arvore B+ e listas invertidas
    static void crudIndexado(String caminhoBinario, ArquivoBinario arqBin, ArvoreBMais arvore,
                              ListaInvertida generos, ListaInvertida paises, Scanner scanner) {
        int opcao;

        do {
            opcao = Menu.indexado(scanner);
            System.out.println();
            switch(opcao) {
                case 0:{
                    System.out.println("Voltando ao menu principal...");
                    break;
                }
                case 1:{
                    long[] posicoes = pesquisarIndice(scanner, arvore, generos, paises);
                    mostrarResultados(arqBin, posicoes);
                    break;
                }
                case 2:{
                    try {
                        long posicao = selecionarRegistro(scanner, arqBin, arvore, generos, paises);
                        Filme filmeExistente = arqBin.lerNaPosicao(posicao);
                        int idAtualiza = filmeExistente == null ? -1 : filmeExistente.getId();

                        if (filmeExistente != null) {
                            System.out.println("\n--- Atualizando Filme (ID " + idAtualiza + ") ---");
                            System.out.println("Aperte Enter para manter o valor atual.");

                            System.out.print("Nome atual (" + filmeExistente.getNome() + "): ");
                            String novoNome = scanner.nextLine();
                            if (!novoNome.isEmpty()) filmeExistente.setNome(novoNome);

                            System.out.print("Data atual (" + filmeExistente.getDataLancamento() + " ms) - Digite no formato MM/DD/YYYY: ");
                            String novaData = scanner.nextLine();
                            if (!novaData.isEmpty()) filmeExistente.setDataLancamento(ImportadorCSV.converterDataManual(novaData));

                            System.out.print("Score atual (" + filmeExistente.getScore() + "): ");
                            String novoScoreStr = scanner.nextLine();
                            if (!novoScoreStr.isEmpty()) filmeExistente.setScore(Float.parseFloat(novoScoreStr));

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

                            System.out.print("País atual (" + filmeExistente.getPais() + ") - Sigla de 2 letras: ");
                            String novoPais = scanner.nextLine();
                            if (!novoPais.isEmpty()) filmeExistente.setPais(novoPais);

                            boolean sucesso = arqBin.atualizar(filmeExistente, posicao);
                            if (sucesso) {
                                System.out.println("\nFilme atualizado com sucesso no arquivo binário!");
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
                case 3:{
                    long posicao = selecionarRegistro(scanner, arqBin, arvore, generos, paises);
                    Filme filme = arqBin.lerNaPosicao(posicao);
                    if (filme != null && arqBin.deletar(filme.getId(), posicao)) {
                        System.out.println("\nFilme deletado com sucesso!");
                    } else {
                        System.out.println("\nFilme não encontrado para exclusão.");
                    }
                    break;
                }
                case 4:{
                    System.out.println("\n--- Inserir Novo Filme (Create) ---");
                    System.out.println("Índices utilizados: Árvore B+ e listas invertidas de gênero e país.");
                    try {
                        int novoId = 0;
                        try {
                            RandomAccessFile raf = new RandomAccessFile(caminhoBinario, "r");
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
                        long dataConvertida = io.ImportadorCSV.converterDataManual(novaData);

                        System.out.print("Score (ex: 84.5): ");
                        float novoScore = Float.parseFloat(scanner.nextLine());

                        System.out.print("Gêneros (separados por vírgula): ");
                        String novosGeneros = scanner.nextLine();
                        String[] arrayGeneros = io.ImportadorCSV.separarGenerosManual(novosGeneros);

                        System.out.print("País (Sigla de 2 letras, ex: US): ");
                        String novoPais = scanner.nextLine();

                        Filme novoFilme = new Filme(novoId, novoNome, dataConvertida, novoScore, arrayGeneros, novoPais);

                        arqBin.inserir(novoFilme);

                        System.out.println("\nFilme criado com sucesso no arquivo binário e indexado na Árvore B+ e nas listas!");

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
    }

    private static long[] pesquisarIndice(Scanner scanner, ArvoreBMais arvore,
                                    ListaInvertida generos, ListaInvertida paises) {
        System.out.println("Qual índice deseja usar?");
        System.out.println("1. Árvore B+ (ID)");
        System.out.println("2. Lista invertida de gêneros");
        System.out.println("3. Lista invertida de países");
        System.out.println("4. Duas listas: gênero E país");
        System.out.print("Escolha: ");
        int indice = Integer.parseInt(scanner.nextLine());
        long[] resultado = new long[0];
        System.out.println();

        switch (indice) {
            case 1: {
                System.out.println("----- Índice utilizado: Árvore B+ -----");
                System.out.print("ID do filme: ");
                long posicao = arvore.buscar(Integer.parseInt(scanner.nextLine()));
                if (posicao != -1) resultado = new long[] {posicao};
                break;
            }
            case 2: {
                System.out.println("----- Índice utilizado: lista invertida de gêneros -----");
                System.out.print("Gênero (ex: Drama): ");
                resultado = generos.buscar(scanner.nextLine());
                break;
            }
            case 3: {
                System.out.println("----- Índice utilizado: lista invertida de países -----");
                System.out.print("País (ex: US): ");
                resultado = paises.buscar(scanner.nextLine());
                break;
            }
            case 4: {
                System.out.println("----- Índices utilizados: listas invertidas de gênero e país (interseção) -----");
                System.out.print("Gênero (ex: Drama): ");
                String genero = scanner.nextLine();
                System.out.print("País (ex: US): ");
                String pais = scanner.nextLine();
                resultado = ListaInvertida.intersecao(generos.buscar(genero), paises.buscar(pais));
                break;
            }
            default: {
                System.out.println("Índice inválido.");
                break;
            }
        }
        return resultado;
    }

    private static void mostrarResultados(ArquivoBinario arqBin, long[] posicoes) {
        int quantidade = 0;
        for (int i = 0; i < posicoes.length; i++) {
            Filme filme = arqBin.lerNaPosicao(posicoes[i]);
            if (filme != null) {
                System.out.println(filme);
                quantidade++;
            }
        }
        System.out.println("Registros encontrados: " + quantidade);
    }

    private static long selecionarRegistro(Scanner scanner, ArquivoBinario arqBin,
                                           ArvoreBMais arvore, ListaInvertida generos,
                                           ListaInvertida paises) {
        long[] posicoes = pesquisarIndice(scanner, arvore, generos, paises);
        mostrarResultados(arqBin, posicoes);
        long resultado = -1;
        if (posicoes.length == 1) {
            resultado = posicoes[0];
        } else if (posicoes.length > 1) {
            System.out.print("Digite o ID de um dos resultados (0 para cancelar): ");
            int id = Integer.parseInt(scanner.nextLine());
            for (int i = 0; i < posicoes.length && resultado == -1; i++) {
                Filme filme = arqBin.lerNaPosicao(posicoes[i]);
                if (filme != null && filme.getId() == id) resultado = posicoes[i];
            }
        }
        return resultado;
    }
}