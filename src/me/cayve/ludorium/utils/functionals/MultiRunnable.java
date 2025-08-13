package me.cayve.ludorium.utils.functionals;

import java.util.ArrayList;

public class MultiRunnable implements Runnable {
	private final ArrayList<Runnable> event = new ArrayList<>();
	private final ArrayList<Integer> oneShotIndexes = new ArrayList<>();
	
	public void remove(Runnable listener) {
		if (oneShotIndexes.contains(event.indexOf(listener)))
			oneShotIndexes.remove(event.indexOf(listener));
		
		event.remove(listener);
	}
	
	public void add(Runnable listener) { event.add(listener); }
	public void oneShot(Runnable listener) 
	{ 
		add(listener);
		oneShotIndexes.add(event.size() - 1);
	}
	
	@Override
	public void run() {
		for (Runnable listener : event)
			listener.run();
		
		while (!oneShotIndexes.isEmpty())
		{
			event.remove((int)oneShotIndexes.getLast());
			oneShotIndexes.removeLast();
		}
	}
}
