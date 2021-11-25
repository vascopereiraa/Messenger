package pt.isec.pd_g33.client.connections;

import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.ConnectionType;

import java.io.*;
import java.net.*;

public class GRDSConnection {

    // GRDS location
    private InetAddress grdsIp;
    private int grdsPort;

    // Server to connect location
    private InetAddress serverIp;
    private int serverPort;

    public GRDSConnection(InetAddress grdsIp, int grdsPort) {
        this.grdsIp = grdsIp;
        this.grdsPort = grdsPort;
    }

    public boolean connectGRDS(){
        try {
            System.out.println("GRDS: " + grdsIp.getHostName() + ":" + grdsPort);

            // Envio de mensagem ao GRDS para informar de novo cliente, para de seguida receber o servidor a que se vai ligar por TCP
            ConnectionMessage connectionMessage = new ConnectionMessage(ConnectionType.Client);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeUnshared(connectionMessage);
            oos.flush();

            // Envio datagrama com IP e PORTO do cliente para o GRDS
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

            serverIp = connectionMessage.getIp();
            serverPort = connectionMessage.getPort();

            System.out.println("Received Server IP: " + serverIp.getHostName() + ":" + serverPort);
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

    public InetAddress getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }
}
