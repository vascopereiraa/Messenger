package pt.isec.pd_g33.server;

import pt.isec.pd_g33.server.connections.GRDSConnection;

public class ThreadHeartbearServer implements Runnable {

    private GRDSConnection grdsConnection;

    public ThreadHeartbearServer(GRDSConnection grdsConnection){
        this.grdsConnection = grdsConnection;
    }

    @Override
    public void run() {

        try {
            while(grdsConnection.getGrdsConnection()){
                Thread.sleep(20000);
                System.out.println("Heartbeat sent to GRDS");
                if(!grdsConnection.connectGRDS()){
                    System.err.println("Server: An error occurred when connecting to GRDS");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}