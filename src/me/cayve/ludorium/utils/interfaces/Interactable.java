package me.cayve.ludorium.utils.interfaces;

import me.cayve.ludorium.utils.events.Event.Subscriber;

public interface Interactable {
	public Subscriber<?> onInteracted();
}
