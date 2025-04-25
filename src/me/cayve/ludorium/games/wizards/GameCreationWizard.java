package me.cayve.ludorium.games.wizards;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.cayve.ludorium.actions.PlayerAction;
import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.ymls.TextYml;

public abstract class GameCreationWizard implements Listener {
	
	private static ArrayList<GameCreationWizard> activeWizards = new ArrayList<GameCreationWizard>();
	
	protected int state = -1;
	protected boolean progressed = true;
	protected PlayerAction currentAction;
	protected Player player;
	protected String instanceName;
	protected UUID tsk = UUID.randomUUID(); //ToolbarMessage source key
	protected UUID stateTsk = UUID.randomUUID(); //Tsk specifically for state messages
	
	protected PlayerAction delayedAction; //If onComplete should be delayed, cache the action that was completed
	protected Task onCompleteDelay;
	
	//this::new is used as Supplier interface in command, any new arguments will require rework - use setters instead
	public GameCreationWizard() {
		activeWizards.add(this);
		
		LudoriumPlugin.registerEvent(this);
	}
	
	/**
	 * Acts as constructor for pre-activation
	 * 
	 * @param instanceName the name of the game instance
	 * @param wizard the player creating the game instance
	 * @return self, to allow for argument chaining
	 */
	public GameCreationWizard apply(String instanceName, Player wizard) {
		if (state != -1) return this;
		
		this.player = wizard;
		this.instanceName = instanceName;
		
		return this;
	}
	
	//Checks if a player is active in any wizard
	public static boolean isInWizard(Player player) {
		if (activeWizards == null)
			return false;
		
		for (GameCreationWizard wizard : activeWizards)
			if (wizard.player.getUniqueId().equals(player.getUniqueId()))
				return true;
		return false;
	}
	
	/**
	 * Destroys all active wizards
	 */
	public static void destroyAll() {
		while (activeWizards.size() > 0) {
			activeWizards.get(0).destroy();
			//No need to remove, destroy removes self
		}
	}
	
	//Destroys self
	public void destroy() {
		activeWizards.remove(this);

		Timer.cancelAllWithKey(tsk);
		ToolbarMessage.clearAllFromSource(tsk);
		ToolbarMessage.clearAllFromSource(stateTsk);
		currentAction.destroy();
		
		HandlerList.unregisterAll(this);
	}
	
	protected PlayerAction setNewAction(PlayerAction action) {
		if (currentAction != null)
			currentAction.destroy();
		
		currentAction = action;
		return currentAction;
	}
	
	//Activate after initialization to allow for custom arguments in children
	public void activateWizard() {
		if (state != -1 || player == null) return;
		
		ToolbarMessage.clearSourceAndSend(player, tsk, TextYml.getText("wizards.started")).setType(eType.SUCCESS).setDuration(2).clearIfSkipped();
		
		skipToState(0);
	}
	
	//Callback for wizard action
	protected void onCompletedAction(PlayerAction action) {
		if (state == -1) return;
		
		//If there should be a delay, start the timer
		if (onCompleteDelay != null && onCompleteDelay.isPaused()) {
			delayedAction = action;
			onCompleteDelay.unpause();
			return;
		}
		
		//Reset delay
		onCompleteDelay = null;
		delayedAction = null;
		
		skipToState(state + 1);
	}
	
	//Callback for wizard action
	protected void onCancelledAction(PlayerAction action) {
		if (state == -1) return;
		
		skipToState(state - 1);
	}
	
	/**
	 * Delays the callback of onComplete (e.g. for animations)
	 * @param delay
	 */
	protected void delayActionComplete(float delay) {
		if (onCompleteDelay != null)
			onCompleteDelay.cancel();
		
		onCompleteDelay = new Task(tsk).registerOnComplete(() -> onCompletedAction(delayedAction)).setDuration(delay).pause();
		Timer.register(onCompleteDelay);
	}
	
	//Using this can screw up progression/regression. ONLY USE IF YOUR PROGRESSION IS DESIGNED TO
	protected void skipToState(int state) {
		progressed = this.state < state;
		this.state = state;
		
		if (state < 0)
			cancelWizard();
		else
			onStateUpdate();
	}
	
	//This is where the custom wizard logic is kept
	protected abstract void onStateUpdate();
	
	
	//Quit the wizard if the player leaves
	@EventHandler
	private void onPlayerLeave(PlayerQuitEvent event) {
		if (player.getUniqueId().equals(event.getPlayer().getUniqueId()))
			cancelWizard();
	}
	
	private void cancelWizard() {
		ToolbarMessage.sendImmediate(player, TextYml.getText("wizards.canceled")).setType(eType.ERROR);
		destroy();
	}
}
