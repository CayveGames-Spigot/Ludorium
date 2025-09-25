package me.cayve.ludorium.games.utils;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.joml.Vector2f;
import org.joml.Vector3f;

import me.cayve.ludorium.LudoriumPlugin;
import me.cayve.ludorium.games.utils.PlayerInventoryManager.InventoryState;
import me.cayve.ludorium.utils.Collider;
import me.cayve.ludorium.utils.Rigidbody;
import me.cayve.ludorium.utils.SourceKey;
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
	private static final float RAN_VEL_RANGE = .25f;
	private SourceKey gameKey;
	private int diceCount;
	
	private String currentPlayer;
	private int currentDiceCount;
	private Consumer<Integer[]> rollCallback;
	
	private ArrayList<ItemEntity> activeRolls = new ArrayList<>();
	
	public GameDie(SourceKey gameKey, int diceCount) {
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
		removeItemFromPlayer();
		destroyActiveRolls();
		
		this.currentPlayer = playerID;
		this.rollCallback = callback;
		this.currentDiceCount = overrideDiceCount;

		PlayerProfileManager.getPlayerProfile(playerID).getComponentOfTypeFrom(InventoryState.class, gameKey)
			.addItem(CustomModel.get(MODEL_ID).asQuantity(currentDiceCount));
	}
	
	public void forceRoll() {
		if (currentPlayer == null) return;
		
		if (activeRolls.isEmpty()) {
			
			OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(currentPlayer));
			
			if (!player.isOnline())
				attemptRollCalculation(false);
			else //Calls the drop event directly in order to recreate natural player drop velocity
				dropDice(((Player)player).dropItem(CustomModel.get(MODEL_ID).asQuantity(currentDiceCount)));
				
		} else
			attemptRollCalculation(false);
	}
	
	public void destroy() {
		HandlerList.unregisterAll(this);
		destroyActiveRolls();
	}
	
	private void destroyActiveRolls() {
		for (ItemEntity roll : activeRolls)
			roll.destroy();
		
		activeRolls.clear();
	}
	
	private void removeItemFromPlayer() {
		if (currentPlayer == null) return;
		
		PlayerProfileManager.getPlayerProfile(currentPlayer).getComponentOfTypeFrom(InventoryState.class, gameKey)
			.removeItem(CustomModel.get(MODEL_ID).asQuantity(currentDiceCount));
	}
	
	private void attemptRollCalculation(boolean waitForRest) {
		//If a dice isn't done rolling, return
		for (ItemEntity dice : activeRolls) {
			if (waitForRest && dice.getComponent(Rigidbody.class).isEnabled())
				return;
		}
		
		//Calculate the roll values
		Integer[] results = new Integer[currentDiceCount];
		
		for (int i = 0; i < currentDiceCount; i++)
			results[i] = new Random().nextInt(6) + 1;
		
		//Reset the current roll and submit the results
		removeItemFromPlayer();
		destroyActiveRolls();
		rollCallback.accept(results);
		
		currentPlayer = null;
		rollCallback = null;
	}
	
	@EventHandler
	private void onItemDrop(PlayerDropItemEvent event) {
		if (currentPlayer == null || 
				!event.getPlayer().getUniqueId().equals(UUID.fromString(currentPlayer)) ||
				!CustomModel.is(event.getItemDrop().getItemStack(), MODEL_ID)) 
			return;
		
		dropDice(event.getItemDrop());
		event.setCancelled(false); //Override lower priorities
	}
	
	private void dropDice(Item item) {
		removeItemFromPlayer();
		
		for (int i = 0; i < currentDiceCount; i++) {
			ItemEntity itemDisplay = new ItemEntity(item.getLocation(), CustomModel.get(MODEL_ID),
					entity -> new Collider(entity.getPositionTransform(), new Vector2f(.4f, .4f)),
					entity -> new Rigidbody(entity.getPositionTransform(), entity.getComponent(Collider.class)));

			Rigidbody rb = itemDisplay.getComponent(Rigidbody.class);
			
			Vector3f velocity = item.getVelocity().toVector3f();
			if (i != 0)
				velocity.add(
						new Random().nextFloat(-RAN_VEL_RANGE, RAN_VEL_RANGE), 
						new Random().nextFloat(-RAN_VEL_RANGE, RAN_VEL_RANGE), 
						new Random().nextFloat(-RAN_VEL_RANGE, RAN_VEL_RANGE));
			
			rb.setVelocity(velocity);
			rb.disableOnRest();
			rb.onRested().subscribe(() -> attemptRollCalculation(true));
			
			activeRolls.add(itemDisplay);
		}
		
		item.remove();
	}
}
