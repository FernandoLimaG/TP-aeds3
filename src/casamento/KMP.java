package casamento;

public class KMP {
    // Monta o vetor de prefixos do padrão
    private static int[] construirPrefixos(String padrao) {
        int[] prefixos = new int[padrao.length()];
        int j = 0;
        for (int i = 1; i < padrao.length(); i++) {
            // Reaproveita o prefixo anterior quando há diferença entre caracteres
            while (j > 0 && padrao.charAt(i) != padrao.charAt(j)) {
                j = prefixos[j - 1];
            }
            if (padrao.charAt(i) == padrao.charAt(j)) {
                j++;
            }
            prefixos[i] = j;
        }
        return prefixos;
    }

    // Conta as ocorrências do padrão no texto.
    public static int contarOcorrencias(String texto, String padrao) {
        if (texto == null || padrao == null || padrao.isEmpty()) {
            throw new IllegalArgumentException("Texto e padrão devem ser válidos e o padrão não pode ser vazio.");
        }
        int[] prefixos = construirPrefixos(padrao);
        int ocorrencias = 0;
        int j = 0;
        for (int i = 0; i < texto.length(); i++) {
            while (j > 0 && texto.charAt(i) != padrao.charAt(j)) {
                j = prefixos[j - 1];
            }
            if (texto.charAt(i) == padrao.charAt(j)) {
                j++;
            }
            if (j == padrao.length()) {
                ocorrencias++;
                // Permite encontrar ocorrências sobrepostas
                j = prefixos[j - 1];
            }
        }
        return ocorrencias;
    }
}
