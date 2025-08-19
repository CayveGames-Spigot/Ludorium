package me.cayve.ludorium.actions;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.cayve.ludorium.actions.SubmitAction.eResult;
import me.cayve.ludorium.utils.ArrayUtils;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.ToolbarMessage.Message.eType;
import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.SinWaveAnimation;
import me.cayve.ludorium.utils.animations.patterns.ArrayAnimations;
import me.cayve.ludorium.utils.entities.BlockEntity;
import me.cayve.ludorium.utils.entities.DisplayEntity;
import me.cayve.ludorium.ymls.TextYml;

public class SelectBlocksAction extends PlayerAction implements Listener {

	private ArrayList<Block> selectedBlocks = new ArrayList<>();;
	private Consumer<Block> selectCallback;
	private SubmitAction submitAction;
	private int blockCount;
	private boolean allowSame;
	
	private ArrayList<DisplayEntity<BlockDisplay>> activeAnimations = new ArrayList<>();
	private boolean animateSelection = true;
	private boolean animateCompletion = true;
	
	//Constructor for selecting multiple blocks
	public SelectBlocksAction(Player player, int blockCount, boolean allowSame, Consumer<PlayerAction> successCallback, 
			Consumer<PlayerAction> failureCallback) {
		super(player, successCallback, failureCallback);
		this.blockCount = blockCount;
		this.allowSame = allowSame;
		
		submitAction = new SubmitAction(player, blockCount == -1 ? eResult.BOTH : eResult.CANCEL, this::onSubmit, this::onCancel);
	}
	
	//Constructor for selecting a single block
	public SelectBlocksAction(Player player, Consumer<PlayerAction> successCallback, Consumer<PlayerAction> failureCallback) {
		super(player, successCallback, failureCallback);
		this.blockCount = 1;
		this.allowSame = false;
		
		submitAction = new SubmitAction(player, eResult.CANCEL, this::onSubmit, this::onCancel);
	}
	
	//Callback for each selected block
	public SelectBlocksAction registerSelectEvent(Consumer<Block> selectCallback) {
		this.selectCallback = selectCallback;
		return this;
	}
	
	@Override
	public void destroy() {
		submitAction.destroy();
		
		for (DisplayEntity<?> animation : activeAnimations)
			animation.destroy();
		
		super.destroy();
	}
	
	@EventHandler
	private void onBlockClick(PlayerInteractEvent event) {
		if (isComplete) return;
		
		//This makes it so that the reminders only appear 10 seconds after the player doesn't do anything
		submitAction.restartReminder();
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK ||
				event.getHand() == EquipmentSlot.OFF_HAND ||
				!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
		
		if (selectedBlocks.contains(event.getClickedBlock()) && !allowSame) {
			ToolbarMessage.clearSourceAndSendImmediate(player, tsk, 
					TextYml.getText(player, "actions.selectBlocks.sameBlock")).setType(eType.ERROR);
			return;
		}
		
		selectedBlocks.add(event.getClickedBlock());
		
		player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, .75f, 1.4f + (.6f * new Random().nextFloat()));
		registerAnimation(event.getClickedBlock());
		
		if (selectCallback != null)
			selectCallback.accept(event.getClickedBlock());
		
		if (selectedBlocks.size() == blockCount)
			finalizeAction();
	}
	
	/**
	 * Sets whether the given animation states will trigger during this action
	 * @param animateSelection
	 * @param animateCompletion
	 */
	public void setAnimationStates(boolean animateSelection, boolean animateCompletion) {
		this.animateSelection = animateSelection;
		this.animateCompletion = animateCompletion;
	}
	
	private void registerAnimation(Block block) {
		//If neither animation cycles will happen, don't even register a new entity
		if (!animateSelection && !animateCompletion) return;
		
		BlockEntity newAnimation = new BlockEntity(block.getLocation(), block.getBlockData(),
				entity -> new Animator(entity.getDisplayTransform()));
		
		activeAnimations.add(newAnimation);
		
		if (animateSelection && !(blockCount == 1 && animateCompletion))
		{
			newAnimation.getComponent(Animator.class).onCompleted().subscribe(newAnimation::disable);
			newAnimation.getComponent(Animator.class).setYAnimation(new SinWaveAnimation(0.3f, 0.1f).subanim(0, 0.5f).setSpeed(1.5f));
		}
	}
	
	private void finalizeAction() {
		if (!animateCompletion)
		{
			publishEvent();
			return;
		}
		
		//Calculates an arbitrary "weight" for how many blocks were selected
		//40 represents the upper bound of blocks
		float weight = Math.clamp(selectedBlocks.size() / 40, 0, 1);
		
		float duration = 2 + (4 * weight);
		float overlap = 0.3f + (0.45f * (1 - weight));

		ArrayAnimations.wave(tsk, 
				ArrayUtils.map(ArrayUtils.toArray(activeAnimations, DisplayEntity.class), Animator.class, 
				(entity) -> entity.getComponent(Animator.class)), 
				duration, overlap, .3f, .1f);
		
		delayedPublish(duration);
	}
	
	//Player crouched to submit
	private void onSubmit(PlayerAction action) {
		if (selectedBlocks.size() == 0)
			cancelEvent();
		else
			finalizeAction();
	}
	
	//Player crouched to cancel
	private void onCancel(PlayerAction action) {
		cancelEvent();
	}

	public ArrayList<Block> getBlocks() { return selectedBlocks; }
	
	public ArrayList<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<Location>();
		
		for (Block block : selectedBlocks)
			locations.add(block.getLocation());
		
		return locations;
	}
	
	public Block getFirstBlock() { return selectedBlocks.get(0); }
	public int getSelectedCount() { return selectedBlocks.size(); }
	public float getProgress() { return selectedBlocks.size() / (float)blockCount; }
}
