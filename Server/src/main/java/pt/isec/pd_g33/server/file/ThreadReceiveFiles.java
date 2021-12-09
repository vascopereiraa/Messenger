package pt.isec.pd_g33.server.file;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;

public class ThreadReceiveFiles implements Runnable {

    private static final int DATA_SIZE = 4000;

    public String ip;
    public int port;
    public String filename;
    public String path;

    public ThreadReceiveFiles(String ip, int port, String path, String filename) {
        this.ip = ip;
        this.port = port;
        this.path = path;
        this.filename = filename;
    }

    @Override
    public void run() {
            try {
                System.out.println("\nSOU O PORTO DO RECEIVE: Ip: " + ip + "Porto: " + port);
                Socket socket = new Socket(ip, port);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(filename);
                out.flush();

                FileOutputStream fileOutputStream = new FileOutputStream(Paths.get(path + filename).toString());
                byte[] bytesRead = new byte[DATA_SIZE];
                int nBytes;
                while(true) {
                    nBytes = in.read(bytesRead);
                    if(nBytes == -1)
                        break;
                    System.out.println("Li: " + nBytes);
                    fileOutputStream.write(bytesRead, 0, nBytes);
                    bytesRead = new byte[DATA_SIZE];
                }

                System.out.println("Received file '" + filename + "' from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                fileOutputStream.close();
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

}
