package tjenkinson.asteriskLiveComs;

import tjenkinson.asteriskLiveComs.program.Program;
import tjenkinson.asteriskLiveComs.serverSocket.ServerSocketManager;
import tjenkinson.asteriskLiveComs.serverSocket.exceptions.ServerSocketAlreadyRunningException;

public class Driver {

	// ip, port, user, password
	public static void main(String[] args) {
		Program program = new Program(args[0], Integer.parseInt(args[1], 10), args[2], args[3]);
		
		program.log("Starting socket server.");
		try {
			new ServerSocketManager(program, 2345);
			program.log("Socket server started.");
		} catch (ServerSocketAlreadyRunningException e) {
			e.printStackTrace();
		}

	}

}
