package server;

import data.ConnectionMessage;
import data.ConnectionType;

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        System.out.println("Server");
        InetAddress grdsIp;
        int grdsPort;
        /*if(args.length < 2) { //todo: mudar length para 4 quando existir sgbd
            System.err.println("Missing args: <ip_grds> <port_grds> <ip_sgbd> <port_sgbd>");
            return;
        }*/
        try {
            //Não recebeu o IP e PORTO do GRDS
            //todo: alterar aqui, para funcionar com multicast
            if(args.length != 3){
                grdsPort = 9001;
                grdsIp = InetAddress.getByName("127.0.0.1");
            }else{ //todo: mudar para 3 para acrescentar a BD
                grdsIp = InetAddress.getByName(args[0]);
                grdsPort = Integer.parseInt(args[1]);
            }
            System.out.println("GRDS: " + grdsIp.getHostName() + ":" + grdsPort);

            ConnectionMessage connectionMessage = new ConnectionMessage(ConnectionType.Server);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(connectionMessage);
            oos.flush();

            DatagramSocket ds = new DatagramSocket();
            //ds.setSoTimeout(3000);
            DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, grdsIp, grdsPort);
            ds.send(dp);

            System.out.println("DatagramPacket enviado ao GRDS");
            dp = new DatagramPacket(new byte[4096],4096);
            ds.receive(dp);

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
            ObjectInputStream ois = new ObjectInputStream(bais);
            connectionMessage = (ConnectionMessage) ois.readObject();

            // System.out.println("Server IP: " + connectionMessage.getIp() + ":" + connectionMessage.getPort());
            System.out.println(connectionMessage.getMessage());
            ds.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            System.out.println("Exception.: Socket timeout");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }




    }
}
