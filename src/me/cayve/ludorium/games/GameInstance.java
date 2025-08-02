package me.cayve.ludorium.games;

import me.cayve.ludorium.games.events.InstanceEventLog;

public abstract class GameInstance {
	
	protected InstanceEventLog logger;
	
	public InstanceEventLog getLogger() { return logger; }
}
