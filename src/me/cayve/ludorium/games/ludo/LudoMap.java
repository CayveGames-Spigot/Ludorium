package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.cayve.ludorium.utils.locational.LocationUtil;
import me.cayve.ludorium.utils.locational.Region;

/**
 * @author Cayve
 * @license GPL v3
 * @repository https://github.com/CayveGames-Spigot/Ludorium
 * @created 7/1/2025
 * 
 * @description
 * Represents the map layout of a custom board that can be reused among templates.
 * Can use world locations to be constructed that later can be used as templates
 * for boards placed elsewhere.
 * 
 * Also represents the custom layout array and should be referenced anywhere a board index is exchanged to promote consistency.
 */
public class LudoMap {

	private static int 	HOME_TILE_COUNT = 4,
						STARTER_TILE_COUNT = 4;
	
	private String mapID;

	private Vector[] relativeLocations;
	private ArrayList<Integer> safeSpaceIndexes = new ArrayList<>();
	
	private int tileCount;
	private int colorCount;
	
	public LudoMap(Vector[] relativeMap, String mapID, int tileCount, boolean isSixPlayers) {
		this.relativeLocations = relativeMap;
		this.mapID = mapID;
		
		this.tileCount = tileCount;
		this.colorCount = isSixPlayers ? 6 : 4;
	}
	
	public LudoMap(ArrayList<Location> tiles, ArrayList<ArrayList<Location>> homes, ArrayList<Region> starters, ArrayList<Location> safeSpaces) {
		this.mapID = UUID.randomUUID().toString();
		
		this.tileCount = tiles.size();
		this.colorCount = starters.size();
		
		relativeLocations = new Vector[tiles.size() + (homes.size() * HOME_TILE_COUNT) + 
		                                 (starters.size() * STARTER_TILE_COUNT)];
		
		Location origin = tiles.getFirst(); //Should always be the red out tile
		
		//Determines relative location for all board tiles
		for (int i = 0; i < tiles.size(); i++)
			relativeLocations[getTileIndex(i)] = calculateRelativity(origin, tiles.get(i));
		
		//Determines relative location for all home tiles
		for (int i = 0; i < homes.size(); i++)
			for (int j = 0; j < HOME_TILE_COUNT; j++)
				relativeLocations[getHomeIndex(i, j)] = calculateRelativity(origin, homes.get(i).get(j));
		
		//Determines relative location for all starter positions
		for (int i = 0; i < starters.size(); i++) {
			float length = starters.get(i).getXLength() / 4f; //Centers the 4 pieces halfway between the center and the edge
			Location center = starters.get(i).getCenter();

			//Calculates all relative corners of the starter region
			relativeLocations[getStarterIndex(i, 0)] = calculateRelativity(origin, 
					LocationUtil.relativeLocation(center, length, 0, length));
			relativeLocations[getStarterIndex(i, 1)] = calculateRelativity(origin, 
					LocationUtil.relativeLocation(center, -length, 0, length));
			relativeLocations[getStarterIndex(i, 2)] = calculateRelativity(origin, 
					LocationUtil.relativeLocation(center, length, 0, -length));
			relativeLocations[getStarterIndex(i, 3)] = calculateRelativity(origin, 
					LocationUtil.relativeLocation(center, -length, 0, -length));
		}
		
		//Determines relative location for all safe spaces
		for (int i = 0; i < safeSpaces.size(); i++) {
			Vector safeSpace = calculateRelativity(origin, safeSpaces.get(i));
			
			for (int j = 0; j < getMapSize(); j++)
				if (relativeLocations[j].equals(safeSpace))
					this.safeSpaceIndexes.add(j);
		}

	}
	
	public String getMapID() { return mapID; }
	
	public int getHomeIndex(int color, int index) {
		return getTileIndex(tileCount) + (color * HOME_TILE_COUNT) + index;
	}
	
	public int getTileIndex(int index) {
		return index;
	}
	
	public int getStarterIndex(int color, int index) {
		return getHomeIndex(colorCount, 0) + (color * STARTER_TILE_COUNT) + index;
	}
	
	public Location getStarterCenter(Location origin, int color) {
		return new Region(
				getWorldLocation(origin, getStarterIndex(color, 0)), 
				getWorldLocation(origin, getStarterIndex(color, 3)), false).getCenter();
	}
	
	public Location getWorldLocation(Location origin, int index) {
		return LocationUtil.relativeLocation(origin, relativeLocations[index]);
	}
	
	public boolean isSafeSpace(int index) {
		return safeSpaceIndexes.contains(index);
	}
	
	public boolean isSixPlayers() { return colorCount == 6; }
	public int getTileCount() { return tileCount; }
	public Vector[] getRelativeLocations() { return relativeLocations; }
	public int getColorCount() { return colorCount; }
	public int getMapSize() { return relativeLocations.length; }
	
	public int getStartTile(int colorIndex) { return (tileCount / getColorCount()) * colorIndex; }
	public int getEndTile(int colorIndex) { return Math.floorMod(getStartTile(colorIndex) - 1, tileCount); }
	
	public Location[] mapFromOrigin(Location origin) {
		Location[] locations = new Location[getMapSize()];
		
		for (int i = 0; i < getMapSize(); i++)
			locations[i] = getWorldLocation(origin, i);
		
		return locations;
	}
	
	private Vector calculateRelativity(Location origin, Location target) {
		return new Vector(
				(float)(target.getX() - origin.getX()),
				(float)(target.getY() - origin.getY()),
				(float)(target.getZ() - origin.getZ()));
	}
}
