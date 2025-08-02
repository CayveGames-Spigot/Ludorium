package me.cayve.ludorium.games.boards;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.cayve.ludorium.utils.animations.Animator;

public class BlockTileMap extends TileMap {

	private Block[] blocks;
	
	public BlockTileMap(Location[] locations) {
		this.blocks = new Block[locations.length];
		
		for (int i = 0; i < locations.length; i++)
			blocks[i] = locations[i].getBlock();
	}
	
	@EventHandler
	private void onBlockInteraction(PlayerInteractEvent event) {
		if (!ArrayUtils.contains(blocks, event.getClickedBlock()) || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == EquipmentSlot.OFF_HAND)
			return;
		
		publishTileInteraction(event.getPlayer(), ArrayUtils.indexOf(blocks, event.getClickedBlock()));
	}

	@Override
	public Animator[] getAnimators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTileIDAt(int index) {
		return null; //Block tile maps do not contain ID's
	}
}
