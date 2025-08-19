package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.cayve.ludorium.games.utils.PlayerStateManager;
import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.utils.functionals.Event.Subscriber;
import me.cayve.ludorium.utils.functionals.Event0;
import me.cayve.ludorium.utils.functionals.Event1;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

/**
 * @author Cayve
 * @license GPL v3
 * @repository https://github.com/CayveGames-Spigot/Ludorium
 * @created 4/7/2025
 * 
 * @description
 * Implements the default lobby for a game including joining, leaving, and player state management
 */
public abstract class GameLobby implements Listener {
	
	protected static int COUNTDOWN_DURATION = 5;
	protected static int JOIN_LEAVE_PROMPT_DURATION = 1;
	
	protected String lobbyKey = UUID.randomUUID().toString();
	private boolean isEnabled;
	
	private BiFunction<Integer, Player, Component> positionLabelFunc;
	
	private String[] players;
	private String host;
	
	private int minimum, maximum, playerCount; //Max and min player count
	
	private boolean storeInventory = false, forceInventoryState;
	
	private Event1<Integer> lobbyJoinEvent = new Event1<>();
	private Event1<Integer> lobbyLeaveEvent = new Event1<>();
	
	private Event0 shutdownEvent = new Event0(); //Called when the lobby is shutdown after running
	private Event0 countdownCompleteEvent = new Event0();
	
	public Subscriber<Consumer<Integer>> onLobbyJoin = lobbyJoinEvent.getSubscriber(),
										onLobbyLeave = lobbyLeaveEvent.getSubscriber();
	public Subscriber<Runnable> onShutdown = shutdownEvent.getSubscriber(),
								onCountdownComplete = countdownCompleteEvent.getSubscriber();
	
	private Task countdown;
	
	/**
	 * Creates a new game lobby. Inventory states will not be stored, and players can change their inventory while in the lobby
	 * @param minimum Minimum amount of players to start the game
	 * @param maximum Maximum amount of players that can join
	 */
	public GameLobby(int minimum, int maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
		players = new String[maximum];
		
		registerEvents();
	}
	
	/**
	 * Creates a new game lobby. Inventory will be stored when joining the game
	 * @param minimum Minimum amount of players to start the game
	 * @param maximum Maximum amount of players that can join
	 * @param forceInventoryState Whether to disallow players from changing their inventory state while in the lobby
	 */
	public GameLobby(int minimum, int maximum, boolean forceInventoryState) {
		this.minimum = minimum;
		this.maximum = maximum;
		players = new String[maximum];
		
		registerEvents();
		
		this.storeInventory = true;
		this.forceInventoryState = forceInventoryState;
	}
	
	/**
	 * Registers the function to generate the label for a given position to be viewed by a given player when needed
	 * @param labelFunc
	 */
	public void registerPositionLabel(BiFunction<Integer, Player, Component> labelFunc) { positionLabelFunc = labelFunc; }
	
	protected Component getPositionLabel(int position, Player viewer) {
		if (positionLabelFunc == null)
			return Component.empty();
		return positionLabelFunc.apply(position, viewer);
	}
	
