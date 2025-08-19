package me.cayve.ludorium.utils.interfaces;

import me.cayve.ludorium.utils.functionals.Event.Subscriber;

public interface Cancelable {
	public void cancel();
	public Subscriber<?> onCanceled();
}
