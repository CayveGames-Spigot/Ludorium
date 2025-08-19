package me.cayve.ludorium.actions;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.cayve.ludorium.utils.ProgressBar;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.patterns.GridAnimations;
import me.cayve.ludorium.utils.entities.BlockEntity;
import me.cayve.ludorium.utils.locational.Grid;
import me.cayve.ludorium.utils.locational.Region;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class SelectRegionAction extends PlayerAction {

	//Construction variables
	private SelectBlocksAction selectBlocksAction;
	
	private Component regionName;
	
	//Product
	private Region region;
	
	private Grid<BlockEntity> animationGrid;
	private final float ANIMATION_DURATION = 2;
	
	private boolean animateSelection = true;
	private boolean animateCompletion = true;
	
	@Override
	public void destroy() 
	{
		super.destroy(); //Unregisters own events
		
		selectBlocksAction.destroy();
		
		if (animationGrid != null)
			animationGrid.forEach((entity) -> entity.destroy());
	}
	
	public SelectRegionAction(Player player, Component regionName, Consumer<PlayerAction> successCallback, Consumer<PlayerAction> failureCallback) {
		super(player, successCallback, failureCallback);
		
		this.regionName = regionName;
		
		ToolbarMessage.clearSourceAndSend(player, tsk, 
				TextYml.getText(player, "actions.region.selectFirst",
					Placeholder.component("name", regionName),
					Placeholder.parsed("progress", ProgressBar.generate(0, 2))))
			.setPermanent();
		selectBlocksAction = new SelectBlocksAction(player, 2, false, this::onCompletedAction, this::onCancelledAction).registerSelectEvent(this::onBlockSelected);
		selectBlocksAction.setAnimationStates(animateSelection, false);
	}
	
	public void setAnimationStates(boolean animateSelection, boolean animateCompletion) {
		this.animateSelection = animateSelection;
		this.animateCompletion = animateCompletion;
		
		selectBlocksAction.setAnimationStates(animateSelection, false);
	}
	
	private void onBlockSelected(Block block) 
	{
		//Update message
		ToolbarMessage.clearSourceAndSend(player, tsk, 
				TextYml.getText(player, "actions.region.selectSecond",
					Placeholder.component("name", regionName),
					Placeholder.parsed("progress", ProgressBar.generate(selectBlocksAction.getSelectedCount(), 2))))
		.setPermanent();
	}
	
	protected void onCompletedAction(PlayerAction action) //Selected block action
	{
		//Create the region and publish action
		ArrayList<Block> selectedBlocks = selectBlocksAction.getBlocks();
		region = new Region(selectedBlocks.get(0).getLocation(), selectedBlocks.get(1).getLocation(), true);
		
		animateCompletion();
		
		delayedPublish(ANIMATION_DURATION); //Wait for animation to finish
	}
	
	protected void animateCompletion() {
		if (!animateCompletion) return;
		
		animationGrid = region.getLocationGrid().map(BlockEntity.class, 
				loc -> new BlockEntity(loc, loc.getBlock().getBlockData(),
						entity -> new Animator(entity.getDisplayTransform())));

		GridAnimations.wave(tsk, 
				animationGrid.map(Animator.class, (entity) -> entity.getComponent(Animator.class)), 
					region.relativeDirection(selectBlocksAction.getBlocks().get(1)), 
					ANIMATION_DURATION, 0.6f, .3f, .1f);
	}
	
	protected void onCancelledAction(PlayerAction action) //Selected block cancelled
	{
		if (selectBlocksAction.getSelectedCount() == 0) //If first selection, cancel region action
			cancelEvent();
		else //If second select, revert to first selection
		{
			ToolbarMessage.clearSourceAndSend(player, tsk, 
					TextYml.getText(player, "actions.region.selectFirst",
						Placeholder.component("name", regionName),
						Placeholder.parsed("progress", ProgressBar.generate(2, 2))))
			.setPermanent();
		}
	}
	
	public Region getRegion() { return region; }
}
