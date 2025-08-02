package me.cayve.ludorium.utils.functionals;

import java.util.ArrayList;

public class MultiRunnable implements Runnable {
	private final ArrayList<Runnable> event = new ArrayList<>();
	
	public void add(Runnable listener) { event.add(listener); }
	
	@Override
	public void run() {
		for (Runnable listener : event)
			listener.run();
	}
}
