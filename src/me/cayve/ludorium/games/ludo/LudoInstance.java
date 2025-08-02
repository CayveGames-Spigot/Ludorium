package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import me.cayve.ludorium.games.GameInstance;
import me.cayve.ludorium.games.events.DiceRollEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent.eAction;
import me.cayve.ludorium.games.ludo.events.PieceBlunderEvent;

/**
 * @author Cayve
 * @license GPL v3
 * @repository https://github.com/CayveGames-Spigot/Ludorium
 * @created 6/8/2025
 * 
 * @description
 * Implements the running game rules and state of a Ludo instance.
 * (No world interaction integration should be present)
 */
public class LudoInstance extends GameInstance {
	//Game options
	private boolean safeSpaces, homeLineup, forceCapture;
	
	private ArrayList<Integer> activePlayerIndexes;
	
	private LudoMap map;
	
	//This corresponds to the index of activePlayerIndexes for the current player
	private int currentTurn = 0;
	private int currentRoll = -1;
	
	private String selectedPiece;
	private int winnerPlayerIndex = -1;
	
	//Board states
	private String[] board; //Empty if no piece present, otherwise contains piece's UUID. Utilizes LudoMap index order
	private ArrayList<String> completedPieces = new ArrayList<>();
	
	private Runnable onInstanceUpdate; //Called during any internal update
	
	public LudoInstance(Runnable onInstanceUpdate, ArrayList<Integer> activePlayerIndexes, LudoMap map,
			boolean safeSpaces, boolean homeLineup, boolean forceCapture) {
		
		this.onInstanceUpdate = onInstanceUpdate;
		
		this.safeSpaces = safeSpaces;
		this.homeLineup = homeLineup;
		this.forceCapture = forceCapture;
		
		this.map = map;
		
		//Fills board tiles with empty spaces
		board = new String[map.getMapSize()];
		Arrays.fill(board, "");
		
		//Sorts the indexes so the turn order goes the same direction
		this.activePlayerIndexes = activePlayerIndexes;
		Collections.sort(this.activePlayerIndexes);

		//Fills in the active starting pieces
		for (int i = 0; i < this.activePlayerIndexes.size(); i++) {
			int index = this.activePlayerIndexes.get(i);
			for (int j = 0; j < 4; j++)
				board[map.getStarterIndex(index, j)] = index + "-" + j; //ID of pieces are PLAYERINDEX-PIECE#
		}
		
		onInstanceUpdate.run();
	}
	
	public void roll(int rollValue) {
		currentRoll = rollValue;
		logger.logEvent(new DiceRollEvent(getCurrentPlayerIndex(), rollValue));
	}
	
	/**
	 * Indicate that a specific tile index has been chosen.
	 * Verification will be according to the last selectPiece() call.
	 * @param tileIndex
	 */
	public void selectTile(int tileIndex) {
		if (selectedPiece == null)
			return;
		
		int targetIndex = getPieceTarget(selectedPiece, false);
		
		//Check that the selected piece moves to the selected tile
		if (targetIndex != tileIndex)
			return;
		
		//Move the piece (handles capturing)
		getPieceTarget(selectedPiece, true);
		
		if (forceCapture && board[targetIndex].isEmpty())
			checkForceCapture(selectedPiece);
		
		checkForWinner();
		
		if (winnerPlayerIndex == -1)
			//Only go to the next turn if the last roll was not a 6
			nextTurn(currentRoll == 6 ? currentTurn : -1);
		
		selectedPiece = null;
	}
	
	/**
	 * Indicates that a specific piece has been selected.
	 * Necessary before selectTile() call, and will verify the current player turn according to the piece ID
	 * (though not that the player who selected IS that turn)
	 * @param pieceID
	 */
	public void selectPiece(String pieceID) {
		if (currentRoll == -1 || !doesPieceMatchCurrentPlayer(pieceID))
			return;
		
		//Check the contents of the targeted tile
		int targetIndex = getPieceTarget(pieceID, false);
		
		//Verify the move is possible and that if its a capture, its not their own piece
		if (targetIndex == -1 || (!board[targetIndex].isEmpty() && doesPieceMatchCurrentPlayer(pieceID)))
			return;
		
		selectedPiece = pieceID;
	}
	
