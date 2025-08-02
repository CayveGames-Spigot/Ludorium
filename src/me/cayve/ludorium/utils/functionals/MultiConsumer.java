package me.cayve.ludorium.utils.functionals;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MultiConsumer<T> implements Consumer<T> {

	private final ArrayList<Consumer<T>> event = new ArrayList<>();
	
	public void add(Consumer<T> listener) { event.add(listener); }
	
	@Override
	public void accept(T value) {
		for (Consumer<T> listener : event)
			listener.accept(value);
	}
}
