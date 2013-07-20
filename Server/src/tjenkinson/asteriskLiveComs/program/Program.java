package tjenkinson.asteriskLiveComs.program;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskQueueEntry;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.AsteriskServerListener;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.Extension;
import org.asteriskjava.live.ManagerCommunicationException;
import org.asteriskjava.live.MeetMeUser;
import org.asteriskjava.live.internal.AsteriskAgentImpl;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.util.DaemonThreadFactory;

import tjenkinson.asteriskLiveComs.program.events.ChannelAddedEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelRemovedEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelToHoldingEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelVerifiedEvent;
import tjenkinson.asteriskLiveComs.program.events.ChannelsToRoomEvent;
import tjenkinson.asteriskLiveComs.program.events.EventListener;
import tjenkinson.asteriskLiveComs.program.events.LiveComsEvent;
import tjenkinson.asteriskLiveComs.program.events.ServerResettingEvent;
import tjenkinson.asteriskLiveComs.program.exceptions.ChannelNotVerifiedException;
import tjenkinson.asteriskLiveComs.program.exceptions.InvalidChannelException;
import tjenkinson.asteriskLiveComs.program.exceptions.MissingKeyException;
import tjenkinson.asteriskLiveComs.program.exceptions.OnlyOneChannel;

public class Program {

	private String asteriskServerIP;
	private int asteriskServerPort;
	private String asteriskServerUser;
	private String asteriskServerSecret;
	
	private ManagerConnection managerConnection;
	private AsteriskServer asteriskServer;
	private Hashtable<Integer,MyAsteriskChannel> channels = null;
	private int lastChannelId = 0;
	private Hashtable<Integer, Room> rooms;
	private ArrayList<EventListener> listeners = new ArrayList<EventListener>();
	private final ExecutorService eventsDispatcherExecutor;
	private Object handleHangupLock = new Object();
	private Object handleConnectionLock = new Object();
	private Object asteriskConnectionLock = new Object();
	private HandleAsteriskServerEvents asteriskServerEventsHandler;
	private HandleManagerEvents managerEventsHandler;
	private boolean hasLoaded = false;
	
	public Program(String ip, int port, String user, String password)
	{
		this.asteriskServerIP = ip;
		this.asteriskServerPort = port;
		this.asteriskServerUser = user;
		this.asteriskServerSecret = password;
		
		asteriskServerEventsHandler = new HandleAsteriskServerEvents();
		managerEventsHandler = new HandleManagerEvents();
		
		eventsDispatcherExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
		ManagerConnectionFactory factory = new ManagerConnectionFactory(asteriskServerIP, asteriskServerPort, asteriskServerUser, asteriskServerSecret);
		log("Starting asterisk manager connection.");
		managerConnection = factory.createManagerConnection();
		log("Started asterisk manager connection.");
		asteriskServer = new DefaultAsteriskServer(managerConnection);
		reset();
		hasLoaded = true;
		log("All systems running!");
	}
	
	public void log(String msg) {
		System.out.println(msg);
	}
	
	public void reset()
	{
		log("Initialising.");
		if (hasLoaded) {
			dispatchEvent(new ServerResettingEvent());
		}
		synchronized(asteriskConnectionLock) {
			asteriskServer.removeAsteriskServerListener(asteriskServerEventsHandler);
			managerConnection.removeEventListener(managerEventsHandler);
		}
		if (channels == null) {
			channels = new Hashtable<Integer,MyAsteriskChannel>();
		}
		else {
			synchronized (channels) {
				channels = new Hashtable<Integer,MyAsteriskChannel>();
			}
		}
		if (rooms == null) {
			rooms = new Hashtable<Integer, Room>();
		}
		else {
			synchronized (rooms) {
				rooms = new Hashtable<Integer, Room>();
			}
		}
		
		try {
			for (AsteriskChannel asteriskChannel : asteriskServer.getChannels())
	        {
				log("Found channel: \""+asteriskChannel+"\".");
				registerChannel(asteriskChannel);
	        }
		}
		catch (ManagerCommunicationException e) {
			log("Can't find any channels because not connected to asterisk server.");
		}
		synchronized(asteriskConnectionLock) {
			try {
				asteriskServer.addAsteriskServerListener(asteriskServerEventsHandler);
			}
			catch (ManagerCommunicationException e) {
				log("Can't add listener because not connected to asterisk server.");
			}
			managerConnection.addEventListener(managerEventsHandler);
		}
	}
	
