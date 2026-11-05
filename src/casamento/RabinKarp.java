package casamento;

public class RabinKarp {
    private static final long BASE = 256;
    private static final long MODULO = 1000000007L;

    // Confere os caracteres quando os hashes coincidem.
    private static boolean comparar(String texto, String padrao, int inicio) {
        boolean igual = true;
        int i = 0;
        while (i < padrao.length() && igual) {
            if (texto.charAt(inicio + i) != padrao.charAt(i)) {
                igual = false;
            }
            i++;
        }
        return igual;
    }

    // Conta as ocorrências usando hash deslizante.
    public static int contarOcorrencias(String texto, String padrao) {
        if (texto == null || padrao == null || padrao.isEmpty()) {
            throw new IllegalArgumentException("Texto e padrão devem ser válidos e o padrão não pode ser vazio.");
        }
        int n = texto.length();
        int m = padrao.length();
        int ocorrencias = 0;
        if (m <= n) {
            long potencia = 1;
            long hashPadrao = 0;
            long hashJanela = 0;

            // Calcula a potência usada na atualização do hash
            for (int i = 1; i < m; i++) {
                potencia = (potencia * BASE) % MODULO;
            }
            for (int i = 0; i < m; i++) {
                hashPadrao = (hashPadrao * BASE + padrao.charAt(i)) % MODULO;
                hashJanela = (hashJanela * BASE + texto.charAt(i)) % MODULO;
            }
            for (int i = 0; i <= n - m; i++) {
                if (hashPadrao == hashJanela && comparar(texto, padrao, i)) {
                    ocorrencias++;
                }
                if (i < n - m) {
                    // Remove o primeiro caractere e adiciona o próximo
                    hashJanela = (hashJanela - texto.charAt(i) * potencia % MODULO + MODULO) % MODULO;
                    hashJanela = (hashJanela * BASE + texto.charAt(i + m)) % MODULO;
                }
            }
        }
        return ocorrencias;
    }
}
