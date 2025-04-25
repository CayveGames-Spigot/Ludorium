package me.cayve.ludorium.games.settings;

import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import me.cayve.ludorium.games.lobbies.GameLobby;

public class InWorldSettings extends GameSettings implements Listener {

	public InWorldSettings(Setting[] settings, GameLobby lobby) {
		super(settings, lobby);
	}
	
	@EventHandler
	private void onInteraction(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Interaction)
				|| !lobby.hasPlayer(event.getPlayer().getUniqueId())) return;
		
		//Change setting
	}
}
