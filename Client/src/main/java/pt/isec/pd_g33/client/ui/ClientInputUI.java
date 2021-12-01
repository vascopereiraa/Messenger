package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.Login;
import pt.isec.pd_g33.shared.Notification;
import pt.isec.pd_g33.shared.Register;
import pt.isec.pd_g33.shared.UserData;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Scanner;

public class ClientInputUI implements Runnable {

    private final ServerConnectionManager serverConnectionManager;
    private final ObjectOutputStream oos;
    private final Scanner scanner;
    private String username, name, password;

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

            //todo: find a way to syncronize
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
        getUserData(false);
        writeToSocket(new Login(username, password));
    }

    private void register() {
        System.out.println("Register");
        getUserData(true);
        writeToSocket(new Register(username, password, name));
    }

    private void getUserData(boolean registerUpdate){
        if(registerUpdate){
            System.out.print("Name: ");
            name = scanner.nextLine();
        }
        System.out.print("Username: ");
        username = scanner.nextLine();
        System.out.print("Password: ");
        password = scanner.nextLine();
    }

    private void menu() {
        while(serverConnectionManager.isUserConnected() && serverConnectionManager.isServerConnected()) {
            System.out.println("""
                    Menu: 
                    1 - Editar dados de utilizador 
                    2 - Listar todos os utilizadores
                    3 - Pesquisar utilizador
                    4 - Visualizar lista de contactos
                    5 - Adicionar contacto
                    6 - Eliminar contacto
                    7 - Criação de grupo
                    8 - Pedidos de contacto pendentes
                    10 - Enviar mensagem""");
            System.out.println();
            int menuDecision = Integer.parseInt(scanner.nextLine());
            mnDecision(menuDecision);
        }
    }

    private void mnDecision(int menuDec){
        switch (menuDec){
            case 1 -> {//working
                getUserData(true);
                writeToSocket(new Data(1,
                        new UserData(username,password,name),
                        serverConnectionManager.getUserData().getUserID())); // User_id original fica no Userdata - toUserId
            }
            //working
            case 2->writeToSocket(new Data(2));
            case 3->{//working
                System.out.println("Indique o utilizador a pesquisar: ");
                name = scanner.nextLine();
                writeToSocket(new Data(3,name));
            }
            case 4 -> writeToSocket(new Data(4));

            case 5 ->{  // Adicionar contacto
                System.out.println("Indique o nome do contacto a adicionar: ");
                username = scanner.nextLine();
                writeToSocket(new Notification(serverConnectionManager.getUserData().getUsername(),username, DataType.Contact));
            }

            case 6->{//todo: not working, eliminar contacto, não qualquer user
                System.out.println("Indique o ulitizador a eliminar: ");
                username = scanner.nextLine();
                writeToSocket(new Data(5, username));
            }
            case 7 ->{
                writeToSocket(new Data(6));
            }
            // Enviar Mensagem
            case 10 -> {
                int menuDecision, groupID;
                System.out.println("1 -> Mensagem de grupo\n2-> Mensagem pessoal");
                menuDecision = Integer.parseInt(scanner.nextLine());
                System.out.print("Escreva a mensagem: ");
                String mensagem = scanner.nextLine();
                System.out.println("Indique o ID do grupo ou Username da pessoa");
                if(menuDecision == 1){
                    groupID = Integer.parseInt(scanner.nextLine());
                    writeToSocket(new Data(10,mensagem,groupID,serverConnectionManager.getUserData()));
                }
                else{
                    username = scanner.nextLine();
                    writeToSocket(new Data(10,mensagem,username,serverConnectionManager.getUserData()));
                }
            }
            default -> {

            }
        }
    }

}
