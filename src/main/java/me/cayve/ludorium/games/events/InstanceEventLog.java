package me.cayve.ludorium.games.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.events.Event1;

public class InstanceEventLog {
	
	private Map<Class<? extends InstanceEvent>, Event1<?>> events = new HashMap<>();
	private final ArrayList<InstanceEvent> unprocessed = new ArrayList<>(),
											processed = new ArrayList<>();
	
	public InstanceEventLog(Subscriber<Runnable> dispatcher) {
		dispatcher.subscribe(this::dispatchUnprocessed);
	}
	
	public void logEvent(InstanceEvent newEvent) { unprocessed.add(newEvent); }
	
	public ArrayList<InstanceEvent> getFullLog() { return processed; }
	
	@SuppressWarnings("unchecked")
	private void dispatchUnprocessed() {
		for (InstanceEvent event : unprocessed) {
			if (events.containsKey(event.getClass()))
				((Event1<InstanceEvent>)events.get(event.getClass())).accept(event);
			
			processed.add(event);
		}
		unprocessed.clear();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends InstanceEvent> Subscriber<Consumer<T>> getSubscriber(Class<T> type) {
		return ((Event1<T>)events.computeIfAbsent(type, x -> new Event1<T>())).getSubscriber();
	}
}
