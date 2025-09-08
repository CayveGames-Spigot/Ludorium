package me.cayve.ludorium.games.boards;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.Whitelist;
import me.cayve.ludorium.utils.animations.Animator;

public abstract class TileMap implements Listener {

	private Consumer<Integer> onTileInteraction;
	private Whitelist<Player> whitelistedPlayers = new Whitelist<>();
	private Whitelist<Integer> whitelistedTiles = new Whitelist<>();
	
	public TileMap() {
		//All tile maps are listeners
		LudoriumPlugin.registerEvent(this);
	}
	
	public void registerOnTileInteraction(Consumer<Integer> listener) { onTileInteraction = listener; }
	
	public Whitelist<Player> getPlayerWhitelist() { return whitelistedPlayers; }
	public Whitelist<Integer> getTileWhitelist() { return whitelistedTiles; }
	
	public abstract Animator[] getAnimators();
	public abstract String getTileIDAt(int index);
	
	/**
	 * Applies this tile map's select effect to the tile index
	 * @param index The tile to select
	 * @param overwriteSelected Whether to unselect any already selected tiles as well
	 */
	public void selectTile(int index, boolean overwriteSelected) {}
	
	/**
	 * Unselects a tile index. -1 to unselect all
	 * @param index
	 */
	public void unselectTile(int index) {}
	
	/**
	 * Applies this tile map's highlight effect to the tile index
	 * @param index The tile to select
	 * @param overwriteSelected Whether to unhighlight any already highlighted tiles as well
	 */
	public void highlightTile(int index, boolean overwriteHighlighted) {}
	/**
	 * Unhighlights a tile index. -1 to unhighlight all
	 * @param index
	 */
	public void unhighlightTile(int index) {}
	
	protected void publishTileInteraction(Player player, Integer tile) {
		if (!whitelistedPlayers.verify(player) || !whitelistedTiles.verify(tile))
			return;
		
		onTileInteraction.accept(tile);
	}
	
	public void destroy() {
		HandlerList.unregisterAll(this);
	}
}
