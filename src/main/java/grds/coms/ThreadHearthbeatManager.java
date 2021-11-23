package grds.coms;

import grds.data.ServerInfo;

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
                //todo: Ponto 5. Verificação se o servidor está vivo a cada 60 segundos.
                Thread.sleep(60000);

                Iterator<ServerInfo> it = serverList.iterator();
                while(it.hasNext()) {
                    ServerInfo sv = it.next();
                    if(sv.getHearthbeat())
                        sv.markAsDead();
                    else
                        it.remove();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
