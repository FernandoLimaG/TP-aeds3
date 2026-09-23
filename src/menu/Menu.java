package menu;

import java.util.Scanner;

public class Menu {
    public static int principal(Scanner sc) {
        System.out.println("=====================================");
        System.out.println("  MENU PRINCIPAL - AEDS III (TP1)  ");
        System.out.println("=====================================");
        System.out.println("1. Realizar Carga da Base de Dados (CSV para Binário)");
        System.out.println("2. TP1 - CRUD Sequencial");
        System.out.println("3. TP2 - CRUD Arquivos Indexado");
        System.out.println("4. Ordenação Externa");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");

        return Integer.parseInt(sc.nextLine());
    }

    public static int sequencial(Scanner sc) {
        System.out.println("=====================================");
        System.out.println("  TP1 - CRUD SEQUENCIAL  ");
        System.out.println("=====================================");
        System.out.println("1. Ler Registro - ID (CRUD)");
        System.out.println("2. Atualizar um Registro - ID (CRUD)");
        System.out.println("3. Deletar um Registro - ID (CRUD)");
        System.out.println("4. Criar um Registro (CRUD)");
        System.out.println("0. Voltar ao menu principal");
        System.out.print("Escolha uma opção: ");

        return Integer.parseInt(sc.nextLine());
    }

    public static int indexado(Scanner sc) {
        System.out.println("=====================================");
        System.out.println("  TP2 - CRUD ARQUIVOS INDEXADO  ");
        System.out.println("=====================================");
        System.out.println("1. Ler um Registro (CRUD)");
        System.out.println("2. Atualizar um Registro (CRUD)");
        System.out.println("3. Deletar um Registro (CRUD)");
        System.out.println("4. Criar um Registro (CRUD)");
        System.out.println("0. Voltar ao menu principal");
        System.out.print("Escolha uma opção: ");

        return Integer.parseInt(sc.nextLine());
    }
}
