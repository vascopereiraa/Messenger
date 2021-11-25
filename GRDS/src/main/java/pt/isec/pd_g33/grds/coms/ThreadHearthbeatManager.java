package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.shared.ServerInfo;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * Ponto 5.
 * Verificação se o servidor está vivo a cada 20 segundos
 * Caso nao responda, não sao mais redirecionados clientes para este servidor
 * Caso seja a 3a vez que nao responde é removido da lista de servidores
 */
public class ThreadHearthbeatManager implements Runnable {

    private final ArrayList<ServerInfo> serverList;

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
