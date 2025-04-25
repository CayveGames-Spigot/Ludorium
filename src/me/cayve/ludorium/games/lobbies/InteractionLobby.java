package me.cayve.ludorium.games.lobbies;

import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import me.cayve.ludorium.games.GameBase;

public class InteractionLobby extends GameLobby implements Listener {

	//Lobby trigger
	private Interaction trigger;
	
	public InteractionLobby(GameBase game, int minimum, int maximum, Interaction trigger) {
		super(game, minimum, maximum);
		
		this.trigger = trigger;
	}
	
	@EventHandler
	private void onInteraction(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Interaction)) return;
		
		if (trigger.equals((Interaction) event.getRightClicked()))
			attemptLobbyJoin(event.getPlayer());
	}
}