	private void registerEvents() {
		LudoriumPlugin.registerEvent(this);
		
		onCountdownComplete.subscribe(this::disable);
		
		Timer.register(countdown = new Task(lobbyKey).setDuration(COUNTDOWN_DURATION).setRefreshRate(1)
				.registerOnUpdate(() -> {
					forEachOnlinePlayer((player) -> {
						ToolbarMessage.sendImmediate(player, lobbyKey + "-countdown", 
								TextYml.getText(player, "in-game.startsIn", 
										Placeholder.parsed("duration", countdown.getWholeSecondsLeft() + "")))
						.clearIfSkipped().setMuted();
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 1);
					});
				}).registerOnComplete(countdownCompleteEvent).pause().refreshOnStart());
	}
	/**
	 * Enables the lobby to allow for joining
	 */
	public void enable() {
		isEnabled = true;
	}
	
	/**
	 * Disables the lobby, disallowing joining
	 */
	public void disable() {
		isEnabled = false;
	}
	/**
	 * @return Whether the lobby is enabled for joining
	 */
	public boolean isEnabled() { return isEnabled; }
	
	protected void startCountdown() {
		if (!isEnabled())
			return;
		
		countdown.restart();
		ToolbarMessage.clearAllFromSource(lobbyKey + "-waiting");
	}
	
	protected void onMaximumReached() {}
	protected void onMinimumReached() 
	{
		ToolbarMessage.clearAllFromSource(lobbyKey + "-waiting");
	}
	protected void onMinimumLost() {
		if (!isEnabled()) //If the game is running and the minimum player count is lost, shut down the lobby
			shutdownEvent.run();
		else
		{
			countdown.pause();
			
			ToolbarMessage.clearAllFromSource(lobbyKey + "-countdown");
			forEachOnlinePlayer(player -> ToolbarMessage.sendQueue(player, lobbyKey + "-waiting", 
					TextYml.getText(player, "in-game.waiting")).setPermanent());
		}
	}
	
	/**
	 * Attempts to join the lobby at the next available position
	 * @param playerID The playerID of the player joining
	 */
	public void attemptLobbyJoin(String playerID) {
		for (int i = 0; i < maximum; i++)
			if (players[i] == null)
				attemptLobbyJoin(playerID, i);
	}
	
	/**
	 * Attempts to join the lobby at the specified position
	 * @param playerID The playerID of the player joining
	 * @param lobbyPosition The position of the lobby to join at
	 */
	public void attemptLobbyJoin(String playerID, int lobbyPosition) {
		if (!isEnabled || hasPlayer(playerID) || players[lobbyPosition] != null)
			return;
		
		Player player = getOnlinePlayer(playerID);
		
		ToolbarMessage.sendImmediate(player, lobbyKey, TextYml.getText(player, "in-game.joined"))
			.setType(eType.SUCCESS).clearIfSkipped().setPriority(1).setDuration(JOIN_LEAVE_PROMPT_DURATION);
		
		ToolbarMessage.sendQueue(player, lobbyKey + "-waiting", TextYml.getText(player, "in-game.waiting")).setPermanent();
		
		promptJoinLeave("in-game.otherJoined", lobbyPosition, player);
		
		players[lobbyPosition] = playerID;
		playerCount++;
		lobbyJoinEvent.accept(lobbyPosition);
		
		if (host == null)
			host = playerID;
		
		if (storeInventory)
			PlayerStateManager.createProfile(player, lobbyKey, forceInventoryState);
		
		//If maximum and minimum are the same amount, only maximum is triggered
		if (playerCount == maximum)
			onMaximumReached();
		else if (playerCount == minimum)
			onMinimumReached();
	}
	
	/**
	 * Attempts to leave the lobby
	 * @param playerID The player leaving the lobby
	 */
	public void attemptLobbyLeave(String playerID) {
		if (!hasPlayer(playerID))
			return;
		
		int lobbyPosition = getPlayerPosition(playerID);
		
		//Remove the player from the lobby and update trackers
		lobbyLeaveEvent.accept(lobbyPosition);
		players[lobbyPosition] = null;
		playerCount--;
		
		//Update host reference if necessary
		if (playerID.equals(host))
			host = playerCount == 0 ? null : players[getOccupiedPositions().get(0)];
		
		//Notify player and all remaining players about the event
		OfflinePlayer player = getOfflinePlayer(playerID);
		
		if (player.isOnline())
			ToolbarMessage.clearSourceAndSendImmediate(player.getPlayer(), lobbyKey, 
					TextYml.getText(player.getPlayer(), "in-game.left")).setType(eType.ERROR).clearIfSkipped();
		
		promptJoinLeave("in-game.otherLeft", lobbyPosition, player);
					
		//Restore the player's inventory
		if (storeInventory)
			PlayerStateManager.removeGameState(playerID, lobbyKey);
		
		//Check if minimum player count has been lost
		if (playerCount == minimum - 1)
			onMinimumLost();
	}
	
	private void promptJoinLeave(String messagePath, int lobbyPosition, OfflinePlayer player) {
		forEachOnlinePlayer(x -> ToolbarMessage.sendImmediate(x, lobbyKey, 
					TextYml.getText(x, messagePath, 
						Placeholder.component(
							"label", (getPositionLabel(lobbyPosition, x) == null ? Component.empty() :
							Component.text(" (")
							.append(getPositionLabel(lobbyPosition, x))
							.append(Component.text(")")))
						),
						Placeholder.component("player", player.isOnline() ? player.getPlayer().displayName() : Component.text(player.getName()))
					)
				)				
				.clearIfSkipped().setPriority(1).setDuration(JOIN_LEAVE_PROMPT_DURATION));
	}
	
	public int getPlayerMax() { return maximum; }
	public int getPlayerMin() { return minimum; }
	public int getPlayerCount() { return playerCount; }
	public String getPlayerAt(int lobbyPosition) { return players[lobbyPosition]; }
	public String getHost() { return host; }
	public String getLobbyKey() { return lobbyKey; }
	
	/**
	 * @return A sorted array of all lobby positions that players are occupying
	 */
	public ArrayList<Integer> getOccupiedPositions() {
		ArrayList<Integer> list = new ArrayList<>();
		
		forEachPosition(i -> { if (players[i] != null) list.add(i); });
		return list;
	}
	
	/**
	 * Gets the lobby position of the playerID
	 * @param playerID
	 * @return
	 */
	public int getPlayerPosition(String playerID) {
		for (int i = 0; i < maximum; i++)
			if (players[i] != null && players[i].equals(playerID))
				return i;
		return -1;
	}
	
	/**
	 * Whether the specified player is in the lobby
	 * @param playerID
	 * @return
	 */
	public boolean hasPlayer(String playerID) 
	{ 
		for (String player : players)
			if (playerID.equals(player))
				return true;
		return false;
	}
	
	/**
	 * Destroys the lobby. Restores player states if applicable
	 */
	public void destroy() 
	{
		ToolbarMessage.clearAllFromSource(lobbyKey);
		Timer.cancelAllWithKey(lobbyKey);
		
		HandlerList.unregisterAll(this);
		
		if (storeInventory)
			forEachPlayer(x -> PlayerStateManager.removeGameState(x, lobbyKey));
	}
	
	public void forEachOnlinePlayer(Consumer<Player> action) {
		forEachPlayer(x -> {
			Player player = getOnlinePlayer(x);
			
			if (player != null)
				action.accept(player);
		});
	}
	
	public void forEachPlayer(Consumer<String> action) {
		forEachPosition(i -> {
			if (players[i] != null)
				action.accept(players[i]);
		});
	}
	
	public void forEachPosition(Consumer<Integer> action) {
		for (int i = 0; i < maximum; i++)
			action.accept(i);
	}
	
	private Player getOnlinePlayer(String playerID) { return Bukkit.getPlayer(UUID.fromString(playerID)); }
	private OfflinePlayer getOfflinePlayer(String playerID) { return Bukkit.getOfflinePlayer(UUID.fromString(playerID)); }
	
	@EventHandler
	//For some reason access needs to be set to public here for it to work,
	//which is odd because all other places allow the EventHandlers to be private.
	//My theory is that since this is a parent class, it doesn't work with private.
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		if (hasPlayer(event.getPlayer().getUniqueId().toString()) && isEnabled())
			attemptLobbyLeave(event.getPlayer().getUniqueId().toString());
	}
}
