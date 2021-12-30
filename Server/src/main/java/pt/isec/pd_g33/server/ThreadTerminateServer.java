package pt.isec.pd_g33.server;

import java.util.Scanner;

public class ThreadTerminateServer implements Runnable {

    @Override
    public void run() {
        String input;
        Scanner scanner = new Scanner(System.in);
        do{
            System.out.println("A qualquer momento, escreva exit para terminar o servidor.");
            input = scanner.nextLine();
        }while (!input.equals("exit"));
    }
}
