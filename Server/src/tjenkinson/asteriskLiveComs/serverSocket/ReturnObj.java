package tjenkinson.asteriskLiveComs.serverSocket;

import java.util.Hashtable;

import douglascrockford.json.JSONObject;



class ReturnObj extends SocketOutput {
	
	private int code;
	private String msg;
	private Object payload;
	
	public ReturnObj(int code, String msg, Object payload) {
		this.code = code;
		this.msg = msg;
		this.payload = payload;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public String getType() {
		return "response";
	}
	
	public Object getPayload() {
		return payload;
	}

	public Hashtable<String,Object> getData() {
		Hashtable<String,Object> data = new Hashtable<String,Object>();
		data.put("code", getCode());
		data.put("msg", getMsg() == null ? false : getMsg());
		data.put("payload", getPayload() == null ? false : getPayload());
		return data;
	}
	
}
