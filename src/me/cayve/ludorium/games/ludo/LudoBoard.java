package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.cayve.ludorium.games.boards.BlockTileMap;
import me.cayve.ludorium.games.boards.GameBoard;
import me.cayve.ludorium.games.boards.TileMapManager;
import me.cayve.ludorium.games.boards.TokenTileMap;
import me.cayve.ludorium.games.events.InstanceEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent.eAction;
import me.cayve.ludorium.games.lobbies.InteractionLobby;
import me.cayve.ludorium.games.utils.CustomModel;
import me.cayve.ludorium.games.utils.GameDie;
import me.cayve.ludorium.utils.Config;
import me.cayve.ludorium.utils.StateMachine;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.entities.ItemEntity;
import me.cayve.ludorium.utils.locational.Vector2D;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class LudoBoard extends GameBoard {

	public static final String[] COLOR_ORDER = { "red", "yellow", "green", "blue", "purple", "black" };
	
	//0 - Board tile map
	//1 - Token tile map
	private TileMapManager tileMaps;
	private LudoInstance gameInstance;
	private LudoMap boardMap;
	
	private Location origin;
	
	private GameDie dice;
	
	private Task rollTimer, selectTimer, endGameTimer;
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
		
		generateLobby();
		
		dice = new GameDie(lobby.getLobbyKey(), 1);
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
		endGameTimer = Timer.register(new Task(uniqueID)).setDuration(15).pause();
		
		rollTimer.registerOnComplete(dice::forceRoll);
		endGameTimer.registerOnComplete(this::endGame);

		states = new StateMachine()
			.newState("ROLL")
				.registerAction(() -> {
					rollTimer.restart();
					
					lobby.forEachOnlinePlayer((player) -> 
						ToolbarMessage.sendImmediate(player, uniqueID + "-roll", 
							TextYml.getText(player, "in-game.ludo.roll", 
									Placeholder.parsed("roll", "..."),
									TextYml.tag("label", getPositionLabel(gameInstance.getCurrentPlayerIndex(), player))))
						.setDuration(rollTimer.getSecondsLeft()).showDuration());
					
					dice.playerRoll(lobby.getPlayerAt(gameInstance.getCurrentPlayerIndex()), (result) -> {
						gameInstance.roll(result[0]);
						rollTimer.pause();
						states.skipTo("SELECT");
					});
				})
				.buildState()
			.newState("SELECT")
				.registerAction(() -> {
					selectTimer.restart();
					
					
					lobby.forEachOnlinePlayer((player) -> 
						ToolbarMessage.clearSourceAndSendImmediate(player, uniqueID + "-roll", 
							TextYml.getText(player, "in-game.ludo.roll", 
									Placeholder.parsed("roll", gameInstance.getLastRoll() + ""),
									TextYml.tag("label", getPositionLabel(gameInstance.getCurrentPlayerIndex(), player))))
						.setDuration(selectTimer.getSecondsLeft()).showDuration());
					
				}).buildState();
	}
	
	@Override
	protected void generateLobby() {
		ArrayList<ItemEntity> tokens = new ArrayList<>();
		
		for (int i = 0; i < boardMap.getColorCount(); i++) {
			ItemEntity token = new ItemEntity(boardMap.getStarterCenter(origin, i), CustomModel.get(Ludo.class, COLOR_ORDER[i]));
			token.setInteraction(new Vector2D(1, 1));
			tokens.add(token);
		}
		
		lobby = new InteractionLobby(2, boardMap.getColorCount(), true, tokens);
		
		lobby.registerPositionLabel(this::getPositionLabel);
		
		super.generateLobby();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		tileMaps.destroy();
		dice.destroy();
	}
	
	private Component getPositionLabel(int position, Player reader) { return TextYml.getText(reader, "words.colors." + COLOR_ORDER[position]); }
	
	private void onGameInstanceUpdate() {
		
		if (gameInstance.getWinnerPlayerIndex() != -1)
		{
			endGameTimer.restart();
			
			lobby.forEachOnlinePlayer((player) -> 
				ToolbarMessage.clearSourceAndSendImmediate(player, uniqueID, 
					TextYml.getText(player, "in-game.ludo.won", 
							TextYml.tag("label", getPositionLabel(gameInstance.getCurrentPlayerIndex(), player))))
				.setDuration(endGameTimer.getSecondsLeft()).showDuration());
			
			return;
		}
		for (InstanceEvent event : gameInstance.getLogger().getUnprocessed()) {
			if (event instanceof TokenMoveEvent moveEvent) {
				((TokenTileMap) tileMaps.getMap(1)).moveToken(moveEvent.getTokenID(), moveEvent.getPath(), true, null, null);
				
				if (moveEvent.getAction() == eAction.BLUNDER)
					lobby.forEachOnlinePlayer((player) -> ToolbarMessage.sendImmediate(player, uniqueID, 
							TextYml.getText(player, "in-game.ludo.blundered", 
									TextYml.tag("label", getPositionLabel(moveEvent.getPlayerTurn(), player)))));
				if (moveEvent.getAction() == eAction.CAPTURE)
					lobby.forEachOnlinePlayer((player) -> ToolbarMessage.sendImmediate(player, uniqueID, 
							TextYml.getText(player, "in-game.ludo.captured", 
									TextYml.tag("label", getPositionLabel(gameInstance.getCurrentPlayerIndex(), player)),
									TextYml.tag("oponent-label", 
											getPositionLabel(gameInstance.getPlayerIndexFromPiece(moveEvent.getTokenID()), player)))));
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
		createStates();
		initializeTileMaps(origin);
		gameInstance = new LudoInstance(this::onGameInstanceUpdate, lobby.getOccupiedPositions(), boardMap, false, false, false);
		onGameInstanceUpdate();
	}

	@Override
	protected void endGame() {
		destroy();
		
		lobby.enable();
	}
	
	public Location getOrigin() { return origin; }
	public LudoMap getMap() { return boardMap; }
}
