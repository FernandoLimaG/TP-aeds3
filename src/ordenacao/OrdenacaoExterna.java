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
        if (caminhos < 2 || limiteMemoria < 1) {
            throw new IllegalArgumentException("Use pelo menos 2 caminhos e memória para 1 registro.");
        }
        this.arquivoOriginal = arquivoOriginal;
        this.caminhos = caminhos;
        this.limiteMemoria = limiteMemoria;
    }

    // Distribuição: Lê o arquivo original, limpa os excluídos, ordena blocos e distribui
    public int distribuir() {
        int arquivosGerados = 0;
        
        try {
            // Descarta temporários de uma execução anterior interrompida.
            for (int i = 0; i < caminhos; i++) {
                try (RandomAccessFile temp = new RandomAccessFile("dados/temp_0_" + i + ".bin", "rw")) {
                    temp.setLength(0);
                }
            }
            RandomAccessFile raf = new RandomAccessFile(this.arquivoOriginal, "r");
            raf.seek(4); // Pula o cabeçalho
            
            Filme[] memoria = new Filme[limiteMemoria];
            int qtdAtual = 0;
            int caminhoAtual = 0;

            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                
                byte[] ba = new byte[tamanho];
                raf.readFully(ba);

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
            throw new IllegalStateException("Erro na fase de distribuição", e);
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
            throw new IllegalStateException("Erro ao gravar arquivo temporário", e);
        }
    }

    // Intercalação dos segmentos, respeitando os caminhos
    public void intercalar(int arquivosGerados) {
        try {
            // Armazenar UltimoID
            RandomAccessFile rafOriginal =
                    new RandomAccessFile(this.arquivoOriginal, "r");
            int ultimoId = rafOriginal.readInt();
            rafOriginal.close();

            // Cada lado possui a quantidade de caminhos recebida do usuário.
            int ladoEntrada = 0;
            int ladoSaida = 1;
            long tamanhoSegmento = this.limiteMemoria;

            // arquivosGerados é a quantidade de segmentos retornada por distribuir().
            while (arquivosGerados > 1) {
                RandomAccessFile[] arquivosEntrada =
                        new RandomAccessFile[this.caminhos];
                RandomAccessFile[] arquivosSaida =
                        new RandomAccessFile[this.caminhos];

                for (int i = 0; i < this.caminhos; i++) {
                    String nomeEntrada =
                            "dados/temp_" + ladoEntrada + "_" + i + ".bin";

                    if (new File(nomeEntrada).exists()) {
                        arquivosEntrada[i] =
                                new RandomAccessFile(nomeEntrada, "r");
                    }

                    String nomeSaida =
                            "dados/temp_" + ladoSaida + "_" + i + ".bin";

                    arquivosSaida[i] = new RandomAccessFile(nomeSaida, "rw");

                    // Limpa o conteúdo da passagem anterior antes de reutilizar.
                    arquivosSaida[i].setLength(0);
                }

                int segmentosGerados = 0;
                boolean existeSegmento = true;

                while (existeSegmento) {
                    Filme[] filmesAtuais = new Filme[this.caminhos];
                    long[] registrosLidos = new long[this.caminhos];
                    existeSegmento = false;

                    // Inicia um segmento de cada caminho de entrada.
                    for (int i = 0; i < this.caminhos; i++) {
                        if (arquivosEntrada[i] != null) {
                            filmesAtuais[i] =
                                    lerProximoFilme(arquivosEntrada[i]);

                            if (filmesAtuais[i] != null) {
                                registrosLidos[i] = 1;
                                existeSegmento = true;
                            }
                        }
                    }

                    if (existeSegmento) {
                        // Alterna o destino dos segmentos entre os caminhos.
                        int caminhoSaida = segmentosGerados % this.caminhos;

                        while (true) {
                            int indiceMenor = -1;

                            // Escolhe o menor ID disponível nos segmentos atuais.
                            for (int i = 0; i < this.caminhos; i++) {
                                if (filmesAtuais[i] != null) {
                                    if (indiceMenor == -1
                                            || filmesAtuais[i].getId()
                                            < filmesAtuais[indiceMenor].getId()) {
                                        indiceMenor = i;
                                    }
                                }
                            }

                            // Todos os segmentos deste grupo terminaram.
                            if (indiceMenor == -1) {
                                break;
                            }

                            byte[] ba = filmesAtuais[indiceMenor].toByteArray();
                            arquivosSaida[caminhoSaida].writeByte(' ');
                            arquivosSaida[caminhoSaida].writeInt(ba.length);
                            arquivosSaida[caminhoSaida].write(ba);

                            if (registrosLidos[indiceMenor] < tamanhoSegmento) {
                                filmesAtuais[indiceMenor] =
                                        lerProximoFilme(arquivosEntrada[indiceMenor]);
                                registrosLidos[indiceMenor]++;
                            } else {
                                filmesAtuais[indiceMenor] = null;
                            }
                        }

                        segmentosGerados++;
                    }
                }

                for (int i = 0; i < this.caminhos; i++) {
                    if (arquivosEntrada[i] != null) {
                        arquivosEntrada[i].close();
                    }
                    arquivosSaida[i].close();
                }

                arquivosGerados = segmentosGerados;

                // Cada passagem reúne até "caminhos" segmentos em um só.
                tamanhoSegmento *= this.caminhos;

                // A saída desta passagem será a entrada da próxima.
                int troca = ladoEntrada;
                ladoEntrada = ladoSaida;
                ladoSaida = troca;
            }

            String nomeNovoArquivo = "dados/dados_ordenado.bin";
            File novoFile = new File(nomeNovoArquivo);

            RandomAccessFile rafNovo =
                    new RandomAccessFile(nomeNovoArquivo, "rw");
            rafNovo.setLength(0);
            rafNovo.writeInt(ultimoId);

            // O único segmento restante está no caminho 0.
            if (arquivosGerados == 1) {
                String nomeFinal = "dados/temp_" + ladoEntrada + "_0.bin";
                RandomAccessFile arquivoFinal =
                        new RandomAccessFile(nomeFinal, "r");

                Filme filme = lerProximoFilme(arquivoFinal);

                while (filme != null) {
                    byte[] ba = filme.toByteArray();
                    rafNovo.writeByte(' ');
                    rafNovo.writeInt(ba.length);
                    rafNovo.write(ba);

                    filme = lerProximoFilme(arquivoFinal);
                }

                arquivoFinal.close();
            }

            rafNovo.close();

            // Remove os caminhos temporários de entrada e saída.
            for (int i = 0; i < this.caminhos; i++) {
                File entrada = new File("dados/temp_0_" + i + ".bin");
                File saida = new File("dados/temp_1_" + i + ".bin");
                entrada.delete();
                saida.delete();
            }

            // Mantém o mesmo nome utilizado pelas operações de CRUD.
            File arquivoAntigo = new File(this.arquivoOriginal);
            java.nio.file.Files.move(novoFile.toPath(), arquivoAntigo.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new IllegalStateException("Erro na fase de intercalação", e);
        }
    }

    // Método auxiliar para avançar a leitura dentro de um arquivo temporário
    private Filme lerProximoFilme(RandomAccessFile tempRaf) throws IOException {
        Filme filme = null;
        while (tempRaf.getFilePointer() < tempRaf.length() && filme == null) {
            byte lapide = tempRaf.readByte();
            int tamanho = tempRaf.readInt();
            byte[] ba = new byte[tamanho];
            tempRaf.readFully(ba);
            if (lapide == ' ') {
                filme = new Filme();
                filme.fromByteArray(ba);
            }
        }
        return filme;
    }
}
