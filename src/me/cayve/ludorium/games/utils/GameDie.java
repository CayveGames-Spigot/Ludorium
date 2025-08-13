package me.cayve.ludorium.games.utils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;

import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.entities.ItemEntity;

/**
 * @author Cayve
 * @license GPL v3
 * @repository https://github.com/CayveGames-Spigot/Ludorium
 * @created 6/24/2025
 * 
 * @description
 * Represents the dice used in a game instance
 */
public class GameDie implements Listener {

	private class ActiveRoll {
		private Item followItem;
		private ItemEntity display;
		
		private ActiveRoll(Item followItem, ItemEntity display) {
			this.followItem = followItem;
			this.display = display;
		}
	}
	private String gameKey;
	private int diceCount;
	
	private String currentPlayer;
	private int currentDiceCount;
	private Consumer<Integer[]> rollCallback;
	
	private ArrayList<ActiveRoll> activeRolls = new ArrayList<>();
	
	public GameDie(String gameKey, int diceCount) {
		this.gameKey = gameKey;
		this.diceCount = diceCount;
		
		LudoriumPlugin.registerEvent(this);
	}
	
	/**
	 * Gives the player dice to roll
	 * @param player The player to roll
	 * @param callback Callback containing array with roll results (with length of diceCount)
	 */
	public void playerRoll(String playerID, Consumer<Integer[]> callback) {
		playerRoll(playerID, diceCount, callback);
	}
	
	public void playerRoll(String playerID, int overrideDiceCount, Consumer<Integer[]> callback) {
		this.currentPlayer = playerID;
		this.rollCallback = callback;
		this.currentDiceCount = overrideDiceCount;
		destroyActiveRolls();
		PlayerStateManager.getGameState(playerID, gameKey).addItem(CustomModel.get("dice").asQuantity(currentDiceCount));
		PlayerStateManager.refreshPlayer(playerID);
	}
	
	public void forceRoll() {
		if (currentPlayer == null) return;
	}
	
	public void destroy() {
		HandlerList.unregisterAll(this);
		destroyActiveRolls();
	}
	
	private void destroyActiveRolls() {
		for (ActiveRoll roll : activeRolls) {
			roll.display.destroy();
			roll.followItem.remove();
		}
	}
	
	@EventHandler
	private void onItemDrop(PlayerDropItemEvent event) {
		if (currentPlayer == null || 
				!event.getPlayer().getUniqueId().equals(UUID.fromString(currentPlayer)) ||
				!CustomModel.is(event.getItemDrop().getItemStack(), "dice")) 
			return;
		
		for (int i = 0; i < currentDiceCount; i++) {
			Item newItem = (Item)event.getItemDrop().copy(event.getItemDrop().getLocation());
			newItem.setCanPlayerPickup(false);
			newItem.setCanMobPickup(false);
			
			ItemEntity itemDisplay = new ItemEntity(event.getItemDrop().getLocation(), CustomModel.get("dice"));
			itemDisplay.spawn();
			activeRolls.add(new ActiveRoll(newItem, itemDisplay));
		}
		
		event.getItemDrop().remove();
	}
}