	private MyAsteriskChannel registerChannel(AsteriskChannel asteriskChannel) {
		MyAsteriskChannel chan = null;
		
		if (asteriskChannel.getName().matches("^.*?/pseudo.*$")) {
			log("Not registering channel \""+asteriskChannel+"\" because it has a \"pseudo\" after a /.");
			return null;
		}
		
		synchronized(channels) {
			int id = ++lastChannelId;
			log("Registering channel \""+asteriskChannel+"\" with id "+id+".");
			chan = new MyAsteriskChannel(asteriskChannel, id);
			channels.put(id, chan);
			sendToDPFn(chan, "WaitVerification", 1);
			dispatchEvent(new ChannelAddedEvent(chan));
			return chan;
		}
	}
	
	private void sendToDPFn(MyAsteriskChannel chan, String fn, int priority) {
		synchronized(channels) {
			log("Sending channel with id "+chan.getId()+" to DPFn \""+fn+"\" at priority "+priority+".");
			chan.getChannel().redirect("Fn"+fn, "start", priority);
		}
		checkRoomCounts();
	}
	
	public ArrayList<Hashtable<String,Object>> getChannels() {
		log("Getting channels.");
		ArrayList<Hashtable<String,Object>> data = new ArrayList<Hashtable<String,Object>>();
		synchronized(channels) {
			Set<Integer> keys = channels.keySet();
			for(Integer id : keys) {
				MyAsteriskChannel channel = channels.get(id);
				data.add(channel.getInfo());
			}
		}
		return data;
	}
	
	public boolean grantAccess(int id, boolean enableHoldingMusic) throws InvalidChannelException {
		synchronized(channels) {
			MyAsteriskChannel channel = getChannelFromId(id);
			if (channel.getVerified()) {
				return false;
			}
			else {
				log("Verifying channel with id "+id+".");
				channel.setVerified();
				channel.setPlayHoldMusic(enableHoldingMusic);
				channel.getChannel().setVariable("EnableHoldingMusic", enableHoldingMusic ? "1":"0");
				sendToDPFn(channel, "GrantAccess", 1);
				dispatchEvent(new ChannelVerifiedEvent(channel.getId()));
				return true;
			}
		}
	}
	
	public void denyAccess(int id) throws InvalidChannelException {
		synchronized(channels) {
			MyAsteriskChannel channel = getChannelFromId(id);
			log("Denying access for channel with id "+id+".");
			sendToDPFn(channel, "DenyAccess", 1);
		}
	}
	
	private MyAsteriskChannel getChannelFromId(int id) throws InvalidChannelException {
		synchronized(channels) {
			MyAsteriskChannel channel = channels.get(id);
			if (channel == null) {
				throw(new InvalidChannelException());
			}
			return channel;
		}
	}
	
