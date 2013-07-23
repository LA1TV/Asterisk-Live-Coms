package tjenkinson.asteriskLiveComs.program;

import java.util.ArrayList;
import java.util.Hashtable;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;


public class MyAsteriskChannel {
	
	public final static int LOCATION_HOLDING = 0;
	public final static int LOCATION_ROOM = 1;
	
	private AsteriskChannel channel;
	private boolean verified = false;
	private int id;
	private boolean playHoldMusic = true;
	private Room room = null;
	
	public MyAsteriskChannel(AsteriskChannel channel, int id) {
		this.channel = channel;
		this.id = id;
	}
	
	public void setPlayHoldMusic(boolean val) {
		playHoldMusic = val;
	}
	
	public AsteriskChannel getChannel() {
		return channel;
	}
	
	public boolean getVerified() {
		return verified;
	}
	
	public void setVerified() {
		verified = true;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean getPlayHoldMusic() {
		return playHoldMusic;
	}
	
	public int getLocation() {
		if (getRoom() == null) {
			return MyAsteriskChannel.LOCATION_HOLDING;
		}
		else {
			return MyAsteriskChannel.LOCATION_ROOM;
		}
	}
	
	public Room getRoom() {
		return room;
	}
	
	public void setRoom(Room room) {
		this.room = room;
	}
	
	public synchronized Hashtable<String,Object> getInfo() {
		
		ArrayList<Integer> channelsInRoom = new ArrayList<Integer>();
		if (getRoom() != null) {
			ArrayList<MyAsteriskChannel> allChannelsInRoom = getRoom().getChannels();
			for(int i=0; i<allChannelsInRoom.size(); i++) {
				if (allChannelsInRoom.get(i).getId() != getId()) {
					channelsInRoom.add(allChannelsInRoom.get(i).getId());
				}
			}
		}
		
		Hashtable<String,Object> info = new Hashtable<String,Object>();
		info.put("id", getId());
		info.put("name", getChannel().getName());
		CallerId callerId = getChannel().getCallerId();
		Hashtable<String,Object> callerIdMap = new Hashtable<String,Object>();
		callerIdMap.put("name", callerId.getName() == null ? false : callerId.getName());
		callerIdMap.put("number", callerId.getNumber() == null ? false : callerId.getNumber());			
		info.put("callerId", callerIdMap);
		info.put("account", getChannel().getAccount() == null ? false : getChannel().getAccount());
		info.put("timeOfCreation", getChannel().getDateOfCreation().getTime());
		info.put("dialedChannel", getChannel().getDialedChannel() == null ? false : getChannel().getDialedChannel());
		info.put("verified", getVerified());
		info.put("location", getLocation());
		info.put("inRoomWith", channelsInRoom);
		return info;
	}
}
