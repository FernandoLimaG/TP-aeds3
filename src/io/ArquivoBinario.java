package io;

import entidade.Filme;

import java.io.RandomAccessFile;
import java.io.IOException;

public class ArquivoBinario {
    private String nomeArquivo;

    public ArquivoBinario(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    // Inicializa o arquivo criando o cabeçalho se ele não existir
    public void inicializar() {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
            if (raf.length() == 0) {
                raf.writeInt(0); // Escreve 0 como o último ID utilizado no cabeçalho
            }
            raf.close();
        } catch (IOException e) {
            System.out.println("Erro ao inicializar o arquivo binário: " + e.getMessage());
        }
    }

    // Insere um novo filme no final do arquivo e atualiza o cabeçalho
    public void inserir(Filme filme) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
            
            // 1. Atualiza o cabeçalho com o novo ID
            raf.seek(0); 
            raf.writeInt(filme.getId());

            // 2. Vai para o final do arquivo para inserir o novo registro
            raf.seek(raf.length());

            // Converte o objeto para vetor de bytes
            byte[] ba = filme.toByteArray();

            // 3. Escreve a Lápide (espaço em branco ' ' significa válido, asterisco '*' significa excluído)
            raf.writeByte(' '); 

            // 4. Escreve o Indicador de Tamanho do registro
            raf.writeInt(ba.length);

            // 5. Escreve o Vetor de Bytes com os dados
            raf.write(ba);

            raf.close();
        } catch (IOException e) {
            System.out.println("Erro ao inserir registro: " + e.getMessage());
        }
    }

    // Método para Ler um registro pelo ID
    public Filme ler(int idBuscado) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "r");
            
            // Pula o cabeçalho (os primeiros 4 bytes do int)
            raf.seek(4);

            // Varre o arquivo até o final
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                
                // Lê o vetor de bytes do registro
                byte[] ba = new byte[tamanho];
                raf.read(ba);

                // Se a lápide for um espaço (' '), o registro é válido
                if (lapide == ' ') {
                    Filme filme = new Filme();
                    filme.fromByteArray(ba); // Converte os bytes de volta para o objeto
                    
                    // Verifica se é o ID que estamos procurando
                    if (filme.getId() == idBuscado) {
                        raf.close();
                        return filme; // Retorna o filme encontrado
                    }
                }
            }
            raf.close();
        } catch (IOException e) {
            System.out.println("Erro ao ler registro: " + e.getMessage());
        }
        return null; // Retorna nulo se não encontrar o ID ou se estiver deletado
    }

    // Método para Deletar (logicamente) um registro pelo ID
    public boolean deletar(int idBuscado) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
            
            raf.seek(4); // Pula o cabeçalho

            while (raf.getFilePointer() < raf.length()) {
                long posicaoLapide = raf.getFilePointer(); // Guarda a posição exata da lápide
                
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                
                byte[] ba = new byte[tamanho];
                raf.read(ba);

                if (lapide == ' ') {
                    Filme filme = new Filme();
                    filme.fromByteArray(ba);
                    
                    if (filme.getId() == idBuscado) {
                        // Volta o ponteiro para a posição da lápide desse registro
                        raf.seek(posicaoLapide);
                        
                        // Escreve '*' para marcar como deletado
                        raf.writeByte('*'); 
                        
                        raf.close();
                        return true; // Deletado com sucesso
                    }
                }
            }
            raf.close();
        } catch (IOException e) {
            System.out.println("Erro ao deletar registro: " + e.getMessage());
        }
        return false; // Retorna falso se não encontrar
    }

    // Método para Atualizar um registro
    public boolean atualizar(Filme filmeAtualizado) {
        try {
            RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "rw");
            raf.seek(4); // Pula o cabeçalho

            while (raf.getFilePointer() < raf.length()) {
                long posicaoLapide = raf.getFilePointer(); // Guarda o início do registro
                
                byte lapide = raf.readByte();
                int tamanhoAntigo = raf.readInt();
                
                byte[] ba = new byte[tamanhoAntigo];
                raf.read(ba);

                if (lapide == ' ') {
                    Filme filme = new Filme();
                    filme.fromByteArray(ba);
                    
                    // Se encontrou o ID que queremos atualizar
                    if (filme.getId() == filmeAtualizado.getId()) {
                        byte[] novoBa = filmeAtualizado.toByteArray();
                        
                        // CÁLCULO DE TAMANHO
                        if (novoBa.length <= tamanhoAntigo) {
                            // Cenário 1: Cabe no mesmo lugar. Volta o ponteiro para depois da Lápide e do Tamanho
                            raf.seek(posicaoLapide + 5); 
                            raf.write(novoBa);
                        } else {
                            // Cenário 2: Aumentou de tamanho. Deleta o antigo e insere no final.
                            raf.seek(posicaoLapide);
                            raf.writeByte('*'); // Lápide de exclusão no antigo
                            
                            raf.seek(raf.length()); // Vai para o fim do arquivo
                            raf.writeByte(' '); // Nova Lápide válida
                            raf.writeInt(novoBa.length); // Novo tamanho
                            raf.write(novoBa); // Novos dados
                        }
                        raf.close();
                        return true;
                    }
                }
            }
            raf.close();
        } catch (IOException e) {
            System.out.println("Erro ao atualizar registro: " + e.getMessage());
        }
        return false;
    }
}