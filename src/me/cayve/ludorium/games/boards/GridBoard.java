package me.cayve.ludorium.games.boards;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.cayve.ludorium.utils.locational.Region;

public class GridBoard extends GameBoard implements Listener {

	private Region region;

	@EventHandler
	private void onBlockInteract(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				|| !region.isInRegion(event.getClickedBlock().getLocation())) return;
		
		//Convert block location and publish
	}
}
