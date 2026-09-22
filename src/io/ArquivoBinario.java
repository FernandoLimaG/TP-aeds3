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
    public long inserir(Filme filme) {
        try {
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(this.nomeArquivo, "rw");
            
            raf.seek(0); 
            raf.writeInt(filme.getId());

            long posicaoRegisto = raf.length();
            raf.seek(posicaoRegisto);

            byte[] ba = filme.toByteArray();

            raf.writeByte(' '); 
            raf.writeInt(ba.length);
            raf.write(ba);

            raf.close();
            return posicaoRegisto;
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    // Método para Ler um registro pelo ID
    public Filme lerComIndice(int idBuscado, arvore.ArvoreBMais arvore) {
        try {
            // 1. Busca o endereço do registro na árvore B+ (O(log n))
            long posicaoNoArquivo = arvore.buscar(idBuscado);

            // Se a árvore retornou -1, o ID não existe
            if (posicaoNoArquivo == -1) {
                return null; 
            }

            // 2. Pula cirurgicamente para a posição exata no arquivo de dados
            RandomAccessFile raf = new RandomAccessFile(this.nomeArquivo, "r");
            raf.seek(posicaoNoArquivo);

            byte lapide = raf.readByte();
            int tamanho = raf.readInt();
            
            byte[] ba = new byte[tamanho];
            raf.read(ba);
            raf.close();

            // Retorna o filme se não estiver deletado
            if (lapide == ' ') {
                Filme filme = new Filme();
                filme.fromByteArray(ba);
                return filme;
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler registro com índice: " + e.getMessage());
        }
        return null; 
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
                        if (novoBa.length == tamanhoAntigo) {
                            // Cenário 1: Cabe no mesmo lugar.
                            // Salta o ponteiro para depois da lápide(1 byte) + tamanho (4 bytes) e atualiza
                            raf.seek(posicaoLapide + 5); 
                            raf.write(novoBa);
                        } else {
                            // Cenário 2: Tamnho alterou
                            // Deleta o antigo (marca lápide) e escreve o novo registro ao final
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