	/**
	 * Removes a player index from the game (cannot be undone)
	 * @param playerIndex
	 */
	public void removePlayer(int playerIndex) {
		removePiece(playerIndex + "");
		
		boolean isRemovedPlayersTurn = currentTurn == activePlayerIndexes.indexOf(playerIndex);
		
		activePlayerIndexes.remove(activePlayerIndexes.indexOf(playerIndex));
		
		if (isRemovedPlayersTurn)
			nextTurn(-1);
		else
			onInstanceUpdate.run(); //Run an update to refresh any valid positions
	}
	
	public String[] getBoardState() { return board; }
	public int getCurrentPlayerIndex() { return activePlayerIndexes.get(currentTurn); }
	public boolean canPieceMove(String pieceID) { return getPieceTarget(pieceID, false) != -1; }
	public String getSelectedPiece() { return selectedPiece; }
	
	/**
	 * Gets the player index of the winner. -1 if no winner yet
	 * @return
	 */
	public int getWinnerPlayerIndex() { return winnerPlayerIndex; }
	
	private boolean doesPieceMatchCurrentPlayer(String pieceID) { return getPlayerIndexFromPiece(pieceID) == getCurrentPlayerIndex(); }
	private int getPlayerIndexFromPiece(String pieceID) { return Integer.valueOf(pieceID.charAt(0)); }
	private int getPieceNumberFromPiece(String pieceID) { return Integer.valueOf(pieceID.charAt(2)); }
	
	private int getPieceIndex(String pieceID) {
		for (int i = 0; i < board.length; i++)
			if (board[i].equals(pieceID))
				return i;
		return -1;
	}
	
	/**
	 * Finds the targeted tile index of the given piece
	 * based on the current roll. 
	 * @param pieceID The piece that is targeting
	 * @param movePiece Whether to actually move the piece to the new spot
	 * @return The index of the targeted tile (-1 if invalid move)
	 */
	private int getPieceTarget(String pieceID, boolean movePiece) {
		//This keeps track of the path the piece takes, if it moves
		ArrayList<Integer> path = new ArrayList<>();
		
		//First, check if piece is still at start
		for (int i = 0; i < 4; i++) {
			int boardIndex = map.getStarterIndex(getCurrentPlayerIndex(), i);
			if (!board[boardIndex].equals(pieceID))
				continue;
			
			if (currentRoll != 6)
				return -1;
			
			path.add(boardIndex);
			return moveToBoardTile(map.getStartTile(getCurrentPlayerIndex()), pieceID, movePiece, path);
		}
		
		//Second, check if piece is in home
		for (int i = 0; i < 4; i++)
		{
			int boardIndex = map.getHomeIndex(getCurrentPlayerIndex(), i);
			
			if (board[boardIndex].equals(pieceID)) {

				return moveDownHomeStretch(i, currentRoll, pieceID, movePiece, path);
			}
		}
		
		//Otherwise, piece is on main board
		for (int i = 0; i < board.length; i++) {
			int boardIndex = map.getTileIndex(i);
			
			if (!board[i].equals(pieceID))
				continue;
			
			if (boardIndex + currentRoll > map.getEndTile(getCurrentPlayerIndex()))
			{
				populatePath(path, boardIndex, boardIndex - map.getEndTile(getCurrentPlayerIndex()));
				return moveDownHomeStretch(-1, currentRoll - (boardIndex - (map.getEndTile(getCurrentPlayerIndex()))), pieceID, movePiece, path);
			}
			
			populatePath(path, boardIndex, currentRoll - 1); //- 1 because moveToBoardTile handles last position
			return moveToBoardTile(boardIndex + currentRoll, pieceID, movePiece, path);
		}
		
		return -1;
	}
	
	private void populatePath(ArrayList<Integer> path, int start, int count) {
		for (int i = start; i < start + count; i++)
			path.add(i);
	}
	
