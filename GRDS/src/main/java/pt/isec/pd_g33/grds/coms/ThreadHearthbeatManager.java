package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.shared.ServerInfo;

import java.util.ArrayList;
import java.util.Iterator;

public class ThreadHearthbeatManager implements Runnable {

    private ArrayList<ServerInfo> serverList;

    public ThreadHearthbeatManager(ArrayList<ServerInfo> serverList){
        this.serverList = serverList;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(20000);
                Iterator<ServerInfo> it = serverList.iterator();
                while(it.hasNext()) {
                    ServerInfo sv = it.next();
                    if(sv.getHearthbeat()){
                        sv.resetHearthbeatFail();
                        sv.markAsDead();
                    }
                    else {
                        sv.incHearthbeatFail();
                        if(sv.getHearthbeatFail() == 3)
                            it.remove();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
