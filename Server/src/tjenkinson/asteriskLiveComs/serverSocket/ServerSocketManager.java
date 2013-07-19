package tjenkinson.asteriskLiveComs.serverSocket;

import tjenkinson.asteriskLiveComs.program.Program;
import tjenkinson.asteriskLiveComs.serverSocket.exceptions.ServerSocketAlreadyRunningException;

public class ServerSocketManager {

	private Thread serverSocketThread = null;

	private static boolean running = false;
	
	
	public static boolean isRunning() {
		return running;
	}
	
	public ServerSocketManager(Program mainProgramObj, int port) throws ServerSocketAlreadyRunningException {
		if (isRunning()) {
			throw(new ServerSocketAlreadyRunningException());
		}
		running = true;
		// start thread
		serverSocketThread = new Thread(new ServerSocketImpl(mainProgramObj, port));
		serverSocketThread.start();
	}
}
