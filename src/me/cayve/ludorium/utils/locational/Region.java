package me.cayve.ludorium.utils.locational;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.joml.Vector2i;

public class Region {
	private float xLength, yLength, zLength;
	private Location minimum, maximum;
	
	public Region(Location cornerOne, Location cornerTwo, boolean isBlockCoordinate) {
		this.minimum = LocationUtil.bottomNorthWestCorner(cornerOne, cornerTwo);
		this.maximum = LocationUtil.upperSouthEastCorner(cornerOne, cornerTwo);
		
		int adjustment = isBlockCoordinate ? 1 : 0;
		xLength = (float)(maximum.getX() - minimum.getX()) + adjustment;
		yLength = (float)(maximum.getY() - minimum.getY()) + adjustment;
		zLength = (float)(maximum.getZ() - minimum.getZ()) + adjustment;
	}
	
	public boolean isInRegion(Location location) {
		return location.getX() >= minimum.getX() && location.getX() <= maximum.getX()
				&& location.getY() >= minimum.getY() && location.getY() <= maximum.getY()
				&& location.getZ() >= minimum.getZ() && location.getZ() <= maximum.getZ();
	}
	
	public float getXLength() { return xLength; }
	public float getYLength() { return yLength; }
	public float getZLength() { return zLength; }
	public float getArea() { return xLength * yLength * zLength; }
	public Location getMinimum() { return minimum; }
	public Location getMaximum() { return maximum; }
	
	/**
	 * Returns the 3D grid of block locations between the minimum and maximum point of this region
	 * @return
	 */
	public Grid<Location> getLocationGrid() {
		Grid<Location> grid = new Grid<Location>(Location.class, 
				(int)Math.floor(xLength), (int)Math.floor(yLength), (int)Math.floor(zLength));
		
		for (int x = 0; x < xLength; x++) {
			for (int y = 0; y < yLength; y++) {
				for (int z = 0; z < zLength; z++) {
					grid.set(x, y, z, LocationUtil.relativeLocation(minimum, x, y, z));
				}
			}
		}
		
		return grid;
	}
	
	public Location getCenter() {
		return LocationUtil.relativeLocation(minimum, xLength/2f, yLength/2f, zLength/2f);
	}
	
	/**
	 * Calculates the direction the given location is relative to the center of the region
	 * @param location
	 * @return
	 */
	public eDirection relativeDirection(Location location) {
		Location center = getCenter();

		return eDirection.fromVector(
				new Vector2i(
						center.getX() == location.getX() ? 0 : location.getX() > center.getX() ? 1 : -1,
						center.getZ() == location.getZ() ? 0 : location.getZ() > center.getZ() ? 1 : -1
				));
	}
	
	/**
	 * Calculates the direction the center of the given block is relative to the center of the region
	 * @param block
	 * @return
	 */
	public eDirection relativeDirection(Block block) {
		return relativeDirection(LocationUtil.relativeLocation(block.getLocation(), 0.5f, 0.5f, 0.5f));
	}
}