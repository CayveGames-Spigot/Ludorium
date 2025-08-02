package me.cayve.ludorium.games.boards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardList {
	private static Map<Class<? extends GameBoard>, Map<String, GameBoard>> activeBoards = new HashMap<>();
	
	public static void add(GameBoard board) {
		if (!activeBoards.containsKey(board.getClass()))
			activeBoards.put(board.getClass(), new HashMap<>());
		
		activeBoards.get(board.getClass()).put(board.getName(), board);
	}
	
	public static boolean remove(String name, Class<? extends GameBoard> type) {
		if (!activeBoards.containsKey(type) || !activeBoards.get(type).containsKey(name)) return false;
		
		activeBoards.get(type).get(name).destroy();
		activeBoards.get(type).remove(name);
		return true;
	}
	
	public static List<String> getNameList(Class<? extends GameBoard> type) {
		List<String> list = new ArrayList<String>();

		if (activeBoards.containsKey(type))
			for (String instance : activeBoards.get(type).keySet())
				list.add(instance);
		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends GameBoard> List<T> getInstanceList(Class<T> type) {
		List<T> list = new ArrayList<>();

		if (activeBoards.containsKey(type))
			for (GameBoard instance : activeBoards.get(type).values())
				list.add((T)instance);
		
		return list;		
	}
	
	public static void destroyAll() {
		for (Class<? extends GameBoard> type : activeBoards.keySet())
			destroyAllOfType(type);
		
		activeBoards.clear();
	}
	
	public static void destroyAllOfType(Class<? extends GameBoard> type) {
		if (!activeBoards.containsKey(type))
			return;
		
		for (GameBoard board : activeBoards.get(type).values())
			board.destroy();
		
		activeBoards.remove(type);
	}
}
