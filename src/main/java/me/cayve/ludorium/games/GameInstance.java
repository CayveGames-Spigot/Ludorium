package me.cayve.ludorium.games;

import me.cayve.ludorium.games.events.InstanceEventLog;
import me.cayve.ludorium.utils.events.Event0;

public abstract class GameInstance {
	
	private Event0 dispatchEvents = new Event0();
	protected InstanceEventLog logger = new InstanceEventLog(dispatchEvents.getSubscriber());
	
	public InstanceEventLog getLogger() { return logger; }
	protected void dispatchEvents() { dispatchEvents.run(); }
}
