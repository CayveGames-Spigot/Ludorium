package me.cayve.ludorium.utils.entities;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display.Brightness;

import me.cayve.ludorium.utils.locational.LocationUtil;
import me.cayve.ludorium.utils.locational.Vector3D;

public class BlockEntity extends DisplayEntity<BlockDisplay> {

	private BlockData blockData;
	private int lightLevel;
	
	public BlockEntity(Location location, BlockData blockData) {
		super(BlockDisplay.class, location);
		
		setLightLevel();
		setBlockData(blockData);
	}
	
	/**
	 * Sets the default light level to that of the block above
	 */
	private void setLightLevel() {
		Location blockAbove = LocationUtil.relativeLocation(transform.getLocation(), 0, 1, 0);
		setLightLevel(Math.max(blockAbove.getBlock().getLightFromBlocks(), blockAbove.getBlock().getLightFromSky()));
	}
	
	public void setLightLevel(int lightLevel) {
		this.lightLevel = lightLevel;
		
		if (display != null)
			display.setBrightness(new Brightness(lightLevel, lightLevel));
	}
	
	public void setBlockData(BlockData blockData) {
		this.blockData = blockData;
		
		if (display != null)
			display.setBlock(blockData);
	}
	
	@Override
	public void move(Location location) {
		super.move(location);
		
		setLightLevel();
	}
	
	@Override
	public void move(Vector3D position) {
		super.move(position);
		
		setLightLevel();
	}
	
	@Override
	public BlockDisplay spawn(boolean rawSpawn) {
		super.spawn(rawSpawn);
		
		setBlockData(blockData);
		setLightLevel(lightLevel);
		
		return display;
	}
}
