package me.cayve.ludorium.utils;

import org.bukkit.configuration.file.FileConfiguration;

import me.cayve.ludorium.main.LudoriumPlugin;

public class Config {

	public static FileConfiguration get() {
		return LudoriumPlugin.getPlugin().getConfig();
	}
}