	public void createRoom(ArrayList<Hashtable<String, Object>> data) throws InvalidChannelException, ChannelNotVerifiedException, MissingKeyException, OnlyOneChannel {
		log("Connecting channels.");
		
		ArrayList<MyAsteriskChannel> channels = new ArrayList<MyAsteriskChannel>();
		
		if (data.size() == 1) {
			throw(new OnlyOneChannel());
		}
		
		int roomNo = -1;
		ArrayList<Integer> ids = new ArrayList<Integer>();
		synchronized(channels) {
			for(int i=0; i<data.size(); i++) {
				if (data.get(i).get("id") == null || data.get(i).get("listenOnly") == null) {
					throw(new MissingKeyException());
				}
				
				if (!getChannelFromId((int)data.get(i).get("id")).getVerified()) {
					throw (new ChannelNotVerifiedException());
				}
				channels.add(getChannelFromId((int)data.get(i).get("id")));
			}
			
			// remove channels from any previous rooms
			for(int i=0; i<channels.size(); i++) {
				removeChannelFromRoom(channels.get(i), true); // true means do not check room counts. will do this later so that channels can seamlessly move rooms if they're already in one
			}
			
			roomNo = 1; // rooms start at 1
			synchronized(rooms) {
				while(rooms.containsKey(roomNo)) {
					roomNo++;
				}
				rooms.put(roomNo, new Room(roomNo, channels));
			}
			// this initialises the room in the library (even though don't use it for rooms)
			asteriskServer.getMeetMeRoom(String.valueOf(roomNo));
			
			// set channel var for each channel so they know what room to join and then enter room
			for(int i=0; i<channels.size(); i++) {
				ids.add(channels.get(i).getId());
				AsteriskChannel channel = channels.get(i).getChannel();
				channel.setVariable("RoomToJoin", String.valueOf(roomNo));
				channel.setVariable("RoomListenInParam", ((boolean)data.get(i).get("listenOnly"))?"m":"");
				Extension extension = channel.getCurrentExtension();
				if (extension != null && extension.getContext().equals("FnToMeeting")) {
					// do nothing because hearing into message and will then enter correct room as just set variable
				}
				else {
					String fnName = "StartMeeting";
		            if (extension != null && !extension.getContext().equals("FnStartMeeting")) {
		            	fnName = "ToMeeting";
		            }
		            sendToDPFn(channels.get(i), fnName, 1);
				}
			}
		}
		checkRoomCounts();
		log("Channels sent to room "+roomNo+".");
		dispatchEvent(new ChannelsToRoomEvent(ids));
	}
	
	public void sendChannelsToHolding(ArrayList<Integer> chanIds) throws InvalidChannelException, ChannelNotVerifiedException {
		log("Sending channels to holding.");
		ArrayList<MyAsteriskChannel> sentChannels = new ArrayList<MyAsteriskChannel>();
		synchronized(channels) {
			for (int i=0; i<chanIds.size(); i++) {
				if (!getChannelFromId(chanIds.get(i)).getVerified()) {
					throw (new ChannelNotVerifiedException());
				}
				sentChannels.add(getChannelFromId(chanIds.get(i)));
			}
			for (int i=0; i<sentChannels.size(); i++) {
				removeChannelFromRoom(sentChannels.get(i));
				sendToHolding(sentChannels.get(i));
			}
		}
	}
	
	// checks room counts and if any are 1 person then put them in holding. also remove any fully empty rooms
	private void checkRoomCounts() {
		log("Checking rooms contain more than 1 person.");
		synchronized(rooms) {
			ArrayList<Room> roomsToEmpty = new ArrayList<Room>();
			Set<Integer> keys = rooms.keySet();
			for(Integer roomNo : keys) {
				Room room = rooms.get(roomNo);
				if (room.getCount() <= 1) {
					roomsToEmpty.add(room);
				}
			}
			
			for (int i=0; i<roomsToEmpty.size(); i++) {
				log("Emptying room no "+roomsToEmpty.get(i).getNo()+" because it only contains one member.");
				emptyRoom(roomsToEmpty.get(i));
			}
		}
	}
	
	private void emptyRoom(Room room) {
		log("Emptying room no "+room.getNo()+".");
		synchronized(rooms) {
			synchronized(channels) {
				ArrayList<MyAsteriskChannel> roomChannels = room.getChannels();
				for(int i=0; i<roomChannels.size(); i++) {
					MyAsteriskChannel channel = roomChannels.get(i);
					room.removeChannel(channel);
					sendToHolding(channel);
				}
			}
			rooms.remove(room.getNo());
		}
	}
	
