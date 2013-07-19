package tjenkinson.asteriskLiveComs.program;

import java.util.Hashtable;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.CallerId;


public class MyAsteriskChannel {
	
	private AsteriskChannel channel;
	private boolean verified = false;
	private int id;
	private boolean playHoldMusic = true;
	
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
	
	public synchronized Hashtable<String,Object> getInfo() {
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
		return info;
	}
}
