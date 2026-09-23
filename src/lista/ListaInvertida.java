package lista;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

public class ListaInvertida {
    private String nomeArquivo;

    public ListaInvertida(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public void inicializar() {
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
            if (raf.length() == 0) raf.writeLong(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void limpar() {
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
            raf.setLength(0);
            raf.writeLong(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // O CSV também usa espaço não separável (\u00A0) antes de alguns gêneros.
    private String normalizar(String termo) {
        return termo.replace('\u00A0', ' ').trim().toLowerCase(Locale.ROOT);
    }

    // Retorna a posição do campo que aponta para a primeira ocorrência do termo.
    private long localizarTermo(RandomAccessFile raf, String termo) throws IOException {
        raf.seek(0);
        long atual = raf.readLong();
        long resultado = -1;
        while (atual != -1 && resultado == -1) {
            raf.seek(atual);
            String palavra = raf.readUTF();
            long campoLista = raf.getFilePointer();
            raf.readLong(); // Primeira ocorrência.
            atual = raf.readLong(); // Próximo termo do dicionário.
            if (palavra.equals(termo)) resultado = campoLista;
        }
        return resultado;
    }

    public void inserir(String termo, int id, long posicaoDados) {
        inserir(termo, id, posicaoDados, false);
    }

    // IDs novos não precisam ser procurados entre as ocorrências já gravadas.
    public void inserir(String termo, int id, long posicaoDados, boolean novo) {
        termo = normalizar(termo);
        if (!termo.isEmpty()) {
            try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
                long campoLista = localizarTermo(raf, termo);
                if (campoLista == -1) {
                    raf.seek(0);
                    long primeiroTermo = raf.readLong();
                    long novoTermo = raf.length();
                    raf.seek(novoTermo);
                    raf.writeUTF(termo);
                    campoLista = raf.getFilePointer();
                    raf.writeLong(-1);
                    raf.writeLong(primeiroTermo);
                    raf.seek(0);
                    raf.writeLong(novoTermo);
                }
                raf.seek(campoLista);
                long primeira = raf.readLong();
                long atual = primeira;
                boolean encontrado = false;
                while (!novo && atual != -1 && !encontrado) {
                    raf.seek(atual);
                    int idAtual = raf.readInt();
                    raf.readLong();
                    long proxima = raf.readLong();
                    if (idAtual == id) {
                        // Atualiza ou reativa a ocorrência, sem repetir o mesmo ID.
                        raf.seek(atual + 4);
                        raf.writeLong(posicaoDados);
                        raf.seek(atual + 20);
                        raf.writeBoolean(true);
                        encontrado = true;
                    }
                    atual = proxima;
                }
                if (!encontrado) {
                    long nova = raf.length();
                    raf.seek(nova);
                    raf.writeInt(id);
                    raf.writeLong(posicaoDados);
                    raf.writeLong(primeira);
                    raf.writeBoolean(true);
                    raf.seek(campoLista);
                    raf.writeLong(nova);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void remover(String termo, int id) {
        termo = normalizar(termo);
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
            long campoLista = localizarTermo(raf, termo);
            long atual = -1;
            if (campoLista != -1) {
                raf.seek(campoLista);
                atual = raf.readLong();
            }
            boolean encontrado = false;
            while (atual != -1 && !encontrado) {
                raf.seek(atual);
                int idAtual = raf.readInt();
                raf.readLong();
                long proxima = raf.readLong();
                if (idAtual == id) {
                    raf.writeBoolean(false);
                    encontrado = true;
                }
                atual = proxima;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long[] buscar(String termo) {
        long[] resultado = new long[0];
        termo = normalizar(termo);
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "r")) {
            long campoLista = localizarTermo(raf, termo);
            if (campoLista != -1) {
                raf.seek(campoLista);
                long primeira = raf.readLong();
                long atual = primeira;
                int quantidade = 0;
                while (atual != -1) {
                    raf.seek(atual + 12);
                    atual = raf.readLong();
                    if (raf.readBoolean()) quantidade++;
                }
                resultado = new long[quantidade];
                atual = primeira;
                int i = 0;
                while (atual != -1) {
                    raf.seek(atual + 4);
                    long posicao = raf.readLong();
                    atual = raf.readLong();
                    if (raf.readBoolean()) resultado[i++] = posicao;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    // Interseção: o registro precisa estar nas duas listas da pesquisa.
    public static long[] intersecao(long[] primeira, long[] segunda) {
        long[] auxiliar = new long[primeira.length];
        int quantidade = 0;
        for (int i = 0; i < primeira.length; i++) {
            boolean encontrado = false;
            for (int j = 0; j < segunda.length && !encontrado; j++) {
                if (primeira[i] == segunda[j]) encontrado = true;
            }
            if (encontrado) auxiliar[quantidade++] = primeira[i];
        }
        long[] resultado = new long[quantidade];
        for (int i = 0; i < quantidade; i++) resultado[i] = auxiliar[i];
        return resultado;
    }
}