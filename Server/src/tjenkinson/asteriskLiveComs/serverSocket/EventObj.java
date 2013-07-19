package tjenkinson.asteriskLiveComs.serverSocket;

import java.util.Hashtable;

class EventObj extends SocketOutput {

	private String eventType;
	private Object payload;
	
	public EventObj(String eventType, Object payload) {
		this.eventType = eventType;
		this.payload = payload;
	}
	
	public String getType() {
		return "event";
	}

	public String getEventType() {
		return eventType;
	}
	
	public Object getPayload() {
		return payload;
	}
	
	public Hashtable<String,Object> getData() {
		Hashtable<String,Object> data = new Hashtable<String,Object>();
		data.put("eventType", getEventType());
		data.put("payload", getPayload());
		return data;
	}
}
