package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.shared.*;

import java.awt.*;
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
        // Login or Register users
        if(!executeLogin()) {
            serverConnectionManager.disconnectClient();
            return;
        }

        // After user logged in has access to remaining functions
        cmdDecision();
    }

    private boolean executeLogin() {
        do {
            System.out.print("$> ");
            String[] command = scanner.nextLine().split("\\s");
            switch (command[0]) {
                case "commands" -> showLoginCommands();
                case "login" -> writeToSocket(new Login(command[1], command[2]));
                case "register" -> {
                    String name = String.join(" ", Arrays.copyOfRange(command, 3, command.length));
                    if(name.length() > 50 || command[1].length() > 30 || command[2].length() > 30) {
                        System.out.println("""
                            [ERROR] Input too long:
                            \t-> Name should have less than 50 characters
                            \t-> Username and Password should have less than 30 characters each
                            """);
                        break;
                    }
                    writeToSocket(new Register(command[1].toLowerCase(), command[2], name));
                }
                case "exit" -> { return false; }
            }

            //todo: SEARCH FOR A BETTER METHOD
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (!serverConnectionManager.isServerConnected() || !serverConnectionManager.isUserConnected());
        return true;
    }

    private void showLoginCommands() {
        System.out.printf("""
                Available Commands:
                
                Login:
                %-35s  %-100s
                %-35s  %-100s
                
                %-35s  %-100s
                %n""",
                "-> Login", "login <username> <password>",
                "-> Sign in", "register <username> <password> <name>",
                "-> Exit client", "exit");
    }

    private void showMainCommands() {
        System.out.printf("""
                Available Commands:
                        
                User:
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                        
                Group:
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                        
                Contacts:
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                        
                Messages:
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                %-35s  %-100s
                
                %-35s  %-100s
                %n""",
                "-> Edit user data", "edit <new_name> <new_username> <new_password>",
                "-> List all users", "listall",
                "-> Search for user", "search <username>",

                "-> Create group", "create <group_name>",
                "-> List all groups", "listgroup",
                "-> Join group", "join <group_id>",
                "-> Accept new group members", "memberaccept <group_id> <username>",
                "-> Remove group member", "memberrm <group_id> <username>",
                "-> Change group name", "rename <group_id> <new_groupname>",
                "-> Delete group", "del <group_id>",
                "-> Leave group", "leave <group_id>",

                "-> Show contact list", "listcontact",
                "-> Show pending contact requests", "pendcontact",
                "-> Add contact", "addc <username>",
                "-> Delete contact", "delc <username>",
                "-> Accept contact request", "accept <username>",
                "-> Reject contact request", "reject <username>",

                "-> Send message to group", "send group <group_id> <...message...>",
                "-> Send message to contact", "send contact <username> <...message...>",
                "-> List messages to contact", "listmsg contact <username>",
                "-> List messages to group", "listmsg group <group_id>",
                "-> List unseen messages", "listunseen",
                "-> Delete message from/to contact/group", "delmsg <message id from group or contact>",

                "-> Exit client", "exit");
    }

    private void cmdDecision() {
        String command;
        do {
            System.out.print("$> ");
            command = scanner.nextLine();
            String[] comParts = command.split("\\s");

            switch (comParts[0]) {
                case "commands" -> showMainCommands(); // ok

                // User
                case "edit" -> writeToSocket(new Data(MenuOption.EDIT_USER,new UserData(comParts[2], comParts[3], comParts[1]),serverConnectionManager.getUserData().getUserID())); // ok
                case "listall" -> writeToSocket(new Data(MenuOption.LIST_USERS)); // ok
                case "search" -> writeToSocket(new Data(MenuOption.SEARCH_USER, comParts[1])); // ok

                // Groups
                case "create" -> writeToSocket(new Data(MenuOption.CREATE_GROUP, serverConnectionManager.getUserData(), comParts[1])); // ok
                case "listgroup" -> writeToSocket(new Data(MenuOption.LIST_GROUPS)); // ok
                case "join" -> writeToSocket(new Data(MenuOption.JOIN_GROUP, serverConnectionManager.getUserData(),Integer.parseInt(comParts[1]))); // ok
                case "memberaccept" -> writeToSocket(new Data(MenuOption.MEMBER_ACCEPT,comParts[2],Integer.parseInt(comParts[1]), serverConnectionManager.getUserData()));// ok
                case "memberrm" -> writeToSocket(new Data(MenuOption.MEMBER_REMOVE, comParts[2], Integer.parseInt(comParts[1]), serverConnectionManager.getUserData()));// ok
                case "rename" -> writeToSocket(new Data(MenuOption.RENAME_GROUP, comParts[2], Integer.parseInt(comParts[1]), serverConnectionManager.getUserData()));// ok
                case "del" -> writeToSocket(new Data(MenuOption.DELETE_GROUP, serverConnectionManager.getUserData().getUsername() ,Integer.parseInt(comParts[1])));// ok
                case "leave" ->writeToSocket(new Data(MenuOption.LEAVE_GROUP, serverConnectionManager.getUserData(), Integer.parseInt(comParts[1])));

                // Contacts
                case "listcontact" -> writeToSocket(new Data(MenuOption.LIST_CONTACTS)); // ok
                case "pendcontact" -> writeToSocket(new Data(MenuOption.PENDING_CONTACT, serverConnectionManager.getUserData().getUsername())); // ok
                case "addc" -> writeToSocket(new Data(MenuOption.ADD_CONTACT,serverConnectionManager.getUserData().getUsername(), comParts[1])); // ok
                case "delc" -> writeToSocket(new Data(MenuOption.DELETE_CONTACT, serverConnectionManager.getUserData(),comParts[1])); // ok
                case "accept" -> writeToSocket(new Data(MenuOption.ACCEPT_CONTACT,serverConnectionManager.getUserData().getUsername(),comParts[1])); // ok
                case "reject" -> writeToSocket(new Data(MenuOption.REJECT_CONTACT,serverConnectionManager.getUserData().getUsername(),comParts[1])); // ok

                // Messages
                case "send" -> { // ok
                    String message = String.join(" ", Arrays.copyOfRange(comParts, 3, (comParts.length )));
                    if(comParts[1].equalsIgnoreCase("contact"))
                        writeToSocket(new Data(MenuOption.SEND_MSG_TO_CONTACT, message, comParts[2], serverConnectionManager.getUserData(), DataType.Message));
                    else
                        writeToSocket(new Data(MenuOption.SEND_MSG_TO_GROUP, message, Integer.parseInt(comParts[2]), serverConnectionManager.getUserData(), DataType.Message));
                }
                case "listmsg" -> {
                    MenuOption contactOrGroup = comParts[1].equalsIgnoreCase("contact") ? MenuOption.LIST_MSG_CONTACT : MenuOption.LIST_MSG_GROUP;
                    writeToSocket(new Data(contactOrGroup,serverConnectionManager.getUserData().getUsername(),comParts[2]));
                }
                case "listunseen" -> { // ok
                    writeToSocket(new Data(MenuOption.LIST_UNSEEN,serverConnectionManager.getUserData().getUsername()));
                }
                case "delmsg" -> writeToSocket(new Data(MenuOption.DELETE_MESSAGE,serverConnectionManager.getUserData().getUsername() , Integer.parseInt(comParts[1])));

                // Files
                case "sendfile" -> {
                    SendFileProc sendFileProc = new SendFileProc(comParts[3], comParts[4]);
                    Thread tsfp = new Thread(sendFileProc);
                    tsfp.start();
                    // sendfile group <group_id> <...path...> <...file...>
                    if(comParts[1].equalsIgnoreCase("contact"))
                        writeToSocket(new Data(MenuOption.SEND_FILE_TO_CONTACT, comParts[4], comParts[2] ,sendFileProc.getSendFileSocketIp(),sendFileProc.getSendFileSocketPort(),serverConnectionManager.getUserData()));
                    else
                        writeToSocket(new Data(MenuOption.SEND_FILE_TO_GROUP, comParts[4], Integer.parseInt(comParts[2]) ,sendFileProc.getSendFileSocketIp(),sendFileProc.getSendFileSocketPort(),serverConnectionManager.getUserData()));
                }

                default -> System.out.println("Indique um comando válido");
            }

        } while (!command.equalsIgnoreCase("exit"));

        writeToSocket(new Data(MenuOption.EXIT, serverConnectionManager.getUserData(), serverConnectionManager.getUserData().getUserID()));
        serverConnectionManager.disconnectClient();
    }

    private void writeToSocket(Object o) {
        try {
            oos.writeUnshared(o);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
