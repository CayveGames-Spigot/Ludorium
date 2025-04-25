package me.cayve.ludorium.games.boards;

import java.util.ArrayList;
import java.util.function.Consumer;

import me.cayve.ludorium.utils.locational.Vector2DInt;

public abstract class GameBoard {
	
	private ArrayList<Consumer<Vector2DInt>> interactionListeners;
	
	public GameBoard() {
		interactionListeners = new ArrayList<Consumer<Vector2DInt>>();
	}
	
	public void registerInteractionListener(Consumer<Vector2DInt> listener) {
		if (interactionListeners.contains(listener)) return;
		
		interactionListeners.add(listener);
	}
	
	public void unregisterInteractionListener(Consumer<Vector2DInt> listener) {
		if (!interactionListeners.contains(listener)) return;
		
		interactionListeners.remove(listener);
	}
	
	protected void publishInteractionEvent(Vector2DInt coord) {
		for (Consumer<Vector2DInt> listener : interactionListeners)
			listener.accept(coord);
	}
}
