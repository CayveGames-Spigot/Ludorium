package me.cayve.ludorium.games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class LudoInstance extends GameInstance {

	//Game options
	private boolean isSixPlayers, safeSpaces, homeLineup, forceCapture;
	
	private ArrayList<Integer> activePlayerIndexes;
	private ArrayList<Integer> safeSpaceIndexes;
	
	//This corresponds to the index of activePlayerIndexes for the current player
	private int currentTurn = 0;
	private int currentRoll = -1;
	private int winnerPlayerIndex = -1;
	
	//Board states
	private String[] board; //Empty if no piece present, otherwise contains piece's UUID
	private String[][] homes;
	private String[][] starters;
	private ArrayList<String> completedPieces = new ArrayList<>();
	
	private Runnable onInstanceUpdate; //Called during any internal update
	private ArrayList<Consumer<String>> onForceCaptureBlunder = new ArrayList<>(); //PieceID that was blundered
	
	public LudoInstance(Runnable onInstanceUpdate, ArrayList<Integer> activePlayerIndexes, ArrayList<Integer> safeSpaceIndexes,
			int tileCount, boolean isSixPlayers, boolean safeSpaces, boolean homeLineup, boolean forceCapture) {
		
		this.onInstanceUpdate = onInstanceUpdate;
		
		this.isSixPlayers = isSixPlayers; //Only refers to the board size, not the actual player count
		this.safeSpaces = safeSpaces;
		this.homeLineup = homeLineup;
		this.forceCapture = forceCapture;
		
		//Fills board tiles with empty spaces
		board = new String[tileCount];
		Arrays.fill(board, "");
		
		//Fills home tiles with empty spaces
		homes = new String[isSixPlayers ? 6 : 4][4];
		for (String[] i : homes)
			Arrays.fill(i, "");
		
		//Fills starter tiles with empty spaces
		starters = new String[isSixPlayers ? 6 : 4][4];
		for (String[] i : starters)
			Arrays.fill(i, "");
		
		//Sorts the indexes so the turn order goes the same direction
		this.activePlayerIndexes = activePlayerIndexes;
		Collections.sort(this.activePlayerIndexes);
		
		this.safeSpaceIndexes = safeSpaceIndexes;
		
		//Fills in the active starting pieces
		for (int i = 0; i < this.activePlayerIndexes.size(); i++) {
			int index = this.activePlayerIndexes.get(i);
			for (int j = 0; j < 4; j++)
				starters[index][j] = index + "-" + j; //ID of pieces are PLAYERINDEX-PIECE#
		}
		
		onInstanceUpdate.run();
	}
	
	public void roll(int rollValue) {
		currentRoll = rollValue;
	}
	
	public void selectPiece(String pieceID) {
		if (currentRoll == -1 || !doesPieceMatchCurrentPlayer(pieceID))
			return;
		
		//Check the contents of the targeted tile
		String targetID = getPieceTarget(pieceID, false);
		
		//Verify the move is possible and that if its a capture, its not their own piece
		if (targetID == null || (!targetID.isEmpty() && doesPieceMatchCurrentPlayer(pieceID)))
			return;
		
		if (forceCapture && targetID.isEmpty())
			checkForceCapture(pieceID);
		
		//Move the piece (handles capturing)
		getPieceTarget(pieceID, true);
		
		checkForWinner();
		
		if (winnerPlayerIndex == -1)
			//Only go to the next turn if the last roll was not a 6
			nextTurn(currentRoll == 6 ? currentTurn : -1);
	}
	
	/**
	 * Removes a player index from the game
	 * @param playerIndex
	 */
	public void removePlayer(int playerIndex) {
		replacePiece(playerIndex + "", null);
		
		boolean isRemovedPlayersTurn = currentTurn == activePlayerIndexes.indexOf(playerIndex);
		
		activePlayerIndexes.remove(activePlayerIndexes.indexOf(playerIndex));
		
		if (isRemovedPlayersTurn)
			nextTurn(-1);
		else
			onInstanceUpdate.run(); //Run an update to refresh any valid positions
	}
	
	public String[] getBoardState() { return board; }
	public String[] getHomeState(int playerIndex) { return homes[playerIndex]; }
	public String[] getStarterState(int playerIndex) { return starters[playerIndex]; }
	public int getCurrentPlayerIndex() { return activePlayerIndexes.get(currentTurn); }
	public boolean canPieceMove(String pieceID) { return getPieceTarget(pieceID, false) != null; }
	
	/**
	 * Gets the player index of the winner. -1 if no winner yet
	 * @return
	 */
	public int getWinnerPlayerIndex() { return winnerPlayerIndex; }
	
	public void registerOnForceCaptureBlunderEvent(Consumer<String> listener) { onForceCaptureBlunder.add(listener); }
	
	private boolean doesPieceMatchCurrentPlayer(String pieceID) { return getPlayerIndexFromPiece(pieceID) == getCurrentPlayerIndex(); }
	private int getPlayerIndexFromPiece(String pieceID) { return Integer.valueOf(pieceID.charAt(0)); }
	private int getPieceIndexFromPiece(String pieceID) { return Integer.valueOf(pieceID.charAt(2)); }
	
	/**
	 * Finds the targeted piece (or empty tile) of the given piece
	 * based on the current roll. 
	 * @param pieceID The piece that is targeting
	 * @param movePiece Whether to actually move the piece to the new spot
	 * @return The targeted piece ID (empty if empty tile, null if invalid move)
	 */
	private String getPieceTarget(String pieceID, boolean movePiece) {
		//First, check if piece is still at start
		for (int i = 0; i < 4; i++) {
			if (!starters[getCurrentPlayerIndex()][i].equals(pieceID))
				continue;
			
			if (currentRoll != 6)
				return null;
			
			return moveToBoardTile(getStartTile(getCurrentPlayerIndex()), pieceID, movePiece);
		}
		
		//Second, check if piece is in home
		for (int i = 0; i < 4; i++)
			if (homes[getCurrentPlayerIndex()][i].equals(pieceID))
				return moveDownHomeStretch(i, currentRoll, pieceID, movePiece);
		
		//Otherwise, piece is on main board
		for (int i = 0; i < board.length; i++) {
			if (!board[i].equals(pieceID))
				continue;
			
			if (i + currentRoll > getEndTile(getCurrentPlayerIndex()))
				return moveDownHomeStretch(-1, currentRoll - (getEndTile(getCurrentPlayerIndex()) - i), pieceID, movePiece);
			
			return moveToBoardTile(i + currentRoll, pieceID, movePiece);
		}
		
		return null;
	}
	
	private String moveToBoardTile(int tileIndex, String pieceID, boolean movePiece) {
		if (!board[tileIndex].isEmpty() && doesPieceMatchCurrentPlayer(board[tileIndex]))
			return null;
		
		if (safeSpaces && safeSpaceIndexes.contains(tileIndex) && !doesPieceMatchCurrentPlayer(board[tileIndex]))
			return null;
		
		if (!movePiece)
			return board[tileIndex];
		
		if (!board[tileIndex].isEmpty())
			returnPieceToStart(board[tileIndex]);
		
		replacePiece(pieceID, null);
		board[tileIndex] = pieceID;
		return pieceID;
	}
	
	private String moveDownHomeStretch(int startingPos, int moveAmount, String pieceID, boolean movePiece) {
		//If the roll overshoots the last tile at all
		if (startingPos + moveAmount > 4)
			return null;
		
		//Check each tile until the target tile and verify
		//there's no piece in-between
		for (int j = 1; j <= moveAmount; j++) {
			//If no piece is in between
			//and the target is the final tile
			//and the game is no homeLineup
			if (startingPos + j == 4)
			{
				if (!homeLineup)
				{
					if (!movePiece)
						return "";
					
					completedPieces.add(pieceID);
					replacePiece(pieceID, null);
					return pieceID;
				}
				else
					return null;
			}
			
			//If any tile up until, and including, the target
			//is not empty, its not valid
			if (!homes[getCurrentPlayerIndex()][startingPos + j].isEmpty())
				return null;
		}
		
		if (!movePiece)
			return "";
		
		replacePiece(pieceID, null);
		homes[getCurrentPlayerIndex()][startingPos + moveAmount] = pieceID;
		return pieceID;
	}
	
	private int getStartTile(int playerIndex) { return (board.length / (isSixPlayers ? 6 : 4)) * playerIndex; }
	private int getEndTile(int playerIndex) { return Math.floorMod(getStartTile(playerIndex) - 1, board.length); }
	
	/**
	 * Replaces the piece's location with the given replacement.
	 * This does not place the replaced piece at a new location.
	 * The search uses startsWith logic, so player indexes can be used to 
	 * replace ALL of a player's pieces. (Since pieceIDs start with player index)
	 * @param pieceID The piece to replace
	 * @param replaceWith The piece to replace with (null to empty the tile)
	 */
	private void replacePiece(String pieceID, String replaceWith) {
		if (replaceWith == null)
			replaceWith = "";
		
		//Checks board tiles
		for (int i = 0; i < board.length; i++) {
			if (board[i].startsWith(pieceID))
				board[i] = replaceWith;
		}
		
		//Checks home and starter tiles
		for (int i = 0; i < activePlayerIndexes.size(); i++) {
			for (int j = 0; j < 4; j++) {
				if (homes[i][j].startsWith(pieceID))
					homes[i][j] = replaceWith;
				if (starters[i][j].startsWith(pieceID))
					starters[i][j] = replaceWith;
			}
		}
	}
	
	/**
	 * Returns the given piece to its starter
	 * @param pieceID 
	 */
	private void returnPieceToStart(String pieceID) {
		replacePiece(pieceID, null);
		
		int playerIndex = getPlayerIndexFromPiece(pieceID);
		for (int i = 0; i < 4; i++) {
			if (starters[playerIndex][i].isEmpty()) {
				starters[playerIndex][i] = pieceID;
				return;
			}
		}
	}
	
	private void nextTurn(int jumpToTurn) {
		if (jumpToTurn != -1)
			currentTurn = jumpToTurn;
		else
			currentTurn = (currentTurn + 1) % activePlayerIndexes.size();
		
		currentRoll = -1;
		
		onInstanceUpdate.run();
	}
	
	private void checkForceCapture(String pieceID) {
		for (int i = 0; i < 4; i++) {
			if (getPieceIndexFromPiece(pieceID) == i)
				continue;
			
			String tempID = getCurrentPlayerIndex() + "-" + i;
			String tempTarget = getPieceTarget(tempID, false);
			
			if (tempTarget != null && !tempTarget.isEmpty()) {
				//Force capture has been blundered
				
				//Return the blundered piece
				returnPieceToStart(tempID);
				
				for (Consumer<String> event : onForceCaptureBlunder)
					event.accept(tempID);;
				return;
			}
		}
	}
	
	private void checkForWinner() {
		for (int i = 0; i < 4; i++) {
			if (homeLineup && homes[getCurrentPlayerIndex()][i].isEmpty())
				return;
			else if (!homeLineup && !completedPieces.contains(getCurrentPlayerIndex() + "-" + i))
				return;
		}
		
		winnerPlayerIndex = getCurrentPlayerIndex();
		
		onInstanceUpdate.run();
	}
}
