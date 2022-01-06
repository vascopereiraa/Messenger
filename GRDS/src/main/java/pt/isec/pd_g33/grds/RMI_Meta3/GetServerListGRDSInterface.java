package pt.isec.pd_g33.grds.RMI_Meta3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GetServerListGRDSInterface extends Remote {
    // Adicionar um observer, para depois notificar os obs com a notificação que o GRDS recebeu
    void addObserver(GetNotificationsObserverInterface obsRef) throws RemoteException;
    void removeObserver(GetNotificationsObserverInterface obsRef) throws RemoteException;
    String getServerList() throws RemoteException;
}
