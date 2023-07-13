package Demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public interface MasterServerClientInterface extends Remote{

	/**
	 * Read file from server
	 * 
	 * @param fileName
	 * @return File data
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RemoteException
	 */
	public List<ReplicaLoc> read(String fileName) throws FileNotFoundException,
			IOException, RemoteException;

	/**
	 * Start a new write transaction
	 * 
	 * @param fileName
	 * @return the required info
	 * @throws RemoteException
	 * @throws IOException
	 */
	
	public List<ReplicaLoc> readReplicas() throws FileNotFoundException,
	IOException, RemoteException;
	
	public WriteAck write(String fileName) throws RemoteException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException;

	public boolean loginUser(String username, String password) throws IOException;

	public void setPermission(String filename, String owner, String permission) throws RemoteException;
	
	public String[] fetchPermission(String filename) throws RemoteException;

	public boolean registerNewUser(String username, String password)throws RemoteException;

	public void updateFileName(String oldfileName, String newfileName) throws RemoteException;

	public String getsaltValue(String userloggedIn) throws RemoteException;


//	public boolean registerNewUser(String username, String password);
	
	//public String setPermission(String filename, String owner, String permission);
	
	
	/**
	 * @param fileName
	 * @return the replica location of the primary replica of that file
	 * @throws RemoteException
	 */
}
