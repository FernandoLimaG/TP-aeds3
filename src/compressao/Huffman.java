
package compressao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Huffman {

    private static class No {
        int simbolo;
        int frequencia;
        int menorSimbolo;
        int esquerdo;
        int direito;

        No(int simbolo, int frequencia, int esquerdo, int direito, int menorSimbolo) {
            this.simbolo = simbolo;
            this.frequencia = frequencia;
            this.esquerdo = esquerdo;
            this.direito = direito;
            this.menorSimbolo = menorSimbolo;
        }
    }

    // Monta a árvore escolhendo repetidamente os dois nós de menor frequência.
    private static int construirArvore(int[] frequencias, No[] nos) {
        boolean[] ativos = new boolean[511];
        int quantidade = 0;

        for (int i = 0; i < 256; i++) {
            if (frequencias[i] > 0) {
                nos[quantidade] = new No(i, frequencias[i], -1, -1, i);
                ativos[quantidade++] = true;
            }
        }

        if (quantidade == 0) return -1;

        while (true) {
            int primeiro = -1;
            int segundo = -1;

            for (int i = 0; i < quantidade; i++) {
                if (!ativos[i]) continue;

                if (primeiro == -1 || comparar(nos[i], nos[primeiro]) < 0) {
                    segundo = primeiro;
                    primeiro = i;
                } else if (segundo == -1 || comparar(nos[i], nos[segundo]) < 0) {
                    segundo = i;
                }
            }

            if (segundo == -1) return primeiro;

            ativos[primeiro] = false;
            ativos[segundo] = false;

            nos[quantidade] = new No(
                    -1,
                    nos[primeiro].frequencia + nos[segundo].frequencia,
                    primeiro,
                    segundo,
                    Math.min(nos[primeiro].menorSimbolo, nos[segundo].menorSimbolo)
            );

            ativos[quantidade++] = true;
        }
    }

    private static int comparar(No a, No b) {
        if (a.frequencia != b.frequencia) {
            return Integer.compare(a.frequencia, b.frequencia);
        }

        return Integer.compare(a.menorSimbolo, b.menorSimbolo);
    }

    private static void gerarCodigos(No[] nos, int indice, String codigo, String[] codigos) {
        No no = nos[indice];

        if (no.esquerdo == -1) {
            codigos[no.simbolo] = codigo.isEmpty() ? "0" : codigo;
            return;
        }

        gerarCodigos(nos, no.esquerdo, codigo + "0", codigos);
        gerarCodigos(nos, no.direito, codigo + "1", codigos);
    }

    // Compacta os bytes completos do arquivo e grava as frequências no cabeçalho.
    public static byte[] comprimir(byte[] dados) throws IOException {
        int[] frequencias = new int[256];

        for (byte dado : dados) {
            frequencias[dado & 255]++;
        }

        No[] nos = new No[511];
        int raiz = construirArvore(frequencias, nos);
        String[] codigos = new String[256];

        if (raiz != -1) {
            gerarCodigos(nos, raiz, "", codigos);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream saida = new DataOutputStream(buffer);

        saida.writeInt(0x48554631);
        saida.writeInt(dados.length);

        for (int frequencia : frequencias) {
            saida.writeInt(frequencia);
        }

        int acumulador = 0;
        int bits = 0;

        for (byte dado : dados) {
            String codigo = codigos[dado & 255];

            for (int i = 0; i < codigo.length(); i++) {
                acumulador = (acumulador << 1) | (codigo.charAt(i) - '0');
                bits++;

                if (bits == 8) {
                    saida.writeByte(acumulador);
                    acumulador = 0;
                    bits = 0;
                }
            }
        }

        if (bits > 0) {
            saida.writeByte(acumulador << (8 - bits));
        }

        saida.flush();
        return buffer.toByteArray();
    }

    // Recria a mesma árvore e recupera a quantidade exata de bytes originais.
    public static byte[] descomprimir(byte[] compactado) throws IOException {
        DataInputStream entrada = new DataInputStream(
                new ByteArrayInputStream(compactado)
        );

        if (entrada.readInt() != 0x48554631) {
            throw new IOException("Arquivo Huffman inválido.");
        }

        int tamanho = entrada.readInt();

        if (tamanho < 0) {
            throw new IOException("Tamanho Huffman inválido.");
        }

        int[] frequencias = new int[256];
        long soma = 0;

        for (int i = 0; i < 256; i++) {
            frequencias[i] = entrada.readInt();

            if (frequencias[i] < 0) {
                throw new IOException("Frequência Huffman inválida.");
            }

            soma += frequencias[i];
        }

        if (soma != tamanho) {
            throw new IOException("Cabeçalho Huffman inconsistente.");
        }

        No[] nos = new No[511];
        int raiz = construirArvore(frequencias, nos);

        ByteArrayOutputStream resultado = new ByteArrayOutputStream(tamanho);

        if (tamanho == 0) {
            return resultado.toByteArray();
        }

        if (nos[raiz].esquerdo == -1) {
            for (int i = 0; i < tamanho; i++) {
                resultado.write(nos[raiz].simbolo);
            }

            return resultado.toByteArray();
        }

        int atual = raiz;

        while (resultado.size() < tamanho) {
            int valor = entrada.read();

            if (valor == -1) {
                throw new IOException("Arquivo Huffman incompleto.");
            }

            for (int bit = 7; bit >= 0 && resultado.size() < tamanho; bit--) {
                atual = ((valor >>> bit) & 1) == 0
                        ? nos[atual].esquerdo
                        : nos[atual].direito;

                if (atual == -1) {
                    throw new IOException("Código Huffman inválido.");
                }

                if (nos[atual].esquerdo == -1) {
                    resultado.write(nos[atual].simbolo);
                    atual = raiz;
                }
            }
        }

        return resultado.toByteArray();
    }
}
