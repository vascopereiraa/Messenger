package pt.isec.pd_g33.grds.RMI_Meta3;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client_Observer extends UnicastRemoteObject implements GetNotificationsObserverInterface {

    protected Client_Observer() throws RemoteException {

    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing Args: <GRDS IP>");
            return;
        }

        try {
            // Obter a ref para o registry em execução
            Registry reg = LocateRegistry.getRegistry(args[0],Registry.REGISTRY_PORT);
            Remote remoteObj = reg.lookup("GRDS_Service");
            GetServerListGRDSInterface rti = (GetServerListGRDSInterface) remoteObj;

            // Adicionar observer
            Client_Observer CO = new Client_Observer();
            // rti.addObserver(CO);

            String input;
            Scanner scanner = new Scanner(System.in);
            System.out.println("Escreva <serverlist> para obter a lista dos servidores atuais");
            System.out.println("Escreva <addObs> para passar a receber notificações recebidas pelo GRDS");
            System.out.println("Escreva <removeObs> para deixar de receber notificações recebidas pelo GRDS");
            do{
                input = scanner.nextLine();

                if(input.equalsIgnoreCase("serverlist"))
                    System.out.println(rti.getServerList());
                if(input.equalsIgnoreCase("addObs"))
                    rti.addObserver(CO);
                if(input.equalsIgnoreCase("removeObs"))
                    rti.removeObserver(CO);

            }while (!input.equals("exit"));

            rti.removeObserver(CO);
        } catch (Exception e) {
            System.out.println("Por favor ligue o GRDS primeiro");
            //ae.printStackTrace();
        }
    }

    @Override
    public void notifyNewNotification(String description) throws RemoteException {
        System.out.println("Notificação do GRDS: " + description);
    }
}
