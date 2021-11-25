package pt.isec.pd_g33.client;

import pt.isec.pd_g33.client.connections.ThreadServerConnection;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.UserData;
import pt.isec.pd_g33.shared.ConnectionType;
import pt.isec.pd_g33.shared.UserData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class Client {

    private static InetAddress grdsIp;
    private static int grdsPort;
    private static ConnectionMessage connectionMessage;

    public static boolean argsProcessing(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("""
                        Missing or Incorrect args
                        Launch program using: java Server <grds_ip> <grds_port>
                        """);
                return false;
            } else {
                grdsIp = InetAddress.getByName(args[0]);
                grdsPort = Integer.parseInt(args[1]);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean connectGRDS(){
        try {
            System.out.println("GRDS: " + grdsIp.getHostName() + ":" + grdsPort);
            // Envio de mensagem ao GRDS para informar de novo cliente, para de seguida receber o servidor a que se vai ligar por TCP
            connectionMessage = new ConnectionMessage(ConnectionType.Client);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeUnshared(connectionMessage);
            oos.flush();
            // Envio datagrama com IP e PORTO do  cliente para o GRDS
            DatagramSocket ds = new DatagramSocket();
            // ds.setSoTimeout(3000);
            DatagramPacket dp = new DatagramPacket(baos.toByteArray(),baos.toByteArray().length, grdsIp, grdsPort);
            ds.send(dp);

            // Criação de um novo DatagramPacket para receber a mensagem do GRDS com IP e PORTO do servidor a ligar por TCP
            dp = new DatagramPacket(new byte[4096],4096);
            System.out.println("DatagramPacket send to GRDS");
            ds.receive(dp);
            // Leitura do DP recebido serializado
            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
            ObjectInputStream ois = new ObjectInputStream(bais);
            connectionMessage = (ConnectionMessage) ois.readObject();

            if(connectionMessage.getPort() == 0 && connectionMessage.getIp() == null) {
                System.err.println("No servers yet");
                return false;
            }
            System.out.println("Received Server IP: " + connectionMessage.getIp().getHostName() + ":" + connectionMessage.getPort());
            ds.close();

        } catch (UnknownHostException e) {
            System.out.println("Exception.: UnknownHostException");
            e.printStackTrace();
            return false;
        } catch (SocketTimeoutException e) {
            System.out.println("Exception.: Socket timeout");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println("Exception.: IOException");
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("Exception.:ClassNotFoundException");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Change to boolean later
    /*public static void connectServerTCP(){

    }*/

    public static void main(String[] args) {
        System.out.println("Client");

        if(!argsProcessing(args)) return;

        if (!connectGRDS()) {
            System.out.println("Client: An error occurred when connecting to GRDS");
            return;
        }

        ThreadServerConnection threadServerConnection = new ThreadServerConnection(connectionMessage);
        Thread serverConnection = new Thread(threadServerConnection);
        serverConnection.start();

    }


}

