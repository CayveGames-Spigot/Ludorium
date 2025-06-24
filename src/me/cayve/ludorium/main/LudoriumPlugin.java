package me.cayve.ludorium.main;

import java.util.logging.Level;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.cayve.ludorium.commands.LudoCommand;
import me.cayve.ludorium.commands.LudoriumCommand;
import me.cayve.ludorium.games.boards.BoardList;
import me.cayve.ludorium.games.wizards.GameCreationWizard;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.entities.LudoriumEntity;
import me.cayve.ludorium.utils.locational.RegionTriggerManager;

public class LudoriumPlugin extends JavaPlugin {

	private static boolean DEVELOPER_MODE = true;
	
	private static LudoriumPlugin main;
	
	public static void registerEvent(Listener listener) {
		main.getServer().getPluginManager().registerEvents(listener, main);
	}
	
	public static LudoriumPlugin getPlugin() { return main; }
	
	public static void debug(String debugMessage) 
	{ 
		if (isDeveloperMode())
			main.getServer().getLogger().log(Level.INFO, debugMessage); 
	}
	
	public static boolean isDeveloperMode() { return DEVELOPER_MODE; }
	
	@Override
	public void onLoad() {
		//Registers game commands
		LudoriumCommand.registerGame(new LudoCommand());
				
		this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
			commands.registrar().register(LudoriumCommand.build());
		});
	}

	@Override
	public void onEnable() {
		main = this;
		
		saveDefaultConfig();
		
		LudoriumEntity.initialize();
		Timer.initialize();
		ToolbarMessage.initialize();
		RegionTriggerManager.initialize();
	}
	public void reloadPlugin() 
	{
		
	}
	
	@Override
	public void onDisable() {
		GameCreationWizard.destroyAll();
		BoardList.destroyAll();
		
		LudoriumCommand.uninitialize();
	}
}
