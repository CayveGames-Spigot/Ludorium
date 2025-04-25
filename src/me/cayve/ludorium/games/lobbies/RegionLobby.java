package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.cayve.ludorium.games.GameBase;
import me.cayve.ludorium.games.RegionTriggerManager;
import me.cayve.ludorium.games.RegionTriggerManager.RegionTriggerListener;
import me.cayve.ludorium.utils.locational.Region;

public class RegionLobby extends GameLobby implements RegionTriggerListener {
	
	private ArrayList<Region> lobbyRegions;
	
	public RegionLobby(GameBase game, int minimum, int maximum) {
		super(game, minimum, maximum);
		
		lobbyRegions = new ArrayList<Region>();
		
		RegionTriggerManager.registerListener(this);
	}
	
	public void destroy() {
		RegionTriggerManager.unregisterListener(this);
	}
	
	//RegionTriggerListener implementations
	public int regionQuantity() { return lobbyRegions.size(); }
	public Region getRegion(int regionIndex) { return lobbyRegions.get(regionIndex); }
	
	public void regionEntered(int regionIndex, Player player) {
		attemptLobbyJoin(player);
	}
	
	public void regionLeft(int regionIndex, Player player) {
		attemptLobbyLeave(player);
	}
}
