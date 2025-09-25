package me.cayve.ludorium.games.events;

import java.util.ArrayList;

public class TokenMoveEvent extends InstanceEvent {

	public enum eAction { MOVE, CAPTURE, BLUNDER }
	
	private String tokenID;
	private ArrayList<Integer> tokenPath = new ArrayList<>();
	private eAction action;
	
	public TokenMoveEvent(int playerTurn, String tokenID, eAction action, int originIndex, int destinationIndex) {
		super(playerTurn);
		this.action = action;
		this.tokenID = tokenID;
		tokenPath.add(originIndex);
		tokenPath.add(destinationIndex);
	}
	
	public TokenMoveEvent(int playerTurn, String tokenID, eAction action, ArrayList<Integer> path) {
		super(playerTurn);
		this.action = action;
		this.tokenID = tokenID;
		this.tokenPath = path;
	}
	
	public String getTokenID() { return tokenID; }
	public int getOriginIndex() { return tokenPath.getFirst(); }
	public int getDestinationIndex() { return tokenPath.getLast(); }
	public ArrayList<Integer> getPath() { return tokenPath; }
	public eAction getAction() { return action; }
}
