package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Register;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class ClientInputUI implements Runnable {

    private ServerConnectionManager serverConnectionManager;
    private ObjectOutputStream oos;
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
        } while(loginDecision < 1 || loginDecision > 2);
    }

    private void writeToSocket(Object o) {
        try {
            oos.writeObject(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login() {
        System.out.println("Login");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        writeToSocket(new Login(username, password));
    }

    public void register() {
        System.out.println("Register");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        writeToSocket(new Register(username, password, name));
    }

}
