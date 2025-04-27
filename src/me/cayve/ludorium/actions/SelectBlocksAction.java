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

import me.cayve.ludorium.actions.CrouchAction.eResult;
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
	private ArrayList<DisplayEntity<BlockDisplay>> activeAnimations = new ArrayList<>();
	private Consumer<Block> selectCallback;
	private CrouchAction submitAction;
	private int blockCount;
	private boolean allowSame;
	
	private boolean animateSelection = true;
	private boolean animateCompletion = true;
	
	//Constructor for selecting multiple blocks
	public SelectBlocksAction(Player player, int blockCount, boolean allowSame, Consumer<PlayerAction> successCallback, Consumer<PlayerAction> failureCallback) {
		super(player, successCallback, failureCallback);
		this.blockCount = blockCount;
		this.allowSame = allowSame;
		
		submitAction = new CrouchAction(player, blockCount == -1 ? eResult.BOTH : eResult.CANCEL, this::onSubmit, this::onCancel);
	}
	
	//Constructor for selecting a single block
	public SelectBlocksAction(Player player, Consumer<PlayerAction> successCallback, Consumer<PlayerAction> failureCallback) {
		super(player, successCallback, failureCallback);
		this.blockCount = 1;
		this.allowSame = false;
		
		submitAction = new CrouchAction(player, eResult.CANCEL, this::onSubmit, this::onCancel);
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
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK ||
				event.getHand() == EquipmentSlot.OFF_HAND ||
				!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
		
		if (selectedBlocks.contains(event.getClickedBlock()) && !allowSame) {
			ToolbarMessage.clearSourceAndSendImmediate(player, tsk, TextYml.getText("actions.selectBlocks.sameBlock")).setType(eType.ERROR);
			return;
		}
		
		selectedBlocks.add(event.getClickedBlock());
		
		player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, .75f, 1.4f + (.6f * new Random().nextFloat()));
		selectAnimation(event.getClickedBlock());
		
		if (selectCallback != null)
			selectCallback.accept(event.getClickedBlock());
		
		if (selectedBlocks.size() == blockCount)
			animateAndPublish();
	}
	
	public void setAnimationStates(boolean animateSelection, boolean animateCompletion) {
		this.animateSelection = animateSelection;
		this.animateCompletion = animateCompletion;
	}
	
	private void selectAnimation(Block block) {
		if (!animateSelection) return;
		
		BlockEntity newAnimation = new BlockEntity(block.getLocation(), block.getBlockData());
		
		activeAnimations.add(newAnimation);
		
		newAnimation.registerOnAnimatorComplete((entity) -> {
			entity.remove();
		});
		
		newAnimation.getAnimator().setYAnimation(new SinWaveAnimation(0.3f, 0.1f).subanim(0, 0.5f).setSpeed(1.5f));
	}
	
	private void animateAndPublish() {
		//If selection animation is enabled and there were less than 2 blocks selected, ignore the animation
		//since the blocks have already been animated
		if ((animateSelection && selectedBlocks.size() <= 2) || !animateCompletion) 
			publishEvent();
		
		//Calculates an arbitrary "weight" for how many blocks were selected
		//40 represents the upper bound of blocks
		float weight = Math.clamp(selectedBlocks.size() / 40, 0, 1);
		
		float duration = 1 + (4 * weight);
		float overlap = 0.3f + (0.45f * (1 - weight));

		ArrayAnimations.wave(tsk, 
				ArrayUtils.map(ArrayUtils.toArray(activeAnimations, DisplayEntity.class), Animator.class, (entity) -> entity.getAnimator()), 
				duration, overlap, .3f, .1f);
		
		delayedPublish(duration);
	}
	
	//Player crouched to submit
	private void onSubmit(PlayerAction action) {
		if (selectedBlocks.size() == 0)
			cancelEvent();
		else
			animateAndPublish();
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
