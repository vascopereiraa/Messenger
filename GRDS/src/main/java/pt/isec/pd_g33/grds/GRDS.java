package pt.isec.pd_g33.grds;

import pt.isec.pd_g33.grds.RMI_Meta3.GetNotificationsObserverInterface;
import pt.isec.pd_g33.grds.RMI_Meta3.GetServerListGRDSInterface;
import pt.isec.pd_g33.grds.coms.ThreadHeartbeatManager;
import pt.isec.pd_g33.grds.coms.ThreadNewConnection;
import pt.isec.pd_g33.grds.coms.ThreadNotificationMulticast;
import pt.isec.pd_g33.grds.data.ServerList;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CopyOnWriteArrayList;

//todo: RMI extends implement
public class GRDS extends UnicastRemoteObject implements GetServerListGRDSInterface {

    private static final String MULTICAST_IP = "230.30.30.30" ;
    private static final int MULTICAST_PORT = 3030;

    private ServerList serverList;

    //todo:  RMI - >Não permite duas threads aceder ao mesmo tempo a este array. Perde performance
    private final CopyOnWriteArrayList<GetNotificationsObserverInterface> observers;

    //todo: RMI obrigatorio ter construtor
    protected GRDS() throws RemoteException {
        //todo: previne problemas de sincronização
        observers = new CopyOnWriteArrayList<>();
        serverList = new ServerList();
    }

    //todo: RMI throws
    public static void main(String[] args) throws RemoteException {
        System.out.println("GRDS");

        if (args.length < 1) {
            System.err.println("Missing Args: <Listening_Port>");
            return;
        }

        GRDS grds = new GRDS();
        grds.startThreads(Integer.parseInt(args[0]));

        //todo: RMI
        Registry reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT/*1099*/);
        reg.rebind("GRDS_Service",grds);
        System.out.println("GRDS_Service lançado...");
    }
    //todo: RMI
    @Override
    public String getServerList() throws RemoteException {
        return serverList.toString();
    }

    @Override
    public void addObserver(GetNotificationsObserverInterface obsRef) throws RemoteException {
        // Guardar todas as referencias de observadores no arraylist, para depois poder informar todos quando recebe notificacao.
        if(observers.contains(obsRef))
            return;
        observers.add(obsRef);
    }

    @Override
    public void removeObserver(GetNotificationsObserverInterface obsRef) throws RemoteException {
        if(!observers.contains(obsRef))
            return;
        observers.remove(obsRef);
    }


    public void startThreads(int listeningPort) {
        // Start threads to accept new Clients and Servers
        try {
            // Unicast thread para servidores
            DatagramSocket datagramSocket = new DatagramSocket(listeningPort);
            ThreadNewConnection unicastThreadAccept = new ThreadNewConnection(datagramSocket, serverList,observers);
            Thread t1 = new Thread(unicastThreadAccept);
            t1.start();

            // Multicast thread para efeitos de descoberta de servidores
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress ia = InetAddress.getByName(MULTICAST_IP);
            InetSocketAddress addr = new InetSocketAddress(ia, MULTICAST_PORT);
            NetworkInterface ni = NetworkInterface.getByName("en0");
            multicastSocket.joinGroup(addr, ni);

            ThreadNewConnection multicastThreadAccept = new ThreadNewConnection(multicastSocket, serverList,observers);
            Thread t2 = new Thread(multicastThreadAccept);
            t2.start();

            // Thread que trata notificações
            ThreadNotificationMulticast notificationMulticast = new ThreadNotificationMulticast(serverList,observers);
            Thread tnm = new Thread(notificationMulticast);
            tnm.start();

            // Heartbeat
            ThreadHeartbeatManager heartbeatManager = new ThreadHeartbeatManager(serverList.getServerInfo(),observers);
            Thread hearthbeatManager = new Thread(heartbeatManager);
            hearthbeatManager.start();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
