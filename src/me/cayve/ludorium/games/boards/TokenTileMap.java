package me.cayve.ludorium.games.boards;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.entities.ItemEntity;

public class TokenTileMap extends TileMap {

	private Location[] tileLocations;
	private ItemEntity[] displayEntities;
	
	private HashMap<String, ItemStack> itemMapping;
	
	private Runnable onMapSet; //Called when the map is set (usually after token moving, to indicate animations are complete)
	
	/**
	 * Creates a new tile map
	 * @param tileLocations The world locations of each tile spot
	 * @param itemMapping The mapping of item type to token IDs. Uses .startsWith() allowing for just the beginning of the ID, if needed
	 * @param onMapSet
	 */
	public TokenTileMap(Location[] tileLocations, HashMap<String, ItemStack> itemMapping, Runnable onMapSet) {
		super();
		
		this.itemMapping = itemMapping;
		this.tileLocations = tileLocations;
		this.onMapSet = onMapSet;
	}
	
	/**
	 * Updates the tile map to match the given state
	 * @param state the state to match
	 * @param forceUpdate Whether to wait for moving tokens to finish, or to force it immediately
	 */
	public void setState(String[] state, boolean forceUpdate) {
		
	}
	
	/**
	 * Moves a token down the specified path
	 * @param tokenID The token to move
	 * @param path The path to follow
	 * @param waitToMove Whether to wait for other tokens to finish their movement
	 * @param startCallback Called when the token begins to move
	 * @param endCallback Called when the token finishes moving
	 */
	public void moveToken(String tokenID, ArrayList<Integer> path, boolean waitToMove, Runnable startCallback, Runnable endCallback) {
		
	}
	
	@Override
	public Animator[] getAnimators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTileIDAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
