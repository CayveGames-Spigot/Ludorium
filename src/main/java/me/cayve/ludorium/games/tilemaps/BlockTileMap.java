package me.cayve.ludorium.games.tilemaps;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.joml.Vector3f;

import com.destroystokyo.paper.ParticleBuilder;

import me.cayve.ludorium.utils.animation.Animator;
import me.cayve.ludorium.utils.locational.Transform;
import me.cayve.ludorium.utils.particles.CircleParticleRig;
import me.cayve.ludorium.utils.particles.ParticleEmitter;

public class BlockTileMap extends TileMap {

	private ParticleEmitter[] emitters;
	private Block[] blocks;
	
	public BlockTileMap(Location[] locations) {
		this.blocks = new Block[locations.length];
		this.emitters = new ParticleEmitter[locations.length];
		
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
	
	@Override
	public void destroy() {
		super.destroy();
		
		me.cayve.ludorium.utils.ArrayUtils.forEach(emitters, i -> i.destroy());
	}
	
	@Override
	public void highlightTile(int index, boolean overwriteHighlighted) {
		if (overwriteHighlighted)
			unhighlightTile(-1);

		if (index == -1)
			me.cayve.ludorium.utils.ArrayUtils.forEachIndex(emitters, i -> highlightTile(i, false));
		else if (emitters[index] == null)
		{
			emitters[index] = new ParticleEmitter(new Transform().setLocation(blocks[index].getLocation()).setOffset(new Vector3f(0.5f, 1, 0.5f)));
			emitters[index].play(new CircleParticleRig(new ParticleBuilder(Particle.DUST).color(Color.MAROON, 0.5f).count(1), .5f));
		}
	}
	
	@Override
	public void unhighlightTile(int index) {
		if (index == -1)
			me.cayve.ludorium.utils.ArrayUtils.forEachIndex(emitters, this::unhighlightTile);
		else if (emitters[index] != null)
		{
			emitters[index].destroy();
			emitters[index] = null;
		}
	}
}
