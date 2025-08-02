package me.cayve.ludorium.main;

import java.util.logging.Level;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.cayve.ludorium.commands.LudoriumCommand;
import me.cayve.ludorium.games.GameRegistrar;
import me.cayve.ludorium.games.boards.BoardList;
import me.cayve.ludorium.games.wizards.GameCreationWizard;
import me.cayve.ludorium.utils.PlayerStateManager;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.entities.LudoriumEntity;
import me.cayve.ludorium.utils.locational.Vector3D;

public class LudoriumPlugin extends JavaPlugin {

	private static boolean DEVELOPER_MODE = true;
	
	private static LudoriumPlugin main;
	
	private boolean loadFailed = false;
	
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
		main = this;
		
		GameRegistrar.registerGames();
		
		this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
			commands.registrar().register(LudoriumCommand.build());
		});
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		ConfigurationSerialization.registerClass(Vector3D.class);
		
		LudoriumEntity.initialize();
		Timer.initialize();
		ToolbarMessage.initialize();
		PlayerStateManager.initialize();
		
		try {
			GameRegistrar.forEachGame(x -> x.load());
		} catch (Exception e) {
			loadFailed = true;
			
			main.getServer().getLogger().log(Level.SEVERE, 
					"Something went wrong while trying to load your Ludorium boards. To avoid overwriting your saved boards, Ludorium will disable.");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	public void reloadPlugin() 
	{
		
	}
	
	@Override
	public void onDisable() {
		if (!loadFailed)
			GameRegistrar.forEachGame(x -> x.save());
		
		GameCreationWizard.destroyAll();
		BoardList.destroyAll();

		LudoriumCommand.uninitialize();
		PlayerStateManager.uninitialize();
	}
}
