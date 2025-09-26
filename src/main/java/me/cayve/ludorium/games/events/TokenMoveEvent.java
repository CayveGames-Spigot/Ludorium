package me.cayve.ludorium.games.events;

public class TokenMoveEvent extends InstanceEvent {

	public enum eAction { MOVE, CAPTURE, BLUNDER }
	
	private final String tokenID;
	private final Integer[] tokenPath;
	private final eAction action;
	
	public TokenMoveEvent(int playerTurn, String tokenID, eAction action, int originIndex, int destinationIndex) {
		super(playerTurn);
		this.action = action;
		this.tokenID = tokenID;
		this.tokenPath = new Integer[] { originIndex, destinationIndex };
	}
	
	public TokenMoveEvent(int playerTurn, String tokenID, eAction action, Integer[] path) {
		super(playerTurn);
		this.action = action;
		this.tokenID = tokenID;
		this.tokenPath = path;
	}
	
	public String getTokenID() { return tokenID; }
	public int getOriginIndex() { return tokenPath[0]; }
	public int getDestinationIndex() { return tokenPath[tokenPath.length - 1]; }
	public Integer[] getPath() { return tokenPath; }
	public eAction getAction() { return action; }
}
