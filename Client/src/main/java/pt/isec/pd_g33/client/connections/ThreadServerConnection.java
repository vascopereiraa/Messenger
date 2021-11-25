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
    private UserData userData;

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

                System.out.println("Message:");
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

    public boolean clientEntry(){
        String username ="",password="",name="";
        System.out.println("""
                MENU:
                1-> Login 
                2-> Registo""");
        int loginDecision = scanner.nextInt();

        try {
            sCli = new Socket(connectionMessage.getIp(), connectionMessage.getPort());
            ObjectOutputStream oos = new ObjectOutputStream(sCli.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(sCli.getInputStream());

            if(loginDecision == 1){
                System.out.println("""
                Indique os dados de login:
                Indique o seu username: 
                """);
                username = scanner.nextLine();
                System.out.println("Indique a sua password: ");
                password = scanner.nextLine();
                Login login = new Login(username,password);
                oos.writeUnshared(login);
                oos.flush();
            }else{
                System.out.println("""
                NOVO REGISTO
                Indique o seu nome: 
                """);
                name = scanner.nextLine();
                System.out.println("Indique o seu username: ");
                username = scanner.nextLine();
                System.out.println("Indique a sua password: ");
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

}
