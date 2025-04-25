package me.cayve.ludorium.games.wizards;

import java.util.ArrayList;

import org.bukkit.Location;

import me.cayve.ludorium.actions.CrouchAction;
import me.cayve.ludorium.actions.CrouchAction.eResult;
import me.cayve.ludorium.actions.SelectBlocksAction;
import me.cayve.ludorium.actions.SelectRegionAction;
import me.cayve.ludorium.games.boards.LudoBoard;
import me.cayve.ludorium.utils.ProgressBar;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.utils.locational.Region;
import me.cayve.ludorium.ymls.TextYml;

public class LudoCreationWizard extends GameCreationWizard {

	//Manual mode will force the wizard to select every board space 
	//Otherwise, the board will attempt to find the spaces automatically
	private boolean isManualMode;
	private boolean isSixPlayer;
	private LudoBoard board;
	
	private String[] colorOrder = { "red", "yellow", "green", "blue", "purple", "black" };
	
	public LudoCreationWizard setManualMode(boolean isManualMode) {
		if (state == -1)
			this.isManualMode = isManualMode;
		
		return this;
	}
	
	public LudoCreationWizard setSixPlayer(boolean isSixPlayer) {
		if (state == -1)
			this.isSixPlayer = isSixPlayer;
		
		return this;
	}
	
	@Override
	protected void onStateUpdate() {
		if (state == 0 && progressed)
			ToolbarMessage.sendQueue(player, tsk, TextYml.getText("wizards.ludo.colorVerification")
					.replace("<colors>", String.format("%s->%s->%s->%s" + (isSixPlayer ? "->%s->%s" : ""),
							TextYml.getText("words.colors." + colorOrder[0]),
							TextYml.getText("words.colors." + colorOrder[1]),
							TextYml.getText("words.colors." + colorOrder[2]),
							TextYml.getText("words.colors." + colorOrder[3]),
							TextYml.getText("words.colors." + colorOrder[4]),
							TextYml.getText("words.colors." + colorOrder[5])).toUpperCase())
					//Replaces color with the ordered RED->YELLOW->GREEN->BLUE and adds the last two if six players
					).setDuration(12).showDuration().clearIfSkipped();
		
		if (isManualMode)
			updateManualState();
		else
			updateAutomaticState();
	}
	
	//States of the automatic mode
	protected void updateAutomaticState() {
		switch (state) {
			case 0: //Started automatic mode / regressed from board identify confirmation
				
				if (!progressed) //Player denied identified board
				{
					destroy();
					ToolbarMessage.sendImmediate(player, TextYml.getText("wizards.canceled")).setType(eType.ERROR);
					return;
				}
				
				ToolbarMessage.sendQueue(player, stateTsk, TextYml.getText("wizards.ludo.attemptingAutomatic"));
				
				setNewAction(new SelectRegionAction(player, "Ludo Board", this::onCompletedAction, this::onCancelledAction));
				
				break;
			case 1: //Player finished selecting the region to identify board / regress not possible
				
				if (board != null) //In case of regress, destroy (animation might be in progress)
					board.destroy();
				
				board = LudoBoard.identifyBoard(((SelectRegionAction)currentAction).getRegion(), isSixPlayer);
				
				if (board != null) {
					ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.ludo.identifiedBoard")).setPermanent();
					board.animate();
					
					setNewAction(new CrouchAction(player, eResult.BOTH, this::onCompletedAction, this::onCancelledAction));
				} else {
					ToolbarMessage.sendImmediate(player, TextYml.getText("wizards.ludo.noBoardIdentified")).setType(eType.ERROR);
					destroy();
				}
			
				break;
			case 2: //If player accepted identified board
				
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.created")).setType(eType.SUCCESS);
				
				createGame();
				break;
		}
	}
	
	//State values to avoid magic numbers
	private static final int SELECT_TILES = 0;
	private static final int SELECT_HOMES = 1;
	private static final int SELECT_STARTER_1 = 2;
	private static final int SELECT_STARTER_2 = 3;
	private static final int SELECT_STARTER_3 = 4;
	private static final int SELECT_STARTER_4 = 5;
	private static final int SELECT_STARTER_5 = 6;
	private static final int SELECT_STARTER_6 = 7;
	private static final int SELECTED_LAST_STARTER = 8;
	private static final int SELECT_DIAGONAL_PAIR_1 = 9;
	private static final int SELECT_DIAGONAL_PAIR_2 = 10;
	private static final int SELECT_DIAGONAL_PAIR_3 = 11;
	private static final int SELECTED_LAST_DIAGONAL_PAIR = 12;
	
