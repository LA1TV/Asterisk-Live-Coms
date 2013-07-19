package tjenkinson.asteriskLiveComs.program.events;

public abstract class AbstractChannelEvent  extends LiveComsEvent {
	
	private int id;
	
	public AbstractChannelEvent(int id) {
		this.id = id;
	}
	
	public int getChannelId() {
		return id;
	}
}
