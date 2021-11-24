package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.shared.ConnectionMessage;

import java.io.*;
import java.net.*;

public class GRDSConnection {

    private InetAddress grdsIp;
    private int grdsPort;
    private ConnectionMessage connectionMessage;
    private boolean grdsConnection;

    public GRDSConnection(InetAddress grdsIp, int grdsPort, ConnectionMessage connectionMessage) {
        this.grdsIp = grdsIp;
        this.grdsPort = grdsPort;
        this.connectionMessage = connectionMessage;
        this.grdsConnection = false;
    }

    public boolean connectGRDS() {
        int tentativaLigacaoGRDS = 0;
        while (tentativaLigacaoGRDS < 3) {
            try {
                // Criação de nova mensagem de conexão com indicação que é um servidor
                // connectionMessage = new ConnectionMessage(ConnectionType.Server);
                // Envio de um DatagramPacket ao GRDS para informar de criação de novo servidor
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(connectionMessage);
                oos.flush();

                DatagramSocket ds = new DatagramSocket();
                ds.setSoTimeout(3000);
                DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, grdsIp, grdsPort);
                ds.send(dp);
                System.out.println("DatagramPacket enviado ao GRDS");

                // Criação de novo DatagramPacket para receber nova mensagem do GRDS confirmando a ligação correta com o GRDS
                dp = new DatagramPacket(new byte[4096], 4096);
                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData(), 0, dp.getLength());
                ObjectInputStream ois = new ObjectInputStream(bais);
                connectionMessage = (ConnectionMessage) ois.readObject();

                // System.out.println("Server IP: " + connectionMessage.getIp() + ":" + connectionMessage.getPort());
                System.out.println(connectionMessage.getMessage());
                ds.close();
                //Caso corra tudo bem, termina tentativa de ligação ao GRDS
                grdsConnection = true;
                break;
            } catch (UnknownHostException e) {
                System.out.println("Exception.: UnknownHostException");
                e.printStackTrace();
                grdsConnection = false;
                return false;
            } catch (SocketTimeoutException e) {
                System.out.println("Exception.: Socket timeout. Não foi possivel estabelecer ligação com o GRDS");
                ++tentativaLigacaoGRDS;
                grdsConnection = false;
            } catch (IOException e) {
                System.out.println("Exception.: IOException");
                e.printStackTrace();
                grdsConnection = false;
                return false;
            } catch (ClassNotFoundException e) {
                System.out.println("Exception.:ClassNotFoundException");
                e.printStackTrace();
                grdsConnection = false;
                return false;
            }
        }
        if(tentativaLigacaoGRDS == 3)
            return false;
        return true;
    }

    public boolean getGrdsConnection(){
        return grdsConnection;
    }
}
