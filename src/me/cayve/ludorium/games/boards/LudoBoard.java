package me.cayve.ludorium.games.boards;

import java.util.ArrayList;

import org.bukkit.Location;

import me.cayve.ludorium.utils.locational.Region;

public class LudoBoard extends GameBoard {

	public ArrayList<Location> tiles = new ArrayList<Location>();
	public ArrayList<Location> homeTiles = new ArrayList<Location>();
	public ArrayList<Location> outTiles = new ArrayList<Location>();
	public ArrayList<Region> centerPlates = new ArrayList<Region>();
	public ArrayList<Location> diagonalPairs = new ArrayList<Location>();
	
	public static LudoBoard identifyBoard(Region region, boolean isSixPlayers) {
		
		return null;
		
	}
	
	public void animate() {
		
	}
	
	public void destroy() {
		
	}
}
