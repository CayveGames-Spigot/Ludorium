package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;

import org.bukkit.Location;

import me.cayve.ludorium.games.boards.GameBoard;
import me.cayve.ludorium.games.boards.TileMapManager;
import me.cayve.ludorium.games.lobbies.InteractionLobby;
import me.cayve.ludorium.utils.Config;
import me.cayve.ludorium.utils.CustomModel;
import me.cayve.ludorium.utils.DiceRoll;
import me.cayve.ludorium.utils.StateMachine;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.locational.Vector2D;

public class LudoBoard extends GameBoard {

	public static final String[] COLOR_ORDER = { "red", "yellow", "green", "blue", "purple", "black" };
	
	//0 - Board tile map
	//1 - Token tile map
	private TileMapManager tileMaps;
	private LudoInstance gameInstance;
	private LudoMap boardMap;
	
	private DiceRoll dice;
	
	private Task rollTimer, selectTimer;
	private StateMachine states;
	
	/**
	 * Creates and generates a new ludo board and lobby
	 * @param name The name of the board
	 * @param boardMap The tile map of the ludo board.
	 * @param origin The world location this board should be relative to. This should be the red out.
	 */
	public LudoBoard(String name, LudoMap boardMap, Location origin) {
		super(name);
		
		this.boardMap = boardMap;

		tileMaps = new TileMapManager(this::onTileSelected);
		
		tileMaps.registerNewMap(boardMap.constructBlockTileMap(origin));
		
		dice = new DiceRoll();
		
		generateLobby();
	}
	
	private void createStates() {
		rollTimer 	= Timer.register(new Task(uniqueID).setDuration((float)Config.get().getDouble("games.ludo.diceRollTimeout")).pause());
		selectTimer = Timer.register(new Task(uniqueID).setDuration((float)Config.get().getDouble("games.ludo.selectPieceTimeout")).pause());
		
		rollTimer.registerOnComplete(dice::forceRoll);

		states = new StateMachine()
			.newState("ROLL")
				.registerAction(() -> {
					rollTimer.restart();
					
					dice.playerRoll(lobby.getPlayerAt(gameInstance.getCurrentPlayerIndex()), uniqueID, 1, (result) -> {
						gameInstance.roll(result.get(0));
						rollTimer.pause();
						states.skipTo("SELECT");
					});
				})
				.buildState()
			.newState("SELECT")
				.registerAction(() -> {
					selectTimer.restart();
					
					
				}).buildState();
	}
	
	@Override
	protected void generateLobby() {
		ArrayList<InteractionLobby.Token> tokens = new ArrayList<>();
		
		for (int i = 0; i < boardMap.getColorCount(); i++)
			tokens.add(new InteractionLobby.Token(
					LudoBoardLocations.getStarterCenter(i, tileMaps.getMap(0)),	//Location of the piece
					CustomModel.get("LUDO_" + COLOR_ORDER[i].toUpperCase()), 	//Model of the piece
					new Vector2D(1, 1)));										//Size of the interaction
		
		lobby = new InteractionLobby(2, boardMap.getColorCount(), tokens);
		
		super.generateLobby();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		tileMaps.destroy();
		dice.destroy();
	}
	
	private void onGameInstanceUpdate() {
		
	}
	
	private void onTileSelected(int tileIndex) {
		if (!states.isCurrentState("SELECT"))
			return;
		
		String selectedPiece = tileMaps.getMap(1).getTileIDAt(tileIndex);
		
		if (selectedPiece != null)
			gameInstance.selectPiece(selectedPiece);
		else
			gameInstance.selectTile(tileIndex);
	}

	@Override
	protected void startGame() {
		gameInstance = new LudoInstance(this::onGameInstanceUpdate, lobby.getActiveIndexes(), boardMap, false, false, false);
		
		createStates();
	}

	@Override
	protected void endGame() {
		// TODO Auto-generated method stub
		
	}
}
