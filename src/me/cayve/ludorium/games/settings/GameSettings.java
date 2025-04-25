package me.cayve.ludorium.games.settings;

import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import me.cayve.ludorium.games.lobbies.GameLobby;
import me.cayve.ludorium.main.LudoriumPlugin;

public abstract class GameSettings {

	public class Setting {
		public String title;
		public String description;
		public Material[] display;
		public int value;
	}
	
	private Setting[] settings;
	protected GameLobby lobby;
	
	public GameSettings(Setting[] settings, GameLobby lobby) {
		this.settings = settings;
		this.lobby = lobby;
		
		if (this instanceof Listener)
			LudoriumPlugin.registerEvent((Listener)this);
	}
	
	public void destroy() {
		HandlerList.unregisterAll((Listener)this);
	}
	
	public int getSettingValue(int settingIndex) { return settings[settingIndex].value; }
}
