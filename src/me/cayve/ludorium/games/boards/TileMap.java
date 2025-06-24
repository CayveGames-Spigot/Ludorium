package me.cayve.ludorium.games.boards;

import java.util.ArrayList;

public abstract class TileMap {

	public static class TileEvent {
		public ArrayList<Runnable> listeners = new ArrayList<>();
	}
}
