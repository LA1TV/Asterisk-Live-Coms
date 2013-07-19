package tjenkinson.asteriskLiveComs.serverSocket;

import java.util.Hashtable;

import douglascrockford.json.JSONObject;

public abstract class SocketOutput {
	
	public final String getJSONString() {
		Hashtable<String,Object> data = getData();
		data.put("type", getType());
		data.put("time", System.currentTimeMillis());
		return new JSONObject(data).toString();
	}
	
	public abstract Hashtable<String,Object> getData();
	public abstract String getType();
}
