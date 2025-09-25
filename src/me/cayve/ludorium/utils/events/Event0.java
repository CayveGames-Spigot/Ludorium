package me.cayve.ludorium.utils.events;

public class Event0 extends Event<Runnable> implements Runnable {
	@Override public void run() { invoke(x -> x.run()); }
}
