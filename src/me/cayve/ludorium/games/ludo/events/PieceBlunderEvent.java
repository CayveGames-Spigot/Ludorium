package me.cayve.ludorium.games.ludo.events;

import me.cayve.ludorium.games.events.InstanceEvent;

public class PieceBlunderEvent extends InstanceEvent {

	private String blunderedPieceID;
	
	public PieceBlunderEvent(int playerTurn, String blunderedPieceID) {
		super(playerTurn);
		this.blunderedPieceID = blunderedPieceID;
	}
	
	public String getBlunderedPieceID() { return blunderedPieceID; }
}
