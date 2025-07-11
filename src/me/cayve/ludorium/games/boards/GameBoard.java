package me.cayve.ludorium.games.boards;

import java.util.HashMap;
import java.util.UUID;

import me.cayve.ludorium.games.lobbies.GameLobby;

public abstract class GameBoard {
	
	protected GameLobby lobby;
	protected String boardName;
	protected String uniqueID = UUID.randomUUID().toString();
	
	private HashMap<String, Integer> missedTurns = new HashMap<>();
	private int missedTurnsAllowance = 2;
	//private GameInstance gameInstance;
	
	public GameBoard(String name) {
		boardName = name;
	}
	
	protected void generateLobby() {
		if (lobby == null)
			return;
		
		lobby.registerCountdownComplete(this::startGame);
		lobby.enable();
	}
	
	public String getName() { return boardName; }
	
	public void destroy() {
		lobby.destroy();
	}
	
	protected abstract void startGame();
	protected abstract void endGame();
	
	/**
	 * Indicate that the player missed their turn. 
	 * If the player misses more in a row than the lobby's allowance,
	 * they will be kicked from the game
	 * @param player
	 */
	protected void missedTurn(String playerID) {
		if (!missedTurns.containsKey(playerID))
			missedTurns.put(playerID, 0);
		
		missedTurns.put(playerID, missedTurns.get(playerID) + 1);
		
		if (missedTurns.get(playerID) >= missedTurnsAllowance)
			lobby.attemptLobbyLeave(playerID);
	}
	
	/**
	 * Indicate that the player acted on their turn.
	 * If the player missed any turns before, their
	 * record is cleared.
	 * @param player
	 */
	protected void playedTurn(String playerID) {
		if (missedTurns.containsKey(playerID))
			missedTurns.remove(playerID);
	}
	
	/**
	 * Sets the allowance for how many turns a player can miss before being kicked from the lobby
	 * @param allowance
	 */
	protected void setMissedTurnAllowance(int allowance) { missedTurnsAllowance = allowance; }
	
}
