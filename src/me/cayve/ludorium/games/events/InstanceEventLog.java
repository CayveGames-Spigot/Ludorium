package me.cayve.ludorium.games.events;

import java.util.ArrayList;

public class InstanceEventLog {
	
	private final ArrayList<InstanceEvent> log = new ArrayList<>();
	private int processedIndex = 0; //Used to keep track of what instance events the board has processed
	
	public void logEvent(InstanceEvent newEvent) { log.add(newEvent); }
	
	/**
	 * Retrieves the unprocessed events and marks them as processed
	 * @return
	 */
	public ArrayList<InstanceEvent> getUnprocessed() {
		ArrayList<InstanceEvent> unprocessed = new ArrayList<>();
		
		for (int i = processedIndex; i < log.size(); i++)
			unprocessed.add(log.get(i));
		
		processedIndex = log.size();
		return unprocessed;
	}
	
	public ArrayList<InstanceEvent> getFullLog() { return log; }
}
