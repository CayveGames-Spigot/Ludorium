package me.cayve.ludorium.utils.interfaces;

import me.cayve.ludorium.utils.events.Event.Subscriber;

/**
 * Used when an object has a task that can be canceled.
 * This doesn't necessarily mean the object is unusable now.
 * For example, an animator should cancel animations.
 */
public interface Cancelable {
	public void cancel();
	public Subscriber<?> onCanceled();
}
