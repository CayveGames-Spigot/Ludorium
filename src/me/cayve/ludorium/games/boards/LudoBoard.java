package me.cayve.ludorium.games.boards;

import java.util.ArrayList;

import org.bukkit.Location;

import me.cayve.ludorium.games.lobbies.InteractionLobby;
import me.cayve.ludorium.utils.CustomModel;
import me.cayve.ludorium.utils.locational.Region;
import me.cayve.ludorium.utils.locational.Vector2D;

public class LudoBoard extends GameBoard {

	public static final String[] COLOR_ORDER = { "red", "yellow", "green", "blue", "purple", "black" };
	
	public static class TileLocations {
		public ArrayList<Location> tiles = new ArrayList<Location>();
		public ArrayList<Location> homeTiles = new ArrayList<Location>();
		public ArrayList<Location> outTiles = new ArrayList<Location>();
		public ArrayList<Region> centerPlates = new ArrayList<Region>();
		
		public void animate() {
			
		}
		
		public void destroy() {
			
		}
	}
	
	private TileLocations tiles;
	private boolean isSixPlayers;
	
	public LudoBoard(String name, TileLocations tiles, boolean isSixPlayers) {
		super(name);
		
		this.tiles = tiles;
		this.isSixPlayers = isSixPlayers;
		
		generateLobby();
		lobby.enable();
	}
	
	public static TileLocations identifyBoard(Region region, boolean isSixPlayers) {
		
		return null;
		
	}
	
	@Override
	protected void generateLobby() {
		ArrayList<InteractionLobby.Token> tokens = new ArrayList<>();
		
		for (int i = 0; i < (isSixPlayers ? 6 : 4); i++)
			tokens.add(new InteractionLobby.Token(
					tiles.centerPlates.get(i).getCenter(), 						//Location of the piece
					CustomModel.get("LUDO_" + COLOR_ORDER[i].toUpperCase()), 	//Model of the piece
					new Vector2D(1, 1)));										//Size of the interaction
		
		lobby = new InteractionLobby(2, (isSixPlayers ? 6 : 4), tokens);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		tiles.destroy();
	}
}
