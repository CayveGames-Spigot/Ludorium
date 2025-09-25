package me.cayve.ludorium.utils;

import java.util.ArrayList;

public class Whitelist<T> {

	private ArrayList<T> whitelist = new ArrayList<>();
	private ArrayList<Runnable> onWhitelistUpdate = new ArrayList<>();
	private boolean whitelistEnabled = false;
	
	public void registerOnWhitelistUpdate(Runnable listener) { onWhitelistUpdate.add(listener); }
	
	public void enableWhitelist() { whitelistEnabled = true; whitelistUpdated(); }
	public void disableWhitelist() { whitelistEnabled = false; whitelistUpdated(); }
	
	public void addWhitelist(T element) { whitelist.add(element); whitelistUpdated(); }
	public void removeWhitelist(T element) { whitelist.remove(element); whitelistUpdated(); }
	public void clearWhitelist() { whitelist.clear(); whitelistUpdated(); }
	
	/**
	 * Copies the contents of the whitelist, excluding the event listeners
	 * @param copy
	 */
	public void copyFrom(Whitelist<T> copy) {
		whitelistEnabled = copy.whitelistEnabled;
		
		whitelist = new ArrayList<>(copy.whitelist);
		
		whitelistUpdated();
	}
	
	public boolean verify(T value) {
		return !whitelistEnabled || (whitelistEnabled && whitelist.contains(value));
	}
	
	private void whitelistUpdated() 
	{
		for (Runnable event : onWhitelistUpdate)
			event.run();
	}
	
}
