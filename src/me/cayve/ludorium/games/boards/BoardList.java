package me.cayve.ludorium.games.boards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardList {
	private static Map<Class<? extends GameBoard>, Map<String, GameBoard>> activeBoards;
	
	public static void save(GameBoard board) {
		if (activeBoards == null)
			activeBoards = new HashMap<>();
		if (!activeBoards.containsKey(board.getClass()))
			activeBoards.put(board.getClass(), new HashMap<>());
		
		activeBoards.get(board.getClass()).put(board.getName(), board);
	}
	
	public static boolean remove(String name, Class<? extends GameBoard> type) {
		if (activeBoards == null || !activeBoards.containsKey(type) || !activeBoards.get(type).containsKey(name)) return false;
		
		activeBoards.get(type).get(name).destroy();
		activeBoards.get(type).remove(name);
		return true;
	}
	
	public static List<String> getList(Class<? extends GameBoard> type) {
		List<String> list = new ArrayList<String>();
		
		if (activeBoards == null) return list;
		
		if (activeBoards.containsKey(type))
			for (String instance : activeBoards.get(type).keySet())
				list.add(instance);
		
		return list;
	}
	
	public static void destroyAll() {
		if (activeBoards == null) return;
		
		for (Map<String, GameBoard> type : activeBoards.values())
			for (GameBoard board : type.values())
				board.destroy();
	}
}
