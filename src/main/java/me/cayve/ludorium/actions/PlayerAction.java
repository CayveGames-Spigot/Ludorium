package me.cayve.ludorium.actions;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import me.cayve.ludorium.LudoriumPlugin;
import me.cayve.ludorium.utils.SourceKey;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;

public abstract class PlayerAction {
	
	protected Consumer<PlayerAction> successCallback;
	protected Consumer<PlayerAction> failureCallback;
	protected Player player;
	protected SourceKey tsk = new SourceKey(); //ToolbarMessage source key
	
	protected boolean isComplete = false;
	private Task publishDelay;
	
	public PlayerAction(Player player, Consumer<PlayerAction> successCallback, Consumer<PlayerAction> failureCallback) {
		this.player = player;
		this.successCallback = successCallback;
		this.failureCallback = failureCallback;
		
		if (this instanceof Listener)
			LudoriumPlugin.registerEvent((Listener)this);
	}
	
	public void destroy() {
		Timer.cancelAllWithKey(tsk);
		ToolbarMessage.clearAllFromSource(tsk);
		
		if (this instanceof Listener)
			HandlerList.unregisterAll((Listener)this);
	}
	
	protected void publishEvent() {
		isComplete = true;
		successCallback.accept(this);

		destroy();
	}
	
	/**
	 * Publishes the event after a given delay
	 * @param delay
	 */
	protected void delayedPublish(float delay) {
		if (publishDelay != null)
			publishDelay.cancel();
		
		isComplete = true;
		publishDelay = new Task(tsk).setDuration(delay).registerOnComplete(this::publishEvent);
		Timer.register(publishDelay);
	}
	
	protected void cancelEvent() {
		failureCallback.accept(this);

		destroy();
	}
}