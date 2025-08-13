package me.cayve.ludorium.games.boards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import me.cayve.ludorium.main.LudoriumException;
import me.cayve.ludorium.utils.ArrayListUtils;
import me.cayve.ludorium.utils.ArrayUtils;
import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.patterns.TokenAnimations;
import me.cayve.ludorium.utils.entities.ItemEntity;

public class TokenTileMap extends TileMap {

	private class TokenMovement  {
		private String tokenID;
		private ArrayList<Integer> path;
		
		private Runnable startCallback, endCallback;
	}
	
	private Location[] tileLocations;
	private ItemEntity[] displayEntities;
	
	private HashMap<String, ItemStack> itemMapping;
	
	private Runnable onMapSet; //Called when the map is set (usually after token moving, to indicate animations are complete)
	
	private ArrayList<TokenMovement> tokenQueue = new ArrayList<>();
	private String[] pendingStateUpdate;
	
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
		this.displayEntities = new ItemEntity[tileLocations.length];
		this.onMapSet = onMapSet;
	}
	
	/**
	 * Updates the tile map to match the given state
	 * @param state the state to match
	 * @param forceUpdate Whether to wait for moving tokens queue to finish, or to force it immediately
	 */
	public void setState(String[] state, boolean forceUpdate) {
		if (!forceUpdate && !tokenQueue.isEmpty()) {
			pendingStateUpdate = state;
			return;
		}
		pendingStateUpdate = null;
		
		ArrayListUtils<ItemEntity> activeDisplays = new ArrayListUtils<>();
		
		for (ItemEntity entity : displayEntities)
		{
			if (entity == null) continue;
			
			if (ArrayUtils.contains(state, x -> x.equals(entity.getID())))
				activeDisplays.add(entity);
			else
				entity.destroy();
		}
		
		displayEntities = new ItemEntity[state.length];
		
		//Loop each new state, moving existing tokens, or creating new ones to match state IDs
		for (int i = 0; i < state.length; i++) {
			if (state[i] == null || state[i].equals("")) continue;
			final int index = i;
			
			ItemEntity token = activeDisplays.find(x -> x.getID().equals(state[index]));
			
			if (token == null)
				token = createToken(state[i], i);
			else
				token.move(tileLocations[i]);
			
			displayEntities[i] = token;
		}
		
		onMapSet.run();
	}
	
	private ItemEntity createToken(String tokenID, int index) {
		for (String key : itemMapping.keySet()) {
			if (tokenID.startsWith(key))
			{
				ItemEntity newToken = new ItemEntity(tileLocations[index], itemMapping.get(key));
				newToken.spawn();
				return newToken;
			}
		}
		
		throw new LudoriumException("Item map does not contain mapping for ID: " + tokenID);
	}
	
	/**
	 * Moves a token down the specified path
	 * @param tokenID The token to move
	 * @param path The path to follow
	 * @param queueMovement Whether to wait for other tokens to finish their movement
	 * @param startCallback Called when the token begins to move
	 * @param endCallback Called when the token finishes moving
	 */
	public void moveToken(String tokenID, ArrayList<Integer> path, boolean queueMovement, Runnable startCallback, Runnable endCallback) {
		TokenMovement newMovement = new TokenMovement();
		
		newMovement.tokenID = tokenID;
		newMovement.path = path;
		newMovement.startCallback = startCallback;
		newMovement.endCallback = endCallback;
		
		if (queueMovement)
			tokenQueue.add(newMovement);
		else
			startMovement(newMovement);
	}
	
	private void startMovement(TokenMovement movement) {
		ItemEntity token = ArrayUtils.find(displayEntities, x -> x.getID().equals(movement.tokenID));
		
		Runnable[] jumpCallbacks = new Runnable[movement.path.size()];
		Arrays.fill(jumpCallbacks, null);
		jumpCallbacks[movement.path.size() - 1] = this::endMovement;
		
		TokenAnimations.jumpTo(token.getAnimator(), 
				ArrayUtils.map(movement.path.toArray(new Integer[0]), Location.class, (i) -> tileLocations[i]), jumpCallbacks, 5, 1);
		
		if (movement.startCallback != null)
			movement.startCallback.run();
	}
	
	private void endMovement() {
		if (tokenQueue.getLast().endCallback != null)
			tokenQueue.getLast().endCallback.run();
		
		tokenQueue.removeFirst();
		
		if (!tokenQueue.isEmpty())
			startMovement(tokenQueue.getFirst());
		else if (pendingStateUpdate != null)
			setState(pendingStateUpdate, true);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		for (ItemEntity entity : displayEntities)
			if (entity != null)
				entity.destroy();
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
