package me.cayve.ludorium.utils.interfaces;

import me.cayve.ludorium.utils.events.Event.Subscriber;

/**
 * Implements enable() and disable()
 */
public interface Toggleable {
	public boolean isEnabled();
	public void enable();
	public void disable();
	
	public Subscriber<?> onEnabled();
	public Subscriber<?> onDisabled();
}
