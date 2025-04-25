package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import me.cayve.ludorium.games.GameBase;

public abstract class GameLobby {
	
	protected GameBase game;
	private ArrayList<UUID> players;
	private int minimum, maximum; //Max and min player count
	
	private ArrayList<Consumer<Player>> lobbyJoinEvent;
	private ArrayList<Consumer<Player>> lobbyLeaveEvent;
	
	public GameLobby(GameBase game, int minimum, int maximum) {
		this.game = game;
		this.minimum = minimum;
		this.maximum = maximum;
		
		players = new ArrayList<UUID>();
		lobbyJoinEvent = new ArrayList<Consumer<Player>>();
		lobbyLeaveEvent = new ArrayList<Consumer<Player>>();
	}
	
	public void attemptLobbyJoin(Player player) {
		//Find next available index and join it
	}
	
	public void attemptLobbyJoin(Player player, int playerIndex) {
		//Attempt to join at index
	}
	
	public void attemptLobbyLeave(Player player) {
		
	}
	
	public void registerJoinListener(Consumer<Player> listener) {
		if (lobbyJoinEvent.contains(listener)) return;
		
		lobbyJoinEvent.add(listener);
	}
	
	public void unregisterJoinListener(Consumer<Player> listener) {
		if (!lobbyJoinEvent.contains(listener)) return;
		
		lobbyJoinEvent.remove(listener);
	}
	
	public void registerLeaveListener(Consumer<Player> listener) {
		if (lobbyLeaveEvent.contains(listener)) return;
		
		lobbyLeaveEvent.add(listener);
	}
	
	public void unregisterLeaveListener(Consumer<Player> listener) {
		if (!lobbyLeaveEvent.contains(listener)) return;
		
		lobbyLeaveEvent.remove(listener);
	}

	
	public int getPlayerMax() { return maximum; }
	public int getPlayerMin() { return minimum; }
	public int getPlayerCount() { return players.size(); }
	public UUID getHost() {
		if (players.size() == 0) return null;
		return players.get(0);
	}
	public boolean hasPlayer(UUID player) { return players.contains(player); }
}