	//States of the manual mode
	private void updateManualState() 
	{
		switch (state) 
		{
			case SELECT_TILES: //Started manual mode / regressed from home selection
				
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.ludo.selectTiles")).setPermanent();
				
				setNewAction(new SelectBlocksAction(player, -1, false, this::onCompletedAction, this::onCancelledAction));
				
				break;
			case SELECT_HOMES: //Player finished selecting all of the tiles / regressed from red starter
				if (board == null)
					board = new LudoBoard();
				
				if (progressed) //If regressed, don't override existing selection
					board.tiles = ((SelectBlocksAction) currentAction).getLocations();
				 
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.ludo.selectHome")).setPermanent();
				 
				setNewAction(new SelectBlocksAction(player, -1, false, this::onCompletedAction, this::onCancelledAction));
				break;
			case SELECT_STARTER_1: //Player finished selecting all of the home tiles / regressed from yellow starter
				if (progressed)
					board.homeTiles = ((SelectBlocksAction) currentAction).getLocations();
				
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.ludo.starters")).setDuration(10).showDuration();
				
				promptStarterRegionSelection(0);
				break;
			case SELECT_STARTER_2:
				saveSelectedStarterRegion();
				promptStarterRegionSelection(1);
				break;
			case SELECT_STARTER_3:
				saveSelectedStarterRegion();
				promptStarterRegionSelection(2);
				break;
			case SELECT_STARTER_4:
				saveSelectedStarterRegion();
				promptStarterRegionSelection(3);
				break;
			case SELECT_STARTER_5:
				saveSelectedStarterRegion();
				
				if  (!isSixPlayer)
					skipToState(SELECT_DIAGONAL_PAIR_1); //Jump past 6p selections
				else
					promptStarterRegionSelection(4);
				break;
			case SELECT_STARTER_6:
				saveSelectedStarterRegion();
				promptStarterRegionSelection(5);
				break;
			case SELECTED_LAST_STARTER:
				saveSelectedStarterRegion();
				
				skipToState(SELECT_DIAGONAL_PAIR_1); //Run the next state too
				break;
			case SELECT_DIAGONAL_PAIR_1: //Player finished selecting all starter regions
				
				if (!progressed) //If regressed to this point, reset all pairs
					board.diagonalPairs.clear();
				
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.ludo.diagonals"));
				ToolbarMessage.sendImmediate(player, stateTsk, TextYml.getText("wizards.ludo.selectDiagonalPair")
						.replace("<progress>", ProgressBar.generate(0, isSixPlayer ? 3 : 2))).setPermanent();
				
				setNewAction(new SelectBlocksAction(player, 2, false, this::onCompletedAction, denied -> createGame())); //Canceling here denies diagonals
				break;
			case SELECT_DIAGONAL_PAIR_2: //Selected first pair of diagonals
				saveSelectedDiagonals();
				
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.ludo.selectDiagonalPair")
						.replace("<progress>", ProgressBar.generate(1, isSixPlayer ? 3 : 2))).setPermanent();
				
				setNewAction(new SelectBlocksAction(player, 2, false, this::onCompletedAction, this::onCancelledAction));
				break;
			case SELECT_DIAGONAL_PAIR_3: //Selected second pair of diagonals
				saveSelectedDiagonals();
				
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText("wizards.ludo.selectDiagonalPair")
						.replace("<progress>", ProgressBar.generate(2, isSixPlayer ? 3 : 2))).setPermanent();
				
				if (isSixPlayer)
					setNewAction(new SelectBlocksAction(player, 2, false, this::onCompletedAction, this::onCancelledAction));
				else
					createGame();
				break;
			case SELECTED_LAST_DIAGONAL_PAIR: //Selected third pair of diagonals (6p)
				saveSelectedDiagonals();
				
				createGame();
				break;
		}
	}
	
	//Separated for readability
	private void saveSelectedDiagonals() {
		if (!progressed) //If regressed here, remove the last two selections (last pair)
		{
			board.diagonalPairs.remove(board.diagonalPairs.size() - 1);
			board.diagonalPairs.remove(board.diagonalPairs.size() - 1);
		}
		
		for (Location location : ((SelectBlocksAction) currentAction).getLocations())
			board.diagonalPairs.add(location);
	}
	
	//Separated for readability
	private void saveSelectedStarterRegion() {
		if (progressed)
		{
			board.centerPlates = new ArrayList<Region>(); //Makes sure this is the first region added (in case of regress)
			board.centerPlates.add(((SelectRegionAction) currentAction).getRegion());
		}
		else //If regress, remove the last region
			board.centerPlates.remove(board.centerPlates.size() - 1);
	}
	
	//Separated for readability
	private void promptStarterRegionSelection(int color) {
		setNewAction(new SelectRegionAction(player, 
				TextYml.getText("wizards.ludo.selectStarter").replace("<color>", TextYml.getText("words.colors." + colorOrder[color])),
				this::onCompletedAction, this:: onCancelledAction));
	}
	
	private void createGame() {
		
		destroy();
	}
}
