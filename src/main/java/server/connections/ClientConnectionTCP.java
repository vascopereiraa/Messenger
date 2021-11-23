package server.connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnectionTCP implements Runnable {

    private Socket sCli;

    public ClientConnectionTCP(Socket scli){
        this.sCli = scli;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(sCli.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(sCli.getInputStream());
            String msg = (String) ois.readObject();

            System.out.println("Mensagem recebida do cliente: " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
