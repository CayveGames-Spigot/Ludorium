package me.cayve.ludorium.games.utils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.joml.Vector2f;

import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.Collider;
import me.cayve.ludorium.utils.Rigidbody;
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

	private static final String MODEL_ID = "dice";
	private String gameKey;
	private int diceCount;
	
	private String currentPlayer;
	private int currentDiceCount;
	private Consumer<Integer[]> rollCallback;
	
	private ArrayList<ItemEntity> activeRolls = new ArrayList<>();
	
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
		PlayerStateManager.getGameState(playerID, gameKey).addItem(CustomModel.get(MODEL_ID).asQuantity(currentDiceCount));
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
		for (ItemEntity roll : activeRolls)
			roll.destroy();
	}
	
	@EventHandler
	private void onItemDrop(PlayerDropItemEvent event) {
		if (currentPlayer == null || 
				!event.getPlayer().getUniqueId().equals(UUID.fromString(currentPlayer)) ||
				!CustomModel.is(event.getItemDrop().getItemStack(), MODEL_ID)) 
			return;
		
		for (int i = 0; i < currentDiceCount; i++) {
			ItemEntity itemDisplay = new ItemEntity(event.getItemDrop().getLocation(), CustomModel.get(MODEL_ID),
					entity -> new Collider(entity.getOriginTransform(), new Vector2f(1, 1)),
					entity -> new Rigidbody(entity.getOriginTransform(), entity.getComponent(Collider.class)));

			Rigidbody rb = itemDisplay.getComponent(Rigidbody.class);
			rb.setVelocity(event.getItemDrop().getVelocity().toVector3f());
			rb.disableOnRest();
			
			activeRolls.add(itemDisplay);
		}
		
		event.getItemDrop().remove();
	}
}
