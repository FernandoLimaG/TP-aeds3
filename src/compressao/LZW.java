
package compressao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LZW {

    private static final int LIMITE = 65536;
    private static final int TAMANHO_TABELA = 131071;

    private static class Dicionario {
        String[] palavras = new String[TAMANHO_TABELA];
        int[] codigos = new int[TAMANHO_TABELA];

        int posicao(String palavra) {
            long hash = 0;

            for (int i = 0; i < palavra.length(); i++) {
                hash = (hash * 31 + palavra.charAt(i)) % TAMANHO_TABELA;
            }

            int pos = (int) hash;

            while (palavras[pos] != null && !palavras[pos].equals(palavra)) {
                pos = (pos + 1) % TAMANHO_TABELA;
            }

            return pos;
        }

        void inserir(String palavra, int codigo) {
            int pos = posicao(palavra);
            palavras[pos] = palavra;
            codigos[pos] = codigo;
        }

        int buscar(String palavra) {
            int pos = posicao(palavra);

            return palavras[pos] == null ? -1 : codigos[pos];
        }
    }

    // Usa dicionário inicial com os 256 bytes e códigos fixos de 16 bits.
    public static byte[] comprimir(byte[] dados) throws IOException {
        Dicionario dicionario = new Dicionario();

        for (int i = 0; i < 256; i++) {
            dicionario.inserir(String.valueOf((char) i), i);
        }

        int proximoCodigo = 256;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream saida = new DataOutputStream(buffer);

        saida.writeInt(0x4C5A5731);
        saida.writeInt(dados.length);

        String atual = "";

        for (byte dado : dados) {
            String combinado = atual + (char) (dado & 255);

            if (dicionario.buscar(combinado) != -1) {
                atual = combinado;
            } else {
                saida.writeShort(dicionario.buscar(atual));

                if (proximoCodigo < LIMITE) {
                    dicionario.inserir(combinado, proximoCodigo++);
                }

                atual = String.valueOf((char) (dado & 255));
            }
        }

        if (!atual.isEmpty()) {
            saida.writeShort(dicionario.buscar(atual));
        }

        saida.flush();
        return buffer.toByteArray();
    }

    // Reconstrói o dicionário na mesma ordem usada durante a compressão.
    public static byte[] descomprimir(byte[] compactado) throws IOException {
        DataInputStream entrada = new DataInputStream(
                new ByteArrayInputStream(compactado)
        );

        if (entrada.readInt() != 0x4C5A5731) {
            throw new IOException("Arquivo LZW inválido.");
        }

        int tamanho = entrada.readInt();

        if (tamanho < 0) {
            throw new IOException("Tamanho LZW inválido.");
        }

        String[] dicionario = new String[LIMITE];

        for (int i = 0; i < 256; i++) {
            dicionario[i] = String.valueOf((char) i);
        }

        int proximoCodigo = 256;
        ByteArrayOutputStream resultado = new ByteArrayOutputStream(tamanho);

        if (tamanho == 0) {
            if (entrada.available() != 0) {
                throw new IOException("LZW vazio inconsistente.");
            }

            return resultado.toByteArray();
        }

        int primeiro = entrada.readUnsignedShort();

        if (primeiro >= 256) {
            throw new IOException("Primeiro código LZW inválido.");
        }

        String anterior = dicionario[primeiro];
        escrever(resultado, anterior);

        while (resultado.size() < tamanho) {
            int codigo = entrada.readUnsignedShort();
            String trecho;

            if (codigo < proximoCodigo) {
                trecho = dicionario[codigo];
            } else if (codigo == proximoCodigo && proximoCodigo < LIMITE) {
                trecho = anterior + anterior.charAt(0);
            } else {
                throw new IOException("Código LZW inválido.");
            }

            if (resultado.size() + trecho.length() > tamanho) {
                throw new IOException("Tamanho descomprimido inválido.");
            }

            escrever(resultado, trecho);

            if (proximoCodigo < LIMITE) {
                dicionario[proximoCodigo++] = anterior + trecho.charAt(0);
            }

            anterior = trecho;
        }

        if (entrada.available() != 0) {
            throw new IOException("Códigos LZW extras.");
        }

        return resultado.toByteArray();
    }

    private static void escrever(ByteArrayOutputStream saida, String palavra) {
        for (int i = 0; i < palavra.length(); i++) {
            saida.write((byte) palavra.charAt(i));
        }
    }
}