	private int moveToBoardTile(int tileIndex, String pieceID, boolean movePiece, ArrayList<Integer> path) {
		if (!board[tileIndex].isEmpty() && doesPieceMatchCurrentPlayer(board[tileIndex]))
			return -1;
		
		if (safeSpaces && map.isSafeSpace(tileIndex) && !doesPieceMatchCurrentPlayer(board[tileIndex]))
			return -1;
		
		if (!movePiece)
			return tileIndex;
		
		if (!board[tileIndex].isEmpty())
			returnPieceToStart(board[tileIndex], eAction.CAPTURE);
		
		path.add(tileIndex);
		logger.logEvent(new TokenMoveEvent(getCurrentPlayerIndex(), pieceID, eAction.MOVE, path));
		
		removePiece(pieceID);
		board[tileIndex] = pieceID;
		return tileIndex;
	}
	
	private int moveDownHomeStretch(int startingPos, int moveAmount, String pieceID, boolean movePiece, ArrayList<Integer> path) {
		//If the roll overshoots the last tile at all
		if (startingPos + moveAmount > 4)
			return -1;
		
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
					int tempIndex = map.getHomeIndex(getCurrentPlayerIndex(), startingPos + j);
					if (!movePiece)
						return tempIndex;
					
					populatePath(path, startingPos, j);
					logger.logEvent(new TokenMoveEvent(getCurrentPlayerIndex(), pieceID, eAction.MOVE, path));
					
					completedPieces.add(pieceID);
					removePiece(pieceID);
					return tempIndex;
				}
				else
					return -1;
			}
			
			//If any tile up until, and including, the target
			//is not empty, its not valid
			if (!board[map.getHomeIndex(getCurrentPlayerIndex(), startingPos + j)].isEmpty())
				return -1;
		}
		
		int homeIndex = map.getHomeIndex(getCurrentPlayerIndex(), startingPos + moveAmount);
		if (!movePiece)
			return homeIndex;
		
		populatePath(path, startingPos, moveAmount);
		logger.logEvent(new TokenMoveEvent(getCurrentPlayerIndex(), pieceID, eAction.MOVE, path));
		
		removePiece(pieceID);
		board[homeIndex] = pieceID;
		return homeIndex;
	}
	
	/**
	 * Sets the piece's current location to empty.
	 * This does not place the piece at a new location.
	 * The search uses startsWith logic, so player indexes can be used to 
	 * replace ALL of a player's pieces. (Since pieceIDs start with player index)
	 * @param pieceID The piece to replace
	 * @param replaceWith The piece to replace with (null to empty the tile)
	 * @return the index the piece was at
	 */
	private void removePiece(String pieceID) {
		for (int i = 0; i < board.length; i++)
			if (board[i].startsWith(pieceID))
				board[i] = "";
	}
	
	/**
	 * Returns the given piece to its starter
	 * @param pieceID 
	 */
	private void returnPieceToStart(String pieceID, eAction action) {
		int pieceIndex = getPieceIndex(pieceID);
		removePiece(pieceID);
		
		int playerIndex = getPlayerIndexFromPiece(pieceID);
		for (int i = 0; i < 4; i++) {
			if (board[map.getStarterIndex(playerIndex, i)].isEmpty()) {
				int starterIndex = map.getStarterIndex(playerIndex, i);
				board[starterIndex] = pieceID;
				
				logger.logEvent(new TokenMoveEvent(getCurrentPlayerIndex(), pieceID, action, pieceIndex, starterIndex));
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
			if (getPieceNumberFromPiece(pieceID) == i)
				continue;
			
			String tempID = getCurrentPlayerIndex() + "-" + i;
			int tempTarget = getPieceTarget(tempID, false);
			
			if (tempTarget != -1 && !board[tempTarget].isEmpty()) {
				//Force capture has been blundered
				
				//Return the blundered piece
				returnPieceToStart(tempID, eAction.BLUNDER);
				
				logger.logEvent(new PieceBlunderEvent(getCurrentPlayerIndex(), tempID));
				return;
			}
		}
	}
	
	private void checkForWinner() {
		for (int i = 0; i < 4; i++) {
			if (homeLineup && board[map.getHomeIndex(getCurrentPlayerIndex(), i)].isEmpty())
				return;
			else if (!homeLineup && !completedPieces.contains(getCurrentPlayerIndex() + "-" + i))
				return;
		}
		
		winnerPlayerIndex = getCurrentPlayerIndex();
		
		onInstanceUpdate.run();
	}
}
