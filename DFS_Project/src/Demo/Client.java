package Demo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import javax.crypto.Cipher;

public class Client {
	
	static MasterServerClientInterface masterStub;
	static Registry registry;
	static String userloggedIn;
	static String decodedFile;
	public String data1;
	public Client() {
		try {
//			registry = LocateRegistry.getRegistry("10.200.12.99",59216);
			registry = LocateRegistry.getRegistry(59218);
			try {
				masterStub = (MasterServerClientInterface) registry.lookup("MasterServerClientInterface");
			} catch (NotBoundException | RemoteException e) {
				
				e.printStackTrace();
			}
			System.out.println("Connection established with master");
			
		}catch(RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void read(String fileName, String key) throws IOException, NotBoundException, CryptoException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		ReplicaLoc replicaLoc = replicalocations.get(0);
//		registry = LocateRegistry.getRegistry(replicaLoc.getAddress(),replicaLoc.getPort());
		registry = LocateRegistry.getRegistry(replicaLoc.getPort());
		ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaLoc.getId());
		String k1 = key;
		String[] permission = fetchPermissions(fileName);
//		System.out.println("File Name " + fileName);
//		System.out.println(k1);
		if(permission[1].contains("r")) {
			k1 = masterStub.getsaltValue(permission[0]);
		}else if(permission[0].equals(userloggedIn)){
			k1 = masterStub.getsaltValue(userloggedIn);
		}
		ReplicaServer.doCrypto(Cipher.DECRYPT_MODE, fileName, k1);
		String fileContent = replicaStub.read(fileName);
			if(fileContent.isEmpty()) {
				System.out.println("File not found");
			}	
			else {
				if (permission[1].contains("r") || permission[0].equals(userloggedIn)){
					System.out.println("File Content: ");
					System.out.println(replicaStub.read(fileName));
					ReplicaServer.doCrypto(Cipher.ENCRYPT_MODE, fileName, k1);
					System.out.println("Read action completed successfully");
					}
				else {
					System.out.println("Access denied");
				}
			}
	}
	public void delete(String fileName) throws IOException, NotBoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		String[] permission = fetchPermissions(fileName);
		if(permission[1].contains("x") || permission[0].equals(userloggedIn)) {
			for(int i = 0; i< replicalocations.size();i++) {
				ReplicaLoc replicaLoc = replicalocations.get(i);
				int replicaID = replicaLoc.getId();
	//			registry = LocateRegistry.getRegistry(replicaLoc.getAddress(),replicaLoc.getPort());
				registry = LocateRegistry.getRegistry(replicaLoc.getPort());
				ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaID);
				boolean status  = replicaStub.delete(fileName);
				if (status) {
					System.out.println("Delete operation completed successfully");
				}
				else {
					System.out.println("File not found");
				}
			}
		}
		else {
			System.out.println("Access denied");
		}
	}
		
	public void rename(String oldfileName, String newfileName) throws IOException, NotBoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		String[] permission = fetchPermissions(oldfileName);
		if (permission[1].contains("x") || permission[0].equals(userloggedIn)) {
			updateFileName(oldfileName,newfileName);
			for(int i = 0; i< replicalocations.size();i++) {
				ReplicaLoc replicaLoc = replicalocations.get(i);
				int replicaID = replicaLoc.getId();
	//			registry = LocateRegistry.getRegistry(replicaLoc.getAddress(), replicaLoc.getPort();
				registry = LocateRegistry.getRegistry(replicaLoc.getPort());
				ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaID);
				String result = replicaStub.rename(oldfileName, newfileName);
				System.out.println(result);
				}	
		}
		else {
			System.out.println("Access denied");
		}
	}
	
	public void updateFileName(String oldfname,String newfname) throws RemoteException {
		 masterStub.updateFileName(oldfname,newfname);
	}
	
	@SuppressWarnings("unused")
	public void write(String fileName, byte[] data, String key) throws IOException, NotBoundException, MessageNotFoundException, CryptoException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
