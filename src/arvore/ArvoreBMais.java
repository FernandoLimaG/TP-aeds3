package arvore;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;

public class ArvoreBMais {
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
            System.out.println("Erro ao inicializar Árvore B+: " + e.getMessage());
        }
    }

    // TODO: Método Inserir (Onde faremos a busca da folha correta e o Split)
    // TODO: Método Buscar (Usando o índice para achar o endereço no dados.bin)
}