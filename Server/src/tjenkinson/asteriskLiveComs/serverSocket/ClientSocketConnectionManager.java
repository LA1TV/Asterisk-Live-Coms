package tjenkinson.asteriskLiveComs.serverSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;

import tjenkinson.asteriskLiveComs.program.events.ChannelAddedEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelRemovedEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelToHoldingEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelVerifiedEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelsToRoomEvent;
import tjenkinson.asteriskLiveComs.program.events.EventListener;
import tjenkinson.asteriskLiveComs.program.events.LiveComsEvent;

class ClientSocketConnectionManager implements Runnable, EventListener {
	
	private Socket client = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private ServerSocketImpl serverSocketManager;
	
	public ClientSocketConnectionManager(Socket client, ServerSocketImpl serverSocketImpl) {
		this.client = client;
		this.serverSocketManager = serverSocketImpl;
		try {
			out = new PrintWriter(this.client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ServerSocketImpl getServerSocketManager() {
		return serverSocketManager;
	}

	public void sendOutput(SocketOutput a) {
		String JSONString = a.getJSONString();
		serverSocketManager.getMainProgramObj().log("Outputting to socket: "+JSONString);
		out.println(JSONString);
	}
	
	public void closeSocket() {
		
		try {
			client.shutdownInput();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			client.shutdownOutput();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverSocketManager.getMainProgramObj().removeEventListener(this);
		serverSocketManager.getMainProgramObj().log("Socket connection lost.");
	}

	@Override
	public void run() {
		serverSocketManager.getMainProgramObj().log("Socket connection made.");
		serverSocketManager.getMainProgramObj().addEventListener(this);
		(new Thread(new IncomingCommandHandler(in, this))).start();
	}

	@Override
	public synchronized void onEvent(LiveComsEvent e) {
		if (e.getClass().getSimpleName().equals("ChannelAddedEvent")) {
			sendOutput(new EventObj("channelAdded", ((ChannelAddedEvent) e).getInfo()));
		}
		else if (e.getClass().getSimpleName().equals("ChannelRemovedEvent")) {
			Hashtable<String,Object> payload = new Hashtable<String,Object>();
			payload.put("channelId", ((ChannelRemovedEvent) e).getChannelId());
			sendOutput(new EventObj("channelRemoved", payload));
		}
		else if (e.getClass().getSimpleName().equals("ChannelsToRoomEvent")) {
			Hashtable<String,Object> payload = new Hashtable<String,Object>();
			payload.put("channelIds", ((ChannelsToRoomEvent) e).getChannelIds());
			sendOutput(new EventObj("channelsToRoom", payload));
		}
		else if (e.getClass().getSimpleName().equals("ChannelToHoldingEvent")) {
			Hashtable<String,Object> payload = new Hashtable<String,Object>();
			payload.put("channelId", ((ChannelToHoldingEvent) e).getChannelId());
			sendOutput(new EventObj("channelToHoldingEvent", payload));
		}
		else if (e.getClass().getSimpleName().equals("ChannelVerifiedEvent")) {
			Hashtable<String,Object> payload = new Hashtable<String,Object>();
			payload.put("channelId", ((ChannelVerifiedEvent) e).getChannelId());
			sendOutput(new EventObj("channelVerified", payload));
		}
		else if (e.getClass().getSimpleName().equals("ServerResettingEvent")) {
			sendOutput(new EventObj("resetting", null));
		}
	}
}