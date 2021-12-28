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

        if(new File(path + "/" + filename).exists())
            return;

        try {
            System.out.println("\nSOU O PORTO DO RECEIVE: Ip: " + ip + "Porto: " + port);
            Socket socket = new Socket(ip, port);
            InputStream in = socket.getInputStream();

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeUnshared(filename);

            FileOutputStream fileOutputStream = new FileOutputStream(Paths.get(path + filename).toString());
            byte[] buf = new byte[DATA_SIZE];
            while(true) {
                int nBytes = in.read(buf);
                if(nBytes == -1)
                    break;
                System.out.println("Li: " + nBytes);
                fileOutputStream.write(buf, 0, nBytes);
                buf = new byte[DATA_SIZE];
            }

            System.out.println("Received file '" + filename + "' from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

            fileOutputStream.close();
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {// Caso o ficheiro seja apagado, esta thread termina
            System.out.println("O ficheiro " + filename + " foi apagado.");
        }
    }

}
