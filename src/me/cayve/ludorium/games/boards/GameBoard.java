package me.cayve.ludorium.games.boards;

import me.cayve.ludorium.games.lobbies.GameLobby;

public abstract class GameBoard {
	
	protected GameLobby lobby;
	protected String boardName;
	//private GameInstance gameInstance;
	
	public GameBoard(String name) {
		boardName = name;
	}
	
	protected abstract void generateLobby();
	
	public String getName() { return boardName; }
	
	public void destroy() {
		lobby.destroy();
	}
}
