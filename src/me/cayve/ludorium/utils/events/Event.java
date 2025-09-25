package me.cayve.ludorium.utils.events;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class Event<T> {

	private static class EventListener<U> {
		private final U listener;
		private final int priority;
		private final boolean isOneShot;
		private final AtomicBoolean hasRun; //Exclusively for oneShots
		
		private EventListener(U listener, int priority, boolean isOneShot) {
			this.listener = listener;
			this.priority = priority;
			this.isOneShot = isOneShot;
			this.hasRun = new AtomicBoolean(false);
		}
	}
	
	public interface Subscription extends AutoCloseable {
		@Override public void close();
	}
	
	public static class Subscriber<U> {
		private CopyOnWriteArrayList<EventListener<U>> listeners = new CopyOnWriteArrayList<>();
		
		public Subscription subscribe(U listener) { return subscribe(listener, false, 0); }
		public Subscription subscribe(U listener, boolean isOneShot) { return subscribe(listener, isOneShot, 0); }
		public Subscription subscribe(U listener, int priority) { return subscribe(listener, false, priority); }
		
		public Subscription subscribe(U listener, boolean isOneShot, int priority) {
			if (listener == null) return null;
			
			listeners.add(new EventListener<U>(listener, priority, isOneShot));
			listeners.sort((x, y) -> Integer.compare(y.priority, x.priority));
			
			return () -> unsubscribe(listener);
		}
		
		public void unsubscribe(U listener) {
			for (EventListener<U> l : listeners)
				if (l.listener == listener)
					listeners.remove(l);
		}
	}
	
	private Subscriber<T> subscriber = new Subscriber<>();
	
	public Subscription subscribe(T listener) 									{ return subscriber.subscribe(listener); }
	public Subscription subscribe(T listener, boolean isOneShot) 				{ return subscriber.subscribe(listener, isOneShot); }
	public Subscription subscribe(T listener, int priority) 					{ return subscriber.subscribe(listener, priority); }
	public Subscription subscribe(T listener, boolean isOneShot, int priority) 	{ return subscriber.subscribe(listener, isOneShot, priority); }
	
	public void unsubscribe(T listener) { subscriber.unsubscribe(listener); }
	
	public Subscriber<T> getSubscriber() { return subscriber; }

	protected void invoke(Consumer<T> invocationMethod) {
		for (EventListener<T> listener : subscriber.listeners) {
			if (listener.isOneShot && !listener.hasRun.compareAndSet(false, true)) continue;
			
			invocationMethod.accept(listener.listener);
			
			if (listener.isOneShot)
				subscriber.unsubscribe(listener.listener);
		}
	}
}
