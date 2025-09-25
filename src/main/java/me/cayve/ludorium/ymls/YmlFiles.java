package me.cayve.ludorium.ymls;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.cayve.ludorium.LudoriumPlugin;

public class YmlFiles {

	public static class YmlFileInfo {
		public File textFile;
		public FileConfiguration customConfig;

		public YmlFileInfo(File textFile, FileConfiguration customConfig) {
			this.textFile = textFile;
			this.customConfig = customConfig;
		}
		
		public void clear() {
			for (String key : customConfig.getKeys(false))
				customConfig.set(key, null);
		}
		
		public void save() {
			try {
				if (customConfig.getKeys(false).isEmpty())
						textFile.delete();
				else
					customConfig.save(textFile);
			} catch (IOException e) {
				LudoriumPlugin.getPlugin().getLogger().log(Level.SEVERE,
						"Could not save config to " + textFile.getName(), e);
			}
		}
	}
	
	public static boolean exists(String fileName) {
		try {
			File file = new File(LudoriumPlugin.getPlugin().getDataFolder(), fileName);
			return file.exists();
		} catch (Exception e) 
		{
			return false;
		}
	}

	public static YmlFileInfo reload(String fileName) {
		YmlFileInfo info = null;
		
		try {
			
			File textFile = new File(LudoriumPlugin.getPlugin().getDataFolder(), fileName);
			
			textFile.getParentFile().mkdirs();
			
			FileConfiguration customConfig = YamlConfiguration.loadConfiguration(textFile);
			
			info = new YmlFileInfo(textFile, customConfig);
			
			Reader defConfigStream = new InputStreamReader(LudoriumPlugin.getPlugin().getResource(fileName),
					"UTF8");
			
			if (defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				
				customConfig.setDefaults(defConfig);
				customConfig.options().copyDefaults(true);
				
				info.save();
			}
		}
		catch (NullPointerException e) {
			//Don't print error if the file is missing, that's normal
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}
}
