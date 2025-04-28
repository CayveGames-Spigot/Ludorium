package me.cayve.ludorium.games.wizards;

import org.bukkit.Location;

import me.cayve.ludorium.actions.CrouchAction;
import me.cayve.ludorium.actions.CrouchAction.eResult;
import me.cayve.ludorium.actions.SelectBlocksAction;
import me.cayve.ludorium.actions.SelectRegionAction;
import me.cayve.ludorium.games.boards.LudoBoard;
import me.cayve.ludorium.utils.ProgressBar;
import me.cayve.ludorium.utils.StateMachine;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.ymls.TextYml;
import net.md_5.bungee.api.ChatColor;

public class LudoCreationWizard extends GameCreationWizard {

	//Manual mode will force the wizard to select every board space 
	//Otherwise, the board will attempt to find the spaces automatically
	private boolean isSixPlayer;
	private LudoBoard board;
	
	private String[] colorOrder = { "red", "yellow", "green", "blue", "purple", "black" };
	
	private int homeTileCount = -1;
	
	public LudoCreationWizard() {
		super();
		
		createAutomaticStates();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		stateMachine.complete();
	}
	
	public LudoCreationWizard setManualMode() {
		if (!stateMachine.hasStarted())
			createManualStates();
		
		return this;
	}
	
	public LudoCreationWizard setSixPlayer() {
		if (!stateMachine.hasStarted())
			this.isSixPlayer = true;
		
		return this;
	}
	
	private void promptColorOrderVerification() {
		ToolbarMessage.sendQueue(player, tsk, TextYml.getText(player, "wizards.ludo.colorVerification")
				.replace("<colors>", String.format("%s->%s->%s->%s" + (isSixPlayer ? "->%s->%s" : ""),
						replaceAllButColor(TextYml.getText(player, "words.colors." + colorOrder[0]), "■"),
						replaceAllButColor(TextYml.getText(player, "words.colors." + colorOrder[1]), "■"),
						replaceAllButColor(TextYml.getText(player, "words.colors." + colorOrder[2]), "■"),
						replaceAllButColor(TextYml.getText(player, "words.colors." + colorOrder[3]), "■"),
						replaceAllButColor(TextYml.getText(player, "words.colors." + colorOrder[4]), "■"),
						replaceAllButColor(TextYml.getText(player, "words.colors." + colorOrder[5]), "■"))
							.toUpperCase())
				//Replaces color with the ordered RED->YELLOW->GREEN->BLUE and adds the last two if six players
				).setDuration(12).showDuration().clearIfSkipped();
	}
	
	private String replaceAllButColor(String original, String replaceWith) {
		return original.replace(ChatColor.stripColor(original), replaceWith);
	}
	
