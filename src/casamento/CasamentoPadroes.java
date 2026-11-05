package casamento;

import entidade.Filme;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CasamentoPadroes {
    // Percorre os filmes ativos e pesquisa no nome.
    private static void buscar(String caminhoArquivo, String padrao, boolean usarKMP) throws IOException {
        String algoritmo = usarKMP ? "KMP" : "Rabin-Karp";
        int filmesEncontrados = 0;
        int totalOcorrencias = 0;
        long inicio = System.nanoTime();

        try (RandomAccessFile arquivo = new RandomAccessFile(caminhoArquivo, "r")) {
            if (arquivo.length() < 4) {
                throw new IOException("Cabeçalho do arquivo binário inválido.");
            }
            arquivo.seek(4);
            while (arquivo.getFilePointer() < arquivo.length()) {
                long posicao = arquivo.getFilePointer();
                if (arquivo.length() - posicao < 5) {
                    throw new IOException("Registro incompleto na posição " + posicao);
                }
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readInt();
                if (tamanho < 0 || tamanho > arquivo.length() - arquivo.getFilePointer()) {
                    throw new IOException("Tamanho inválido na posição " + posicao);
                }
                if (lapide == ' ') {
                    byte[] dados = new byte[tamanho];
                    arquivo.readFully(dados);
                    Filme filme = new Filme();
                    filme.fromByteArray(dados);
                    int ocorrencias = usarKMP
                            ? KMP.contarOcorrencias(filme.getNome(), padrao)
                            : RabinKarp.contarOcorrencias(filme.getNome(), padrao);
                    if (ocorrencias > 0) {
                        filmesEncontrados++;
                        totalOcorrencias += ocorrencias;
                        System.out.printf("ID: %5d", filme.getId());
                        System.out.println(" | Nome: " + filme.getNome() + " | Ocorrências: " + ocorrencias);
                    }
                } else {
                    arquivo.seek(arquivo.getFilePointer() + tamanho);
                }
            }
        }
        long fim = System.nanoTime();

        System.out.println("\n--- Resultados da Busca ---");
        System.out.println("Algoritmo: " + algoritmo);
        System.out.println("Filmes encontrados: " + filmesEncontrados);
        System.out.println("Total de ocorrências: " + totalOcorrencias);
        System.out.printf("Tempo total: %.3f ms%n", (fim - inicio) / 1000000.0);
    }

    // Executa o algoritmo de casamento selecionado.
    public static void executar(String caminhoArquivo, String padrao, int algoritmo) throws IOException {
        if (padrao == null || padrao.isEmpty()) {
            throw new IllegalArgumentException("O padrão não pode ser vazio.");
        }

        switch (algoritmo) {
            case 1: {
                System.out.println("\n--- Busca KMP ---");
                buscar(caminhoArquivo, padrao, true);
                break;
            }
            case 2: {
                System.out.println("\n--- Busca Rabin-Karp ---");
                buscar(caminhoArquivo, padrao, false);
                break;
            }
            default: {
                System.out.println("Algoritmo inválido.");
                break;
            }
        }
    }
}
