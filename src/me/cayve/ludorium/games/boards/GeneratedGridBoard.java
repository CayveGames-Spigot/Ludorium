package me.cayve.ludorium.games.boards;

import org.bukkit.Material;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class GeneratedGridBoard extends GeneratedBoard implements Listener {
	
	private float scale;
	private Material typeOne, typeTwo;
	
	public GeneratedGridBoard(float scale, Material typeOne, Material typeTwo) {
		this.scale = scale;
		this.typeOne = typeOne;
		this.typeTwo = typeTwo;
	}
	
	protected void generateBoard() {
		
	}
	
	@EventHandler
	private void onInteraction(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Interaction)) return;
		
		// check if interaction is grid
		// convert and publish
	}
}
