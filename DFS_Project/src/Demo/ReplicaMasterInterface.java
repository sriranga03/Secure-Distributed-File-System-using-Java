package Demo;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public interface ReplicaMasterInterface extends ReplicaInterface{
	
	/**
	 * creates the file at the replica server 
	 * @param fileName
	 * @throws IOException 
	 */
	public void createFile(String fileName) throws RemoteException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException;
	
	/**
	 * makes the current replica the master of the passed file
	 * @param fileName 
	 * @param slaveReplicas another replicas having the files
	 * @throws NotBoundException 
	 */
	public void takeCharge(String fileName, List<ReplicaLoc> slaveReplicas) throws RemoteException, NotBoundException ;
	
	/**
	 * @return true if the replica alive and received the call .. no return otherwise,
	 */
	public boolean isAlive() throws RemoteException;
	
}
