package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class GameLobby {
	
	private boolean isEnabled;
	
	private ArrayList<String> players = new ArrayList<>();
	
	private int minimum, maximum; //Max and min player count
	
	private ArrayList<Consumer<Integer>> lobbyJoinEvent = new ArrayList<>();
	private ArrayList<Consumer<Integer>> lobbyLeaveEvent = new ArrayList<>();
	
	private ArrayList<Runnable> countdownCompleteEvent = new ArrayList<>();
	
	public GameLobby(int minimum, int maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
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
	
	public void attemptLobbyJoin(String playerID) {
		//Find next available index and join it
	}
	
	public void attemptLobbyJoin(String playerID, int playerIndex) {
		//Attempt to join at index
	}
	
	public void attemptLobbyLeave(String playerID) {
		
	}
	
	public void registerJoinListener(Consumer<Integer> listener) { lobbyJoinEvent.add(listener); }
	public void registerLeaveListener(Consumer<Integer> listener) { lobbyLeaveEvent.add(listener); }
	
	/**
	 * Called when the lobby countdown is completed
	 * @param listener
	 */
	public void registerCountdownComplete(Runnable listener) { countdownCompleteEvent.add(listener); }

	public int getPlayerMax() { return maximum; }
	public int getPlayerMin() { return minimum; }
	public int getPlayerCount() { return players.size(); }
	public String getPlayerAt(int index) { return players.get(index); }
	public String getHost() {
		if (players.size() == 0) return null;
		return players.get(0);
	}
	/**
	 * @return A sorted array of all lobby indexes that players are occupying
	 */
	public ArrayList<Integer> getActiveIndexes() {
		return null;
	}
	
	public boolean hasPlayer(String playerID) { return players.contains(playerID); }
	
	public void destroy() {}
}
