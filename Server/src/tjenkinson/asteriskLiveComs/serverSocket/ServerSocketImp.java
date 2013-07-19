package tjenkinson.asteriskLiveComs.serverSocket;

import java.io.IOException;
import java.net.ServerSocket;

import tjenkinson.asteriskLiveComs.program.Program;

class ServerSocketImpl implements Runnable {
	
	private Program mainProgramObj;
	private int port;
	private ServerSocket serverSocket;
	
	ServerSocketImpl(Program mainProgramObj, int port) {
		this.mainProgramObj = mainProgramObj;
		this.port = port;
	}
	
	public Program getMainProgramObj() {
		return mainProgramObj;
	}
	
	@Override
	public void run() {
		try {
		    serverSocket = new ServerSocket(port);
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