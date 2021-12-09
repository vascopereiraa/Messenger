package pt.isec.pd_g33.client.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class SendFileProc implements Runnable {

    private static final int DATA_SIZE = 4000;

    private final String path;
    private final String filename;
    private ServerSocket serverSocket;

    public SendFileProc(String path, String filename) {
        this.path = path;
        this.filename = filename;

        try {
            this.serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSendFileSocketIp() {
        return serverSocket.getInetAddress().getHostAddress();
    }

    public int getSendFileSocketPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            Socket sCli = serverSocket.accept();
            ObjectOutputStream oos = new ObjectOutputStream(sCli.getOutputStream());

            System.out.println("Vou enviar o ficheiro: [" + filename + "] para o socket: " + getSendFileSocketIp() + ":" + getSendFileSocketPort());
            FileInputStream fis = new FileInputStream(Paths.get(path, filename).toString());
            while(fis.available() > 0){
                byte[] fileChunck = new byte[DATA_SIZE];
                int nBytes = fis.read(fileChunck);
                oos.write(fileChunck,0,nBytes);
                oos.flush();
            }
            /*fis.close();
            oos.close();
            sCli.close();*/
            //todo: debug
            System.out.println("Mensagem enviada com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IOException: run");
        }
    }
}
