package ordenacao;

import entidade.Filme;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;

public class OrdenacaoExterna {
    private String arquivoOriginal;
    private int caminhos;
    private int limiteMemoria;

    public OrdenacaoExterna(String arquivoOriginal, int caminhos, int limiteMemoria) {
        this.arquivoOriginal = arquivoOriginal;
        this.caminhos = caminhos;
        this.limiteMemoria = limiteMemoria;
    }

    // FASE 1: Lê o arquivo original, limpa os excluídos, ordena blocos e distribui
    public int distribuir() {
        int arquivosGerados = 0;
        
        try {
            RandomAccessFile raf = new RandomAccessFile(this.arquivoOriginal, "r");
            raf.seek(4); // Pula o cabeçalho
            
            Filme[] memoria = new Filme[limiteMemoria];
            int qtdAtual = 0;
            int caminhoAtual = 0;

            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                
                byte[] ba = new byte[tamanho];
                raf.read(ba);

                // Só processa se NÃO for um registro deletado (isso remove os espaços em branco)
                if (lapide == ' ') {
                    Filme filme = new Filme();
                    filme.fromByteArray(ba);
                    
                    memoria[qtdAtual] = filme;
                    qtdAtual++;
                    
                    // Se a memória encheu, ordena e joga para o arquivo temporário
                    if (qtdAtual == limiteMemoria) {
                        ordenarVetorMemoria(memoria, qtdAtual);
                        gravarArquivoTemporario(memoria, qtdAtual, caminhoAtual);
                        
                        arquivosGerados++;
                        caminhoAtual = (caminhoAtual + 1) % caminhos; // Alterna entre os caminhos
                        qtdAtual = 0; // Limpa a memória
                    }
                }
            }
            
            // Grava os registros que sobraram na memória e não preencheram um bloco completo
            if (qtdAtual > 0) {
                ordenarVetorMemoria(memoria, qtdAtual);
                gravarArquivoTemporario(memoria, qtdAtual, caminhoAtual);
                arquivosGerados++;
            }
            
            raf.close();
            
        } catch (IOException e) {
            System.out.println("Erro na fase de distribuição: " + e.getMessage());
        }
        
