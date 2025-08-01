package me.cayve.ludorium.utils;

import org.bukkit.configuration.file.FileConfiguration;

import me.cayve.ludorium.games.Game;
import me.cayve.ludorium.games.GameRegistrar;
import me.cayve.ludorium.main.LudoriumPlugin;

public class Config {
	private static String ROOT_PATH = "games.";
	
	public static String getRootPath() { return ROOT_PATH; }
	
	public static FileConfiguration getConfig() {
		return LudoriumPlugin.getPlugin().getConfig();
	}
	
	public static double getDouble(Class<? extends Game> type, String path) { 
		return getDouble(ROOT_PATH + GameRegistrar.getPrefix(type) + "." + path); 
	}
	
	public static double getDouble(String path) { return getConfig().getDouble(path); }
	
	public static boolean getBoolean(Class<? extends Game> type, String path) { 
		return getBoolean(ROOT_PATH + GameRegistrar.getPrefix(type) + "." + path); 
	}
	
	public static boolean getBoolean(String path) { return getConfig().getBoolean(path); }
}
