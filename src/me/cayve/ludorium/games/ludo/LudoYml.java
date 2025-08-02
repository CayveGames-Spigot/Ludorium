package me.cayve.ludorium.games.ludo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import me.cayve.ludorium.games.boards.BoardList;
import me.cayve.ludorium.utils.locational.Vector3D;
import me.cayve.ludorium.ymls.YmlFiles;
import me.cayve.ludorium.ymls.YmlFiles.YmlFileInfo;

/**
 * @author Cayve
 * @license GPL v3
 * @repository https://github.com/CayveGames-Spigot/Ludorium
 * @created 8/1/2025
 * 
 * @description
 * Implements the saving and loading of ludo boards from the dedicated yml file
 */
public class LudoYml {
	
	private static String FILE_NAME = "ludo.yml";
	
	public static void saveBoards(List<LudoBoard> boards) {
		YmlFileInfo yml = YmlFiles.reload(FILE_NAME);
		
		yml.clear();
		
		for (LudoBoard board : boards) {
			LudoMap map = board.getMap();
			
			yml.customConfig.set("boards." + board.getName() + ".origin", board.getOrigin());
			yml.customConfig.set("boards." + board.getName() + ".map", map.getMapID());
			
			if (!yml.customConfig.contains("maps." + map.getMapID())) {
				yml.customConfig.set("maps." + map.getMapID() + ".tileCount", map.getTileCount());
				yml.customConfig.set("maps." + map.getMapID() + ".isSixPlayers", map.isSixPlayers());
				
				yml.customConfig.set("maps." + map.getMapID() + ".relativeLocations", Arrays.asList(map.getRelativeLocations()));
			}
		}
		
		yml.save();
	}
	
	public static void loadBoards() {
		YmlFileInfo yml = YmlFiles.reload(FILE_NAME);
		
		if (yml.customConfig.getConfigurationSection("boards") == null)
			return;
		
		Map<String, LudoMap> loadedMaps = new HashMap<>();
		
		for (String key : yml.customConfig.getConfigurationSection("boards").getKeys(false)) {
			String map = yml.customConfig.getString("boards." + key + ".map");
			Location origin = yml.customConfig.getLocation("boards." + key + ".origin");
			
			//If the map dependency hasn't been loaded, load it
			if (!loadedMaps.containsKey(map)) {
				int tileCount = 
						yml.customConfig.getInt("maps." + map + ".tileCount");
				boolean isSixPlayers = 
						yml.customConfig.getBoolean("maps." + map + ".isSixPlayers");
				@SuppressWarnings("unchecked")
				List<Vector3D> relativeLocations = (List<Vector3D>) 
						yml.customConfig.getList("maps." + map + ".relativeLocations");
				
				loadedMaps.put(map, new LudoMap(relativeLocations.toArray(new Vector3D[0]), map, tileCount, isSixPlayers));
			}

			BoardList.add(new LudoBoard(key, loadedMaps.get(map), origin));
		}
	}
}
