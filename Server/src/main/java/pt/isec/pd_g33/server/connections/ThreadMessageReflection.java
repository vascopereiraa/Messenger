package pt.isec.pd_g33.server.connections;

import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.file.ThreadReceiveFiles;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.Notification;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.List;

// Thread para receber datagramas UDP enviados pelo GRDS
public class ThreadMessageReflection implements Runnable {

    private static List<UserInfo> listUsers;
    private final String folderPath;
    private final int sendFilesPort;
    private final MulticastSocket multicastSocket;

    public ThreadMessageReflection(List<UserInfo> listUsers, String folderPath, int sendFilesPort, MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
        this.listUsers = listUsers;
        this.folderPath = folderPath;
        this.sendFilesPort = sendFilesPort;
    }

    @Override
    public void run() {
        while(true){
            try {
                DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                multicastSocket.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Notification notification = (Notification) ois.readObject();

                // System.out.println("Recebi uma mensagem refletida: " + notification.getFromUsername() + " : "+ notification.getToUsername() + " : " + notification.getDataType());

                if(notification.getDataType() == DataType.File && notification.getPorto() != sendFilesPort) {
                    ThreadReceiveFiles trf = new ThreadReceiveFiles(notification.getIp(),notification.getPorto(),
                            folderPath, notification.getContent());
                    Thread ttrf = new Thread(trf);
                    ttrf.start();
                }
                // Caso seja notificacao para apagar um ficheiro, vai apagar ficheiro do diretorio com o nome: x
                if(notification.getDataType() == DataType.File && notification.getToUsername().equals("deletefile")){ // fromUserName contem o nome do ficheiro
                    File file = new File(folderPath + File.separator + notification.getFromUsername());
                    if (file.exists())
                        file.delete();
                }else{
                    System.out.println(notification);
                    if(!notification.isUpdateFiles()){ // Caso não seja um update de ficheiros, avisa o cliente da notificação normal.
                        // Envia notificação ao cliente correto caso ele esteja connectado a este servidor
                        listUsers.forEach(u -> {
                            if (notification.getToUsername().equals(u.getUsername())) {
                                System.out.println("\nVou enviar a notificação ao utilizador: " + u.getUsername()
                                + " Notificacao: " + notification.getFromUsername() + " : " + notification.getToUsername()
                                + " : " + notification.getDataType());
                                u.writeSocket(notification);
                            }
                        });
                    }
                }

            } catch (Exception e) {
                System.err.println("Multicast reading terminado!");
                break;
            }
        }
    }

    public static void terminaClientes(ConnectionMessage connectionMessage){
        ClientConnectionTCP.sendNotificationToGRDS(new Notification("serverTerminated",connectionMessage.getPort(),DataType.Message));
        listUsers.forEach(u -> u.writeSocket(new Notification("serverTerminated",DataType.Message)));
    }

}
