package arvore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NoBMais {
    public int ordem;
    public boolean folha;
    public int numChaves;
    public int[] chaves;           
    public long[] ponteirosDados;  
    public long[] filhos;          
    public long proximaFolha;      
    
    public NoBMais(int ordem) {
        this.ordem = ordem;
        this.folha = true;
        this.numChaves = 0;
        this.chaves = new int[ordem - 1];
        this.ponteirosDados = new long[ordem - 1];
        this.filhos = new long[ordem];
        this.proximaFolha = -1;
        
        for (int i = 0; i < ordem - 1; i++) {
            chaves[i] = -1;
            ponteirosDados[i] = -1;
        }
        for (int i = 0; i < ordem; i++) {
            filhos[i] = -1;
        }
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeBoolean(this.folha);
        dos.writeInt(this.numChaves);
        
        for (int i = 0; i < ordem - 1; i++) {
            dos.writeInt(this.chaves[i]);
            dos.writeLong(this.ponteirosDados[i]);
        }
        
        for (int i = 0; i < ordem; i++) {
            dos.writeLong(this.filhos[i]);
        }
        
        dos.writeLong(this.proximaFolha);

        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.folha = dis.readBoolean();
        this.numChaves = dis.readInt();
        
        for (int i = 0; i < ordem - 1; i++) {
            this.chaves[i] = dis.readInt();
            this.ponteirosDados[i] = dis.readLong();
        }
        
        for (int i = 0; i < ordem; i++) {
            this.filhos[i] = dis.readLong();
        }
        
        this.proximaFolha = dis.readLong();
    }
}