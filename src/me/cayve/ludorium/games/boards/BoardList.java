package me.cayve.ludorium.games.boards;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoardList {
	private static ArrayList<GameBoard> activeBoards = new ArrayList<>();
	
	public static void add(GameBoard board) { activeBoards.add(board); }
	
	public static boolean remove(GameBoard board) 
	{
		//Guard to protect removeAll() from failing entirely
		try {
			board.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return activeBoards.remove(board);
	}
	
	public static boolean remove(String name, Class<? extends GameBoard> type) {
		for (GameBoard board : activeBoards) {
			if (board.getClass().isAssignableFrom(type) && board.getName().equals(name))
				return remove(board);
		}
		return false;
	}
	
	
	public static void removeAll() { forEach(x -> remove(x)); }
	public static void removeAllOfType(Class<? extends GameBoard> type) { forEachOfType(type, x -> remove(x)); }
	
	@SuppressWarnings("unchecked")
	public static <T extends GameBoard> List<T> getInstanceListOfType(Class<T> type) {
		List<T> list = new ArrayList<>();
		forEachOfType(type, x -> list.add((T)x));
		return list;
	}
	
	public static void forEach(Consumer<GameBoard> action) {
		ArrayList<GameBoard> temp = new ArrayList<>(activeBoards);
		for (GameBoard board : temp)
			action.accept(board);
	}
	
	public static void forEachOfType(Class<? extends GameBoard> type, Consumer<GameBoard> action) {
		forEach(x -> {
			if (x.getClass().isAssignableFrom(type))
				action.accept(x);
		});
	}
}
