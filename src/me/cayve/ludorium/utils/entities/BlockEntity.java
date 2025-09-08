package me.cayve.ludorium.utils.entities;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display.Brightness;

import me.cayve.ludorium.utils.locational.LocationUtil;

public class BlockEntity extends DisplayEntity<BlockDisplay> {

	private BlockData blockData;
	private int lightLevel = -1;
	
	@SafeVarargs
	public BlockEntity(Location location, BlockData blockData, 
			Function<DisplayEntity<BlockDisplay>, EntityComponent>... componentFactory) { 
		construct(location, blockData, null, componentFactory); 
	}
	@SafeVarargs
	public BlockEntity(Location location, BlockData blockData, String displayID, 
			Function<DisplayEntity<BlockDisplay>, EntityComponent>... componentFactory) { 
		construct(location, blockData, displayID, componentFactory); 
	}
	
	@SafeVarargs
	protected final void construct(Location location, BlockData blockData, String displayID, 
			Function<DisplayEntity<BlockDisplay>, EntityComponent>... componentFactory) {
		this.blockData = blockData;
		
		construct(BlockDisplay.class, location, displayID, componentFactory);
		
		originTransform.onUpdated().subscribe(this::setLightLevel);
	}
	
	/**
	 * Sets the default light level to that of the block above
	 */
	private void setLightLevel() {
		Location blockAbove = LocationUtil.relativeLocation(originTransform.getLocation(), 0, 1, 0);
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
	public void enable() {
		super.enable();
		
		setBlockData(blockData);
		
		if (lightLevel == -1)
			setLightLevel();
		else
			setLightLevel(lightLevel);
	}
}
