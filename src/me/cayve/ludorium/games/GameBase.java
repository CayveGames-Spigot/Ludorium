package me.cayve.ludorium.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.cayve.ludorium.games.lobbies.GameLobby;

public abstract class GameBase {
	private static Map<Class<? extends GameBase>, Map<String, GameBase>> activeInstances;
	
	private GameLobby lobby;
	
	public static boolean deleteGameInstance(String name, Class<? extends GameBase> type) {
		if (activeInstances == null || !activeInstances.containsKey(type) || !activeInstances.get(type).containsKey(name)) return false;
		
		activeInstances.get(type).get(name).destroyInstance();
		activeInstances.get(type).remove(name);
		return true;
	}
	
	public static List<String> getInstanceList(Class<? extends GameBase> type) {
		List<String> list = new ArrayList<String>();
		
		if (activeInstances == null) return list;
		
		if (activeInstances.containsKey(type))
			for (String instance : activeInstances.get(type).keySet())
				list.add(instance);
		
		return list;
	}
	
	public GameBase(String name) 
	{
		if (activeInstances == null)
			activeInstances = new HashMap<>();
		if (!activeInstances.containsKey(this.getClass()))
			activeInstances.put(this.getClass(), new HashMap<>());
		
		activeInstances.get(this.getClass()).put(name, this);
	}
	
	protected abstract void destroyInstance();
	
	public GameLobby getLobby() { return lobby; }
}
