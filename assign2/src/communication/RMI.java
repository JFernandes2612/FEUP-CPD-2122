package communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {
    String join() throws RemoteException;

    String leave() throws RemoteException;
}
