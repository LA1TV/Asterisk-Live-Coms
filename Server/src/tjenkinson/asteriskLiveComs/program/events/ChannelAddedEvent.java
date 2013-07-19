package tjenkinson.asteriskLiveComs.program.events;

import java.util.Hashtable;

import tjenkinson.asteriskLiveComs.program.MyAsteriskChannel;
 
public class ChannelAddedEvent extends LiveComsEvent {
	
	private MyAsteriskChannel channel;
	public ChannelAddedEvent(MyAsteriskChannel channel) {
		this.channel = channel;
	}
	
	public Hashtable<String,Object> getInfo() {
		return channel.getInfo();
	}
}
