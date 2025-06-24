package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

public abstract class GameLobby {
	
	private boolean isEnabled;
	
	private ArrayList<UUID> players;
	private int minimum, maximum; //Max and min player count
	
	private ArrayList<Consumer<Integer>> lobbyJoinEvent;
	private ArrayList<Consumer<Integer>> lobbyLeaveEvent;
	
	public GameLobby(int minimum, int maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
		
		players = new ArrayList<UUID>();
		lobbyJoinEvent = new ArrayList<Consumer<Integer>>();
		lobbyLeaveEvent = new ArrayList<Consumer<Integer>>();
	}
	
	/**
	 * Enables the lobby to allow for joining
	 */
	public void enable() {
		isEnabled = true;
	}
	
	/**
	 * Disables the lobby, disallowing joining
	 */
	public void disable() {
		isEnabled = false;
	}
	public boolean isEnabled() { return isEnabled; }
	
	public void attemptLobbyJoin(Player player) {
		//Find next available index and join it
	}
	
	public void attemptLobbyJoin(Player player, int playerIndex) {
		//Attempt to join at index
	}
	
	public void attemptLobbyLeave(Player player) {
		
	}
	
	public void registerJoinListener(Consumer<Integer> listener) {
		lobbyJoinEvent.add(listener);
	}
	
	public void registerLeaveListener(Consumer<Integer> listener) {
		lobbyLeaveEvent.add(listener);
	}

	public int getPlayerMax() { return maximum; }
	public int getPlayerMin() { return minimum; }
	public int getPlayerCount() { return players.size(); }
	public UUID getHost() {
		if (players.size() == 0) return null;
		return players.get(0);
	}
	public boolean hasPlayer(UUID player) { return players.contains(player); }
	
	public void destroy() {}
}
