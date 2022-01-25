package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.grds.RMI_Meta3.GetNotificationsObserverInterface;
import pt.isec.pd_g33.grds.data.ServerList;
import pt.isec.pd_g33.shared.ConnectionMessage;
import pt.isec.pd_g33.shared.ConnectionType;
import pt.isec.pd_g33.shared.ServerInfo;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

// Thread dedicada a receber conexões UDP por parte do servidor, via UDP unicast e UDP multicast
public class ThreadNewConnection implements Runnable {

    private final DatagramSocket ds;
    private final ServerList serverList;
    private CopyOnWriteArrayList<GetNotificationsObserverInterface> observers;

    public ThreadNewConnection(DatagramSocket ds, ServerList serverList, CopyOnWriteArrayList<GetNotificationsObserverInterface> observers) {
        this.ds = ds;
        this.serverList = serverList;
        this.observers = observers;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while(true){
            try{
                DatagramPacket dp = new DatagramPacket(new byte[4096], 4096);
                ds.receive(dp);

                System.out.println("Received");

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                ConnectionMessage connectionMessage = (ConnectionMessage) ois.readObject();

                // Message Processing
                if(connectionMessage.getConnectionType() == ConnectionType.Server) {
                    // Tenta inserir o servidor, se ja existir, atualiza o indicador que esta vivo, caso contrario, insere servidor
                    if(serverList.addServer(new ServerInfo(dp.getAddress(), connectionMessage.getPort()))) {
                        connectionMessage.setMessage("Server_%04d".formatted(serverList.getNextIndex()));
                        // System.out.println("New server info: " + dp.getAddress().getHostAddress() + " : " + connectionMessage.getPort());
                        System.out.println(serverList);
                        //todo: RMI, servidor adicionado, enviar notificação
                        sendNotification("Um novo servidor foi adicionado ao GRDS. Info de servidor. " + serverList.getServerInfo().get(serverList.getServerInfo().size() - 1));
                    }
                }
                else { // Mensagem de cliente, atribui servidor
                    //todo: RMI, cliente adicionado, enviar notificação
                    sendNotification("Um novo cliente foi adicionado ao GRDS. Porto do cliente. " + connectionMessage.getPort());
                    connectionMessage.insertServerInfo(serverList.getNextServer());
                    // System.out.println("New client info: " + connectionMessage.getIp() + " : " + connectionMessage.getPort());
                }
                // Envio de mensagem de volta ao cliente(informa de server a conectar) e servidor
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.writeUnshared(connectionMessage);
                out.flush();

                dp.setData(baos.toByteArray());
                dp.setLength(baos.toByteArray().length);
                ds.send(dp);

                // Tem em conta conexões em que existiu um fail do hearthbeat, bem como servidores novos. Reenvia os ficheiros todos
                if((connectionMessage.getConnectionType() == ConnectionType.Server && serverList.getServerInfo().get(serverList.getServerInfo().size() - 1).isNewServer())
                || (connectionMessage.getConnectionType() == ConnectionType.Server && serverList.getServerInfoByPorto(connectionMessage.getPort()).getHearthbeatFail() > 0)){
                    // Obtenção de todos os ficheiros existentes
                    ThreadNotificationMulticast.synchronizeFiles();
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification(String notificacao){
        Iterator<GetNotificationsObserverInterface> it = observers.iterator();
        GetNotificationsObserverInterface itnext = null;
        while(it.hasNext()){
            try {
                itnext = it.next();
                itnext.notifyNewNotification(notificacao);
            } catch (Exception e) {
                System.out.println("Listener já não existe");
                observers.remove(itnext);
            }
        }
    }
}
