package pt.isec.pd_g33.client.connections;

import pt.isec.pd_g33.shared.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import static pt.isec.pd_g33.client.Client.connectGRDS;


public class ThreadServerConnection implements Runnable {

    private ConnectionMessage connectionMessage;
    private Scanner scanner = new Scanner(System.in);
    private Socket sCli;
    private static UserData userData;

    public ThreadServerConnection(ConnectionMessage connectionMessage){
        this.connectionMessage = connectionMessage;
    }

    @Override
    public void run() {
        //todo: change name
        if(!clientEntry())
            return;

        while(true) {
            try {
                sCli = new Socket(connectionMessage.getIp(), connectionMessage.getPort());
                ObjectOutputStream oos = new ObjectOutputStream(sCli.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(sCli.getInputStream());

                System.out.println("Menu: " +
                        "1-> Editar dados de utilizador" +
                        "2-> Listar todos os utilizadores" +
                        "3-> Pesquisar utilizador" +
                        "4-> Visualizar lista de contactos" +
                        "5-> Eliminar contacto" +
                        "6-> Criação de grupo");
                int menuDecision = Integer.parseInt(scanner.nextLine());
                switch (menuDecision){
                    case 1 ->{
                        editarDadosUtilizador();
                    }
                    default -> {
                        System.out.println("Escolhe uma opcao nabo");
                    }
                }

                String msg = scanner.nextLine();
                Data dataSend = new Data(msg, DataType.Message);
                oos.writeUnshared(dataSend);
                oos.flush();

                Data dataReceived = (Data) ois.readObject();
                System.out.println("Message Received:" + dataReceived.getContent());

            } catch (IOException e) { //todo: Ponto4: Quando perde ligação com o servidor TCP, vai tentar reconnectar a um novo
                System.err.println("Servidor fechou a porta TCP." + e);
                connectGRDS();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void editarDadosUtilizador() {
        Data newData = new Data(1);
        System.out.println("Edite os seus dados: \nIndique o seu nome:");
        String name = scanner.nextLine();
        System.out.print("Indique o seu username: ");
        String username = scanner.nextLine();
        System.out.print("Indique a sua password: ");
        String password = scanner.nextLine();

        newData.setFromUserId(new UserData(username,password,name));
        newData.setContent(userData.getUsername());
    }

    public boolean clientEntry(){
        String username ="",password="",name="";
        System.out.println("""
                MENU:
                1-> Login 
                2-> Registo""");
        int loginDecision = Integer.parseInt(scanner.nextLine());


        try {
            sCli = new Socket(connectionMessage.getIp(), connectionMessage.getPort());
            ObjectOutputStream oos = new ObjectOutputStream(sCli.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(sCli.getInputStream());

            if(loginDecision == 1){
                System.out.print("Indique os dados de login: \nUsername:");
                username = scanner.nextLine();
                System.out.print("Password: ");
                password = scanner.nextLine();
                Login login = new Login(username,password);
                oos.writeUnshared(login);
                oos.flush();
            }else{
                System.out.println("Novo registo: \nIndique o seu nome:");
                name = scanner.nextLine();
                System.out.print("Indique o seu username: ");
                username = scanner.nextLine();
                System.out.print("Indique a sua password: ");
                password = scanner.nextLine();
                Register register = new Register(username,password,name);
                oos.writeUnshared(register);
                oos.flush();
            }

            String msgReceived = (String) ois.readObject();
            if(msgReceived.contains("invalido")){
                //todo: debug
                System.out.println("Login/Registo inválido");
                return false;
            }else{
                if(loginDecision == 2)
                    userData = new UserData(username,password,name);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

}
