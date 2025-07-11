package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import me.cayve.ludorium.main.LudoriumPlugin;

public class PlayerStateManager implements Listener {
	
	/**
	 * Class storing the attribute profile of a player
	 * If one exists, they are playing a game that depends on it
	 */
	private static class PlayerProfile {
		private PlayerState personalState;
		private HashMap<String, PlayerState> gameStates = new HashMap<>();
		
		//This is to keep track of which state is currently active
		private ArrayList<String> gameKeys = new ArrayList<>();
		private int activeIndex;
		
		private void addAdditionalState(String gameKey, PlayerState gameState) {
			if (gameStates.containsKey(gameKey))
				return;
			
			gameStates.put(gameKey, gameState);
			gameKeys.add(gameKey);
		}
		
		private void removeState(String gameKey) {
			gameKeys.remove(gameKey);
			gameStates.remove(gameKey);
		}
		
		private PlayerProfile(PlayerState personalState, String gameKey, PlayerState gameState) {
			this.personalState = personalState;
			
			addAdditionalState(gameKey, gameState);
		}
		
		private PlayerState getActiveGameState() {
			if (gameKeys.size() == 0)
				return null;
			
			activeIndex = Math.floorMod(activeIndex, gameKeys.size());
			
			return gameStates.get(gameKeys.get(activeIndex));
		}
	}
	
	/**
	 * Class storing the attributes of a player state
	 */
	public static class PlayerState {
		private ItemStack[] contents = new ItemStack[41];
		
		private float exp = 0;
		private int level = 0;
		private int foodLevel = 20;
		private double health = 20;
		private float saturation = 20;
		
		//Whether external forces can change the game profile. (i.e. picking up items)
		//the events prohibiting external forces are lowest priority, so overwriting is possible
		private boolean allowExternalAlteration = false;
		
		public PlayerState(boolean allowExternalAlteration) {
			this.allowExternalAlteration = allowExternalAlteration;
		}
		
