package me.cayve.ludorium.games.boards;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import me.cayve.ludorium.utils.animations.Animator;

public class BlockTileMap extends TileMap {

	private Block[] blocks;
	
	public BlockTileMap(Block[] blocks) {
		this.blocks = blocks;
	}
	
	@EventHandler
	private void onBlockInteraction(PlayerInteractEvent event) {
		if (!ArrayUtils.contains(blocks, event.getClickedBlock()))
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
		return null;
	}
}
