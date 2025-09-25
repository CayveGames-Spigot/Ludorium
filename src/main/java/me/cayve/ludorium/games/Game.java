package me.cayve.ludorium.games;

public interface Game {
	/**
	 * Called onLoad()
	 */
	public void initialize();
	
	public void save();
	public void load();
}
