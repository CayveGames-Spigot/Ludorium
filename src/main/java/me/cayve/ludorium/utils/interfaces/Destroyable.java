package me.cayve.ludorium.utils.interfaces;

import me.cayve.ludorium.utils.events.Event.Subscriber;

/**
 * Used to implement destroyable functionality.
 * Objects that require cleanup when destroyed should implement this.
 * For example, objects that own entities, or active timers.
 */
public interface Destroyable {
	public void destroy();
	public Subscriber<?> onDestroyed();
}
