package pt.isec.pd_g33.server.Heartbeat;

import pt.isec.pd_g33.server.connections.GRDSConnection;
import pt.isec.pd_g33.server.data.UserInfo;
import pt.isec.pd_g33.server.database.DatabaseManager;

import java.util.List;

public class ThreadHeartbeatClient implements Runnable {

    private final DatabaseManager databaseManager;

    public ThreadHeartbeatClient(DatabaseManager databaseManager){
        this.databaseManager = databaseManager;
        this.databaseManager.setConnection();
    }

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        try {
            while(true){
                Thread.sleep(30000);
                //System.out.println("Heartbeat client status");
                databaseManager.updateAllUserStatus();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
