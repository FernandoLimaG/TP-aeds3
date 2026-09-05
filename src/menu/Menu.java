package menu;

import java.util.Scanner;

public class Menu {
    public static int principal(Scanner sc) {
        System.out.println("=====================================");
        System.out.println("  MENU PRINCIPAL - AEDS III (TP1)  ");
        System.out.println("=====================================");
        System.out.println("1. Realizar Carga da Base de Dados (CSV para Binário)");
        System.out.println("2. Ler um Registro (CRUD)");
        System.out.println("3. Atualizar um Registro (CRUD)");
        System.out.println("4. Deletar um Registro (CRUD)");
        System.out.println("5. Ordenação Externa");
<<<<<<< HEAD
=======
        System.out.println("6. Criar um Registro (CRUD)");
>>>>>>> c081460 (adicionada opcao 6 no menu)
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");

        return Integer.parseInt(sc.nextLine());
    }
}
