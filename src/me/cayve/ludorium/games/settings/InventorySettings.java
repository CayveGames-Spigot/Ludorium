package me.cayve.ludorium.games.settings;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import me.cayve.ludorium.games.lobbies.GameLobby;

public class InventorySettings extends GameSettings implements Listener {

	private Inventory inventory;
	
	public InventorySettings(Setting[] settings, GameLobby lobby) {
		super(settings, lobby);
		
		//Create inventory
		//lobby.registerJoinListener(this::onLobbyJoin);
		//lobby.registerLeaveListener(this::onLobbyLeave);
		onLobbyJoin(null);
		onLobbyLeave(null);
	}
	
	public void openSettings(Player player) { player.openInventory(inventory); }
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event) {
		if (!inventory.equals(event.getInventory())
				|| !lobby.getHost().equals(event.getWhoClicked().getUniqueId())) return;
		
		//Update settings
		//Start game if possible
	}
	
	@EventHandler
	private void onInventoryClose(InventoryCloseEvent event) {
		if (!inventory.equals(event.getInventory())
				|| !(event.getPlayer() instanceof Player)) return;
		
		lobby.attemptLobbyLeave(event.getPlayer().getUniqueId().toString());
	}
	
	private void onLobbyJoin(Player player) 
	{
		//Update player list
	}
	private void onLobbyLeave(Player player) 
	{
		//Update player list
	}
}
