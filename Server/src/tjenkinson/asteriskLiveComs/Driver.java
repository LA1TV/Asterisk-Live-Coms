package tjenkinson.asteriskLiveComs;

import tjenkinson.asteriskLiveComs.program.Program;
import tjenkinson.asteriskLiveComs.program.exceptions.NoAsteriskConnectionException;
import tjenkinson.asteriskLiveComs.serverSocket.ServerSocketManager;
import tjenkinson.asteriskLiveComs.serverSocket.exceptions.ServerSocketAlreadyRunningException;

public class Driver {

	// asterisk ip, asterisk port, asterisk user, asterisk password, live coms server port
	public static void main(String[] args) {
		Program program = null;
		boolean connected;
		do {
			connected = true;
			try {
				System.out.println("Starting server.");
				program = new Program(args[0], Integer.parseInt(args[1], 10), args[2], args[3]);
			} catch (NoAsteriskConnectionException e) {
				// try and start again in 10 seconds
				System.out.println("Sever failed to start because could not connect to the asterisk server. Trying agian in 10 seconds.");
				connected = false;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		} while(!connected);
		program.log("Server started.");
		program.log("Starting socket server.");
		try {
			new ServerSocketManager(program, Integer.parseInt(args[4], 10));
			program.log("Socket server started.");
		} catch (ServerSocketAlreadyRunningException e) {
			e.printStackTrace();
		}
	}
}
