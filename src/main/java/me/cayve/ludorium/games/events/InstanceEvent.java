package me.cayve.ludorium.games.events;

public abstract class InstanceEvent {

	private int playerTurn;
	
	public InstanceEvent(int playerTurn) {
		this.playerTurn = playerTurn;
	}
	
	public int getPlayerTurn() { return playerTurn; }
}
