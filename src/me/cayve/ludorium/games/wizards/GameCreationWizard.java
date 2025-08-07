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
import me.cayve.ludorium.utils.StateMachine;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;

public abstract class GameCreationWizard implements Listener {
	
	private static ArrayList<GameCreationWizard> activeWizards = new ArrayList<GameCreationWizard>();
	
	protected StateMachine stateMachine;
	
	protected PlayerAction currentAction;
	protected Player player;
	protected String instanceName;
	protected String tsk = UUID.randomUUID().toString(); //ToolbarMessage source key
	protected String stateTsk = UUID.randomUUID().toString(); //Tsk specifically for state messages
	
	protected PlayerAction delayedAction; //If onComplete should be delayed, cache the action that was completed
	protected Task onCompleteDelay;
	
	//this::new is used as Supplier interface in command, any new arguments will require rework - use setters instead
	public GameCreationWizard() {
		activeWizards.add(this);
		
		LudoriumPlugin.registerEvent(this);
		
		stateMachine = new StateMachine();
	}
	
	/**
	 * Acts as constructor for pre-activation
	 * 
	 * @param instanceName the name of the game instance
	 * @param wizard the player creating the game instance
	 * @return self, to allow for argument chaining
	 */
	public GameCreationWizard apply(String instanceName, Player wizard) {
		if (stateMachine.hasStarted()) return this;
		
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
		
		if (currentAction != null)
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
		if (stateMachine.hasStarted() || player == null) return;
		
		ToolbarMessage.clearSourceAndSend(player, tsk, TextYml.getText(player, "wizards.started"))
			.setType(eType.SUCCESS).setDuration(2).clearIfSkipped();
		
		stateMachine.next();
	}
	
	//Callback for wizard action
	protected void onCompletedAction(PlayerAction action) {
		if (!stateMachine.hasStarted()) return;
		
		//If there should be a delay, start the timer
		if (onCompleteDelay != null && onCompleteDelay.isPaused()) {
			delayedAction = action;
			onCompleteDelay.unpause();
			return;
		}
		
		//Reset delay
		onCompleteDelay = null;
		delayedAction = null;
		
		stateMachine.next();
	}
	
	//Callback for wizard action
	protected void onCanceledAction(PlayerAction action) {
		if (!stateMachine.hasStarted()) return;
		
		stateMachine.previous();
		
		if (stateMachine.getStateIndex() < 0)
			cancelWizard();
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
	
	//Quit the wizard if the player leaves
	@EventHandler
	private void onPlayerLeave(PlayerQuitEvent event) {
		if (player.getUniqueId().equals(event.getPlayer().getUniqueId()))
			cancelWizard();
	}
	
	protected void cancelWizard(Component customMessage) {
		ToolbarMessage.sendImmediate(player, customMessage).setType(eType.ERROR);
		destroy();
	}
	protected void cancelWizard() {
		cancelWizard(TextYml.getText(player, "wizards.canceled"));
	}
	
	protected void completeWizard() {
		ToolbarMessage.sendImmediate(player, TextYml.getText(player, "wizards.completed")).setType(eType.SUCCESS);
		destroy();
	}
}
