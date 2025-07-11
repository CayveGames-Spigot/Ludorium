package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;

import me.cayve.ludorium.games.boards.BlockTileMap;
import me.cayve.ludorium.utils.locational.LocationUtil;
import me.cayve.ludorium.utils.locational.Region;
import me.cayve.ludorium.utils.locational.Vector3D;

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

	private Vector3D[] relativeLocations;
	
	private int tileCount;
	private int colorCount;
	
	public LudoMap(Vector3D[] relativeMap, int tileCount, boolean isSixPlayers) {
		this.relativeLocations = relativeMap;
		
		this.tileCount = tileCount;
		this.colorCount = isSixPlayers ? 6 : 4;
	}
	
	public LudoMap(ArrayList<Location> tiles, ArrayList<ArrayList<Location>> homes, ArrayList<Region> starters, ArrayList<Location> safeSpaces) {
		this.tileCount = tiles.size();
		this.colorCount = starters.size();
		
		relativeLocations = new Vector3D[tiles.size() + (homes.size() * HOME_TILE_COUNT) + 
		                                 (starters.size() * STARTER_TILE_COUNT) + safeSpaces.size()];
		
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
			//Calculates all relative corners of the starter region
			relativeLocations[getStarterIndex(i, 0)] = calculateRelativity(origin, 
					starters.get(i).getMinimum());
			relativeLocations[getStarterIndex(i, 1)] = calculateRelativity(origin, 
					LocationUtil.relativeLocation(starters.get(i).getMinimum(), 1, 0, 0));
			relativeLocations[getStarterIndex(i, 2)] = calculateRelativity(origin, 
					LocationUtil.relativeLocation(starters.get(i).getMinimum(), 0, 0, 1));
			relativeLocations[getStarterIndex(i, 3)] = calculateRelativity(origin, 
					LocationUtil.relativeLocation(starters.get(i).getMinimum(), 1, 0, 1));
		}
		
		//Determines relative location for all safe spaces
		int safeSpaceStartIndex = getStarterIndex(colorCount, STARTER_TILE_COUNT);
		
		for (int i = 0; i < relativeLocations.length; i++)
			relativeLocations[safeSpaceStartIndex + i] = calculateRelativity(origin, safeSpaces.get(i));
	}
	
	public int getHomeIndex(int color, int index) {
		return getTileIndex(tileCount) + (color * HOME_TILE_COUNT) + index;
	}
	
	public int getTileIndex(int index) {
		return index;
	}
	
	public int getStarterIndex(int color, int index) {
		return getHomeIndex(colorCount, HOME_TILE_COUNT) + (color * STARTER_TILE_COUNT) + index;
	}
	
	public Location getWorldLocation(Location origin, int index) {
		return LocationUtil.relativeLocation(origin, relativeLocations[index]);
	}
	
	public boolean isSafeSpace(int index) {
		int safeSpaceIndex = getStarterIndex(colorCount, STARTER_TILE_COUNT);
		
		while (safeSpaceIndex < relativeLocations.length) {
			if (relativeLocations[safeSpaceIndex].equals(relativeLocations[index])) {
				return true;
			}
			safeSpaceIndex++;
		}
		
		return false;
	}
	
	public boolean isSixPlayers() { return colorCount == 6; }
	public int getColorCount() { return colorCount; }
	public int getMapSize() { return getStarterIndex(colorCount, STARTER_TILE_COUNT); }
	
	public int getStartTile(int colorIndex) { return (tileCount / getColorCount()) * colorIndex; }
	public int getEndTile(int colorIndex) { return Math.floorMod(getStartTile(colorIndex) - 1, tileCount); }
	
	public BlockTileMap constructBlockTileMap(Location origin) {
		Block[] blocks = new Block[getMapSize()];
		
		for (int i = 0; i < getMapSize(); i++)
			blocks[i] = getWorldLocation(origin, i).getBlock();
		
		return new BlockTileMap(blocks);
	}
	
	private Vector3D calculateRelativity(Location origin, Location target) {
		return new Vector3D(
				(float)(target.getX() - origin.getX()),
				(float)(target.getY() - origin.getY()),
				(float)(target.getZ() - origin.getZ()));
	}
}