        return arquivosGerados;
    }

    // Ordenação manual em memória primária (Selection Sort por ID)
    private void ordenarVetorMemoria(Filme[] memoria, int tamanho) {
        for (int i = 0; i < tamanho - 1; i++) {
            int indiceMenor = i;
            for (int j = i + 1; j < tamanho; j++) {
                if (memoria[j].getId() < memoria[indiceMenor].getId()) {
                    indiceMenor = j;
                }
            }
            Filme temp = memoria[indiceMenor];
            memoria[indiceMenor] = memoria[i];
            memoria[i] = temp;
        }
    }

    // Grava o bloco ordenado em arquivo da rodada atual
    private void gravarArquivoTemporario(Filme[] memoria, int tamanho, int caminho) {
        // Nome padrão: temp_caminho.bin (ex: dados/temp_0.bin)
        String nomeArquivo = "dados/temp_0_" + caminho + ".bin"; 
        
        try {
            RandomAccessFile tempRaf = new RandomAccessFile(nomeArquivo, "rw");
            
            // Vai para o final do arquivo temporário (pois podemos escrever mais blocos nele)
            tempRaf.seek(tempRaf.length());
            
            for (int i = 0; i < tamanho; i++) {
                byte[] ba = memoria[i].toByteArray();
                tempRaf.writeByte(' ');
                tempRaf.writeInt(ba.length);
                tempRaf.write(ba);
            }
            
            tempRaf.close();
        } catch (IOException e) {
            System.out.println("Erro ao gravar arquivo temporário: " + e.getMessage());
        }
    }

    // FASE 2: Intercalação (Merge dos caminhos)
    public void intercalar() {
        try {
            // Arrays para controlar os arquivos e o registro atual de cada um
            RandomAccessFile[] arquivosTemp = new RandomAccessFile[this.caminhos];
            Filme[] filmesAtuais = new Filme[this.caminhos];
            boolean[] fimDeArquivo = new boolean[this.caminhos];
            int maxId = 0;

            // 1. Abre os arquivos temporários e lê o primeiro registro de cada
            for (int i = 0; i < this.caminhos; i++) {
                String nomeArquivo = "dados/temp_0_" + i + ".bin";
                File file = new File(nomeArquivo);
                
                if (file.exists()) {
                    arquivosTemp[i] = new RandomAccessFile(nomeArquivo, "r");
                    filmesAtuais[i] = lerProximoFilme(arquivosTemp[i]);
                    if (filmesAtuais[i] == null) {
                        fimDeArquivo[i] = true; // Arquivo já estava vazio
                    }
                } else {
                    fimDeArquivo[i] = true; // Arquivo não existe
                }
            }

            // 2. Prepara o NOVO arquivo principal limpo
            String nomeNovoArquivo = "dados/dados_ordenado.bin";
            File novoFile = new File(nomeNovoArquivo);
            if (novoFile.exists()) novoFile.delete();
            
            RandomAccessFile rafNovo = new RandomAccessFile(nomeNovoArquivo, "rw");
            rafNovo.writeInt(0); // Escreve um cabeçalho temporário

            // 3. Laço principal da Intercalação (encontra o menor ID entre os arquivos)
            while (true) {
                int indiceMenor = -1;
                int menorId = Integer.MAX_VALUE;

                // Varre o array buscando quem tem o menor ID na rodada atual
                for (int i = 0; i < this.caminhos; i++) {
                    if (!fimDeArquivo[i] && filmesAtuais[i] != null) {
                        if (filmesAtuais[i].getId() < menorId) {
                            menorId = filmesAtuais[i].getId();
                            indiceMenor = i;
                        }
                    }
                }

                // Se não achou nenhum, todos os arquivos temporários chegaram ao fim
                if (indiceMenor == -1) {
                    break; 
                }

                // 4. Grava o vencedor (menor ID) no arquivo novo
                Filme filmeGravar = filmesAtuais[indiceMenor];
                byte[] ba = filmeGravar.toByteArray();
                
                rafNovo.writeByte(' '); // Lápide de registro válido
                rafNovo.writeInt(ba.length);
                rafNovo.write(ba);
                
                // Mantém o controle do maior ID do sistema para o cabeçalho
                if (filmeGravar.getId() > maxId) {
                    maxId = filmeGravar.getId();
                }

                // 5. Avança a leitura apenas no arquivo que venceu a disputa
                filmesAtuais[indiceMenor] = lerProximoFilme(arquivosTemp[indiceMenor]);
                if (filmesAtuais[indiceMenor] == null) {
                    fimDeArquivo[indiceMenor] = true;
                }
            }

            // 6. Atualiza o cabeçalho definitivo e fecha o arquivo novo
            rafNovo.seek(0);
            rafNovo.writeInt(maxId);
            rafNovo.close();

            // 7. Limpeza da pasta (Fecha os temporários e deleta tudo que não serve mais)
            for (int i = 0; i < this.caminhos; i++) {
                if (arquivosTemp[i] != null) {
                    arquivosTemp[i].close();
                    File f = new File("dados/temp_0_" + i + ".bin");
                    f.delete();
                }
            }
            
            // Substitui o arquivo original desordenado pelo novo ordenado
            File arquivoAntigo = new File(this.arquivoOriginal);
            if (arquivoAntigo.exists()) arquivoAntigo.delete();
            novoFile.renameTo(arquivoAntigo);

        } catch (IOException e) {
            System.out.println("Erro na fase de intercalação: " + e.getMessage());
        }
    }

    // Método auxiliar para avançar a leitura dentro de um arquivo temporário
    private Filme lerProximoFilme(RandomAccessFile tempRaf) throws IOException {
        while (tempRaf.getFilePointer() < tempRaf.length()) {
            byte lapide = tempRaf.readByte();
            int tamanho = tempRaf.readInt();
            byte[] ba = new byte[tamanho];
            tempRaf.read(ba);

            if (lapide == ' ') {
                Filme filme = new Filme();
                filme.fromByteArray(ba);
                return filme;
            }
        }
        return null;
    }
    
}