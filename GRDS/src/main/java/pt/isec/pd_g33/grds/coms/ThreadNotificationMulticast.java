package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.grds.RMI_Meta3.GetNotificationsObserverInterface;
import pt.isec.pd_g33.grds.data.ServerList;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.Notification;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.util.concurrent.CopyOnWriteArrayList;

// Thread que recebe notificações vindas de 1 servidor para depois refletir pelos restantes servidores.
public class ThreadNotificationMulticast implements Runnable {

    private static final int UNICAST_SEND_NOTIFICATION_PORT = 1000;
    private static final int UNICAST_RECEIVE_NOTIFICATION_PORT = 2000;
    private static final String UNICAST_RECEIVE_NOTIFICATION_IP = "255.255.255.255";
    private static final CopyOnWriteArrayList<Notification> filesReceived = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<GetNotificationsObserverInterface> observers;

    private static DatagramSocket ds;
    private static MulticastSocket multicastSocket;
    private static ServerList serverList;

    public ThreadNotificationMulticast(ServerList serverList,CopyOnWriteArrayList<GetNotificationsObserverInterface> observers) {
        this.serverList = serverList;
        this.observers = observers;
    }

    @Override
    public void run() {
            try{
                ds = new DatagramSocket(UNICAST_RECEIVE_NOTIFICATION_PORT);
                multicastSocket = new MulticastSocket(UNICAST_SEND_NOTIFICATION_PORT);
                multicastSocket.setBroadcast(true);

                while(true) {
                    DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                    ds.receive(dp);

                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Notification notification = (Notification) ois.readObject();
                    System.out.println("GRDS received notification: " + notification.getDataType());

                    // Caso a notificacao seja de ficheiros
                    if(notification.getDataType() == DataType.File)
                        if(notification.getToUsername().equals("deletefile"))
                            // Notificacao para eliminar o ficheiro, remove da lista; getContent tem nome ficheiro quando é send
                            filesReceived.removeIf(obj -> obj.getContent().equals(notification.getFromUsername())); //getFromUsername tem nome do ficheiro quando é delete
                        else
                            // Notificacao para adicionar ficheiro, adiciona a lista
                            filesReceived.add(notification);

                    // Apagar servidor da lista de servers, caso ele faça EXIT.
                    if(notification.getContent().equals("serverTerminated") && notification.getDataType() == DataType.Message){
                        //todo: RMI -> tratamento quando server é eliminado
                        for(GetNotificationsObserverInterface obs : observers)
                            obs.notifyNewNotification("Um servidor foi eliminado do GRDS. Info do servidor. " + serverList.getServerInfoByPorto(notification.getPorto()));
                        System.out.println("O servidor " + notification.getPorto() + " foi eliminado");
                        serverList.getServerInfo().removeIf(obj -> obj.getPort() == obj.getPort());
                    }

                    //todo: RMI -> tratamento geral de notificações
                    trataMensagemRMI(notification);

                    // Envio em multicast para todos os servidores
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(baos);
                    out.writeUnshared(notification);
                    out.flush();

                    dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length,InetAddress.getByName(UNICAST_RECEIVE_NOTIFICATION_IP),UNICAST_SEND_NOTIFICATION_PORT);
                    multicastSocket.send(dp);
                }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void trataMensagemRMI(Notification notification) throws RemoteException {
        String description = null;
        switch (notification.getDataType()) {
            case Message -> {
                // Servidor terminou ordenadamente, terminando assim o cliente, tal como o exit.
                if(notification.getContent().equals("serverTerminated"))
                    description = "Servidor do Porto: " + notification.getPorto() + " fechou conexão.";
                else if (notification.getToGroupId() != 0)
                    description = "Tem uma mensagem por visualizar no grupo " + notification.getToGroupId() + ".:" + notification.getToGroupName() + " enviada por " + notification.getFromUsername() + ".";
                else
                    description = "O cliente " + notification.getFromUsername() + " enviou uma mensagem ao cliente " + notification.getToUsername();
            }
            case File -> {
                if (notification.getToGroupId() != 0)
                    description = "Tem um ficheiro por visualizar no grupo " + notification.getToGroupId() + ".:" + notification.getToGroupName() +
                            " com o nome: " + notification.getContent() + " enviado por " + notification.getFromUsername() + ".";
                else
                    System.out.println("O cliente " + notification.getFromUsername() + " enviou um ficheiro com o nome: " + notification.getContent() + " para o cliente " + notification.getToUsername());
            }
            case Contact -> System.out.println("O cliente : " + notification.getFromUsername() + " enviou um pedido de contacto para o cliente  " + notification.getToUsername() + ".");
            case Group ->{
                if(notification.getContent().contains("aceite"))
                    System.out.println("O pedido de adesão do cliente:" + notification.getFromUsername() + " ao grupo " + notification.getToGroupId() + ".:" + notification.getToGroupName() + " foi aceite.");
                else
                    System.out.println("O cliente: " + notification.getFromUsername() +" foi removido do grupo " + notification.getToGroupId() + ".:" + notification.getToGroupName() + " pelo administrador.");
            }
        }
        //todo: RMI, servidor adicionado, enviar notificação
        for(GetNotificationsObserverInterface obs : observers)
            obs.notifyNewNotification(description);
    }

    public static void synchronizeFiles() {
        for (Notification notification : filesReceived) {
            try {
                // Feito para que todos os servidores que estejam a escuta no multicast,
                // não enviem de novo a notificação ao cliente, pois é usado o mesmo socket para avisar o novo server dos ficheiros.
                notification.setUpdateFiles(true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.writeUnshared(notification);
                out.flush();
                DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, InetAddress.getByName(UNICAST_RECEIVE_NOTIFICATION_IP), UNICAST_SEND_NOTIFICATION_PORT);
                multicastSocket.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
