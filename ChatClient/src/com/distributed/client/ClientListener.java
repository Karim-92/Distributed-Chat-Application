package com.distributed.client;

import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JTextArea;
import com.distributed.client.ClientGUI;

public class ClientListener implements Runnable {
	private int port;
	private final JTextArea textArea;
	ServerSocket providerSocket;
	Socket socket;
    String Address; 
    String targetIP;
    String targetPort;
    String targetName;
    ClientGUI caller;
    
    //constructor
	ClientListener(int port, JTextArea textArea) {
		this.port = port;
		this.textArea = textArea;
	}
	
	 public ClientGUI getCaller() {
		return caller;
	}

	public void setCaller(ClientGUI caller) {
		this.caller = caller;
	}

	//get target client name
	public String getTargetName() {
		return targetName;
	}

	//set the target client name
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	private void display(final String s) {
	        EventQueue.invokeLater(new Runnable() {
	            //@Override
	            public void run() {
	                textArea.append(s + "\n");
	            }
	        });
	    }
	
    /*
     * This function runs the thread pending, it checks if the socket is on or off then
     * acts upon the message received from the broadcasting server
     */
	@Override
	public void run() {
		while (true) {
			try {
				if (socket != null) {
					socket.close();
				}
				if (providerSocket != null) {
					providerSocket.close();
				}
				
				//start a new socket with specified port and accept connections
				providerSocket = new ServerSocket(port, 10);
				socket = providerSocket.accept();
				Address = socket.getInetAddress().getHostAddress();
				DataInputStream dis2 = new DataInputStream(
						socket.getInputStream());
				String s;
				
				/*If received message starts with connectback: reconnect to client
				 *If received message starts with disconnect then disconnect client
				 *If received message starts with anything else display it 
				 */
				while (true) {
					s = dis2.readUTF();
					if (s.startsWith("connectback")) {
						s = s.substring(11).trim();
						String[] connectionData = s.split(":");
						targetIP = connectionData[0];
						targetPort = connectionData[1];
						targetName = connectionData[2];
						System.out.println("received connectback info " + s);
						caller.connectToClient(targetIP, targetPort);
					} else if (s.startsWith("disconnect")) {
						display("Disconnected from chat, back to server.");
						dis2.close();
						socket.close();
						providerSocket.close();
						caller.setTalkingToServer(true);
						break;
					} else {
						if (targetName != null) {
							display(targetName + ">> " + s);
						} else {
							display("Other>> " + s);
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
