package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Register;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class ClientInputUI implements Runnable {

    private final ServerConnectionManager serverConnectionManager;
    private final ObjectOutputStream oos;
    private final Scanner scanner;

    public ClientInputUI(ServerConnectionManager scm) {
        this.serverConnectionManager = scm;
        oos = serverConnectionManager.getSocketOutputStream();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        int loginDecision;
        do {
            System.out.println("""
                    MENU:
                    1 - Login
                    2 - Registo""");
            System.out.print(" > ");
            loginDecision = Integer.parseInt(scanner.nextLine());
            if (loginDecision == 1)
                login();
            if (loginDecision == 2)
                register();
            System.out.println();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(loginDecision < 1 || loginDecision > 2 || !serverConnectionManager.isUserConnected());

        menu();
    }

    private void writeToSocket(Object o) {
        try {
            oos.writeObject(o);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        System.out.println("Login");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        writeToSocket(new Login(username, password));
    }

    private void register() {
        System.out.println("Register");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        writeToSocket(new Register(username, password, name));
    }

    private void menu() {
        while(serverConnectionManager.isUserConnected() && serverConnectionManager.isServerConnected()) {
            System.out.println("""
                    Menu: 
                    1 - Editar dados de utilizador
                    2 - Listar todos os utilizadores
                    3 - Pesquisar utilizador
                    4 - Visualizar lista de contactos
                    5 - Eliminar contacto
                    6 - Criação de grupo""");
            System.out.println();
            int menuDecision = Integer.parseInt(scanner.nextLine());
        }
    }

}
