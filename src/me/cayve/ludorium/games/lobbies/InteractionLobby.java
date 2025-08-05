package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;

import me.cayve.ludorium.utils.Config;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.utils.animations.LinearAnimation;
import me.cayve.ludorium.utils.animations.OffsetAnimation;
import me.cayve.ludorium.utils.animations.SinWaveAnimation;
import me.cayve.ludorium.utils.entities.DisplayEntity;
import me.cayve.ludorium.utils.entities.ItemEntity;
import me.cayve.ludorium.ymls.TextYml;

public class InteractionLobby extends GameLobby {
	
	private ArrayList<ItemEntity> displays = new ArrayList<>();
	
	private Task graceCountdown;
	
	int skipCount = 0;
	
	public InteractionLobby(int minimum, int maximum, ItemEntity globalToken) {
		super(minimum, maximum);
		
		addDisplay(globalToken, -1);
		
		registerEvents();
	}
	
	public InteractionLobby(int minimum, int maximum, ArrayList<ItemEntity> tokens) {
		super(minimum, maximum);
		
		for (int i = 0; i < tokens.size(); i++)
			addDisplay(tokens.get(i), i);
		
		registerEvents();
	}
	
	private void registerEvents() {
		registerJoinListener((index) -> displays.get(index).getAnimator().cancelAnimations());
		
		registerLeaveListener((index) -> playIdleAnimation(displays.get(index)));
		
		Timer.register(graceCountdown = new Task(lobbyKey)
				.setDuration(Config.getInteger("games.graceCountdown") - COUNTDOWN_DURATION).setRefreshRate(.1f)
				.registerOnUpdate(() -> {
					skipCount = 0;
					forEachOnlinePlayer(player -> { skipCount += (player.isSneaking() ? 1 : 0); });
					
					if (skipCount == getPlayerCount()) {
						graceCountdown.pause();
						
						forEachOnlinePlayer(player -> ToolbarMessage.sendImmediate(player, lobbyKey + "-interation", 
								TextYml.getText(player, "in-game.skipped")).setType(eType.ERROR).setDuration(1).setPriority(1).setType(eType.ERROR));
						
						startCountdown();
						return;
					}
					
					forEachOnlinePlayer(player -> 
						ToolbarMessage.sendImmediate(player, lobbyKey + "-interation", 
							TextYml.getText(player, "in-game.startsIn")
								.replace("<duration>", (graceCountdown.getWholeSecondsLeft() + COUNTDOWN_DURATION) + "") +
							(skipCount > 0 ? " " + TextYml.getText(player, "in-game.crouchToSkip")
								.replace("<count>", skipCount + "")
								.replace("<total>", getPlayerCount() + "") : ""))
						.clearIfSkipped().setMuted());
				})
				.registerOnComplete(this::startCountdown)).pause().refreshOnStart();;
	}
	
	@Override
	public void enable() {
		super.enable();
		
		for (ItemEntity display : displays)
		{
			display.spawn();
			playIdleAnimation(display);
		}
	}
	
	@Override
	public void disable() {
		super.disable();
		
		for (DisplayEntity<?> display : displays)
			display.remove();
	}
	
	@Override
	protected void onMinimumReached() {
		super.onMinimumReached();
		
		//graceCountdown.restart();
	}
	
	@Override
	protected void onMinimumLost() {
		super.onMinimumLost();
		
		if (isEnabled())
		{
			graceCountdown.pause();
			ToolbarMessage.clearAllFromSource(lobbyKey + "-interation");
		}
	}
	
	@Override
	protected void onMaximumReached() {
		super.onMaximumReached();
		
		graceCountdown.pause();
		startCountdown();
	}
	
	private void addDisplay(ItemEntity token, int lobbyPosition) {
		token.registerOnInteractedWith((player) -> { onInteraction(player.getUniqueId().toString(), lobbyPosition); });
		
		displays.add(token);
	}
	
	private void playIdleAnimation(ItemEntity token) {
		token.getAnimator().setYawAnimation(new LinearAnimation(0, 360).loops().setSpeed(.4f).randomize());
		token.getAnimator().setYAnimation(new OffsetAnimation(1, new SinWaveAnimation(.3f)).loops().setSpeed(.2f).randomize());
	}
	
	private void onInteraction(String playerID, int lobbyPosition) {

		if (hasPlayer(playerID)) {
			
			int oldPos = getPlayerPosition(playerID);
			attemptLobbyLeave(playerID);
			
			if (oldPos == lobbyPosition)
				return;
		}
		
		if (lobbyPosition == -1)
			attemptLobbyJoin(playerID);
		else
			attemptLobbyJoin(playerID, lobbyPosition);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		for (DisplayEntity<?> display : displays)
			display.destroy();
	}
}
