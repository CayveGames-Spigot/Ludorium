package me.cayve.ludorium.games.events;

public class TurnChangeEvent extends InstanceEvent {

	private int previousTurn;
	private int turnChangeReason;
	
	public TurnChangeEvent(int playerTurn, int previousTurn, int turnChangeReason) {
		super(playerTurn);
		this.previousTurn = previousTurn;
		this.turnChangeReason = turnChangeReason;
	}

	public int getPreviousTurn() { return previousTurn; }
	public int getTurnChangeReason() { return turnChangeReason; }
}
