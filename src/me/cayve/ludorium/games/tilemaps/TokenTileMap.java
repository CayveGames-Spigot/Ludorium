package me.cayve.ludorium.games.tilemaps;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2f;
import org.joml.Vector3f;

import com.destroystokyo.paper.ParticleBuilder;

import me.cayve.ludorium.main.LudoriumException;
import me.cayve.ludorium.utils.ArrayListUtils;
import me.cayve.ludorium.utils.ArrayUtils;
import me.cayve.ludorium.utils.Collider;
import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.LinearAnimation;
import me.cayve.ludorium.utils.animations.rigs.HoverAnimationRig;
import me.cayve.ludorium.utils.animations.rigs.PathingAnimationRig;
import me.cayve.ludorium.utils.entities.ItemEntity;
import me.cayve.ludorium.utils.particles.ParticleEmitter;
import me.cayve.ludorium.utils.particles.ParticleRig;
import me.cayve.ludorium.utils.particles.ParticleStroke;

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

		ArrayList<ItemEntity> activeDisplays = new ArrayList<>();
		
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
			
			ItemEntity token = ArrayListUtils.find(activeDisplays, x -> x.getID().equals(state[index]));
			
			if (token == null)
				token = createToken(state[i], i);
			else
				token.getPositionTransform().setLocation(tileLocations[i]);
			
			token.getPositionTransform().resetOffsets();
			
			displayEntities[i] = token;
		}
		
		if (onMapSet != null)
			onMapSet.run();
	}
	
	private ItemEntity createToken(String tokenID, int index) {

		for (String mapKey : tokenMapping.keySet()) {
			if (tokenID.startsWith(mapKey)) {
				ItemEntity newToken = new ItemEntity(tileLocations[index], tokenMapping.get(mapKey).tokenItem, tokenID,
						entity -> new Animator(entity.getPositionTransform()),
						entity -> new Collider(entity.getPositionTransform(), tokenMapping.get(mapKey).tokenBounds),
						entity -> new ParticleEmitter(entity.getPositionTransform()));

				newToken.getComponent(Collider.class).onInteracted().subscribe((player) -> 
					publishTileInteraction(player, getTileIndexOf(newToken.getID())));
				
				//Creates a vertical red line above the token when the token is highlighted
				ParticleRig rig = new ParticleRig()
						.addStroke(
								new ParticleStroke(
										new ParticleBuilder(Particle.DUST).color(Color.MAROON, .5f).count(1),
										r -> r.setYAnimation(new LinearAnimation(0, 0.25f))).setTimeIncrement(.5f));
				rig.getLocalPosition().setOffset(new Vector3f(0, 1, 0));
				newToken.getComponent(ParticleEmitter.class).play(rig);
				
				newToken.getComponent(ParticleEmitter.class).disable();
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
	
	private int getTileIndexOf(String tileID) {
		for (int i = 0; i < displayEntities.length; i++)
			if (displayEntities[i] != null && displayEntities[i].getID().equals(tileID))
				return i;
		return -1;
	}
	
	@Override
	public void highlightTile(int index, boolean overwriteHighlighted) {
		if (overwriteHighlighted)
			unhighlightTile(-1);

		if (index == -1)
			ArrayUtils.forEachIndex(displayEntities, i -> highlightTile(i, false));
		else if (displayEntities[index] != null)
			displayEntities[index].getComponent(ParticleEmitter.class).enable();
	}
	
	@Override
	public void unhighlightTile(int index) {
		if (index == -1)
			ArrayUtils.forEachIndex(displayEntities, this::unhighlightTile);
		else if (displayEntities[index] != null)
			displayEntities[index].getComponent(ParticleEmitter.class).disable();
	}
	
	@Override
	public void selectTile(int index, boolean overwriteSelected) {
		if (overwriteSelected)
			unselectTile(-1);
		
		if (index == -1)
			ArrayUtils.forEachIndex(displayEntities, i -> selectTile(i, false));
		else if (displayEntities[index] != null)
			displayEntities[index].getComponent(Animator.class).play(new HoverAnimationRig(.4f, .1f, .1f, .1f));
	}
	
	@Override
	public void unselectTile(int index) {
		if (index == -1)
			ArrayUtils.forEachIndex(displayEntities, this::unselectTile);
		else if (displayEntities[index] != null)
			displayEntities[index].getComponent(Animator.class).cancelRigType(HoverAnimationRig.class);
	}

}
