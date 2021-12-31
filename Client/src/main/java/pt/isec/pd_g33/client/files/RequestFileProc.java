package pt.isec.pd_g33.client.files;

import java.io.*;
import java.net.Socket;

public class RequestFileProc implements Runnable {

    public static final int DATA_SIZE = 4000;

    private final File saveLocation;
    private final String filename;
    private final String requestFileIp;
    private final int requestFilePort;

    public RequestFileProc(File saveLocation, String filename, String requestFileIp, int requestFilePort) {
        this.saveLocation = saveLocation;
        this.filename = filename;
        this.requestFileIp = requestFileIp;
        this.requestFilePort = requestFilePort;
    }

    @Override
    public void run() {

        try {
            Socket socket = new Socket(requestFileIp, requestFilePort);

            InputStream in = socket.getInputStream();

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeUnshared(filename);

            FileOutputStream fileOutputStream = new FileOutputStream(saveLocation.toString() + File.separator + filename);
            byte[] buf = new byte[DATA_SIZE];
            while(true) {
                int nBytes = in.read(buf);
                if(nBytes == -1)
                    break;
               // System.out.println("Li: " + nBytes);
                fileOutputStream.write(buf, 0, nBytes);
                buf = new byte[DATA_SIZE];
            }

            fileOutputStream.close();
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
