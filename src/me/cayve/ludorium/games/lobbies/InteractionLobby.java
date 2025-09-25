package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;

import me.cayve.ludorium.utils.Collider;
import me.cayve.ludorium.utils.Config;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.rigs.HoverAnimationRig;
import me.cayve.ludorium.utils.entities.DisplayEntity;
import me.cayve.ludorium.utils.entities.ItemEntity;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class InteractionLobby extends GameLobby {
	
	private ArrayList<ItemEntity> displays = new ArrayList<>();
	
	private Task graceCountdown;
	
	int skipCount = 0;
	
	public InteractionLobby(int minimum, int maximum, ItemEntity globalToken) {
		super(minimum, maximum);
		
		addDisplay(globalToken, -1);
		
		registerEvents();
	}
	
	public InteractionLobby(int minimum, int maximum, boolean forceInventoryState, ItemEntity globalToken) {
		super(minimum, maximum, forceInventoryState);
		
		addDisplay(globalToken, -1);
		
		registerEvents();
	}
	
	public InteractionLobby(int minimum, int maximum, ArrayList<ItemEntity> tokens) {
		super(minimum, maximum);
		
		for (int i = 0; i < tokens.size(); i++)
			addDisplay(tokens.get(i), i);
		
		registerEvents();
	}
	
	public InteractionLobby(int minimum, int maximum, boolean forceInventoryState, ArrayList<ItemEntity> tokens) {
		super(minimum, maximum, forceInventoryState);
		
		for (int i = 0; i < tokens.size(); i++)
			addDisplay(tokens.get(i), i);
		
		registerEvents();
	}
	
	private void registerEvents() {
		onLobbyJoin().subscribe((index) -> displays.get(index).getComponent(Animator.class).cancel());
		
		onLobbyLeave().subscribe((index) -> displays.get(index).getComponent(Animator.class).play(new HoverAnimationRig()));
		
		Timer.register(graceCountdown = new Task(lobbyKey)
				.setDuration(Config.getInteger("games.graceCountdown") - COUNTDOWN_DURATION).setRefreshRate(.1f)
				.registerOnUpdate(() -> {
					skipCount = 0;
					forEachOnlinePlayer(player -> { skipCount += (player.isSneaking() ? 1 : 0); });
					
					if (skipCount == getPlayerCount()) {
						graceCountdown.pause();
						
						messenger.sendAll("interaction", ToolbarMessage::sendImmediate, new Message(v -> 
							TextYml.getText(v, "in-game.skipped")).setType(eType.ERROR).setDuration(1).setPriority(1).setType(eType.ERROR));
						
						startCountdown();
						return;
					}
					
					messenger.sendAll("interaction", ToolbarMessage::sendImmediate, new Message(v ->
							//The game starts in...
							TextYml.getText(v, "in-game.startsIn",
								Placeholder.parsed("duration", (graceCountdown.getWholeSecondsLeft() + COUNTDOWN_DURATION) + ""))
							//(Crouch to skip 1/4)
							.append(skipCount <= 0 ? Component.empty() : 
								Component.text(" ")
								.append(TextYml.getText(
									v, "in-game.crouchToSkip",
									Placeholder.parsed("count", skipCount + ""),
									Placeholder.parsed("total", getPlayerCount() + "")))))
						.clearIfSkipped().setMuted());

				})
				.registerOnComplete(this::startCountdown)).pause().refreshOnStart();;
	}
	
	@Override
	public void enable() {
		super.enable();
		
		for (ItemEntity display : displays)
		{
			display.enable();
			display.getComponent(Animator.class).play(new HoverAnimationRig());
		}
	}
	
	@Override
	public void disable() {
		super.disable();
		
		for (DisplayEntity<?> display : displays)
			display.disable();
	}
	
	@Override
	protected void onMinimumReached() {
		super.onMinimumReached();
		
		graceCountdown.restart();
	}
	
	@Override
	protected void onMinimumLost() {
		super.onMinimumLost();
		
		if (isEnabled())
		{
			graceCountdown.pause();
			messenger.clearContext("interaction");
		}
	}
	
	@Override
	protected void onMaximumReached() {
		super.onMaximumReached();
		
		graceCountdown.pause();
		startCountdown();
	}
	
	private void addDisplay(ItemEntity token, int lobbyPosition) {
		token.getComponent(Collider.class).onInteracted().subscribe((player) -> { onInteraction(player.getUniqueId().toString(), lobbyPosition); });
		
		displays.add(token);
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
