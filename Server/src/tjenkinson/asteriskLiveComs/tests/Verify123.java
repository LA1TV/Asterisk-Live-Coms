package tjenkinson.asteriskLiveComs.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import douglascrockford.json.JSONArray;
import douglascrockford.json.JSONObject;


public class Verify123 {


	private Socket client = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	public Verify123() throws UnknownHostException, IOException {
		client = new Socket("127.0.0.1", 2345);
		out = new PrintWriter(this.client.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		JSONObject mainObj = new JSONObject();
	
		mainObj.put("action", "grantAccess");
		mainObj.put("id", 1);
		mainObj.put("enableHoldMusic", false);
		out.println(mainObj.toString());
		System.out.println(in.readLine());
		
		mainObj = new JSONObject();
		mainObj.put("action", "grantAccess");
		mainObj.put("id", 2);
		mainObj.put("enableHoldMusic", true);
		out.println(mainObj.toString());
		System.out.println(in.readLine());
		
		mainObj = new JSONObject();
		mainObj.put("action", "grantAccess");
		mainObj.put("id", 3);
		mainObj.put("enableHoldMusic", true);
		out.println(mainObj.toString());
		System.out.println(in.readLine());
		
		
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		new Verify123();
	}
}
