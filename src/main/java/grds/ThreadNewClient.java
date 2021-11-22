package grds;

import data.ConnectionMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ThreadNewClient implements Runnable {

    private DatagramSocket ds;

    public ThreadNewClient(DatagramSocket ds) {
        this.ds = ds;
    }

    @Override
    public void run() {

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
