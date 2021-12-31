package pt.isec.pd_g33.server.file;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
* Estar a escuta de conexões com outros servidores que irao requisitar o envio de determinado ficheiro
* indicado pela notificação refletida
*/
public class ThreadSendFiles implements Runnable {

    private final ServerSocket ss;
    private String folderPath;
    private final ArrayList<Socket> socketsCreated = new ArrayList<>();

    public ThreadSendFiles(ServerSocket ss) {
            this.ss = ss;
    }

    public int getPortToReceiveFiles() { return ss.getLocalPort(); }

    public String getIpToReceiveFiles() {
        System.err.println("File Receive ServerSocket:" + ss.getInetAddress().getHostAddress() + ":" + ss.getLocalPort());
        return ss.getInetAddress().getHostAddress();
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket sCli = ss.accept();

                SendFilesProcedure sfp = new SendFilesProcedure(sCli, folderPath);
                Thread tsfp = new Thread(sfp);
                tsfp.start();
                // Termina Clientes
                socketsCreated.add(sCli);

            } catch (IOException e) {
                System.err.println("Thread send files foi terminada.");
                try{
                    for(Socket socket : socketsCreated)
                        socket.close();
                }catch(Exception ex){
                    System.err.println("Exception: Closing sendfiles sockets");
                }
                return;
            }
        }
    }
}

class SendFilesProcedure implements Runnable {

    private static final int DATA_SIZE = 4000;

    private final Socket sCli;
    private OutputStream oos;
    private ObjectInputStream ois;
    private final String folderPath;

    public SendFilesProcedure(Socket sCli, String folderPath) {
        this.sCli = sCli;
        this.folderPath = folderPath;
        try {
            this.oos = sCli.getOutputStream();
            this.ois = new ObjectInputStream(sCli.getInputStream());
        } catch (IOException ignored) {
            System.err.println("IOException: SendFilesProcedure");
        }
    }

    @Override
    public void run() {
        String filename = "";
        try {
            filename = (String) ois.readUnshared();
            System.out.println("Vou enviar o ficheiro: [" + filename + "] para o socket: " + sCli.getInetAddress().getHostAddress() + " : " + sCli.getPort());

            FileInputStream fis = new FileInputStream(new File(folderPath + File.separator + filename).toString());
            int nBytes;
            while(fis.available() != 0){
                byte[] fileChunck = new byte[DATA_SIZE];
                nBytes = fis.read(fileChunck);
                oos.write(fileChunck,0,nBytes);
                //oos.flush();
            }
            //todo: debug
            System.out.println("Ficheiro recebida com sucesso!");
            fis.close();
            oos.flush();
            ois.close();
            sCli.close();

        } catch (Exception e) {
            System.err.println("IOException: Thread sendfile " + filename + " closed.");
        }
    }

}