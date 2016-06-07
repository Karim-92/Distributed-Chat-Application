package com.distributed.client;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import com.distributed.client.ClientListener;


@SuppressWarnings("unused")
public class ClientGUI implements ActionListener, Runnable{
	
	//Variables declaration, variables are self explanatory
	private static String HOST="127.0.0.1";
	private static int port=1234;
    private Thread thread;
    private DataInputStream dis;
    private DataOutputStream dos;
    private DataOutputStream dosClient;
	private Status status;
    private boolean isTalkingToServer = true;
    private boolean inGroup = false;
    private int clientListeningPort = 0;
    private ClientListener listener;
    private Socket socket;
    private Socket clientSocket;
    private String name;
    
    //GUI shapes and variables declaration
    private final JFrame frame = new JFrame();
    private final JTextField textField = new JTextField(40);
    private final JTextArea textArea = new JTextArea(25, 40);
    private final JButton send = new JButton("Send");
    
    /*
     * 
     * Functions start
     * 
     */
    
    //client constructor
    public ClientGUI(Status status) {
        this.status = status;
        frame.setTitle("Chat " + status);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getRootPane().setDefaultButton(send);
        frame.add(textField, BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.add(send, BorderLayout.SOUTH);
        frame.setLocation(status.offset, 300);
        frame.pack();
        send.addActionListener(this);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        display(status.activity + HOST + " on port " + port);
        thread = new Thread(this, status.toString());
    }
    
    //start thread
    public void start() {
        frame.setVisible(true);
        thread.start();
    }
    
    //set host
    public static void setHOST(String host) {
		HOST = host;
	}   
	
    //Enumeration of status of client
    public static enum Status {
        Client(100, "Trying"), Server(500, "Awaiting");
        private int offset;
        private String activity;

        private Status(int offset, String activity) {
            this.offset = offset;
            this.activity = activity;
        }
    }

    //Connect to a client via an IP and port and stop talking to server
    public void connectToClient(String targetIP, String targetPort) throws NumberFormatException, UnknownHostException, IOException {
    	clientSocket = new Socket(targetIP, Integer.parseInt(targetPort));
		isTalkingToServer = false;
		dosClient = new DataOutputStream(clientSocket.getOutputStream());
		display("Connected to " + targetIP + " on port " + targetPort );
		display("Type disconnect to leave this chat.");
    }

	//Set client status
	public void setTalkingToServer(boolean isTalkingToServer) {
		this.isTalkingToServer = isTalkingToServer;
	}
	
	//Function to verify the number contains only integers
	public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
	
	//Function to display messages and other instructions in the text area of the client
    private void display(final String s) {
        EventQueue.invokeLater(new Runnable() {
            //@Override
            public void run() {
                textArea.append(s + "\n");
            }
        });
    }


	@Override
	public void run() {
		// TODO Auto-generated method stub
        try {
            socket = new Socket(HOST, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            display("Connected");   
            String s = "";
            while (true) {
            	s = dis.readUTF();
            	if (isTalkingToServer) {
            		if (inGroup) {
            			display(s);
            		} else {
            			display("Server> " + s);
            		}
            	} else {
            		display("Other> " + s);
            	}
            }
        } catch (Exception e) {
            display(e.getMessage());
            e.printStackTrace(System.err);
        }
	}


	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		// TODO Auto-generated method stub
    	String s = textField.getText();
    	if (name == null) {
    		name = s;
    	}
    	if (isTalkingToServer) {
    		if (s.startsWith("connect")) {
    			s = s.substring(7).trim();
    			String[] connectionData = s.split(":");
    			String targetName = connectionData[0];
    			String targetIP = connectionData[1];
    			String targetPort = connectionData[2];
    			display("Connecting to " + targetIP + " on port " + targetPort);
    			try {
    				connectToClient(targetIP, targetPort);
    				//send connection details to other client
    				String data = "connectback " + clientSocket.getLocalAddress().getHostAddress()
    						+ ":" + clientListeningPort + ":" + name;
    				dosClient.writeUTF(data);
    				textField.setText("");
    				listener.setTargetName(targetName);
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		} else if (s.startsWith("leave")) {
    			 try {
    		            dos.writeUTF("Bye");
    		            dis.close();
    		            dos.close();
    		            socket.close();
    		            textField.setText("");
    		        } catch (Exception e) {
    		            System.out.println(e.getMessage());
    		        }
    		} 
    		else if (s.startsWith("join group")) {
    			inGroup = true;
    			try {
    				dos.writeUTF(s);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			textField.setText("");
    		}
    		else if (s.startsWith("leave group")) {
    			inGroup = false;
    			try {
    				dos.writeUTF(s);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			textField.setText("");
    		}
    		else if (inGroup) {
    			try {
    				if (s .equals ("ls clients") || s .equals ("ls group"))
    					dos.writeUTF(s);
    				else
    					dos.writeUTF(name + "> " +s);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			textField.setText("");
    		}
    		else {
    			try {
    				dos.writeUTF(s);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			display("ME> " + s);
    			textField.setText("");
    			if (isInteger(s)) {
    				clientListeningPort = Integer.parseInt(s);
    				
    				listener = new ClientListener(clientListeningPort, textArea);
    				listener.setCaller(this);
    				ExecutorService executor = Executors.newCachedThreadPool();
    			    executor.submit(listener);
    			    
    				display("\nOptions: \nType connect NAME:IP_ADDRESS:PORT to connect to a client\nType join group to join a group\nType leave group to leave a group\nType leave to logout\nType ls clients to get a list of all clients\nType ls group to get a list of a group clients\n");

    			}
    		}
    	}
    	else {
    		if (s.startsWith("disconnect")) {
    			try {
    				dosClient.writeUTF("disconnect");
    				dosClient.close();
					clientSocket.close();
					listener.socket.close();
					listener.providerSocket.close();
					display("Disconnected from chat, back to server.");
					isTalkingToServer = true;
					textField.setText("");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		else {
    		try {
				dosClient.writeUTF(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			display("ME> " + s);
			textField.setText("");
    		}
    	}
	}
	

    public static void main(String[] args) {
    	String IP = "";
    	if (args.length > 0) {
    		IP = args[0];
    		ClientGUI.setHOST(IP);
    	}
        EventQueue.invokeLater(new Runnable() {
            //@Override
            public void run() {
                ClientGUI x =  new ClientGUI(Status.Client);
                x.start();
            }
        });
    }
}