//		String permission = fetchPermissions(fileName);
		String k1 = key;
		String[] permission = fetchPermissions(fileName);
		if(permission[1].contains("r")) {
			k1 = masterStub.getsaltValue(permission[0]);
		}else if(permission[0].equals(userloggedIn)){
			k1 = masterStub.getsaltValue(userloggedIn);
		}
		if (permission[1].contains("w") || permission[0].equals(userloggedIn)) {
			for(int i = 0; i< replicalocations.size();i++) {
				
				ReplicaLoc replicaLoc = replicalocations.get(i);
				int replicaID = replicaLoc.getId();
	//			registry = LocateRegistry.getRegistry(replicaLoc.getAddress(),replicaLoc.getPort());
				registry = LocateRegistry.getRegistry(replicaLoc.getPort());
				ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaID);
				FileContent fileContent = new FileContent(fileName);
				fileContent.setData(data);
				ChunkAck chunkAck;
				chunkAck = replicaStub.write(i+1, 1, fileContent);
				System.out.println("Write action completed successfully");
				replicaStub.commit(i+1, 1);
				
				ReplicaServer.doCrypto(Cipher.ENCRYPT_MODE, fileName, k1);
				
			}
		}
		else {
			System.out.println("Access denied");
		}
	}
	
	public boolean registerNewUser(String username, String password) {
		try {
			boolean flag = masterStub.registerNewUser(username,password);
			return flag;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean loginUser(String username, String password) {
		try {
			if( masterStub.loginUser(username, password)) {
				userloggedIn = username;
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	// call master to create permission for a file
	public void createPermissions(String filename, String owner, String permissions) throws IOException {
		masterStub.setPermission(filename, owner, permissions);
	}
	
	//call master to get permission details for operations
	public String[] fetchPermissions(String filename) throws RemoteException {
		String[] permission = masterStub.fetchPermission(filename);
		return permission;
	}
	
	public boolean operations(boolean cont, Client c, Scanner input,String key) {
		
		try {
			
			do {
				System.out.println("Select the operation to be performed:\n1.Read\n2.Write\n3.Rename\n4.Delete\n5.ListFiles");
				String operation = input.nextLine().toLowerCase();
				switch(operation) {
				case "read":
					System.out.print("Enter file name: ");
					String fname = input.nextLine();
					String encodedFileName = Base64.getEncoder().encodeToString(fname.getBytes());
					c.read(encodedFileName, key);
					break;
				case "write":
					System.out.println("Enter file name: ");
					String fwname = input.nextLine();
					String encodedFileName1 = Base64.getEncoder().encodeToString(fwname.getBytes());
					File tempfile = new File(".//Replica_0/"+encodedFileName1);
					if(!tempfile.exists()) {
						System.out.println("Define permissions for the file:");
						String permissions = input.nextLine();
						c.createPermissions(encodedFileName1, userloggedIn, permissions);

					}
					else {
						String[] permission = c.fetchPermissions(encodedFileName1); 
						String k1 = key;
						if(permission[1].contains("w")) {
							k1 = masterStub.getsaltValue(permission[0]);
						}
						else if(permission[0].equals(userloggedIn)) {
							k1 = masterStub.getsaltValue(userloggedIn);
						}
						else {
							System.out.println("Access denied");
							break;
						}
						ReplicaServer.doCrypto(Cipher.DECRYPT_MODE, encodedFileName1, k1);
					}
					System.out.println("Enter the content to be written: ");
					String content = input.nextLine();	
					byte[] bcontent = content.getBytes(StandardCharsets.UTF_8);
					c.write(encodedFileName1,bcontent,key);
					break;
				case "rename":
					System.out.print("Enter the file name to be changed: ");
					String oldfname = input.nextLine();
					System.out.print("Enter the new file name: ");
					String newfname = input.nextLine();
					c.rename(Base64.getEncoder().encodeToString(oldfname.getBytes()), Base64.getEncoder().encodeToString(newfname.getBytes()));
					break;		
				case "delete":
					System.out.print("Enter the file name: ");
					String fdname = input.nextLine();
					c.delete(Base64.getEncoder().encodeToString(fdname.getBytes()));
					break;
				case "listfiles":
					ReplicaServer.listAllfiles();
					break;
				default:
					System.out.println("Invalid Operation");
					break;
				}
				System.out.println("Do you still want to continue?yes/no: ");
				String con = input.nextLine();
				if(con.contentEquals("no")) {
					cont = false;
					System.out.println("User logged out successfully!!");
					break;
				}
			}while(cont == true);
		}catch(Exception e){
			e.printStackTrace();
		}
		return cont;
	}
	
	
	
	public static void main(String arg[]) throws NoSuchAlgorithmException {
		try {
			String key = "0123456789abcdef0123456789abcdef";
			Client c = new Client();
			Scanner input = new Scanner(System.in);
			boolean cont = true;
			boolean flag = false;
			do {
				System.out.println("SignIn or SignUp?");
				String login = input.nextLine().toLowerCase();
				switch(login) {
				case "signup":
					System.out.println("Enter username:");
					String uname = input.nextLine();
					System.out.println("Enter password:");
					String password = input.nextLine();
					
					if (c.registerNewUser(uname, password)) {
						System.out.println("User registered successfully");
					}
					else {
						System.out.println("Invalid inputs");
						flag = true;
					}
				case "signin":
					System.out.println("Sign in");
					System.out.println("Enter username:");
					String username = input.nextLine();
					System.out.println("Enter password:");
					String pwd = input.nextLine();
					boolean access = c.loginUser(username, pwd);
					if(access) {
						System.out.println("Logged in Successfully");
						flag = c.operations(cont,c,input,key);
					}else {
						System.out.println("Incorrect username or password");
						flag = true;
					}
					break;
				
				default:
					System.out.println("Invalid input");
					flag = true;
					break;
				}
				cont = flag;
				}while(cont);
			input.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}

