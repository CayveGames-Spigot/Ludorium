package me.cayve.ludorium.games;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.cayve.ludorium.utils.locational.Region;

public class RegionTriggerManager {
	
	//Interface for region listeners - listeners should contain their own regions
	public interface RegionTriggerListener {
		public abstract int regionQuantity();
		public abstract Region getRegion(int regionIndex);
		public abstract void regionEntered(int regionIndex, Player player);
		public abstract void regionLeft(int regionIndex, Player player);
	}

	//All active listeners
	private static ArrayList<RegionTriggerListener> listeners;
	
	public static void initialize() {
		listeners = new ArrayList<RegionTriggerListener>();
		
		//Start timer
	}
	
	public static void registerListener(RegionTriggerListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public static void unregisterListener(RegionTriggerListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
}
