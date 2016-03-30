package com.distributed.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
		try {
			
			//Create a new Server Socket
			ServerSocket sv = new ServerSocket(1234);
			
			//creating thread for kicking
			kick k = new kick();
			k.start();
			
			while (true) {
				//2.Listen for Clients
				Socket c;
				c = sv.accept();
				System.out.println("A new client has arrived");
				
				//creating a thread for each client
				ClientHandler ch = new ClientHandler(c);
				ch.start();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}

class Client{

	//client user variables
	private String name;
	private String ipAddress;
	private int port;
	private String connectionStatus;

	//data exchange variables
	DataOutputStream dos;
	DataInputStream dis;
	Socket socket;


	//client constructor
	Client(){
		this.name="noName";
		this.ipAddress="noAddress";
		this.port=0;
		this.connectionStatus="Offline";
	}

	//Set methods for variables
	public void setName(String name) {
		this.name = name;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setConnectionStatus(Boolean connected) {

		if(connected==true)
			this.connectionStatus = "Online";
		else
			this.connectionStatus="Offline";
	}


	public void setDos(DataOutputStream dos) {
		this.dos = dos;
	}

	public void setDis(DataInputStream dis) {
		this.dis = dis;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}


	//Get methods for variables
	public String getConnectionStatus() {
		return connectionStatus;
	}

	public void setConnectionStatus(String connectionStatus) {
		this.connectionStatus = connectionStatus;
	}

	public String getName() {
		return name;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

	public DataOutputStream getDos() {
		return dos;
	}

	public DataInputStream getDis() {
		return dis;
	}

	public Socket getSocket() {
		return socket;
	}
}

//Client handling class
class ClientHandler extends Thread{

	private static Socket client;
	public static int clientCounter=0;
	public static int groupCounter=0;

	public static ArrayList<Client> clients=new ArrayList<>();
	public static ArrayList<Client> groups=new ArrayList<>();

	//class constructor
	ClientHandler(Socket client){
		ClientHandler.client=client;
	}

	//run the thread task
	public void run(){
		try{
			String availableList="";
			int availableFlag=1;
			int availableGroupFlag=1;

			Client currentClient = new Client();
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			DataInputStream dis = new DataInputStream(client.getInputStream());

			//Set the client's IP address as the client of the current socket
			currentClient.setIpAddress(client.getInetAddress().getHostAddress());

			//Set username
			dos.writeUTF("Please enter your username: ");
			currentClient.setName(dis.readUTF());

			//Set port number
			dos.writeUTF("Please enter your port number: ");
			currentClient.setPort(Integer.parseInt(dis.readUTF()));

			currentClient.setConnectionStatus(true);
			currentClient.setDos(dos);
			currentClient.setDis(dis);
			currentClient.setSocket(client);

			//Check if client already exists in the system

			//if exists, mark as online
			for(int i=0; i<clientCounter; i++){
				if(currentClient.getIpAddress().equals(clients.get(i).getIpAddress())
						&& (currentClient.getPort() == clients.get(i).getPort())){
					System.out.println("This address is already connected.");
					availableFlag=0;
					if(clients.get(i).getConnectionStatus()=="Offline")
					{
						clients.get(i).setConnectionStatus(true);
						for(int j=0; j<clientCounter; j++){
							clients.get(j).dos.writeUTF(currentClient.getName()+":"+currentClient.getIpAddress()+":"+currentClient.getPort()+" is now Online.");
						}
						clientCounter++;
						availableFlag=0;
					}
				}
			}

			//if doesn't exist add the client
			if (availableFlag == 1)
			{
				clients.add(currentClient);
				for (int i = 0 ; i<clientCounter ; i++)
				{
					clients.get(i).dos.writeUTF(currentClient.getName() + ":" + 
							currentClient.getIpAddress() + ":" + currentClient.getPort()  +  " is now Online");
				}
				clientCounter++;
				availableFlag = 0;

			}

			//Sending list of available clients to newly added client
			availableList="";
			for (int i = 0 ; i<clientCounter ; i++)
			{
				availableList = availableList + clients.get(i).getName() + ":" + 
						clients.get(i).getIpAddress() + ":" + clients.get(i).getPort() + ":" + 
						clients.get(i).getConnectionStatus() + "\n";
			}
			dos.writeUTF("All Clients: \n" + availableList);

			//Sending list of group to the new client
			if (groupCounter == 0)
				dos.writeUTF("Group Clients:\n" + "no clients in group 1");
			else
			{
				availableList = "";
				for (int i = 0 ; i<groupCounter ; i++)
				{
					availableList = availableList + groups.get(i).getName() + ":" + 
							groups.get(i).getIpAddress() + ":" + groups.get(i).getPort() + ":" + 
							groups.get(i).getConnectionStatus() + "\n";
				}
				dos.writeUTF("Group 1 Clients:\n" + availableList);
			}

			while(true){

				//Take input from the client
				String message=dis.readUTF();

				//Leave chat on saying Bye
				if(message.equals("Bye")){
					currentClient.setConnectionStatus(false);
					for(int i=0; i<clientCounter;i++){
						clients.get(i).dos.writeUTF(currentClient.getName()+":"+currentClient.getIpAddress()+":"
								+currentClient.getPort()+" is now Offline.");
					}
					dis.close();
					dos.close();
					client.close();
				}

				//List clients if message is "ls client"
				else if(message.equals("ls clients")){
					availableList="";
					for (int i = 0 ; i<clientCounter ; i++)
					{
						availableList = availableList + clients.get(i).getName() + ":" + 
								clients.get(i).getIpAddress() + ":" + clients.get(i).getPort() + ":" + 
								clients.get(i).getConnectionStatus() + "\n";
					}
					dos.writeUTF("All Clients: \n" + availableList);
				}

				//List clients in the group
				else if (message .equals("ls group"))
				{
					if (groupCounter == 0)
						dos.writeUTF("Group Clients:\n" + "no clients in group.");
					else
					{
						availableList = "";
						for (int i = 0 ; i<groupCounter ; i++)
						{
							availableList = availableList + groups.get(i).getName() + ":" + 
									groups.get(i).getIpAddress() + ":" + groups.get(i).getPort() + ":" + 
									groups.get(i).getConnectionStatus() + "\n";
						}
						dos.writeUTF("Group Clients:\n" + availableList);

					}
				}

				//Join group on typing "join group"
				else if (message.equals("join group"))
				{
					if (availableGroupFlag == 1)
					{
						groups.add(currentClient);
						groupCounter +=1;
						availableGroupFlag = 0;
						for (int i = 0 ; i<clientCounter ; i++)
						{
							clients.get(i).dos.writeUTF(currentClient.getName() + ":" + 
									currentClient.getIpAddress() + ":" + currentClient.getPort() +  " joined the group!");
						}
					}
				}

				//Leave the group on typing "leave group"
				else if (message.equals("leave group"))
				{
					if (availableGroupFlag == 0)
					{
						for (int i = 0; i< groupCounter; i++)
						{
							if (currentClient.getName().equals (groups.get(i).getName()))
							{
								groups.remove(i);
								groupCounter -=1;
								availableGroupFlag = 1;
								for (int j = 0 ; j<clientCounter ; j++)
								{
									clients.get(j).dos.writeUTF(currentClient.getName() + ":" + 
											currentClient.getIpAddress() + ":" + currentClient.getPort()  +  " left the group!");
								}
								break;
							}
						}
					}
					else
						dos.writeUTF("You are not in the group!");
				}

				//If another message, broadcast it to all of the group
				else if (availableGroupFlag == 0)
				{
					for (int i = 0 ; i<groupCounter ; i++)
					{
						if (groups.get(i).getConnectionStatus() == "Online")
							groups.get(i).dos.writeUTF(message);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}


//User kicking handling class
class kick extends Thread {

	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		String serverInput;
		while (true){
			serverInput = sc.nextLine();
			if (serverInput.startsWith("kick"))
			{
				serverInput = serverInput.substring(4).trim();
				
				//Remove user from list if found
				for (int i = 0; i< ClientHandler.clientCounter; i++)
				{
					if (serverInput.equals(ClientHandler.clients.get(i).getName()))
					{
						try {
							ClientHandler.clients.get(i).dos.writeUTF("You have been kicked from the system by the Admin!");
							ClientHandler.clients.get(i).dos.close();
							ClientHandler.clients.get(i).dis.close();
							ClientHandler.clients.get(i).socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						ClientHandler.clients.remove(i);
						ClientHandler.clientCounter -= 1;
						
						//Broadcast to all clients that the client has been kicked
						for (int j = 0 ; j<ClientHandler.clientCounter ; j++)
						{
							try {
								ClientHandler.clients.get(j).dos.writeUTF(serverInput + " has been kicked out by the admin!");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						System.out.println("User removed");
						break;
					}
					
				}

				//Remove user from the group
				for (int i = 0; i< ClientHandler.groupCounter; i++)
				{
					if (serverInput.equals(ClientHandler.groups.get(i).getName()))
					{
						ClientHandler.groups.remove(i);
						ClientHandler.groupCounter -= - 1;
						System.out.println("User removed from the group.");
						break;
					}	
				}
			}
		}
	}
}