package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.cayve.ludorium.utils.locational.Region;
import me.cayve.ludorium.utils.locational.RegionTriggerManager;
import me.cayve.ludorium.utils.locational.RegionTriggerManager.RegionTriggerListener;

public class RegionLobby extends GameLobby implements RegionTriggerListener {
	
	private ArrayList<Region> lobbyRegions;
	
	public RegionLobby(int minimum, int maximum) {
		super(minimum, maximum);
		
		lobbyRegions = new ArrayList<Region>();
		
		RegionTriggerManager.registerListener(this);
	}
	
	@Override
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
