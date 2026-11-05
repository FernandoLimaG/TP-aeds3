# Trabalho Prático: Algoritmos e Estruturas de Dados III

---

## Integrantes
- **Bruno Pais Moacir** - [GitHub](https://github.com/BrunoMoacir)
- **Fernando de Lima Gonçalves** - [GitHub](https://github.com/FernandoLimaG)

---

## Objetivo
>"Os Trabalhos Práticos de AEDS III tem como objetivo permitir que o aluno implemente a
>representação de entidades em registros, armazene-os em memória secundária e faça a
>manipulação destes registros através de acesso sequencial"

---

## Etapas
- **TP 1** - Criação da base de dados, Manipulação de Arquivo Sequencial: Implementação + Vídeo
- **TP 2** - Manipulação de Arquivo Indexado com Árvore B+, Hash e Lista Invertida: Implementação + Vídeo
- **TP 3** - Compactação e Casamento de Padrões: Implementação + Vídeo
- **TP 4** - Criptografia: Implementação + Relatório Final

---

## Base de Dados
***`IMDB movies dataset`***
- Link: [Kaggle: IMDB Movies](https://www.kaggle.com/datasets/ashpalsingh1525/imdb-movies-dataset/)
- Licença: [CDLA-Permissive-1.0](https://cdla.dev/permissive-1-0/)
- Acesso local: [CSV](/dados/imdb_movies.csv)

---

## Tecnologias
- **Linguagem:** Java
- **Ambiente de Desenvolvimento (IDE):** VSCode, IntelliJ

---

## Como Compilar e Rodar o Projeto

### Via Terminal
1. Clone o repositório:
```bash
git clone https://github.com/FernandoLimaG/TP-aeds3
cd TP-aeds3
```

ou

```bash
git clone git@github.com:FernandoLimaG/TP-aeds3.git
cd TP-aeds3
```

2. Compile o código fonte:
```bash
javac -d bin -sourcepath src src/Main.java
```

3. Execute
```bash
java -cp bin Main
```

---

## Arquivos Indexados

As estruturas utilizadas foram: **Árvore B+** e **Lista Invertida**.

### Árvore B+

- Chave implementada: **ID**
- Ordem Parametrizada: **100** (máximo)
- Par: **ID | Posição arquivo de dados**
- Arquivo: `dados/indice_arvore.bin`


### Listas invertidas

Foram escolhidos **Gênero** e **País** como base, podendo ser combinados.

- `dados/lista_generos.bin`: gênero.
- `dados/lista_paises.bin`: país.

### Uso dos índices

No submenu TP2, ler, atualizar e excluir permitem escolher:

- 1: Árvore B+ por ID.
- 2: lista de gêneros.
- 3: lista de países.
- 4: gênero E país, por interseção.

>As alterações feitas pelo submenu TP1 (CRUD Sequencial) também atualizam os três índices para garantir o comportamento esperado do programa.

---

## Casamento de Padrões

Para o TP3 foram escolhidos os algoritmos **KMP (Knuth-Morris-Pratt)** e **Rabin-Karp**, principalmente pela simplicidade de implementação, eficiência e utilização de técnicas distintas de busca: `prefixos` e `hashing`, respectivamente.

### KMP
- Utiliza vetor de prefixos para evitar comparações desnecessárias, reaproveitando informações de correspondências anteriores.
- Complexidade: O(n + m).

### Rabin-Karp
- Utiliza hashing para comparar o padrão com janelas do texto, atualizando o hash a cada deslocamento.
- Complexidade esperada: O(n + m)
- Complexidade pior caso: O(nm)

### Implementação

Os algoritmos foram implementados separadamente, permitindo ao usuário selecionar qual deseja executar pelo menu. A busca é realizada no campo **NOME** dos filmes, percorrendo os registros ativos do arquivo binário e imprimindo os filmes encontrados, a quantidade de ocorrências e o tempo total de execução.

---


## Compressão de Dados

O TP3 utiliza os algoritmos **Huffman** e **LZW** para comprimir todo o conteúdo binário de `dados/dados.bin`, incluindo cabeçalho, lápides, indicadores de tamanho e bytes dos registros. O arquivo original não é modificado durante a compressão.

### Huffman

- Calcula a frequência dos 256 possíveis valores de byte.
- Constrói uma árvore binária de frequências utilizando nós e vetores próprios.
- Atribui códigos de tamanho variável, com menos bits para os símbolos mais frequentes.
- Armazena as frequências no cabeçalho do arquivo compactado para reconstruir a árvore durante a descompressão.

### LZW

- Inicia o dicionário com os 256 possíveis valores de byte.
- Adiciona sequências encontradas durante a leitura, até o limite de 65.536 entradas.
- Utiliza uma tabela hash própria com sondagem linear para consultar o dicionário durante a compressão.
- Grava códigos de tamanho fixo de 16 bits e reconstrói o dicionário durante a descompressão.

### Implementação

Os algoritmos foram implementados separadamente, permitindo que o sistema execute ambos durante a compressão do arquivo binário. A implementação utiliza estruturas próprias e realiza a leitura e escrita dos arquivos com `RandomAccessFile`.

Ao selecionar a opção de compressão, são gerados dois arquivos, um para cada algoritmo, seguindo um sistema de versionamento:

- `dados/dadosHuffmanCompressao1.bin`
- `dados/dadosLZWCompressao1.bin`

A cada nova compressão, a versão é incrementada, preservando os arquivos anteriores.

O sistema apresenta o tamanho original, o tamanho comprimido, a porcentagem de ganho ou perda e o tempo de execução de cada algoritmo, indicando qual obteve o melhor resultado.

Na descompressão, o usuário informa a versão desejada. Os dois algoritmos são executados e seus resultados são comparados para verificar a integridade dos dados. Após a escolha do usuário, o arquivo `dados.bin` é restaurado e os índices da Árvore B+ e das listas invertidas são reconstruídos.


## Videos
- [Youtube: TP1](https://youtu.be/JvL54032Eag)
- [Youtube: TP2](https://youtu.be/Z_lo2dfalNs)