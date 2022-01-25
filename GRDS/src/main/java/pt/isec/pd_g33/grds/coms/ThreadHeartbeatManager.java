package pt.isec.pd_g33.grds.coms;

import pt.isec.pd_g33.grds.RMI_Meta3.GetNotificationsObserverInterface;
import pt.isec.pd_g33.shared.ServerInfo;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Ponto 5.
 * Verificação se o servidor está vivo a cada 20 segundos
 * Caso nao responda, não sao mais redirecionados clientes para este servidor
 * Caso seja a 3a vez que nao responde é removido da lista de servidores
 */
public class ThreadHeartbeatManager implements Runnable {

    private final CopyOnWriteArrayList<ServerInfo> serverList;
    private final CopyOnWriteArrayList<GetNotificationsObserverInterface> observers;

    public ThreadHeartbeatManager(CopyOnWriteArrayList<ServerInfo> serverList, CopyOnWriteArrayList<GetNotificationsObserverInterface> observers){
        this.serverList = serverList;
        this.observers = observers;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(20000);
                Iterator<ServerInfo> it = serverList.iterator();
                while(it.hasNext()) {
                    ServerInfo sv = it.next();
                    if(sv.getHearthbeat()) {
                        long time = System.currentTimeMillis();
                        //System.out.println((time - sv.getDate()) / 1000);
                        sv.setDate(time);
                        sv.resetHearthbeatFail();
                        sv.markAsDead();
                    }
                    else {
                        sv.incHearthbeatFail();
                        if(sv.getHearthbeatFail() == 3){
                            sendNotification("Um servidor foi eliminado do GRDS. Info do servidor. " + it);
                            it.remove();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Servidor já foi eliminado");
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
