package tjenkinson.asteriskLiveComs.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import douglascrockford.json.JSONArray;
import douglascrockford.json.JSONObject;


public class GetChannels {


	private Socket client = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	public GetChannels() throws UnknownHostException, IOException {
		client = new Socket("127.0.0.1", 2345);
		out = new PrintWriter(this.client.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		JSONObject mainObj = new JSONObject();
		mainObj.put("action", "getChannels");
		out.println(mainObj.toString());
		System.out.println(in.readLine());

	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		new GetChannels();
	}
}
