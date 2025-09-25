package me.cayve.ludorium.games.tilemaps;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import me.cayve.ludorium.utils.Whitelist;

public class TileMapManager {

	private ArrayList<TileMap> tileMaps = new ArrayList<>();
	private Consumer<Integer> onTileInteraction;
	private Whitelist<Player> whitelistedPlayers = new Whitelist<>();
	private Whitelist<Integer> whitelistedTiles = new Whitelist<>();
	
	public TileMapManager(Consumer<Integer> onTileInteraction) {
		this.onTileInteraction = onTileInteraction;
		
		whitelistedPlayers.registerOnWhitelistUpdate(() -> tileMaps.forEach(x -> x.getPlayerWhitelist().copyFrom(whitelistedPlayers)));
		whitelistedTiles.registerOnWhitelistUpdate(() -> tileMaps.forEach(x -> x.getTileWhitelist().copyFrom(whitelistedTiles)));
	}
	
	public Whitelist<Player> getPlayerWhitelist() { return whitelistedPlayers; }
	public Whitelist<Integer> getTileWhitelist() { return whitelistedTiles; }
	public TileMap getMap(int mapIndex) { return tileMaps.get(mapIndex); }
	
	public void forEachMap(Consumer<TileMap> action) {
		for (TileMap map : tileMaps)
			action.accept(map);
	}
	
	public void registerNewMap(TileMap map) {
		tileMaps.add(map);
		map.registerOnTileInteraction(onTileInteraction);
	}
	
	public void destroy() {
		for (TileMap map : tileMaps)
			map.destroy();
		tileMaps.clear();
	}
}
