package tjenkinson.asteriskLiveComs.program.events;

 
public class ChannelRemovedEvent extends AbstractChannelEvent {
	
	private int id;
	public ChannelRemovedEvent(int id) {
		this.id = id;
	}
	
	public int getChannelId() {
		return id;
	}
}
