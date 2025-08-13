package me.cayve.ludorium.utils.functionals;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MultiConsumer<T> implements Consumer<T> {

	private final ArrayList<Consumer<T>> event = new ArrayList<>();
	private final ArrayList<Integer> oneShotIndexes = new ArrayList<>();
	
	public void remove(Consumer<T> listener) {
		if (oneShotIndexes.contains(event.indexOf(listener)))
			oneShotIndexes.remove(event.indexOf(listener));
		
		event.remove(listener);
	}
	public void add(Consumer<T> listener) { event.add(listener); }
	public void oneShot(Consumer<T> listener) 
	{ 
		add(listener);
		oneShotIndexes.add(event.size() - 1);
	}
	
	@Override
	public void accept(T value) {
		for (Consumer<T> listener : event)
			listener.accept(value);
		
		while (!oneShotIndexes.isEmpty())
		{
			event.remove((int)oneShotIndexes.getLast());
			oneShotIndexes.removeLast();
		}
	}
}
