package me.cayve.ludorium.games.events;

public class GameFinishEvent extends InstanceEvent {

	public enum eGameState { IN_PROGRESS, NO_WINNER, DRAW, WINNER }
	
	private eGameState state;
	
	public GameFinishEvent(int playerTurn, eGameState state) {
		super(playerTurn);
		this.state = state;
	}

	public eGameState getGameState() { return state; }
}
