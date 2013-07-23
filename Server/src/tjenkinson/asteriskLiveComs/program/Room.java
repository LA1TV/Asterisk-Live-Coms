package tjenkinson.asteriskLiveComs.program;

import java.util.ArrayList;


public class Room {
	// channels in room
	private ArrayList<MyAsteriskChannel> channels;
	// room no
	private int no;
	
	public Room(int no, ArrayList<MyAsteriskChannel> channels) {
		this.no = no;
		this.channels = channels;
		for(int i=0; i<channels.size(); i++) {
			channels.get(i).setRoom(this);
		}
	}
	
	public int getNo() {
		return no;
	}
	
	public boolean containsChannel(MyAsteriskChannel channel) {
		return channels.contains(channel);
	}
	
	// this will only be used if a channel hangs up
	public void removeChannel(MyAsteriskChannel channel) {
		channel.setRoom(null);
		channels.remove(channel);
	}
	
	public int getCount() {
		return channels.size();
	}
	
	public boolean isEmpty() {
		return channels.isEmpty();
	}
	
	public ArrayList<MyAsteriskChannel> getChannels() {
		return channels;
	}
	
}
