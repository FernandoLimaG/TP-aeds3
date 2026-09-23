package io;

import entidade.Filme;
import arvore.ArvoreBMais;
import lista.ListaInvertida;
import java.io.RandomAccessFile;
import java.io.IOException;

public class ArquivoBinario {
    private String nomeArquivo;
    private ArvoreBMais arvore;
    private ListaInvertida listaGeneros;
    private ListaInvertida listaPaises;

    public ArquivoBinario(String nomeArquivo, ArvoreBMais arvore,
                          ListaInvertida listaGeneros, ListaInvertida listaPaises) {
        this.nomeArquivo = nomeArquivo;
        this.arvore = arvore;
        this.listaGeneros = listaGeneros;
        this.listaPaises = listaPaises;
    }

    public void inicializar() {
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
            if (raf.length() == 0) raf.writeInt(0);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao inicializar dados", e);
        }
    }

    public void limpar() {
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
            raf.setLength(0);
            raf.writeInt(0);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao limpar dados", e);
        }
        arvore.limpar();
        listaGeneros.limpar();
        listaPaises.limpar();
    }

    private void indexar(Filme filme, long posicao, boolean novo) {
        arvore.inserir(filme.getId(), posicao);
        String[] generos = filme.getGeneros();
        for (int i = 0; i < generos.length; i++) {
            String genero = generos[i].replace('\u00A0', ' ').trim();
            boolean repetido = false;
            for (int j = 0; j < i && !repetido; j++) {
                repetido = genero.equalsIgnoreCase(generos[j].replace('\u00A0', ' ').trim());
            }
            if (!repetido) listaGeneros.inserir(genero, filme.getId(), posicao, novo);
        }
        listaPaises.inserir(filme.getPais(), filme.getId(), posicao, novo);
    }

    private void removerDasListas(Filme filme) {
        String[] generos = filme.getGeneros();
        for (int i = 0; i < generos.length; i++) {
            listaGeneros.remover(generos[i], filme.getId());
        }
        listaPaises.remover(filme.getPais(), filme.getId());
    }

    // Necessário depois de ordenar: as posições físicas dos registros mudam.
    public void reconstruirIndices() {
        arvore.limpar();
        listaGeneros.limpar();
        listaPaises.limpar();
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "r")) {
            raf.seek(4);
            while (raf.getFilePointer() < raf.length()) {
                long posicao = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                if (tamanho < 0 || tamanho > raf.length() - raf.getFilePointer()) {
                    throw new IOException("Tamanho inválido de registro.");
                }
                if (lapide == ' ') {
                    byte[] ba = new byte[tamanho];
                    raf.readFully(ba);
                    Filme filme = new Filme();
                    filme.fromByteArray(ba);
                    indexar(filme, posicao, true);
                } else {
                    raf.seek(raf.getFilePointer() + tamanho);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao reconstruir índices", e);
        }
    }

    public long inserir(Filme filme) {
        long posicao;
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
            int ultimoId = raf.readInt();
            if (filme.getId() <= ultimoId) {
                throw new IllegalArgumentException("O novo ID deve ser maior que o último ID utilizado.");
            }
            byte[] ba = filme.toByteArray();
            posicao = raf.length();
            raf.seek(posicao);
            raf.writeByte(' ');
            raf.writeInt(ba.length);
            raf.write(ba);
            raf.seek(0);
            raf.writeInt(filme.getId());
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao inserir registro", e);
        }
        indexar(filme, posicao, true);
        return posicao;
    }

    // A posição recebida vem da árvore ou de uma das listas invertidas.
    public Filme lerNaPosicao(long posicao) {
        Filme filme = null;
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "r")) {
            if (posicao >= 4 && posicao <= raf.length() - 5) {
                raf.seek(posicao);
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                if (tamanho < 0 || tamanho > raf.length() - raf.getFilePointer()) {
                    throw new IOException("Tamanho inválido de registro.");
                }
                if (lapide == ' ') {
                    byte[] ba = new byte[tamanho];
                    raf.readFully(ba);
                    filme = new Filme();
                    filme.fromByteArray(ba);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler registro", e);
        }
        return filme;
    }

    // TP1: percorre os registros até encontrar o ID ativo solicitado.
    private long localizarSequencial(int idBuscado) {
        long posicao = -1;
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "r")) {
            raf.seek(4);
            while (raf.getFilePointer() < raf.length() && posicao == -1) {
                long inicio = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                if (tamanho < 0 || tamanho > raf.length() - raf.getFilePointer()) {
                    throw new IOException("Tamanho inválido de registro.");
                }
                byte[] ba = new byte[tamanho];
                raf.readFully(ba);
                if (lapide == ' ') {
                    Filme filme = new Filme();
                    filme.fromByteArray(ba);
                    if (filme.getId() == idBuscado) posicao = inicio;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro na busca sequencial", e);
        }
        return posicao;
    }

    public Filme ler(int idBuscado) {
        return lerNaPosicao(localizarSequencial(idBuscado));
    }

    // A localização continua sequencial; a gravação também sincroniza os índices.
    public boolean deletar(int idBuscado) {
        return deletar(idBuscado, localizarSequencial(idBuscado));
    }

    public boolean atualizar(Filme atualizado) {
        return atualizar(atualizado, localizarSequencial(atualizado.getId()));
    }

    public Filme lerComIndice(int idBuscado, ArvoreBMais arvore) {
        Filme filme = lerNaPosicao(arvore.buscar(idBuscado));
        if (filme != null && filme.getId() != idBuscado) filme = null;
        return filme;
    }

    public boolean deletar(int idBuscado, long posicao) {
        boolean sucesso = false;
        Filme filme = lerNaPosicao(posicao);
        if (filme != null && filme.getId() == idBuscado) {
            try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
                raf.seek(posicao);
                raf.writeByte('*');
            } catch (IOException e) {
                throw new IllegalStateException("Erro ao deletar registro", e);
            }
            arvore.remover(idBuscado);
            removerDasListas(filme);
            sucesso = true;
        }
        return sucesso;
    }

    public boolean atualizar(Filme atualizado, long posicao) {
        boolean sucesso = false;
        Filme antigo = lerNaPosicao(posicao);
        if (antigo != null && antigo.getId() == atualizado.getId()) {
            long novaPosicao = posicao;
            try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "rw")) {
                raf.seek(posicao + 1);
                int tamanhoAntigo = raf.readInt();
                byte[] ba = atualizado.toByteArray();
                if (ba.length == tamanhoAntigo) {
                    raf.write(ba);
                } else {
                    novaPosicao = raf.length();
                    raf.seek(novaPosicao);
                    raf.writeByte(' ');
                    raf.writeInt(ba.length);
                    raf.write(ba);
                    raf.seek(posicao);
                    raf.writeByte('*');
                }
            } catch (IOException e) {
                throw new IllegalStateException("Erro ao atualizar registro", e);
            }
            removerDasListas(antigo);
            indexar(atualizado, novaPosicao, false);
            sucesso = true;
        }
        return sucesso;
    }
}
