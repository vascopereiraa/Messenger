package pt.isec.pd_g33.client.files;

import java.io.*;
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
        File file = null;
        try (Socket sCli = serverSocket.accept()) {

            OutputStream out = sCli.getOutputStream();
            ObjectInputStream ois = new ObjectInputStream(sCli.getInputStream());

            System.out.println("Vou enviar o ficheiro: [" + filename + "] para o socket: " + getSendFileSocketIp() + ":" + getSendFileSocketPort());
            file = new File(path + File.separator + filename);
            FileInputStream fis = new FileInputStream(file.toString());
            // int fileSize = 0;
            int lidos;
            while(fis.available() != 0){
                byte[] fileChunk = new byte[DATA_SIZE];
                lidos = fis.read(fileChunk);
                out.write(fileChunk, 0, lidos);
                // fileSize += lidos;
            }

            String s = (String) ois.readUnshared();
            System.out.println(s);

            fis.close();
            out.close();
            ois.close();
            serverSocket.close();

            System.out.println("Mensagem enviada com sucesso! "/* + fileSize*/);
        } catch (IOException | ClassNotFoundException e) {
            file.delete(); // Apaga ficheiro caso ocorra algo mal, ficheiro esta incompleto
            e.printStackTrace();
            System.err.println("IOException: run");
        }
    }
}
