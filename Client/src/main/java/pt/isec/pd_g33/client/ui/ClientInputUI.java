package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
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
                    12 - Aderir a um grupo .: join "groupName"
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
                case "commands" -> showMainCommands();

                // User
                case "edit" -> writeToSocket(new Data(MenuOption.EDIT_USER,
                        new UserData(comParts[2], comParts[3], comParts[1]),
                        serverConnectionManager.getUserData().getUserID())); // User_id original fica no Userdata - toUserId
                case "listall" -> writeToSocket(new Data(MenuOption.LIST_USERS));
                case "search" -> writeToSocket(new Data(MenuOption.SEARCH_USER, comParts[1]));

                // Groups
                case "create" -> writeToSocket(new Data(MenuOption.CREATE_GROUP, serverConnectionManager.getUserData(), comParts[1]));
                case "listgroup" -> writeToSocket(new Data(MenuOption.LIST_GROUPS));
                case "join" -> writeToSocket(new Data(MenuOption.JOIN_GROUP, serverConnectionManager.getUserData(),Integer.parseInt(comParts[1])));
                case "memberaccept" -> writeToSocket(new Data(MenuOption.MEMBER_ACCEPT,comParts[2],Integer.parseInt(comParts[1]), serverConnectionManager.getUserData()));
                case "memberrm" -> writeToSocket(new Data(MenuOption.MEMBER_REMOVE, comParts[2], Integer.parseInt(comParts[1]), serverConnectionManager.getUserData()));
                case "rename" -> writeToSocket(new Data(MenuOption.RENAME_GROUP, comParts[2], Integer.parseInt(comParts[1])));
                case "del" -> writeToSocket(new Data(MenuOption.DELETE_GROUP, serverConnectionManager.getUserData().getUsername() ,Integer.parseInt(comParts[1])));
                // todo: Leave Group
                case "leave" ->writeToSocket(new Data(MenuOption.LEAVE_GROUP, Integer.parseInt(comParts[1])));

                // Contacts
                case "listcontact" -> writeToSocket(new Data(MenuOption.LIST_CONTACTS));
                case "pendcontact" -> writeToSocket(new Data(MenuOption.PENDING_CONTACT, serverConnectionManager.getUserData().getUsername()));
                case "addc" -> writeToSocket(new Notification(serverConnectionManager.getUserData().getUsername(), comParts[1], DataType.Contact));
                case "delc" -> writeToSocket(new Data(MenuOption.DELETE_CONTACT, serverConnectionManager.getUserData(),comParts[1]));
                case "accept" -> writeToSocket(new Data(MenuOption.ACCEPT_CONTACT,serverConnectionManager.getUserData().getUsername(),comParts[1]));
                case "reject" -> writeToSocket(new Data(MenuOption.REJECT_CONTACT,serverConnectionManager.getUserData().getUsername(),comParts[1]));

                // Messages
                // todo: Finish send with correct verifications
                case "send" -> {
                    MenuOption contactOrGroup = comParts[1].equalsIgnoreCase("contact") ? MenuOption.SEND_MSG_TO_CONTACT : MenuOption.SEND_MSG_TO_GROUP;
                    String message = String.join(" ", Arrays.copyOfRange(comParts, 3, (comParts.length )));
                    writeToSocket(new Data(contactOrGroup, message, Integer.parseInt(comParts[2]), serverConnectionManager.getUserData()));
                }
                case "listmsg" -> {
                    MenuOption contactOrGroup = comParts[1].equalsIgnoreCase("contact") ? MenuOption.LIST_MSG_CONTACT : MenuOption.LIST_MSG_GROUP;
                    writeToSocket(new Data(contactOrGroup,serverConnectionManager.getUserData().getUsername(),comParts[2]));
                }
                case "listunseen" -> {
                    writeToSocket(new Data(MenuOption.LIST_UNSEEN,serverConnectionManager.getUserData().getUsername()));
                }
                case "delmsg" -> writeToSocket(new Data(MenuOption.DELETE_MESSAGE,serverConnectionManager.getUserData().getUsername() , Integer.parseInt(comParts[1])));
            }
        } while (!command.equalsIgnoreCase("sair"));

        writeToSocket(new Data(MenuOption.EXIT, serverConnectionManager.getUserData(), serverConnectionManager.getUserData().getUserID()));
        serverConnectionManager.disconnectClient();
    }
}
