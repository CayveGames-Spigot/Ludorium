package me.cayve.ludorium.utils.functionals;

public class Event0 extends Event<Runnable> implements Runnable {
	@Override public void run() { invoke(x -> x.run()); }
}
