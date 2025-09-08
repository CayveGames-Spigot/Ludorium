package me.cayve.ludorium.games.boards;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2f;

import me.cayve.ludorium.main.LudoriumException;
import me.cayve.ludorium.utils.ArrayListUtils;
import me.cayve.ludorium.utils.ArrayUtils;
import me.cayve.ludorium.utils.Collider;
import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.rigs.HoverAnimationRig;
import me.cayve.ludorium.utils.animations.rigs.PathingAnimationRig;
import me.cayve.ludorium.utils.entities.ItemEntity;

public class TokenTileMap extends TileMap {

	private class TokenMovement  {
		private String tokenID;
		private ArrayList<Integer> path;
		
		private Runnable startCallback, endCallback;
	}
	
	public record TokenInfo (ItemStack tokenItem, Vector2f tokenBounds) {}
	
	private Location[] tileLocations;
	private ItemEntity[] displayEntities;
	
	private HashMap<String, TokenInfo> tokenMapping;
	
	private Runnable onMapSet; //Called when the map is set (usually after token moving, to indicate animations are complete)
	
	private ArrayList<TokenMovement> tokenQueue = new ArrayList<>();
	private String[] pendingStateUpdate;
	
	/**
	 * Creates a new tile map
	 * @param tileLocations The world locations of each tile spot
	 * @param itemMapping The mapping of item type to token IDs. Uses .startsWith() allowing for just the beginning of the ID, if needed
	 * @param onMapSet Called when the map is set (usually after token moving, to indicate animations are complete)
	 */
	public TokenTileMap(Location[] tileLocations, HashMap<String, TokenInfo> tokenMapping, Runnable onMapSet) {
		super();
		
		this.tokenMapping = tokenMapping;
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
				token.getOriginTransform().setLocation(tileLocations[i]);
			
			displayEntities[i] = token;
		}
		
		onMapSet.run();
	}
	
	private ItemEntity createToken(String tokenID, int index) {

		for (String mapKey : tokenMapping.keySet()) {
			if (tokenID.startsWith(mapKey)) {
				ItemEntity newToken = new ItemEntity(tileLocations[index], tokenMapping.get(mapKey).tokenItem, tokenID,
						entity -> new Animator(entity.getOriginTransform(), entity.getDisplayTransform()),
						entity -> new Collider(entity.getDisplayTransform(), tokenMapping.get(mapKey).tokenBounds));

				newToken.getComponent(Collider.class).onInteracted().subscribe((player) -> publishTileInteraction(player, index));
				return newToken;
			}
		}
		
		throw new LudoriumException("Token map does not contain mapping for ID: " + tokenID);
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
		{
			tokenQueue.add(newMovement);
			
			if (tokenQueue.size() == 1)
				startMovement(newMovement);
		}
		else
			startMovement(newMovement);
	}
	
	private void startMovement(TokenMovement movement) {
		ItemEntity token = ArrayUtils.find(displayEntities, x -> x.getID().equals(movement.tokenID));

		Runnable[] jumpCallbacks = new Runnable[movement.path.size()];

		jumpCallbacks[movement.path.size() - 1] = this::endMovement;
		
		token.getComponent(Animator.class).play(new PathingAnimationRig(
				ArrayUtils.map(movement.path.toArray(new Integer[0]), Location.class, (i) -> tileLocations[i]), jumpCallbacks, .5f, 1));

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
		if (displayEntities[index] == null)
			return null;
		
		return displayEntities[index].getID();
	}
	
	@Override
	public void selectTile(int index, boolean overwriteSelected) {
		if (overwriteSelected)
			unselectTile(-1);
		
		if (index == -1 || displayEntities[index] == null)
			return;
		
		displayEntities[index].getComponent(Animator.class).play(new HoverAnimationRig(.4f, .1f, .1f, .1f));
	}
	
	@Override
	public void unselectTile(int index) {
		if (index == -1)
			for (int i = 0; i < displayEntities.length; i++)
				unselectTile(i);
		else if (displayEntities[index] != null)
			displayEntities[index].getComponent(Animator.class).cancelRigType(HoverAnimationRig.class);
	}

}
