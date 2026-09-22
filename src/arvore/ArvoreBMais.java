package arvore;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;

public class ArvoreBMais {

    private static class Promocao {
        int chave;
        long filhoDireito;

        Promocao(int chave, long filhoDireito) {
            this.chave = chave;
            this.filhoDireito = filhoDireito;
        }
    }

    private String nomeArquivo;
    private int ordem;
    private long posicaoRaiz;

    public ArvoreBMais(String nomeArquivo, int ordem) {
        this.nomeArquivo = nomeArquivo;
        this.ordem = ordem;
        this.posicaoRaiz = -1;
    }

    public void inicializar() {
        try {
            File file = new File(this.nomeArquivo);
            if (!file.exists()) {
                RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
                NoBMais raiz = new NoBMais(this.ordem);
                byte[] ba = raiz.toByteArray();
                this.posicaoRaiz = 8;
                raf.writeLong(this.posicaoRaiz);
                raf.write(ba);
                raf.close();
            } else {
                RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "r");
                this.posicaoRaiz = raf.readLong();
                raf.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void inserir(int id, long ponteiroDados) {
        try {
            Promocao promocao = inserirRecursivo(this.posicaoRaiz, id, ponteiroDados);
            if (promocao != null) {
                NoBMais novaRaiz = new NoBMais(this.ordem);
                novaRaiz.folha = false;
                novaRaiz.chaves[0] = promocao.chave;
                novaRaiz.filhos[0] = this.posicaoRaiz;
                novaRaiz.filhos[1] = promocao.filhoDireito;
                novaRaiz.numChaves = 1;
                
                long novaPosicao = tamanhoArquivo();
                escreverNo(novaRaiz, novaPosicao);
                
                this.posicaoRaiz = novaPosicao;
                atualizarCabecalho();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Promocao inserirRecursivo(long posAtual, int id, long ptr) throws IOException {
        NoBMais no = lerNo(posAtual);
        
        if (no.folha) {
            inserirNaFolha(no, id, ptr);
            if (no.numChaves == this.ordem) {
                return splitFolha(no, posAtual);
            } else {
                escreverNo(no, posAtual);
                return null;
            }
        } else {
            int i = 0;
            while (i < no.numChaves && id > no.chaves[i]) {
                i++;
            }
            Promocao promo = inserirRecursivo(no.filhos[i], id, ptr);
            if (promo != null) {
                inserirNoInterno(no, promo.chave, promo.filhoDireito);
                if (no.numChaves == this.ordem) {
                    return splitInterno(no, posAtual);
                } else {
                    escreverNo(no, posAtual);
                    return null;
                }
            }
            return null;
        }
    }

    private void inserirNaFolha(NoBMais no, int id, long ptr) {
        int i = no.numChaves - 1;
        while (i >= 0 && no.chaves[i] > id) {
            no.chaves[i + 1] = no.chaves[i];
            no.ponteirosDados[i + 1] = no.ponteirosDados[i];
            i--;
        }
        no.chaves[i + 1] = id;
        no.ponteirosDados[i + 1] = ptr;
        no.numChaves++;
    }

    private void inserirNoInterno(NoBMais no, int chave, long filhoDireito) {
        int i = no.numChaves - 1;
        while (i >= 0 && no.chaves[i] > chave) {
            no.chaves[i + 1] = no.chaves[i];
            no.filhos[i + 2] = no.filhos[i + 1];
            i--;
        }
        no.chaves[i + 1] = chave;
        no.filhos[i + 2] = filhoDireito;
        no.numChaves++;
    }

    private Promocao splitFolha(NoBMais no, long posAtual) throws IOException {
        int meio = this.ordem / 2;
        NoBMais novoNo = new NoBMais(this.ordem);
        novoNo.folha = true;
        
        for (int i = meio; i < this.ordem; i++) {
            novoNo.chaves[i - meio] = no.chaves[i];
            novoNo.ponteirosDados[i - meio] = no.ponteirosDados[i];
            novoNo.numChaves++;
            no.chaves[i] = -1;
            no.ponteirosDados[i] = -1;
        }
        no.numChaves = meio;
        novoNo.proximaFolha = no.proximaFolha;
        
        long novaPos = tamanhoArquivo();
        no.proximaFolha = novaPos;
        
        escreverNo(novoNo, novaPos);
        escreverNo(no, posAtual);
        
        return new Promocao(novoNo.chaves[0], novaPos);
    }

    private Promocao splitInterno(NoBMais no, long posAtual) throws IOException {
        int meio = this.ordem / 2;
        NoBMais novoNo = new NoBMais(this.ordem);
        novoNo.folha = false;
        
        int chavePromovida = no.chaves[meio];
        
        for (int i = meio + 1; i < this.ordem; i++) {
            novoNo.chaves[i - (meio + 1)] = no.chaves[i];
            novoNo.filhos[i - (meio + 1)] = no.filhos[i];
            novoNo.numChaves++;
            no.chaves[i] = -1;
            no.filhos[i] = -1;
        }
        novoNo.filhos[this.ordem - (meio + 1)] = no.filhos[this.ordem];
        no.filhos[this.ordem] = -1;
        
        no.chaves[meio] = -1;
        no.numChaves = meio;
        
        long novaPos = tamanhoArquivo();
        escreverNo(novoNo, novaPos);
        escreverNo(no, posAtual);
        
        return new Promocao(chavePromovida, novaPos);
    }

    private NoBMais lerNo(long posicao) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "r");
        raf.seek(posicao);
        int tamanho = 1 + 4 + (this.ordem * 12) + ((this.ordem + 1) * 8) + 8;
        byte[] ba = new byte[tamanho];
        raf.read(ba);
        raf.close();
        
        NoBMais no = new NoBMais(this.ordem);
        no.fromByteArray(ba);
        return no;
    }

    private void escreverNo(NoBMais no, long posicao) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
        raf.seek(posicao);
        raf.write(no.toByteArray());
        raf.close();
    }

    private long tamanhoArquivo() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "r");
        long tam = raf.length();
        raf.close();
        return tam;
    }

    private void atualizarCabecalho() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
        raf.seek(0);
        raf.writeLong(this.posicaoRaiz);
        raf.close();
    }
    
    public long buscar(int id) {
        if (this.posicaoRaiz == -1) return -1;
        try {
            return buscarRecursivo(this.posicaoRaiz, id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    private long buscarRecursivo(long posAtual, int id) throws IOException {
        NoBMais no = lerNo(posAtual);

        if (no.folha) {
            for (int i = 0; i < no.numChaves; i++) {
                if (no.chaves[i] == id) {
                    return no.ponteirosDados[i];
                }
            }
            return -1;
        } else {
            int i = 0;
            while (i < no.numChaves && id >= no.chaves[i]) {
                i++;
            }
            return buscarRecursivo(no.filhos[i], id);
        }
    }
}