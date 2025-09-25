package me.cayve.ludorium.games.events;

public class TokenSelectionEvent extends InstanceEvent {

	private String[] selectedTokens;
	private Integer[] targetedTiles;
	
	public TokenSelectionEvent(int playerTurn, String[] selectedTokens, Integer[] targetedTiles) {
		super(playerTurn);
		this.selectedTokens = selectedTokens;
		this.targetedTiles = targetedTiles;
	}
	
	public String[] getSelectedTokens() { return selectedTokens; }
	public Integer[] getTargetedTiles() { return targetedTiles; }
}
