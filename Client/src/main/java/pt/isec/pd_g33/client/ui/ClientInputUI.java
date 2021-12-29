package pt.isec.pd_g33.client.ui;

import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.client.files.SendFileProc;
import pt.isec.pd_g33.shared.*;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Delayed;

public class ClientInputUI implements Runnable {

    private final ServerConnectionManager serverConnectionManager;
    private final ObjectOutputStream oos;
    private final Scanner scanner;
    private String[] command = {"empty"};
    // Variavel possivel de obter fora da thread
    private volatile String[] loginOrRegister;

    public ClientInputUI(ServerConnectionManager scm, String[] loginOrRegister) {
        System.out.println("Dados loginRegisto CONSTRUTOR");
        for(String item : loginOrRegister)
            System.out.print(item);
        this.loginOrRegister = loginOrRegister;
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

    public String[] getLoginOrRegister(){
        return loginOrRegister;
    }

    private boolean executeLogin()  {
        /*System.out.println("Dados loginRegisto");
        for(String item : loginOrRegister)
            System.out.print(item);*/
        if(!loginOrRegister[0].equalsIgnoreCase("empty")){
            writeToSocket(new Login(loginOrRegister[1], loginOrRegister[2]));
        }else {
            do {
                // Caso ja tenha feito o login antes, é porque trocou de servidor e vai se reconectar automaticamente
                // Caso seja a primeira vez a entrar
                System.out.print("$> ");
                command = scanner.nextLine().split("\\s");
                try {
                    switch (command[0]) {
                        case "commands" -> showLoginCommands();
                        case "login" -> writeToSocket(new Login(command[1], command[2]));
                        case "register" -> {
                            String name = String.join(" ", Arrays.copyOfRange(command, 3, command.length));
                            if (name.length() > 50 || command[1].length() > 30 || command[2].length() > 30) {
                                System.out.println("""
                                        [ERROR] Input too long:
                                        \t-> Name should have less than 50 characters
                                        \t-> Username and Password should have less than 30 characters each
                                        """);
                                break;
                            }
                            writeToSocket(new Register(command[1].toLowerCase(), command[2], name));
                        }
                        case "exit" -> {
                            return false;
                        }
                        default -> System.out.println("Insira um comando valido. Para consultar comandos escreva: commands ");
                    }
                } catch (Exception e) {
                    System.out.println("Insira um comando valido. Para consultar comandos escreva: commands ");
                    //e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } while (!serverConnectionManager.isServerConnected() || !serverConnectionManager.isUserConnected());
        }
        // Caso o user esteja conectado, vai ser guardado na variavel volatil os dados de login. Para poder ser acedido
        // por outras classes (ServerConnectionManager), caso seja  atribuido um novo servidor, não seja necessário fazer o login de novo.
        if(serverConnectionManager.isServerConnected())
            loginOrRegister = command;
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

                "-> Send message to group", "sendmsg group <group_id> <...message...>",
                "-> Send message to contact", "sendmsg contact <username> <...message...>",
                "-> Send file to group", "sendfile group <group_id> <...path...> <...file...>",
                "-> Send file to contact", "sendfile contact <username> <...path...> <...file...>",
                "-> Delete file sent", "deletefile <filename>",
                "-> Request file from contact", "get contact <username> <...file...> <...save_location...>",
                "-> Request file from group", "get group <group_id> <...file...> <...save_location...>",
                "-> List messages/files to contact", "list contact <username>",
                "-> List messages/files to group", "list group <group_id>",
                "-> List unseen messages", "listunseen",
                "-> Delete message from/to contact/group", "delmsg <message id from group or contact>",

                "-> Exit client", "exit");
    }

    private void cmdDecision() {
        do {
            try{
                System.out.print("$> ");
                command = scanner.nextLine().split("\\s");
                switch (command[0]) {
                    case "commands" -> showMainCommands(); // ok

                    // User
                    case "edit" -> writeToSocket(new Data(MenuOption.EDIT_USER,new UserData(command[2], command[3], command[1]),serverConnectionManager.getUserData().getUserID())); // ok
                    case "listall" -> writeToSocket(new Data(MenuOption.LIST_USERS)); // ok
                    case "search" -> writeToSocket(new Data(MenuOption.SEARCH_USER, command[1])); // ok

                    // Groups
                    case "create" -> writeToSocket(new Data(MenuOption.CREATE_GROUP, serverConnectionManager.getUserData(), command[1])); // ok
                    case "listgroup" -> writeToSocket(new Data(MenuOption.LIST_GROUPS)); // ok
                    case "join" -> writeToSocket(new Data(MenuOption.JOIN_GROUP, serverConnectionManager.getUserData(),Integer.parseInt(command[1]))); // ok
                    case "memberaccept" -> writeToSocket(new Data(MenuOption.MEMBER_ACCEPT,command[2],Integer.parseInt(command[1]), serverConnectionManager.getUserData()));// ok
                    case "memberrm" -> writeToSocket(new Data(MenuOption.MEMBER_REMOVE, command[2], Integer.parseInt(command[1]), serverConnectionManager.getUserData()));// ok
                    case "rename" -> writeToSocket(new Data(MenuOption.RENAME_GROUP, command[2], Integer.parseInt(command[1]), serverConnectionManager.getUserData()));// ok
                    case "del" -> writeToSocket(new Data(MenuOption.DELETE_GROUP, serverConnectionManager.getUserData().getUsername() ,Integer.parseInt(command[1])));// ok
                    case "leave" ->writeToSocket(new Data(MenuOption.LEAVE_GROUP, serverConnectionManager.getUserData(), Integer.parseInt(command[1]))); // ok

                    // Contacts
                    case "listcontact" -> writeToSocket(new Data(MenuOption.LIST_CONTACTS)); // ok
                    case "pendcontact" -> writeToSocket(new Data(MenuOption.PENDING_CONTACT, serverConnectionManager.getUserData().getUsername())); // ok
                    case "addc" -> writeToSocket(new Data(MenuOption.ADD_CONTACT,serverConnectionManager.getUserData().getUsername(), command[1])); // ok
                    case "delc" -> writeToSocket(new Data(MenuOption.DELETE_CONTACT, serverConnectionManager.getUserData(),command[1])); // ok
                    case "accept" -> writeToSocket(new Data(MenuOption.ACCEPT_CONTACT,serverConnectionManager.getUserData().getUsername(),command[1])); // ok
                    case "reject" -> writeToSocket(new Data(MenuOption.REJECT_CONTACT,serverConnectionManager.getUserData().getUsername(),command[1])); // ok

                    // Messages
                    case "sendmsg" -> { // ok
                        String message = String.join(" ", Arrays.copyOfRange(command, 3, (command.length )));
                        if(command[1].equalsIgnoreCase("contact"))
                            writeToSocket(new Data(MenuOption.SEND_MSG_TO_CONTACT, message, command[2], serverConnectionManager.getUserData(), DataType.Message));
                        else
                            writeToSocket(new Data(MenuOption.SEND_MSG_TO_GROUP, message, Integer.parseInt(command[2]), serverConnectionManager.getUserData(), DataType.Message));
                    }
                    case "list" -> {
                        MenuOption contactOrGroup = command[1].equalsIgnoreCase("contact") ? MenuOption.LIST_MSG_FILES_CONTACT : MenuOption.LIST_MSG_FILES_GROUP;
                        writeToSocket(new Data(contactOrGroup,serverConnectionManager.getUserData().getUsername(),command[2]));
                    }
                    case "listunseen" -> { // ok
                        writeToSocket(new Data(MenuOption.LIST_UNSEEN,serverConnectionManager.getUserData().getUsername()));
                    }
                    case "delmsg" -> writeToSocket(new Data(MenuOption.DELETE_MESSAGE,serverConnectionManager.getUserData().getUsername() , Integer.parseInt(command[1])));

                    // Files
                    case "sendfile" -> {
                        SendFileProc sendFileProc = new SendFileProc(command[3], command[4]);
                        Thread tsfp = new Thread(sendFileProc);
                        tsfp.start();
                        // sendfile group <group_id> <...path...> <...file...>
                        if(command[1].equalsIgnoreCase("contact"))
                            writeToSocket(new Data(MenuOption.SEND_FILE_TO_CONTACT, command[4], command[2] ,sendFileProc.getSendFileSocketIp(),sendFileProc.getSendFileSocketPort(),serverConnectionManager.getUserData()));
                        else
                            writeToSocket(new Data(MenuOption.SEND_FILE_TO_GROUP, command[4], Integer.parseInt(command[2]) ,sendFileProc.getSendFileSocketIp(),sendFileProc.getSendFileSocketPort(),serverConnectionManager.getUserData()));
                    }
                    case "deletefile" -> writeToSocket(new Data(MenuOption.DELETE_FILE,command[1],serverConnectionManager.getUserData().getUserID()));
                    case "get" -> {
                        if(serverConnectionManager.setSaveLocation(command[4])) {
                            if (command[1].equalsIgnoreCase("contact"))
                                writeToSocket(new Data(MenuOption.REQUEST_FILE_FROM_CONTACT, command[3], command[2], null, 0, serverConnectionManager.getUserData()));
                            else
                                writeToSocket(new Data(MenuOption.REQUEST_FILE_FROM_GROUP, command[3], Integer.parseInt(command[2]), null, 0, serverConnectionManager.getUserData()));
                        }
                        else
                            System.out.println("[ERROR] Save location is invalid!");
                    }
                    case "exit" -> {}
                    default -> System.out.println("Indique um comando válido");
                }
            }catch (Exception  e){
                System.out.println("Insira um comando valido. Para consultar comandos escreva: commands ");
            }
        } while (!command[0].equalsIgnoreCase("exit"));
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

