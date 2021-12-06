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
import java.util.Arrays;
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
        do {
            String[] command = scanner.nextLine().split("\\s");
            switch (command[0]) {
                case "login" -> writeToSocket(new Login(command[1], command[2]));
                case "register" -> {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 1; i < command.length - 2; ++i)
                        sb.append(command[i]).append(" ");
                    writeToSocket(new Register(command[command.length - 2],
                            command[command.length - 1], sb.toString()));
                }
            }

            //todo: find a way to syncronize
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            System.out.println(serverConnectionManager.isServerConnected() + "" + serverConnectionManager.isUserConnected());
        } while (!serverConnectionManager.isServerConnected() || !serverConnectionManager.isUserConnected());
        // Caso tenha login ou registo feito, pode fazer comandos
        cmdDecision();
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
        while (serverConnectionManager.isUserConnected() && serverConnectionManager.isServerConnected()) {
            System.out.println("""
                    Comandos disponiveis: 
                    1 - Editar dados de utilizador .: edit "new name" "new username" "new password"
                    2 - Listar todos os utilizadores .: listall 
                    3 - Pesquisar utilizador .: search "username"
                    4 - Visualizar lista de contactos .:  listcontact
                    5 - Adicionar contacto .: addc "username"
                    6 - Eliminar contacto .:delc "username"
                    7 - Pedidos de contacto pendentes .: pendcontact
                    8 - Aceitar pedido de contacto .: accept "username" 
                    10 - Enviar mensagem .: send "pers or group" "group id or person username" "message to send"
                    11 - Criar novo grupo .: create "groupName"
                    """);
            System.out.println();
        }
    }

    private void cmdDecision() {
        String command;
        do {
            System.out.println("Insira o comando");
            command = scanner.nextLine();
            String[] comParts = command.split("\\s");

            switch (comParts[0]) {
                //1-> ok
                case "edit" -> writeToSocket(new Data(1,
                        new UserData(comParts[2], comParts[3], comParts[1]),
                        serverConnectionManager.getUserData().getUserID())); // User_id original fica no Userdata - toUserId
                //2-> ok
                case "listall" -> writeToSocket(new Data(2));
                //3-> ok
                case "search" -> writeToSocket(new Data(3, comParts[1]));
                //4-> ok
                case "listcontact" -> writeToSocket(new Data(4));
                //5-> ok
                case "addc" -> writeToSocket(new Notification(serverConnectionManager.getUserData().getUsername(), comParts[1], DataType.Contact));
                //todo: 6-> eliminar contacto
                case "delc" -> writeToSocket(new Data(6, serverConnectionManager.getUserData(),comParts[1]));
                //7 -> ok
                case "pendc" -> writeToSocket(new Data(7, serverConnectionManager.getUserData().getUsername()));
                //todo: 8-> accept "contact"
                case "accept" -> writeToSocket(new Data(8,serverConnectionManager.getUserData().getUsername(),comParts[1]));
                //todo: 9 -> reject "contact"
                case "reject" -> writeToSocket(new Data(9,serverConnectionManager.getUserData().getUsername(),comParts[1]));
                //todo: 10-> testar grupo
                case "send" -> {
                    int persGroup = comParts[1].equalsIgnoreCase("pers") ? 1 : 2;
                    String usernameGroupID = comParts[2];

                    System.out.println(persGroup + " " + usernameGroupID);

                    String mensagem = String.join(" ", Arrays.copyOfRange(comParts, 3, (comParts.length )));
                    if (persGroup == 2)
                        writeToSocket(new Data(10, mensagem, Integer.parseInt(usernameGroupID), serverConnectionManager.getUserData()));
                    else
                        writeToSocket(new Data(10, mensagem, usernameGroupID, serverConnectionManager.getUserData()));
                }
                case "create"->{

                }
                default -> {

                }
            }
        } while (!command.equalsIgnoreCase("sair"));

        writeToSocket(new Data(-1, serverConnectionManager.getUserData(), serverConnectionManager.getUserData().getUserID()));
    }
}
