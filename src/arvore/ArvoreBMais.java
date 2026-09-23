package arvore;

import java.io.RandomAccessFile;
import java.io.IOException;

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
        if (ordem < 3) {
            throw new IllegalArgumentException("A ordem da árvore deve ser pelo menos 3.");
        }
        this.nomeArquivo = nomeArquivo;
        this.ordem = ordem;
        this.posicaoRaiz = -1;
    }

    // Abre a raiz existente ou grava a primeira folha vazia.
    public void inicializar() {
        try (RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw")) {
            if (raf.length() == 0) {
                this.posicaoRaiz = 8;
                raf.writeLong(this.posicaoRaiz);
                raf.write(new NoBMais(this.ordem).toByteArray());
            } else {
                this.posicaoRaiz = raf.readLong();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao inicializar a árvore B+: " + e.getMessage(), e);
        }
    }

    // Reinicia o índice para carga ou reconstrução a partir dos dados.
    public void limpar() {
        try (RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw")) {
            raf.setLength(0);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao limpar a árvore B+", e);
        }
        inicializar();
    }

    // Uma promoção que chega ao topo cria uma nova raiz.
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
            throw new IllegalStateException("Erro na árvore B+: " + e.getMessage(), e);
        }
    }

    // Desce até a folha e propaga divisões de nós para o pai.
    private Promocao inserirRecursivo(long posAtual, int id, long ptr) throws IOException {
        NoBMais no = lerNo(posAtual);
        Promocao resultado = null;

        if (no.folha) {
            int i = 0;
            while (i < no.numChaves && no.chaves[i] < id) i++;
            if (i < no.numChaves && no.chaves[i] == id) {
                // Um ID existente recebe o novo endereço, sem duplicar a chave.
                no.ponteirosDados[i] = ptr;
                escreverNo(no, posAtual);
            } else {
                inserirNaFolha(no, id, ptr);
                if (no.numChaves == this.ordem) {
                    resultado = splitFolha(no, posAtual);
                } else {
                    escreverNo(no, posAtual);
                }
            }
        } else {
            int i = 0;
            // O separador é a primeira chave da folha à direita.
            while (i < no.numChaves && id >= no.chaves[i]) i++;
            Promocao promo = inserirRecursivo(no.filhos[i], id, ptr);
            if (promo != null) {
                inserirNoInterno(no, promo.chave, promo.filhoDireito);
                if (no.numChaves == this.ordem) {
                    resultado = splitInterno(no, posAtual);
                } else {
                    escreverNo(no, posAtual);
                }
            }
        }
        return resultado;
    }

    // Abre espaço para inserir o par ID/endereço na ordem das chaves.
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

    // Insere o separador promovido e a referência ao novo filho direito.
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

    // Divide a folha e copia sua primeira chave à direita para o pai.
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

    // Retira a chave central do nó e a promove ao pai.
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

    // Lê uma página completa no endereço gravado pelo índice.
    private NoBMais lerNo(long posicao) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "r");
        raf.seek(posicao);
        int tamanho = 1 + 4 + (this.ordem * 12) + ((this.ordem + 1) * 8) + 8;
        byte[] ba = new byte[tamanho];
        raf.readFully(ba);
        raf.close();
        
        NoBMais no = new NoBMais(this.ordem);
        no.fromByteArray(ba);
        return no;
    }

    // Sobrescreve ou acrescenta a página no endereço informado.
    private void escreverNo(NoBMais no, long posicao) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
        raf.seek(posicao);
        raf.write(no.toByteArray());
        raf.close();
    }

    // O fim do arquivo é o endereço disponível para uma nova página.
    private long tamanhoArquivo() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "r");
        long tam = raf.length();
        raf.close();
        return tam;
    }

    // A raiz pode mudar depois de uma divisão no topo.
    private void atualizarCabecalho() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
        raf.seek(0);
        raf.writeLong(this.posicaoRaiz);
        raf.close();
    }
    
    // Retorna o endereço dos dados ou -1 quando não há registro ativo.
    public long buscar(int id) {
        long resultado = -1;
        try {
            if (this.posicaoRaiz != -1) {
                resultado = buscarRecursivo(this.posicaoRaiz, id);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao buscar na árvore B+", e);
        }
        return resultado;
    }

    // Os separadores orientam a descida; os endereços estão nas folhas.
    private long buscarRecursivo(long posAtual, int id) throws IOException {
        NoBMais no = lerNo(posAtual);
        long resultado = -1;
        int i = 0;
        if (no.folha) {
            while (i < no.numChaves && no.chaves[i] < id) i++;
            if (i < no.numChaves && no.chaves[i] == id) {
                resultado = no.ponteirosDados[i];
            }
        } else {
            while (i < no.numChaves && id >= no.chaves[i]) i++;
            resultado = buscarRecursivo(no.filhos[i], id);
        }
        return resultado;
    }

    // Exclusão lógica: mantém o separador, mas invalida o endereço na folha.
    public boolean remover(int id) {
        boolean removido = false;
        if (buscar(id) != -1) {
            inserir(id, -1);
            removido = true;
        }
        return removido;
    }
}
