package me.cayve.ludorium.utils.interfaces;

import me.cayve.ludorium.utils.functionals.Event.Subscriber;

/**
 * Implements destroy()
 */
public interface Destroyable {
	public void destroy();
	public Subscriber<?> onDestroyed();
}
