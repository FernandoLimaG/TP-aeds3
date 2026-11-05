
package compressao;

import io.ArquivoBinario;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Scanner;

public class GerenciadorCompressao {

    private String nomeArquivo;
    private ArquivoBinario arquivoBinario;

    public GerenciadorCompressao(String nomeArquivo, ArquivoBinario arquivoBinario) {
        this.nomeArquivo = nomeArquivo;
        this.arquivoBinario = arquivoBinario;
    }

    private String nomeVersao(String algoritmo, int versao) {
        int ponto = nomeArquivo.lastIndexOf('.');
        String base = ponto == -1 ? nomeArquivo : nomeArquivo.substring(0, ponto);
        String extensao = ponto == -1 ? "" : nomeArquivo.substring(ponto);
        return base + algoritmo + "Compressao" + versao + extensao;
    }

    private int proximaVersao() {
        int versao = 1;
        while (new File(nomeVersao("Huffman", versao)).exists()
                || new File(nomeVersao("LZW", versao)).exists()) versao++;
        return versao;
    }

    private byte[] lerBytes(String caminho) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(caminho, "r")) {
            if (raf.length() > Integer.MAX_VALUE)
                throw new IOException("Arquivo grande demais para processamento em memória.");
            byte[] dados = new byte[(int) raf.length()];
            raf.readFully(dados);
            return dados;
        }
    }

    private void escreverBytes(String caminho, byte[] dados) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(caminho, "rw")) {
            raf.setLength(0);
            raf.write(dados);
        }
    }

    private void mostrarResultado(String algoritmo, int original, int comprimido, long tempo) {
        double percentual = original == 0 ? 0 : 100.0 * (original - comprimido) / original;
        System.out.printf("%s: %d -> %d bytes | ganho/perda: %.2f%% | tempo: %.3f ms%n",
                algoritmo, original, comprimido, percentual, tempo / 1_000_000.0);
    }

    // Executa ambos os algoritmos sobre todos os bytes e guarda a próxima versão.
    public void comprimir() throws IOException {
        System.out.println("\n=====================================");
        System.out.println("  TP3 - COMPRESSÃO DE DADOS");
        System.out.println("=====================================");
        System.out.println("Arquivo original: " + nomeArquivo);
        System.out.println("Lendo todos os bytes do arquivo binário...");

        byte[] original = lerBytes(nomeArquivo);
        int versao = proximaVersao();

        System.out.println("Tamanho original: " + original.length + " bytes");
        System.out.println("Nova versão de compressão: " + versao);

        System.out.println("\n[1/4] Executando compressão Huffman...");
        long inicio = System.nanoTime();
        byte[] huffman = Huffman.comprimir(original);
        long tempoHuffman = System.nanoTime() - inicio;
        System.out.println("Compressão Huffman concluída.");

        System.out.println("\n[2/4] Executando compressão LZW...");
        inicio = System.nanoTime();
        byte[] lzw = LZW.comprimir(original);
        long tempoLZW = System.nanoTime() - inicio;
        System.out.println("Compressão LZW concluída.");

        String destinoHuffman = nomeVersao("Huffman", versao);
        String destinoLZW = nomeVersao("LZW", versao);

        System.out.println("\n[3/4] Gravando os arquivos compactados...");

        try {
            escreverBytes(destinoHuffman, huffman);
            escreverBytes(destinoLZW, lzw);
        } catch (IOException e) {
            new File(destinoHuffman).delete();
            new File(destinoLZW).delete();
            throw e;
        }

        System.out.println("Arquivo Huffman criado: " + destinoHuffman);
        System.out.println("Arquivo LZW criado: " + destinoLZW);

        System.out.println("\n[4/4] Comparando os resultados...");
        System.out.println("-------------------------------------");
        System.out.println("Versão criada com sucesso: " + versao);

        mostrarResultado("Huffman", original.length, huffman.length, tempoHuffman);
        mostrarResultado("LZW", original.length, lzw.length, tempoLZW);

        if (huffman.length == lzw.length) {
            System.out.println("Empate na taxa de compressão.");
        } else {
            System.out.println("Melhor compressão: "
                    + (huffman.length < lzw.length ? "Huffman" : "LZW"));
        }

        if (tempoHuffman == tempoLZW) {
            System.out.println("Empate no tempo de compressão.");
        } else {
            System.out.println("Mais rápido: "
                    + (tempoHuffman < tempoLZW ? "Huffman" : "LZW"));
        }

        System.out.println("-------------------------------------");
        System.out.println("O arquivo original foi preservado.");
        System.out.println("Os compactados estão na pasta dados/.");
    }

    // Confere os resultados dos dois algoritmos antes de substituir dados.bin.
    public void descomprimir(int versao, Scanner scanner) throws IOException {
        if (versao <= 0) {
            throw new IllegalArgumentException("Versão inválida.");
        }

        String origemHuffman = nomeVersao("Huffman", versao);
        String origemLZW = nomeVersao("LZW", versao);

        if (!new File(origemHuffman).isFile() || !new File(origemLZW).isFile()) {
            throw new IOException("Os dois arquivos da versão precisam existir.");
        }

        System.out.println("\n=====================================");
        System.out.println("  TP3 - DESCOMPRESSÃO DE DADOS");
        System.out.println("=====================================");
        System.out.println("Versão selecionada: " + versao);
        System.out.println("Arquivo Huffman: " + origemHuffman);
        System.out.println("Arquivo LZW: " + origemLZW);

        System.out.println("\n[1/3] Descomprimindo com Huffman...");
        long inicio = System.nanoTime();
        byte[] dadosHuffman = Huffman.descomprimir(lerBytes(origemHuffman));
        long tempoHuffman = System.nanoTime() - inicio;
        System.out.println("Huffman concluído: " + dadosHuffman.length + " bytes recuperados.");

        System.out.println("\n[2/3] Descomprimindo com LZW...");
        inicio = System.nanoTime();
        byte[] dadosLZW = LZW.descomprimir(lerBytes(origemLZW));
        long tempoLZW = System.nanoTime() - inicio;
        System.out.println("LZW concluído: " + dadosLZW.length + " bytes recuperados.");

        System.out.println("\n[3/3] Conferindo a integridade dos resultados...");

        if (!Arrays.equals(dadosHuffman, dadosLZW)) {
            throw new IOException("Resultados diferentes. Restauração cancelada.");
        }

        System.out.printf("Huffman: %.3f ms%n", tempoHuffman / 1_000_000.0);
        System.out.printf("LZW: %.3f ms%n", tempoLZW / 1_000_000.0);
        System.out.println("Mais rápido: "
                + (tempoHuffman <= tempoLZW ? "Huffman" : "LZW"));
        System.out.println("Integridade: resultados idênticos ("
                + dadosHuffman.length + " bytes).");

        System.out.println("\nAtenção: a restauração substituirá " + nomeArquivo);
        System.out.println("1. Restaurar Huffman");
        System.out.println("2. Restaurar LZW");
        System.out.println("0. Cancelar");
        System.out.print("Escolha: ");

        int escolha = Integer.parseInt(scanner.nextLine());

        if (escolha == 0) {
            System.out.println("Restauração cancelada. Arquivo original preservado.");
            return;
        }

        if (escolha != 1 && escolha != 2) {
            throw new IllegalArgumentException("Opção inválida.");
        }

        byte[] restaurado = escolha == 1 ? dadosHuffman : dadosLZW;
        File original = new File(nomeArquivo);
        Path temporario = Files.createTempFile(original.getParentFile().toPath(),
                "restauracao-", ".tmp");

        try {
            System.out.println("\nSubstituindo o arquivo de dados pela versão " + versao + "...");
            escreverBytes(temporario.toString(), restaurado);
            Files.move(temporario, original.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporario);
        }

        System.out.println("Arquivo restaurado: " + nomeArquivo);
        System.out.println("Reconstruindo a Árvore B+ e as listas invertidas...");
        arquivoBinario.reconstruirIndices();
        System.out.println("Reconstrução dos índices concluída.");
        System.out.println("Descompressão finalizada com sucesso.");
    }

    public void listarVersoes() {
        System.out.println("\n--- VERSÕES DE COMPRESSÃO ---");
        System.out.println("Diretório dos arquivos: " + new File(nomeArquivo).getParent());

        int total = 0;

        for (int i = 1; i < proximaVersao(); i++) {
            boolean huffman = new File(nomeVersao("Huffman", i)).isFile();
            boolean lzw = new File(nomeVersao("LZW", i)).isFile();

            if (huffman || lzw) {
                System.out.println("Versão " + i + ": Huffman "
                        + (huffman ? "OK" : "ausente")
                        + ", LZW " + (lzw ? "OK" : "ausente"));

                if (huffman) System.out.println("  " + nomeVersao("Huffman", i));
                if (lzw) System.out.println("  " + nomeVersao("LZW", i));

                total++;
            }
        }

        if (total == 0) {
            System.out.println("Nenhuma versão encontrada.");
        }
    }

    public void menu(Scanner scanner) {
        int opcao = -1;

        do {
            try {
                System.out.println("\n=====================================");
                System.out.println("  TP3 - COMPRESSÃO DE DADOS");
                System.out.println("=====================================");
                System.out.println("1. Comprimir (Huffman e LZW)");
                System.out.println("2. Descomprimir versão");
                System.out.println("3. Listar versões");
                System.out.println("0. Voltar");
                System.out.print("Escolha: ");

                opcao = Integer.parseInt(scanner.nextLine());

                switch (opcao) {
                    case 1:
                        comprimir();
                        break;

                    case 2:
                        System.out.print("Versão: ");
                        descomprimir(Integer.parseInt(scanner.nextLine()), scanner);
                        break;

                    case 3:
                        listarVersoes();
                        break;

                    case 0:
                        break;

                    default:
                        System.out.println("Opção inválida.");
                }

            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Operação não concluída: " + e.getMessage());
            }

        } while (opcao != 0);
    }
}
