package tjenkinson.asteriskLiveComs.serverSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import tjenkinson.asteriskLiveComs.program.exceptions.ChannelNotVerifiedException;
import tjenkinson.asteriskLiveComs.program.exceptions.InvalidChannelException;
import tjenkinson.asteriskLiveComs.program.exceptions.MissingKeyException;
import tjenkinson.asteriskLiveComs.program.exceptions.OnlyOneChannel;
import douglascrockford.json.JSONArray;
import douglascrockford.json.JSONException;
import douglascrockford.json.JSONObject;

class IncomingCommandHandler implements Runnable {

	private BufferedReader in;
	private ClientSocketConnectionManager clientSocketManager;
	
	public IncomingCommandHandler(BufferedReader in, ClientSocketConnectionManager clientSocketManager) {
		this.in = in;
		this.clientSocketManager = clientSocketManager;
	}
	
	public void run() {
		String inputLine = null;
	
		try {
			while ((inputLine = in.readLine()) != null) {
				
				clientSocketManager.getServerSocketManager().getMainProgramObj().log("Socket request received.");
				JSONObject inputLineJSON = null;
				ReturnObj returnObj = null;
				boolean parseError = false;
				try {
					inputLineJSON = new JSONObject(inputLine);
				}
				catch(JSONException e) {
					returnObj  = new ReturnObj(1, "Could not parse JSON string.", null);
					parseError = true;
				}
				
				if (!parseError) {
					clientSocketManager.getServerSocketManager().getMainProgramObj().log("Socket request contents: "+inputLine);
					if (!inputLineJSON.has("action") || inputLineJSON.getString("action") == null) {
						returnObj  = new ReturnObj(2, "The action was not specified.", null);
					}
					else if (inputLineJSON.getString("action").equals("getChannels")) {
						returnObj = new ReturnObj(0, null, clientSocketManager.getServerSocketManager().getMainProgramObj().getChannels());
					}
					else if (inputLineJSON.getString("action").equals("grantAccess")) {
						try {
							if (clientSocketManager.getServerSocketManager().getMainProgramObj().grantAccess(inputLineJSON.getInt("id"), inputLineJSON.getBoolean("enableHoldMusic"))) {
								returnObj = new ReturnObj(0, "The channel has been granted access.", null);
							}
							else {
								returnObj = new ReturnObj(103, "The channel has already been granted access.", null);
							}
						} catch (JSONException e) {
							returnObj = new ReturnObj(101, "JSON invalid for command.", null);
						} catch (InvalidChannelException e) {
							returnObj = new ReturnObj(102, "The channel id is invalid.", null);
						}
					}
					else if (inputLineJSON.getString("action").equals("denyAccess")) {
						try {
							clientSocketManager.getServerSocketManager().getMainProgramObj().denyAccess(inputLineJSON.getInt("id"));
							returnObj = new ReturnObj(0, "The channel has been denied access.", null);
						} catch (JSONException e) {
							returnObj = new ReturnObj(101, "JSON invalid for command.", null);
						} catch (InvalidChannelException e) {
							returnObj = new ReturnObj(102, "The channel id is invalid.", null);
						}
					}
					else if (inputLineJSON.getString("action").equals("routeChannels")) {
						try {
							
							ArrayList<Hashtable<String,Object>> data = new ArrayList<Hashtable<String,Object>>();
							
							JSONArray chanIds = inputLineJSON.getJSONArray("channels");
							for (int i=0; i<chanIds.length(); i++) {
								JSONObject a = chanIds.getJSONObject(i);
								Hashtable<String,Object> row = new Hashtable<String,Object>();
								row.put("id", a.getInt("id"));
								row.put("listenOnly", a.getBoolean("listenOnly"));
								data.add(row);
							}
							clientSocketManager.getServerSocketManager().getMainProgramObj().createRoom(data);
							returnObj = new ReturnObj(0, "The channels have been routed.", null);
						} catch (JSONException e) {
							returnObj = new ReturnObj(101, "JSON invalid for command.", null);
						} catch (InvalidChannelException e) {
							returnObj = new ReturnObj(102, "One or more of the channel id's are invalid.", null);
						} catch (ChannelNotVerifiedException e) {
							returnObj = new ReturnObj(103, "One or more of the channel id's haven't been verified yet.", null);
						} catch (MissingKeyException e) {
							returnObj = new ReturnObj(104, "One or more keys are missing from the JSON string.", null);
						} catch (OnlyOneChannel e) {
							returnObj = new ReturnObj(105, "You need to specify more than one channel.", null);
						}
					}
					else if (inputLineJSON.getString("action").equals("getRoomGroups")) {
						returnObj = new ReturnObj(0, null, clientSocketManager.getServerSocketManager().getMainProgramObj().getRoomGroups());
					}
					else if (inputLineJSON.getString("action").equals("sendToHolding")) {
						try {
							
							ArrayList<Integer> data = new ArrayList<Integer>();
							
							JSONArray chanIds = inputLineJSON.getJSONArray("channels");
							for (int i=0; i<chanIds.length(); i++) {
								data.add(chanIds.getInt(i));
							}
							clientSocketManager.getServerSocketManager().getMainProgramObj().sendChannelsToHolding(data);
							returnObj = new ReturnObj(0, "The channels have been sent to holding.", null);
						} catch (JSONException e) {
							returnObj = new ReturnObj(101, "JSON invalid for command.", null);
						} catch (InvalidChannelException e) {
							returnObj = new ReturnObj(102, "One or more of the channel id's are invalid.", null);
						} catch (ChannelNotVerifiedException e) {
							returnObj = new ReturnObj(103, "One or more of the channel id's haven't been verified yet.", null);
						}
					}
					else if (inputLineJSON.getString("action").equals("reset")) {
						clientSocketManager.getServerSocketManager().getMainProgramObj().reset();
						returnObj = new ReturnObj(0, "The server is being reset.", null);
					}
					else if (inputLineJSON.getString("action").equals("isConnectedToAsterisk")) {
						returnObj = new ReturnObj(0, null, clientSocketManager.getServerSocketManager().getMainProgramObj().isConnectedToAsterisk());
					}
					else if (inputLineJSON.getString("action").equals("ping")) {
						returnObj = new ReturnObj(0, "Hello!", null);
					}
				}
				
				if (returnObj == null) {
					returnObj = new ReturnObj(3, "Nothing to process command.", null);
				}
				clientSocketManager.sendOutput(returnObj);
				clientSocketManager.getServerSocketManager().getMainProgramObj().log("Socket request completed.");
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			clientSocketManager.closeSocket();
		}
	}
}