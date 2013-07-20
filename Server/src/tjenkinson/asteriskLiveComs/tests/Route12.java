package tjenkinson.asteriskLiveComs.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import douglascrockford.json.JSONArray;
import douglascrockford.json.JSONObject;


public class Route12 {


	private Socket client = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	public Route12() throws UnknownHostException, IOException {
		client = new Socket("127.0.0.1", 2345);
		out = new PrintWriter(this.client.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		JSONObject mainObj = new JSONObject();

		mainObj.put("action", "routeChannels");

		JSONArray channels = new JSONArray();
		JSONObject channel = new JSONObject();
		channel.put("id", 1);
		channel.put("listenOnly", false);
		channels.put(channel);
		channel = new JSONObject();
		channel.put("id", 2);
		channel.put("listenOnly", false);
		channels.put(channel);
		mainObj.put("channels", channels);
		out.println(mainObj.toString());
		System.out.println(in.readLine());
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		new Route12();
	}
}
