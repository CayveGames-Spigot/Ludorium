package me.cayve.ludorium.games.events;

public class DiceRollEvent extends InstanceEvent {

	private int rollValue;
	
	public DiceRollEvent(int playerTurn, int rollValue) {
		super(playerTurn);
		this.rollValue = rollValue;
	}
	
	public int getRollValue() { return rollValue; }
}
