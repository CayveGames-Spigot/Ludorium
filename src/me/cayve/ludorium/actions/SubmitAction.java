package me.cayve.ludorium.actions;

import java.util.function.Consumer;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.cayve.ludorium.utils.ProgressBar;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class SubmitAction extends PlayerAction implements Listener {

	public enum eResult { SUBMIT, CANCEL, BOTH }
	public enum eCancelContext { CANCEL, GO_BACK, SKIP }
	private static final String[] cancelContexts = { "cancel", "goBack", "skip" };
	
	private float duration = 1.5f;
	private Task submitTask, transitionTask, cancelTask, sfxTask, reminderTask;
	private eResult resultType;
	private eCancelContext cancelContext = eCancelContext.CANCEL;
	
	private Message messageObject;
	
	public SubmitAction(Player player, eResult resultType, Consumer<PlayerAction> successCallback, Consumer<PlayerAction> failureCallback) {
		super(player, successCallback, failureCallback);
		this.resultType = resultType;
		
		createDefaults();
	}
	
	public SubmitAction(Player player, eResult resultType, long duration, Consumer<PlayerAction> successCallback, Consumer<PlayerAction> failureCallback) {
		super(player, successCallback, failureCallback);
		this.duration = duration;
		this.resultType = resultType;
		
		createDefaults();
	}
	
	public SubmitAction setCancelContext(eCancelContext context) { this.cancelContext = context; return this; }

	private void createDefaults() {
		sfxTask = Timer.register(new Task(tsk).registerOnUpdate(() -> {
			if (submitTask != null && !submitTask.isComplete())
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.75f, 1 + (submitTask.getPercentTimeCompleted()));
			else if (transitionTask != null && !transitionTask.isComplete())
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, .25f, .6f);
			else if (cancelTask != null && !cancelTask.isComplete())
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.75f, 1 + (cancelTask.getPercentTimeLeft()));
		}).pause());
		
		reminderTask = Timer.register(new Task(tsk).registerOnUpdate(() -> {
			if (!player.isSneaking())
				ToolbarMessage.sendImmediate(player, tsk, TextYml.getText(player, "actions.crouch.hold",
						Placeholder.parsed("context", 
								(resultType == eResult.BOTH || resultType == eResult.SUBMIT ?
										TextYml.getText(player, "actions.crouch.confirm") : "") + 
								(resultType == eResult.BOTH ? "/" : "") + 
								(resultType == eResult.BOTH || resultType == eResult.CANCEL ?
										TextYml.getText(player, "actions.crouch." + cancelContexts[cancelContext.ordinal()]) : ""))))
				.clearIfSkipped();
		}).setRefreshRate(30));
	}
	
	public void restartReminder() {
		reminderTask.setRefreshRate(10);
		reminderTask.restart();
	}
	
	private void updateMessage() {
		Component message = Component.empty();
		
		if (resultType == eResult.SUBMIT || resultType == eResult.BOTH)
		{
			if (submitTask.isComplete())
				message.append(TextYml.getText(player, "actions.crouch.confirmed"))
					.append(MiniMessage.miniMessage().deserialize(ProgressBar.generate(submitTask.getPercentTimeCompleted(), "[<green>%i%o<reset>]")));
			else
				message.append(TextYml.getText(player, "actions.crouch.confirm"))
					.append(Component.text(ProgressBar.generate(submitTask.getPercentTimeCompleted())));
		}
		if (resultType == eResult.BOTH)
			message.append(Component.text(ProgressBar.generate(transitionTask.getPercentTimeCompleted(), 6, " %i %o ")));
		if (resultType == eResult.BOTH || resultType == eResult.CANCEL)
			message.append(TextYml.getText(player, "actions.crouch." + cancelContexts[cancelContext.ordinal()]))
				.append(Component.text(ProgressBar.generate(cancelTask.getPercentTimeCompleted())));
		
		if (messageObject != null)
			messageObject.updateMessage(message);
		else
			messageObject = ToolbarMessage.clearSourceAndSendImmediate(player, tsk, message).setPermanent().refreshEveryTick().setPriority(1);
	}
	
	@EventHandler
	private void onCrouch(PlayerToggleSneakEvent event) {
		if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;

		if (event.isSneaking()) {
			if (duration == -1) {
				publishEvent();
				return;
			}
			
			//Create submit prompt and timer
			if (resultType == eResult.SUBMIT || resultType == eResult.BOTH) {
				sfxTask.setRefreshRate((long)Math.round(duration * 20) / 4); //Calculates the time it will take for one bar to complete
				submitTask = Timer.register(new Task(tsk).setDuration(duration).setRefreshRate(0)
						.registerOnUpdate(this::updateMessage)
						.registerOnComplete(() -> {
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 2);
							if (resultType == eResult.BOTH)
							{
								transitionTask.restart();
								sfxTask.setRefreshRate(1 + (long)Math.round(duration * 2 * 20) / 6);
							}
							else
								publishEvent();
						}));
			}
			//Create transition timer
			if (resultType == eResult.BOTH)
			{
				transitionTask = Timer.register(new Task(tsk).setDuration(duration * 2).setRefreshRate(0)
						.registerOnUpdate(this::updateMessage)
						.registerOnComplete(() -> {
							sfxTask.setRefreshRate((long)Math.round(duration * 20) / 4);
							cancelTask.restart();
						})).pause(); //Pause the timer to be ready for the previous task to start
			}
			//Create cancel prompt and timer
			if (resultType == eResult.BOTH || resultType == eResult.CANCEL) {
				cancelTask = Timer.register(new Task(tsk).setDuration(duration).setRefreshRate(0)
						.registerOnUpdate(this::updateMessage)
						.registerOnComplete(this::cancelEvent));
				//If this is a series of tasks, pause it for the previous task to start
				if (resultType == eResult.BOTH)
					cancelTask.pause();
			}
			
			//Unpause the sound effect
			sfxTask.unpause();
			
			updateMessage();
		}
		else 
		{
			//If released after completion, publish
			if (submitTask != null && submitTask.isComplete())
				publishEvent();
			else { //If released before completion, reset
				Timer.cancelAllWithKey(tsk);
				ToolbarMessage.clearAllFromSource(tsk);
				messageObject = null;
				
				createDefaults();
			}
		}
	}
}
