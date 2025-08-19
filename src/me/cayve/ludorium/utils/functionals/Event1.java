package me.cayve.ludorium.utils.functionals;

import java.util.function.Consumer;

public class Event1<T> extends Event<Consumer<T>> implements Consumer<T> {
	@Override public void accept(T t) { invoke(x -> x.accept(t)); }
}
