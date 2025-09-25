package me.cayve.ludorium.games.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;

import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.ArrayListUtils;
import me.cayve.ludorium.utils.SourceKey;

public class PlayerProfileManager implements Listener {

	public interface ProfileComponent {
		public void subscribe(String listenerID);
		public void unsubscribe(String listenerID);
	}
	
	public static class PlayerProfile {
		private final String playerID;
		
		//Represents each unique profile a player can have
		//The source key is where each component originates from
		//Each component represents each part of that specific profile that should be viewed (i.g. inventory, messages, etc)
		private final Map<SourceKey, ArrayList<ProfileComponent>> uniqueProfiles = new HashMap<>();
		//Represents any profile states that are unique to the player's personal profile (i.g. the player's original inventory)
		private ArrayList<ProfileComponent> personalProfiles = new ArrayList<>();
		
		private final ArrayList<SourceKey> keyIndexes = new ArrayList<>();
		private int currentIndex;
		
		public PlayerProfile(String playerID) {
			this.playerID = playerID;
		}
		
		/**
		 * Returns the first component of a specified type that is currently active on a player
		 * @param <T>
		 * @param type
		 * @return
		 */
		public <T extends ProfileComponent> T getActiveOfType(Class<T> type) {
			return getComponentOfTypeFrom(type, keyIndexes.get(currentIndex));
		}
		
		/**
		 * Returns the first component of a specified type that is currently active on a player
		 * @param <T>
		 * @param type
		 * @return
		 */
		public <T extends ProfileComponent> T getComponentOfTypeFrom(Class<T> type, SourceKey source) {
			for (ProfileComponent component : uniqueProfiles.get(source))
				if (component.getClass().isAssignableFrom(type))
					return type.cast(component);
			return null;
		}
		
		/**
		 * Returns the first personal component of a specified type that on the player
		 * @param <T>
		 * @param type
		 * @return
		 */
		public <T extends ProfileComponent> T getPersonalOfType(Class<T> type) {
			for (ProfileComponent component : personalProfiles)
				if (component.getClass().isAssignableFrom(type))
					return type.cast(component);
			return null;
		}
		
		/**
		 * Add a component that is personal to the player (i.g. their original inventory state)<p>
		 * Personal components will be restored when no unique profiles are present, or when the player disconnects.
		 * However, when the player reconnects, their selected profile will be re-applied (if it still exists)
		 * @param component
		 */
		public void addPersonalComponent(ProfileComponent component) {
			personalProfiles.add(component);
		}
		
		public void addProfileComponent(SourceKey profileKey, ProfileComponent component) {
			uniqueProfiles.computeIfAbsent(profileKey, x -> new ArrayList<ProfileComponent>());
			
			uniqueProfiles.get(profileKey).add(component);
			if (!keyIndexes.contains(profileKey))
				keyIndexes.add(profileKey);
			
			if (currentIndex == -1)
				shiftCurrentIndex(1, false);
			else if (currentIndex == keyIndexes.indexOf(profileKey))
				shiftCurrentIndex(0, false);
		}
		
		public void removeProfile(SourceKey profileKey) {
			if (!uniqueProfiles.containsKey(profileKey))
				return;
			
			//If the profile is what's current, unsubscribe from it before we remove it
			if (keyIndexes.indexOf(profileKey) == currentIndex)
				forEachComponentAt(currentIndex, x -> x.unsubscribe(playerID));

			keyIndexes.remove(profileKey);
			uniqueProfiles.remove(profileKey);
			
			if (uniqueProfiles.isEmpty())
				deletePlayerProfile(playerID);
			else
				shiftCurrentIndex(0, false);
		}
		
		private void restorePersonal() {
			forEachComponentAt(currentIndex, x -> x.unsubscribe(playerID));
			
			for (ProfileComponent component : personalProfiles)
				component.subscribe(playerID);
		}
		
		private void restoreCurrentIndex() {
			for (ProfileComponent component : personalProfiles)
				component.unsubscribe(playerID);
			
			forEachComponentAt(currentIndex, x -> x.subscribe(playerID));
		}
		
		private void forEachComponentAt(int index, Consumer<ProfileComponent> action) {
			if (index == -1 || index >= keyIndexes.size())
				return;
			
			for (ProfileComponent component : uniqueProfiles.get(keyIndexes.get(index)))
				action.accept(component);
		}
		
		private void shiftCurrentIndex(int shiftBy, boolean unsubscribeCurrent) {
			int previousIndex = currentIndex;
			currentIndex = Math.floorMod(currentIndex + shiftBy, keyIndexes.size());
			
			if (unsubscribeCurrent)
				forEachComponentAt(previousIndex, x -> x.unsubscribe(playerID));
			forEachComponentAt(currentIndex, x -> x.subscribe(playerID));
		}
	}
	
	private static PlayerProfileManager instance;
	
	private static final ArrayList<PlayerProfile> playerProfiles = new ArrayList<>();
	
	public static void initialize() {
		if (instance == null)
		{
			instance = new PlayerProfileManager();
			LudoriumPlugin.registerEvent(instance);
		}
	}
	
	public static void uninitialize() {
		HandlerList.unregisterAll(instance);
		
		ArrayList<PlayerProfile> temp = new ArrayList<>(playerProfiles);
		for (PlayerProfile profile : temp)
			LudoriumPlugin.callSafely(() -> deletePlayerProfile(profile.playerID));
	}
	
	public static void deletePlayerProfile(String playerID) {
		ArrayListUtils.runIfFound(playerProfiles, x -> x.playerID.equals(playerID), profile -> {
			profile.restorePersonal();
			playerProfiles.remove(profile);
		});
	}
	
	public static PlayerProfile getPlayerProfile(String playerID) { return getPlayerProfile(playerID, true); }
	
	public static PlayerProfile getPlayerProfile(String playerID, boolean createIfMissing) {
		PlayerProfile profile = ArrayListUtils.find(playerProfiles, x -> x.playerID.equals(playerID));
		
		if (profile == null && createIfMissing)
		{
			profile = new PlayerProfile(playerID);
			playerProfiles.add(profile);
		}
		
		return profile;
	}
	
	@EventHandler
	private void onPlayerJump(PlayerJumpEvent event) {
		ArrayListUtils.runIfFound(playerProfiles, 
				x -> x.playerID.equals(event.getPlayer().getUniqueId().toString()), 
				x -> x.shiftCurrentIndex(1, true));
	}
	
	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event) {
		ArrayListUtils.runIfFound(playerProfiles, 
				x -> x.playerID.equals(event.getPlayer().getUniqueId().toString()), 
				x -> x.restoreCurrentIndex());
	}
	
	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent event) {
		ArrayListUtils.runIfFound(playerProfiles, 
				x -> x.playerID.equals(event.getPlayer().getUniqueId().toString()), 
				x -> x.restorePersonal());
	}
}