	private void createAutomaticStates() {
		stateMachine = new StateMachine()
				.newState()
					.registerProgress(this::promptColorOrderVerification)
					.registerAction(() -> {
						ToolbarMessage.sendQueue(player, stateTsk, TextYml.getText(player, "wizards.ludo.attemptingAutomatic"));
						
						setNewAction(new SelectRegionAction(player, "Ludo Board", this::onCompletedAction, this::onCanceledAction));
					}).registerComplete(() -> {
						board = LudoBoard.identifyBoard(((SelectRegionAction)currentAction).getRegion(), isSixPlayer);
							
						if (board == null)
							cancelWizard(TextYml.getText(player, "wizards.ludo.noBoardIdentified"));
					}).buildState()
				.newState()
					.registerAction(() -> {
						ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText(player, "wizards.ludo.identifiedBoard")).setPermanent();
						board.animate();
						
						setNewAction(new CrouchAction(player, eResult.BOTH, this::onCompletedAction, this::onCanceledAction));
					})
					.registerComplete(() -> {
						ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText(player, "wizards.created")).setType(eType.SUCCESS);
						
						createGame();
					})
					.registerIncomplete(() -> {
						board.destroy();
						
						cancelWizard();
					}).buildState();
	}
	
	private void createManualStates() {
		stateMachine = new StateMachine()
			/*
			 * Select tiles
			 */
			.newState()
				.registerProgress(this::promptColorOrderVerification)
				.registerAction(() -> { //Started manual mode
					ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText(player, "wizards.ludo.selectTiles")
							.replace("<color>", TextYml.getText(player, "words.colors." + colorOrder[0]).toUpperCase())).setPermanent();
					
					setNewAction(new SelectBlocksAction(player, -1, false, this::onCompletedAction, this::onCanceledAction));
				})
				.registerComplete(() -> {
					board = new LudoBoard();
					
					board.tiles = ((SelectBlocksAction) currentAction).getLocations();
				}).buildState()
				
			/*
			 * Select homes
			 */
			.newState("HOME SELECTION")
				.registerAction(() -> {
					ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText(player, "wizards.ludo.selectHome")
							.replace("<color>", 
									TextYml.getText(player, "words.colors." + colorOrder[stateMachine.contextualIndex("homeColor")]).toUpperCase()))
							.setPermanent();
					 
					setNewAction(new SelectBlocksAction(player, homeTileCount, false, this::onCompletedAction, this::onCanceledAction));
				})
				.registerComplete(() -> {
					for (Location location : ((SelectBlocksAction) currentAction).getLocations())
						board.homeTiles.add(location);
					
					if (stateMachine.contextualIndex("homeColor") == 0)
						homeTileCount = ((SelectBlocksAction) currentAction).getSelectedCount();
				}).buildState()
				
			.copyState("HOME SELECTION").buildState()
			.copyState("HOME SELECTION").buildState()
			.copyState("HOME SELECTION").buildState();
		
		if (isSixPlayer)
			stateMachine
				.copyState("HOME SELECTION").buildState()
				.copyState("HOME SELECTION").buildState();
		
			/*
			 * Select starter region
			 */
			stateMachine.newState("STARTER REGION")
				.registerRegress(() -> {
					board.centerPlates.remove(board.centerPlates.size() - 1);
				})
				.registerAction(() -> {
					setNewAction(new SelectRegionAction(player, 
							TextYml.getText(player, "wizards.ludo.selectStarter")
								.replace("<color>", 
										TextYml.getText(player, "words.colors." + colorOrder[stateMachine.contextualIndex("starterColor")]).toUpperCase()),
							this::onCompletedAction, this:: onCanceledAction));
				})
				.registerComplete(() -> {
					board.centerPlates.add(((SelectRegionAction) currentAction).getRegion());	
				}).buildState()
				
			.copyState("STARTER REGION").buildState()
			.copyState("STARTER REGION").buildState()
			.copyState("STARTER REGION").buildState();
		
		if (isSixPlayer)
			stateMachine
				.copyState("STARTER REGION").buildState()
				.copyState("STARTER REGION").buildState();
		
		/*
		 * Select diagonals
		 */
		stateMachine.newState("DIAGONAL")
			.registerRegress(() -> {
				board.diagonalPairs.removeLast();
				board.diagonalPairs.removeLast();
			})
			.registerProgress(() -> {
				ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText(player, "wizards.ludo.diagonals"));
			})
			.registerAction(() -> {
				ToolbarMessage.sendImmediate(player, stateTsk, TextYml.getText(player, "wizards.ludo.selectDiagonalPair")
						.replace("<progress>", ProgressBar.generate(stateMachine.contextualIndex("diagonalProgress"), isSixPlayer ? 3 : 2))).setPermanent();
				
				setNewAction(new SelectBlocksAction(player, 2, false, this::onCompletedAction, this::onCanceledAction));
			})
			.registerComplete(() -> {
				for (Location location : ((SelectBlocksAction) currentAction).getLocations())
					board.diagonalPairs.add(location);
			})
			.registerIncomplete(() -> { //Player denied diagonals
				createGame();
			}).buildState();
		
		if (isSixPlayer)
			stateMachine.copyState("DIAGONAL")
				.registerIncomplete(null)
				.registerProgress(null);
		
		stateMachine.copyState("DIAGONAL")
			.registerIncomplete(null)
			.registerProgress(null)
			.registerComplete(() -> {
				for (Location location : ((SelectBlocksAction) currentAction).getLocations())
					board.diagonalPairs.add(location);
				
				createGame();
			});
	}
	
	private void createGame() {
		
		completeWizard();
	}
}
