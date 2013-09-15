package tjenkinson.asteriskLiveComs.serverSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import tjenkinson.asteriskLiveComs.program.Program;

class ServerSocketImpl implements Runnable {
	
	private Program mainProgramObj;
	private String host;
	private int port;
	private ServerSocket serverSocket;
	
	ServerSocketImpl(Program mainProgramObj, String host, int port) {
		this.mainProgramObj = mainProgramObj;
		this.host = host;
		this.port = port;
	}
	
	public Program getMainProgramObj() {
		return mainProgramObj;
	}
	
	@Override
	public void run() {
		try {
		    serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// keep accepting new connections
		while(true) {
			try {
				// gets socket to client when client connects and hands this over to a new thread. then waits for any more connections
				(new Thread(new ClientSocketConnectionManager(serverSocket.accept(), this))).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}