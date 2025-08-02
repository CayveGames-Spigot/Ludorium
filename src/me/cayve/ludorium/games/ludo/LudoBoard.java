package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import me.cayve.ludorium.games.boards.BlockTileMap;
import me.cayve.ludorium.games.boards.GameBoard;
import me.cayve.ludorium.games.boards.TileMapManager;
import me.cayve.ludorium.games.boards.TokenTileMap;
import me.cayve.ludorium.games.events.InstanceEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent;
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
	
	private Location origin;
	
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
		this.origin = origin;
		
		tileMaps = new TileMapManager(this::onTileSelected);
		
		dice = new DiceRoll();
		
		generateLobby();
	}
	
	private void initializeTileMaps(Location origin) {
		Location[] locationMap = boardMap.mapFromOrigin(origin);
		
		HashMap<String, ItemStack> itemMapping = new HashMap<>();
		
		for (int i = 0; i < COLOR_ORDER.length; i++)
			//Ludo token IDs are COLOR-PIECE#, so only the first part (the color) needs to be mapped
			itemMapping.put(i + "", CustomModel.get(Ludo.class, COLOR_ORDER[i]));
		
		tileMaps.registerNewMap(new BlockTileMap(locationMap));
		tileMaps.registerNewMap(new TokenTileMap(locationMap, itemMapping, () -> states.skipTo("ROLL")));
	}
	
	private void createStates() {
		rollTimer 	= Timer.register(new Task(uniqueID).setDuration((float)Config.getDouble(Ludo.class, "diceRollTimeout")).pause());
		selectTimer = Timer.register(new Task(uniqueID).setDuration((float)Config.getDouble(Ludo.class, "selectPieceTimeout")).pause());
		
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
					boardMap.getStarterCenter(origin, i),			//Location of the piece
					CustomModel.get(Ludo.class, COLOR_ORDER[i]), 	//Model of the piece
					new Vector2D(1, 1)));							//Size of the interaction
		
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
		
		if (gameInstance.getWinnerPlayerIndex() != -1)
		{
			endGame();
			return;
		}
		for (InstanceEvent event : gameInstance.getLogger().getUnprocessed()) {
			if (event instanceof TokenMoveEvent moveEvent) {
				((TokenTileMap) tileMaps.getMap(1)).moveToken(moveEvent.getTokenID(), moveEvent.getPath(), true, null, null);
			}
		}
		
		((TokenTileMap) tileMaps.getMap(1)).setState(gameInstance.getBoardState(), false);
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
		initializeTileMaps(origin);
		gameInstance = new LudoInstance(this::onGameInstanceUpdate, lobby.getActiveIndexes(), boardMap, false, false, false);
		
		createStates();
	}

	@Override
	protected void endGame() {
		destroy();
		
		lobby.enable();
	}
	
	public Location getOrigin() { return origin; }
	public LudoMap getMap() { return boardMap; }
}
