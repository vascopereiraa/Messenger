package grds;

import data.ConnectionMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ThreadNewClient implements Runnable {

    private DatagramSocket ds;
    private String s;

    public ThreadNewClient(DatagramSocket ds, String s) {
        this.ds = ds;
        this.s = s;
    }

    @Override
    public void run() {
        System.err.println("Thread a arrancar! - " + s);
        while(true){
            try{
                DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                ds.receive(dp);
                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData(), 0 , dp.getLength());
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
