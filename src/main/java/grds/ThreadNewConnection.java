package grds;

import data.ConnectionMessage;
import data.ConnectionType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ThreadNewConnection implements Runnable {

    private DatagramSocket ds;
    private ServerList serverList;

    public ThreadNewConnection(DatagramSocket ds, ServerList serverList) {
        this.ds = ds;
        this.serverList = serverList;
    }

    @Override
    public void run() {
        System.err.println("Thread a arrancar!");
        while(true){
            try{
                DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                ds.receive(dp);
                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                ConnectionMessage connectionMessage = (ConnectionMessage) ois.readObject();
                System.out.println("GRDS received: " + connectionMessage.getIp() + ":" + connectionMessage.getPort());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