	private void sendToHolding(MyAsteriskChannel chan) {
		Extension extension = chan.getChannel().getCurrentExtension();
		if (extension == null || !extension.getContext().equals("FnHolding")) {
			sendToDPFn(chan, "ToHolding", 1);
			dispatchEvent(new ChannelToHoldingEvent(chan.getId()));
		}
	}
	
	// removes a channel from any rooms they are in.
	private void removeChannelFromRoom(MyAsteriskChannel channel)
	{
		removeChannelFromRoom(channel, false);
	}
	private void removeChannelFromRoom(MyAsteriskChannel channel, boolean doNotCheckCounts)
	{
		synchronized(rooms) {
			Set<Integer> keys = rooms.keySet();
			Room room = null;
			for(Integer roomNo : keys) {
				room = rooms.get(roomNo);
				synchronized(channels) {
					if (room.containsChannel(channel)) {
						break;
					}
				}
			}
			if (room != null) {
				log("Removing channel with id "+channel.getId()+" from room.");
				room.removeChannel(channel);
				if (!doNotCheckCounts) {
					checkRoomCounts();
				}
			}
		}
	}
	
	public void addEventListener(EventListener a) {
		listeners.add(a);
	}
	
	public void removeEventListener(EventListener a) {
		listeners.remove(a);
	}
	
	private void dispatchEvent(final LiveComsEvent e) {
		synchronized (eventsDispatcherExecutor) {
			eventsDispatcherExecutor.execute(new Runnable()
	        {
	            public void run()
	            {
	            	for(int i=0; i<listeners.size(); i++) {
	            		listeners.get(i).onEvent(e);
	            	}
	            }
	        });
		}
	}
	
	private class HandleHangup implements Runnable {

		private HangupEvent e;
		public HandleHangup(HangupEvent e) {
			this.e = e;
		}
		@Override
		public void run() {
			synchronized (handleHangupLock) {
				
				String chanName = e.getChannel();
				MyAsteriskChannel channel = null;
				synchronized(channels) {
					Set<Integer> keys = channels.keySet();
					for(Integer id : keys) {
						if (channels.get(id).getChannel().getName().equals(chanName)) {
							channel = channels.get(id);
							break;
						}
					}
					if (channel != null) {
						removeChannelFromRoom(channel);
						int channelId = channel.getId();
						channels.remove(channelId);
						dispatchEvent(new ChannelRemovedEvent(channelId));
						log("Removed channel with id "+channel.getId()+".");
					}
				}
				
			}
		}
		
	}
	
	private class HandleConnectionEvent implements Runnable {

		@Override
		public void run() {
			synchronized (handleConnectionLock) {
				log("Connection to server has been lost or reconnected so broadcasting channel removed events and resetting.");
				synchronized(channels) {
					Set<Integer> keys = channels.keySet();
					for(Integer id : keys) {
						dispatchEvent(new ChannelRemovedEvent(channels.get(id).getId()));
					}
				}
				reset();
			}
		}
		
	}
	
	private class HandleManagerEvents implements ManagerEventListener {
		@Override
		public void onManagerEvent(ManagerEvent e) {
			String eName = e.getClass().getSimpleName().toString();
			if (eName.equals("HangupEvent")) {
				new Thread(new HandleHangup((HangupEvent)e)).start();
			}
			else if (eName.equals("ConnectEvent") || eName.equals("DisconnectEvent")) {
				new Thread(new HandleConnectionEvent()).start();
			}
		}
	}
	
	private class HandleAsteriskServerEvents implements AsteriskServerListener {
		@Override
		public void onNewAsteriskChannel(AsteriskChannel channel) {
			registerChannel(channel);
		}

		@Override
		public void onNewMeetMeUser(MeetMeUser user) {}

		@Override
		public void onNewAgent(AsteriskAgentImpl agent) {}

		@Override
		public void onNewQueueEntry(AsteriskQueueEntry entry) {}
	}

}
