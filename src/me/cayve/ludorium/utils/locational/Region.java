package me.cayve.ludorium.utils.locational;

import org.bukkit.Location;

import me.cayve.ludorium.utils.eDirection;
import me.cayve.ludorium.utils.entities.BlockEntity;

public class Region {
	private int xLength, yLength, zLength;
	private Location minimum, maximum;
	
	public Region(Location cornerOne, Location cornerTwo) {
		this.minimum = LocationUtil.bottomNorthWestCorner(cornerOne, cornerTwo);
		this.maximum = LocationUtil.upperSouthEastCorner(cornerOne, cornerTwo);
		
		xLength = maximum.getBlockX() - minimum.getBlockX() + 1;
		yLength = maximum.getBlockY() - minimum.getBlockY() + 1;
		zLength = maximum.getBlockZ() - minimum.getBlockZ() + 1;
	}
	
	public boolean isInRegion(Location location) {
		return location.getX() >= minimum.getX() && location.getX() <= maximum.getX()
				&& location.getY() >= minimum.getY() && location.getY() <= maximum.getY()
				&& location.getZ() >= minimum.getZ() && location.getZ() <= maximum.getZ();
	}
	
	/**
	 * Returns the 3D grid of locations between the minimum and maximum point of this region
	 * @return
	 */
	public Grid3D<Location> get3DLocationGrid() {
		Grid3D<Location> grid = new Grid3D<Location>(Location.class, xLength, yLength, zLength);
		
		for (int x = 0; x < xLength; x++) {
			for (int y = 0; y < yLength; y++) {
				for (int z = 0; z < zLength; z++) {
					grid.set(x, y, z, LocationUtil.relativeLocation(minimum, x, y, z));
				}
			}
		}
		
		return grid;
	}
	
	/**
	 * Returns the grid of locations between the minimum and maximum point of this region.
	 * Y is sourced from the minimum point
	 * @return [x][z]
	 */
	public Grid2D<Location> get2DLocationGrid() {
		Grid2D<Location> grid = new Grid2D<Location>(Location.class, xLength, zLength);
		
		for (int x = 0; x < xLength; x++) {
				for (int z = 0; z < zLength; z++) {
					grid.set(x, z, LocationUtil.relativeLocation(minimum, x, 0, z));
				}
		}
		
		return grid;
	}
	
	/**
	 * Generates a grid of BlockEntities based on the blocks in the 2D region.
	 * Can return null cells.
	 * @return
	 */
	public Grid2D<BlockEntity> generate2DDisplayGrid() {
		return get2DLocationGrid().map(BlockEntity.class, (location) -> {
			return new BlockEntity(location, location.getBlock().getBlockData());
		});
	}
	
	public Location getCenter() {
		return LocationUtil.relativeLocation(minimum, xLength/2f, yLength/2f, zLength/2f);
	}
	
	public eDirection relativeDirection(Location location) {
		Location center = getCenter();

		return eDirection.fromVector(
				new Vector2DInt(
						center.getX() == location.getX() ? 0 : location.getX() > center.getX() ? 1 : -1,
						center.getZ() == location.getZ() ? 0 : location.getZ() > center.getZ() ? 1 : -1
				));
	}
}