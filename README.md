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
- **TP 3** - Compactação com Huffman e LZW: Implementação + Vídeo
- **TP 4** - Casamento de Padrões e Criptografia: Implementação + Relatório Final

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

### Árvore B

- Chave implementada: **ID**
- Ordem Parametrizada: **100** (máximo)
- Par: **ID | Posição arquivo de dados**
- Arquivo: `dados/indice_arvore.bin`


### Listas invertidas

Foram escolhidos **gênero** e **país** como base, podendo ser combinados.

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

## Videos
- [Youtube: TP1](https://youtu.be/JvL54032Eag)
- [Youtube: TP2](https://youtu.be/Z_lo2dfalNs)