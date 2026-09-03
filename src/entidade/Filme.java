package entidade;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Filme {
    private int id; // Usado no cabeçalho e controle
    private String nome; // 1. String de tamanho variável (names)
    private long dataLancamento; // 2. Data em milissegundos (date_x)
    private float score; // 3. Float (score)
    private String[] generos; // 4. Lista de valores (genre)
    private String pais; // 5. String de tamanho fixo de 2 caracteres (country)

    public Filme() {
        this.id = -1;
        this.nome = "";
        this.dataLancamento = 0;
        this.score = 0.0f;
        this.generos = new String[0];
        this.pais = "--";
    }

    public Filme(int id, String nome, long dataLancamento, float score, String[] generos, String pais) {
        this.id = id;
        this.nome = nome;
        this.dataLancamento = dataLancamento;
        this.score = score;
        this.generos = generos;
        this.pais = formatarPais(pais);
    }

    // Garante que o país tenha exatamente 2 caracteres (String de tamanho fixo)
    private String formatarPais(String p) {
        String formatado = "";
        for (int i = 0; i < 2; i++) {
            if (i < p.length()) {
                formatado += p.charAt(i);
            } else {
                formatado += " ";
            }
        }
        return formatado;
    }

    // --- GETTERS E SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public long getDataLancamento() { return dataLancamento; }
    public void setDataLancamento(long dataLancamento) { this.dataLancamento = dataLancamento; }

    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }

    public String[] getGeneros() { return generos; }
    public void setGeneros(String[] generos) { this.generos = generos; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = formatarPais(pais); }

    // método para converter o objeto em vetor de bytes
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
        dos.writeLong(this.dataLancamento);
        dos.writeFloat(this.score);

        dos.writeInt(this.generos.length);
        for (int i = 0; i < this.generos.length; i++) {
            dos.writeUTF(this.generos[i]);
        }

        dos.writeUTF(this.pais);

        return baos.toByteArray();
    }

    // Método para preencher o objeto a partir de um vetor de bytes
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.nome = dis.readUTF();
        this.dataLancamento = dis.readLong();
        this.score = dis.readFloat();

        int qtdGeneros = dis.readInt();
        this.generos = new String[qtdGeneros];
        for (int i = 0; i < qtdGeneros; i++) {
            this.generos[i] = dis.readUTF();
        }

        this.pais = dis.readUTF();
    }

    @Override
    public String toString() {
        String strGen = "[";
        for (int i = 0; i < generos.length; i++) {
            strGen += generos[i];
            if (i < generos.length - 1) strGen += ", ";
        }
        strGen += "]";

        // Converte o long de volta para string apenas para exibir no console
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(this.dataLancamento);
        String dataExibicao = cal.get(java.util.Calendar.DAY_OF_MONTH) + "/" +
                (cal.get(java.util.Calendar.MONTH) + 1) + "/" +
                cal.get(java.util.Calendar.YEAR);

        return "ID: " + id + " | Nome: " + nome + " | Data: " + dataExibicao +
                " | Score: " + score + " | Gêneros: " + strGen + " | País: " + pais;
    }
}