package me.cayve.ludorium.games.utils;

import java.util.ArrayList;
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
import org.bukkit.inventory.ItemStack;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import me.cayve.ludorium.games.utils.PlayerProfileManager.PlayerProfile;
import me.cayve.ludorium.games.utils.PlayerProfileManager.ProfileComponent;
import me.cayve.ludorium.main.LudoriumPlugin;

public class PlayerInventoryManager implements Listener {
	
	/**
	 * Class storing the attributes of a player inventory state
	 */
	public static class InventoryState implements ProfileComponent {
		private final ArrayList<String> viewers = new ArrayList<>();
		
		private ItemStack[] contents = new ItemStack[41];
		
		private float exp = 0;
		private int level = 0;
		private int foodLevel = 20;
		private double health = 20;
		private float saturation = 20;
		
		//Whether external forces can change the game profile. (i.e. picking up items)
		//the events prohibiting external forces are lowest priority, so overwriting is possible
		private final boolean allowExternalAlteration;
		
		public InventoryState(boolean allowExternalAlteration) {
			this.allowExternalAlteration = allowExternalAlteration;
		}
		
		public InventoryState(Player player, boolean allowExternalAlteration) {
			this.allowExternalAlteration = allowExternalAlteration;
			
			//Save the personal state
			this.contents 	= player.getInventory().getContents();
			this.exp 		= player.getExp();
			this.level 		= player.getLevel();
			this.foodLevel 	= player.getFoodLevel();
			this.saturation = player.getSaturation();
			this.health 	= player.getHealth();
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
					refreshViewers();
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
				if (contents[i] != null && contents[i].equals(item))
					contents[i] = null;
			refreshViewers();
		}
		
		public void setItem(int index, ItemStack item) { contents[index] = item; 	refreshViewers(); }
		public void setArmor(int index, ItemStack item) { contents[index + 36] = item; refreshViewers(); }
		public void setBoots(ItemStack item) 		{ setArmor(0, item); }
		public void setLeggings(ItemStack item) 	{ setArmor(1, item); }
		public void setChestplate(ItemStack item) 	{ setArmor(2, item); }
		public void setHelmet(ItemStack item) 		{ setArmor(3, item); }
		public void setExp(float exp) 				{ this.exp = exp; 				refreshViewers(); }
		public void setLevel(int level) 			{ this.level = level; 			refreshViewers(); }
		public void setHealth(double health) 		{ this.health = health; 		refreshViewers(); }
		public void setSaturation(float saturation) { this.saturation = saturation; refreshViewers(); }
		
		@Override public void subscribe(String playerID) {
			viewers.add(playerID);
			setPlayerState(playerID, this); 
		}
		@Override public void unsubscribe(String playerID) { 
			viewers.remove(playerID);
			setPlayerState(playerID, new InventoryState(false)); 
		} 
		
		private void refreshViewers() {
			for (String viewer : viewers)
				setPlayerState(viewer, this);
		}
		
		/**
		 * Restores a given player state for a player, if they're online
		 * @param playerID
		 * @param state
		 */
		private static void setPlayerState(String playerID, InventoryState state) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerID));
			
			if (!player.isOnline()) return;
			
			//Before changing a player's state, verify their personal state is saved.
			//This is done at the last minute (right before the state is actually applied)
			//because the player may be disconnected while game states are being created,
			//so their personal state cannot be accessed. I don't love InventoryState
			//itself handling profile states, but I think it's the safest way to do this
			//that allows games to stay autonomous without worrying about player specific issues
			//(like manipulating their displayed inventory while they may be offline)
			verifyPersonalState(player.getPlayer());
			
			player.getPlayer().getInventory().setContents(state.contents);
			player.getPlayer().setExp		(state.exp);
			player.getPlayer().setLevel		(state.level);
			player.getPlayer().setFoodLevel	(state.foodLevel);
			player.getPlayer().setSaturation(state.saturation);
			player.getPlayer().setHealth	(state.health);
		}
		
		private static void verifyPersonalState(Player player) {
			PlayerProfile profile = PlayerProfileManager.getPlayerProfile(player.getUniqueId().toString());
			
			//If the profile doesn't contain a personal state, add it before anything else
			if (profile.getPersonalOfType(InventoryState.class) == null)
				profile.addPersonalComponent(new InventoryState(player, true));
		}
	}
	
	//Instantiated to allow for EventHandler
	private static PlayerInventoryManager instance;

	public static void initialize() {
		if (instance == null)
		{
			instance = new PlayerInventoryManager();
			LudoriumPlugin.registerEvent(instance);
		}
	}
	
	public static void uninitialize() {
		HandlerList.unregisterAll(instance);
	}

	private boolean canEventAlter(String playerID) { 
		PlayerProfile profile = PlayerProfileManager.getPlayerProfile(playerID, false);
		
		if (profile == null || profile.getActiveOfType(InventoryState.class) == null)
			return true;
		
		return profile.getActiveOfType(InventoryState.class).allowExternalAlteration;
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