		/**
		 * Adds the given item to the first available slot
		 * @param item
		 * @return Whether the addition was successful
		 */
		public boolean addItem(ItemStack item) {
			for (int i = 0; i < contents.length; i++) {
				if (contents[i] == null) {
					contents[i] = item;
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Removes the given item from the inventory
		 * @param item
		 */
		public void removeItem(ItemStack item) {
			for (int i = 0; i < contents.length; i++)
				if (contents[i].equals(item))
					contents[i] = null;
		}
		
		public void setItem(int index, ItemStack item) { contents[index] = item; }
		public void setArmor(int index, ItemStack item) { contents[index + 36] = item; }
		public void setBoots(ItemStack item) { setArmor(0, item); }
		public void setLeggings(ItemStack item) { setArmor(1, item); }
		public void setChestplate(ItemStack item) { setArmor(2, item); }
		public void setHelmet(ItemStack item) { setArmor(3, item); }
		public void setExp(float exp) { this.exp = exp; }
		public void setLevel(int level) { this.level = level; }
		public void setHealth(double health) { this.health = health; }
		public void setSaturation(float saturation) { this.saturation = saturation; }
	}
	
	//Singleton
	private static PlayerStateManager instance;
	
	private HashMap<String, PlayerProfile> playerProfiles = new HashMap<>();
	
	public PlayerStateManager() {
		LudoriumPlugin.registerEvent(this);
	}
	
	public static void initialize() {
		if (instance == null)
			instance = new PlayerStateManager();
	}
	
	public static void uninitialize() {
		instance.destroy();
	}
	
	public void destroy() {
		HandlerList.unregisterAll(this);
		
		for (String playerID : playerProfiles.keySet())
		{
			//If any inventory restore fails, all others don't
			try {
				
				deleteProfile(playerID);
			
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the game profile state of the given player
	 * @param playerID
	 * @param gameKey the key of the gameState to retrieve
	 * @return
	 */
	public static PlayerState getGameState(String playerID, String gameKey) 
				{ return instance.playerProfiles.get(playerID).gameStates.get(gameKey); }
	
	/**
	 * Removes a game state from a player's profile. If their profile empties,
	 * their original inventory is restored
	 * @param playerID
	 * @param gameKey
	 */
	public static void removeGameState(String playerID, String gameKey) {
		if (!instance.playerProfiles.containsKey(playerID))
			return;
		
		instance.playerProfiles.get(playerID).removeState(gameKey);
		
		PlayerState refreshTo = instance.playerProfiles.get(playerID).getActiveGameState();
		
		//If there's no more game states in the players profile, delete it and restore
		if (refreshTo == null)
			deleteProfile(playerID);
		else
			restorePlayerState(playerID, refreshTo);
	}

	/**
	 * Creates a new player state profile for the given player. If a profile already exists,
	 * the gameKey is simply added to their list of game states
	 * @param player The player to create a new profile for. Must be online.
	 * @param gameKey The key for the initial game state
	 * @param allowExternalAlterations Whether external forces can change the game profile. (i.e. picking up items)
	 * The events prohibiting external forces are lowest priority, so overwriting is possible
	 */
	public static void createProfile(Player player, String gameKey, boolean allowExternalAlterations) {
		String playerID = player.getUniqueId().toString();
		
		PlayerState newGameState = new PlayerState(allowExternalAlterations);
		
		//If no profile exists, make one and populate it
		if (!instance.playerProfiles.containsKey(playerID))
			instance.playerProfiles.put(playerID, new PlayerProfile(storePersonalPlayerState(player), gameKey, newGameState));
		else 
		{ //If there already exists a profile for this player, add the new game state to the list and update the displayed state
			if (instance.playerProfiles.get(playerID).gameStates.containsKey(gameKey))
				return;
			
			instance.playerProfiles.get(playerID).addAdditionalState(gameKey, newGameState);
		}
		
		restorePlayerState(player, newGameState);
	}
	
	/**
	 * Delete a player's entire state profile and restore their inventory
	 * @param playerID
	 */
	public static void deleteProfile(String playerID) {
		if (!instance.playerProfiles.containsKey(playerID))
			return;
		
		restorePlayerState(playerID, instance.playerProfiles.get(playerID).personalState);
		
		instance.playerProfiles.remove(playerID);
	}
	
	/**
	 * Returns the personal player state of the given player
	 * Type 'Player' because they're required to be online
	 * @param player
	 */
	private static PlayerState storePersonalPlayerState(Player player) {
		PlayerState personalState = new PlayerState(false);
		
		//Save the personal state
		personalState.contents 			= player.getInventory().getContents();
		personalState.exp 				= player.getExp();
		personalState.level 			= player.getLevel();
		personalState.foodLevel 		= player.getFoodLevel();
		personalState.saturation 		= player.getSaturation();
		personalState.health 			= player.getHealth();
		
		return personalState;
	}
	
	/**
	 * Restores a given player state for a player, if they're online
	 * @param playerID
	 * @param state
	 */
	private static void restorePlayerState(String playerID, PlayerState state) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerID));
		
		if (player.isOnline())
			restorePlayerState(player.getPlayer(), state);
	}
	/**
	 * Restores a given player state for a player
	 * Type 'Player' because they're required to be online
	 * @param player
	 */
	private static void restorePlayerState(Player player, PlayerState state) {
		player.getInventory().setContents(state.contents);
		player.setExp		(state.exp);
		player.setLevel		(state.level);
		player.setFoodLevel	(state.foodLevel);
		player.setSaturation(state.saturation);
		player.setHealth	(state.health);
	}
	
	private boolean canEventAlter(String playerID) { 
		if (!playerProfiles.containsKey(playerID))
			return true;
		
		return playerProfiles.get(playerID).getActiveGameState().allowExternalAlteration;
	}
	
	@EventHandler
	private void onPlayerJump(PlayerJumpEvent event) {
		if (playerProfiles.containsKey(event.getPlayer().getUniqueId().toString()))
			playerProfiles.get(event.getPlayer().getUniqueId().toString()).activeIndex++;
	}
	
	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event) {
		if (playerProfiles.containsKey(event.getPlayer().getUniqueId().toString()))
			restorePlayerState(event.getPlayer(), playerProfiles.get(event.getPlayer().getUniqueId().toString()).getActiveGameState());
	}
	
	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent event) {
		if (playerProfiles.containsKey(event.getPlayer().getUniqueId().toString()))
			restorePlayerState(event.getPlayer(), playerProfiles.get(event.getPlayer().getUniqueId().toString()).personalState);
	}
	
	//Lowest priority allows other events to overwrite the event
	//like if for some reason a game needed a player to pick an item up
	@EventHandler(priority = EventPriority.LOWEST)
	private void onInventoryInteractEvent(InventoryInteractEvent event) {
		if (!canEventAlter(event.getWhoClicked().getUniqueId().toString()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
		if (!canEventAlter(event.getEntity().getUniqueId().toString()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPickupExperienceEvent(PlayerPickupExperienceEvent event) {
		if (!canEventAlter(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPickupItemEvent(EntityPickupItemEvent event) {
		if (!canEventAlter(event.getEntity().getUniqueId().toString()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onDropItemEvent(PlayerDropItemEvent event) {
		if (!canEventAlter(event.getPlayer().getUniqueId().toString()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onDamageEvent(EntityDamageEvent event) {
		if (!canEventAlter(event.getEntity().getUniqueId().toString()))
			event.setDamage(0);
	}
}
