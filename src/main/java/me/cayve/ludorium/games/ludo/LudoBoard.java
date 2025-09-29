package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.joml.Vector2f;

import me.cayve.ludorium.games.boards.GameBoard;
import me.cayve.ludorium.games.events.ActionChoiceEvent;
import me.cayve.ludorium.games.events.DiceRollEvent;
import me.cayve.ludorium.games.events.GameFinishEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent.eAction;
import me.cayve.ludorium.games.events.TokenSelectionEvent;
import me.cayve.ludorium.games.events.TurnChangeEvent;
import me.cayve.ludorium.games.lobbies.InteractionLobby;
import me.cayve.ludorium.games.tilemaps.BlockTileMap;
import me.cayve.ludorium.games.tilemaps.TileMapManager;
import me.cayve.ludorium.games.tilemaps.TokenTileMap;
import me.cayve.ludorium.games.utils.CustomModel;
import me.cayve.ludorium.games.utils.GameDie;
import me.cayve.ludorium.utils.ArrayUtils;
import me.cayve.ludorium.utils.Collider;
import me.cayve.ludorium.utils.Config;
import me.cayve.ludorium.utils.DelayedActionQueue;
import me.cayve.ludorium.utils.ProgressBar;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message;
import me.cayve.ludorium.utils.animation.Animator;
import me.cayve.ludorium.utils.entities.ItemEntity;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class LudoBoard extends GameBoard {

	public static final String[] COLOR_ORDER = { "red", "yellow", "green", "blue", "purple", "black" };

	public static final Vector2f TOKEN_BOUNDS = new Vector2f(.4f, .7f);
	
	private static final float MIN_DICE_ROLL_DURATION = 2f;
	
	//0 - Board tile map
	//1 - Token tile map
	private TileMapManager tileMaps;
	private LudoInstance gameInstance;
	private LudoMap boardMap;
	
	private Location origin;
	
	private GameDie dice;
	
	private Task rollTimer, selectTimer, endGameTimer;
	private DelayedActionQueue actionQueue = new DelayedActionQueue();
	
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
		HashMap<String, TokenTileMap.TokenInfo> tokenMapping = new HashMap<>();
		
		for (int i = 0; i < COLOR_ORDER.length; i++)
			//Ludo token IDs are COLOR-PIECE#, so only the first part (the color) needs to be mapped
			tokenMapping.put(i + "", new TokenTileMap.TokenInfo(CustomModel.get(Ludo.class, COLOR_ORDER[i]), TOKEN_BOUNDS));
		
		tileMaps.registerNewMap(new BlockTileMap(boardMap.mapFromOrigin(origin, false)));
		tileMaps.registerNewMap(new TokenTileMap(boardMap.mapFromOrigin(origin, true), tokenMapping, null));
		
		((TokenTileMap) tileMaps.getMap(1)).setState(gameInstance.getBoardState(), false);
	}
	
	private void initializeTimers() {
		rollTimer 	= Timer.register(new Task(uniqueID).setDuration((float)Config.getDouble(Ludo.class, "diceRollTimeout")).pause());
		selectTimer = Timer.register(new Task(uniqueID).setDuration((float)Config.getDouble(Ludo.class, "selectPieceTimeout")).pause());
		endGameTimer = Timer.register(new Task(uniqueID)).setDuration(15).pause();
		
		rollTimer.registerOnComplete(dice::forceRoll);
		endGameTimer.registerOnComplete(this::endGame);
	}
	
	private void initializeGameInstance() {
		gameInstance = new LudoInstance(lobby.getOccupiedPositions(), boardMap, false, true, false, 2, false);
		
		gameInstance.getLogger().getSubscriber(TurnChangeEvent.class).subscribe(this::onNextTurn);
		gameInstance.getLogger().getSubscriber(DiceRollEvent.class).subscribe(this::onDiceRoll);
		gameInstance.getLogger().getSubscriber(TokenMoveEvent.class).subscribe(this::onTokenMove);
		gameInstance.getLogger().getSubscriber(GameFinishEvent.class).subscribe(this::onGameEnd);
		gameInstance.getLogger().getSubscriber(ActionChoiceEvent.class).subscribe(this::onNewChoice);
		gameInstance.getLogger().getSubscriber(TokenSelectionEvent.class).subscribe(this::onTokenSelected);
	}
	
	@Override
	protected void generateLobby() {
		ArrayList<ItemEntity> tokens = new ArrayList<>();
		
		for (int i = 0; i < boardMap.getColorCount(); i++) {
			ItemEntity token = new ItemEntity(
					boardMap.getStarterCenter(origin, i), 
					CustomModel.get(Ludo.class, COLOR_ORDER[i]),
					entity -> new Collider(entity.getPositionTransform(), TOKEN_BOUNDS),
					entity -> new Animator(entity.getPositionTransform()));
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
		actionQueue.destroy();
		
		//If destroy happens while a game is not active
		if (gameInstance == null) 
			return;
		
		rollTimer.cancel();
		selectTimer.cancel();
		endGameTimer.cancel();
	}
	
	private Component getPositionLabel(int position, Player reader) { return TextYml.getText(reader, "words.colors." + COLOR_ORDER[position]); }
	
	private void onNextTurn(TurnChangeEvent event) {
		if (event.getTurnChangeReason() == LudoTurnDefinition.NO_VALID_MOVES)
			actionQueue.queue(ToolbarMessage.DEFAULT_MESSAGE_DURATION, () -> {
				lobby.getMessenger().sendAll(ToolbarMessage::clearSourceAndSendImmediate, 
						new Message(v -> TextYml.getText(v, "in-game.ludo.no-moves", 
							Placeholder.component("label", getPositionLabel(event.getPreviousTurn(), v)))));
			});
		else if (event.getTurnChangeReason() == LudoTurnDefinition.NO_VALID_MOVES_WITH_GRACE)
			actionQueue.queue(ToolbarMessage.DEFAULT_MESSAGE_DURATION, () -> {
				lobby.getMessenger().sendAll(ToolbarMessage::clearSourceAndSendImmediate, 
						new Message(v -> TextYml.getText(v, "in-game.ludo.no-moves-with-grace", 
							Placeholder.component("label", getPositionLabel(event.getPreviousTurn(), v)),
							Placeholder.parsed("turns-left", 
									ProgressBar.newBuild().barCount(gameInstance.getNoMovesGraceAllowance())
										.generate(gameInstance.getCurrentNoMovesGraceLeft() + 1)))));
			});
		
		//Action duration of 0 because it waits on the dice roll
		actionQueue.queue(0, () -> {
			rollTimer.restart();
			
			lobby.getMessenger().sendAll(ToolbarMessage::clearSourceAndSendImmediate,
				new Message(v -> TextYml.getText(v, "in-game.ludo.roll", 
					Placeholder.component("label", getPositionLabel(event.getPlayerTurn(), v))))
				.linkDurationToTimer(rollTimer).showDuration(.5f));
			
			if (event.getTurnChangeReason() == LudoTurnDefinition.ROLLED_6_EXTRA_TURN)
				lobby.getMessenger().sendAll(ToolbarMessage::sendImmediate, 
					new Message(v -> TextYml.getText(v, "in-game.ludo.reroll", 
						Placeholder.component("label", getPositionLabel(event.getPlayerTurn(), v)))));

			dice.playerRoll(lobby.getPlayerAt(event.getPlayerTurn()), (result) -> {
				gameInstance.roll(result[0]);
				rollTimer.pause();
			});
		});
	}
	
	private void onDiceRoll(DiceRollEvent event) {
		actionQueue.queue(MIN_DICE_ROLL_DURATION, () -> {
			selectTimer.restart();
			
			lobby.getMessenger().sendAll(ToolbarMessage::clearSourceAndSendImmediate, 
					new Message(v -> TextYml.getText(v, "in-game.ludo.rolled", 
						Placeholder.parsed("roll", event.getRollValue() + ""),
						Placeholder.component("label", getPositionLabel(event.getPlayerTurn(), v))))
					.linkDurationToTimer(selectTimer).showDuration(.5f));
		});
	}
	
	private void onTokenMove(TokenMoveEvent event) {
		actionQueue.queue(event.getPath().length * TokenTileMap.TOKEN_ANIM_JUMP_DURATION, () -> {
			((TokenTileMap) tileMaps.getMap(1)).moveToken(event.getTokenID(), event.getPath(), true, null, null);
			
			if (event.getAction() == eAction.BLUNDER)
				lobby.getMessenger().sendAll(ToolbarMessage::sendImmediate, 
					new Message(v -> TextYml.getText(v, "in-game.ludo.blundered", 
						Placeholder.component("label", getPositionLabel(event.getPlayerTurn(), v)))));
			if (event.getAction() == eAction.CAPTURE)
				lobby.getMessenger().sendAll(ToolbarMessage::sendImmediate, 
					new Message(v -> TextYml.getText(v, "in-game.ludo.captured", 
						Placeholder.component("label", getPositionLabel(event.getPlayerTurn(), v)),
						Placeholder.component("oponent-label", 
							getPositionLabel(gameInstance.getPlayerIndexFromPiece(event.getTokenID()), v)))));
			
			((TokenTileMap) tileMaps.getMap(1)).setState(gameInstance.getBoardState(), false);
		});
	}
	
	private void onGameEnd(GameFinishEvent event) {
		endGameTimer.restart();
		
		lobby.getMessenger().sendAll(ToolbarMessage::clearSourceAndSendImmediate, 
				new Message(v -> TextYml.getText(v, "in-game.ludo.won", 
						Placeholder.component("label", getPositionLabel(event.getPlayerTurn(), v))))
			.linkDurationToTimer(endGameTimer).showDuration(.5f));
	}

	private void onNewChoice(ActionChoiceEvent<String> event) {
		if (event.getActionChoices().length != 0)
		{
			for (String choice : event.getActionChoices())
				tileMaps.getMap(1).highlightTile(gameInstance.getPieceIndex(choice), false);
		}
		else
			tileMaps.getMap(1).unhighlightTile(-1);
	}
	
	private void onTokenSelected(TokenSelectionEvent event) {
		if (event.getSelectedTokens().length == 0)
			tileMaps.getMap(1).unselectTile(-1);
		else
			ArrayUtils.forEach(event.getSelectedTokens(), x -> tileMaps.getMap(1).selectTile(gameInstance.getPieceIndex(x), true));
		
		//Highlight any tiles that are the targets of any selected token
		if (event.getTargetedTiles().length == 0)
			tileMaps.getMap(0).unhighlightTile(-1);
		else
			ArrayUtils.forEach(event.getTargetedTiles(), x -> tileMaps.getMap(0).highlightTile(x, true));
	}
	
	private void onTileSelected(int tileIndex) {
		String selectedPiece = tileMaps.getMap(1).getTileIDAt(tileIndex);

		if (selectedPiece != null && gameInstance.selectPiece(selectedPiece))
			return;
		
		gameInstance.selectTile(tileIndex);
	}

	@Override
	protected void startGame() {
		initializeTimers();
		initializeGameInstance();
		initializeTileMaps(origin);
		
		gameInstance.start(true);
	}

	@Override
	protected void endGame() {
		destroy();
		
		lobby.enable();
	}
	
	public Location getOrigin() { return origin; }
	public LudoMap getMap() { return boardMap; }
}
