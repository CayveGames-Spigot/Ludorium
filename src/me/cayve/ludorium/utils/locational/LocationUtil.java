package me.cayve.ludorium.utils.locational;

import org.bukkit.Location;

public class LocationUtil {

	public static Location blockCenter(Location loc) {
		Location newLoc = loc.clone();
		newLoc.setX(loc.getBlockX() + 0.5f);
		newLoc.setZ(loc.getBlockZ() + 0.5f);
		return newLoc;
	}

	public static Location blockLocation(Location loc) {
		Location newLoc = loc.clone();
		newLoc.setX(loc.getBlockX());
		newLoc.setY(loc.getBlockY());
		newLoc.setZ(loc.getBlockZ());
		return newLoc;
	}

	public static Location relativeLocation(Location loc, float x, float y, float z) {
		Location newLoc = loc.clone();
		newLoc.setX(loc.getX() + x);
		newLoc.setY(loc.getY() + y);
		newLoc.setZ(loc.getZ() + z);
		return newLoc;
	}

	public static Location relativeLocation(Location loc, Vector3D vector) {
		Location newLoc = loc.clone();
		newLoc.setX(loc.getX() + vector.x);
		newLoc.setY(loc.getY() + vector.y);
		newLoc.setZ(loc.getZ() + vector.z);
		return newLoc;
	}

	public static Location bottomNorthWestCorner(Location locOne, Location locTwo) {
		return new Location(locOne.getWorld(), Math.min(locOne.getX(), locTwo.getX()), Math.min(locOne.getY(), locTwo.getY()), Math.min(locOne.getZ(), locTwo.getZ()));
	}
	
	public static Location upperSouthEastCorner(Location locOne, Location locTwo) {
		return new Location(locOne.getWorld(), Math.max(locOne.getX(), locTwo.getX()), Math.max(locOne.getY(), locTwo.getY()), Math.max(locOne.getZ(), locTwo.getZ()));
	}
}
