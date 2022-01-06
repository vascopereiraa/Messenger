package pt.isec.pd_g33.grds.RMI_Meta3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GetNotificationsObserverInterface extends Remote {
    void notifyNewNotification(String description) throws RemoteException;
}
