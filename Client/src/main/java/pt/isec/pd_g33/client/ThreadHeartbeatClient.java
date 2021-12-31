package pt.isec.pd_g33.client;

import com.sun.nio.sctp.Notification;
import pt.isec.pd_g33.client.connections.ServerConnectionManager;
import pt.isec.pd_g33.client.ui.ClientInputUI;
import pt.isec.pd_g33.shared.Data;
import pt.isec.pd_g33.shared.DataType;
import pt.isec.pd_g33.shared.MenuOption;

public class ThreadHeartbeatClient implements Runnable {

    private final ServerConnectionManager scm;

    public ThreadHeartbeatClient(ServerConnectionManager serverConnectionManager) {
        this.scm = serverConnectionManager;
    }

    @Override
    public void run() {
        try {
            while(scm.getServerConnected()){
                Thread.sleep(25000);
                //System.out.println("Heartbeat sent to Server");
                ClientInputUI.writeToSocket(new Data(MenuOption.SET_ONLINE,scm.getUserData(),DataType.Message));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
