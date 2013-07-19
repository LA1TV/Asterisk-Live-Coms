package tjenkinson.asteriskLiveComs.program.events;

import java.util.ArrayList;

public abstract class AbstractChannelsEvent extends LiveComsEvent {
	private ArrayList<Integer> ids;
	
	public AbstractChannelsEvent(ArrayList<Integer> ids) {
		this.ids = ids;
	}
	
	public ArrayList<Integer> getChannelIds() {
		return ids;
	}
}
