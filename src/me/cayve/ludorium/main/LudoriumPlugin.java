package me.cayve.ludorium.main;

import java.util.logging.Level;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.cayve.ludorium.commands.LudoriumCommand;
import me.cayve.ludorium.games.GameRegistrar;
import me.cayve.ludorium.games.boards.BoardList;
import me.cayve.ludorium.games.utils.PlayerInventoryManager;
import me.cayve.ludorium.games.wizards.GameCreationWizard;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.entities.LudoriumEntity;

public class LudoriumPlugin extends JavaPlugin {

	private static boolean DEVELOPER_MODE = true;
	
	private static LudoriumPlugin main;

	public static void registerEvent(Listener listener) {
		main.getServer().getPluginManager().registerEvents(listener, main);
	}
	
	public static LudoriumPlugin getPlugin() { return main; }
	
	/**
	 * Runs an action guarded with a default try/catch and prints and stack trace caught
	 * @param action
	 */
	public static void callSafely(Runnable action) {
		try {
			action.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints a debug message if the plugin is currently in developer mode
	 * @param debugMessage
	 */
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

		LudoriumEntity.initialize();
		Timer.initialize();
		ToolbarMessage.initialize();
		PlayerInventoryManager.initialize();
		
		//Each game type is guarded as well, per instance is up to type implementation
		callSafely(() -> GameRegistrar.forEachGame(x -> x.load()));
	}
	
	public void reloadPlugin() 
	{
		
	}
	
	@Override
	public void onDisable() {
		//Each game type is guarded as well, per instance is up to type implementation
		callSafely(() -> GameRegistrar.forEachGame(x -> x.save())); 
		
		callSafely(GameCreationWizard::destroyAll);
		callSafely(BoardList::removeAll); //Each board removal is guarded as well

		callSafely(PlayerInventoryManager::uninitialize); //Each player state removal is guarded as well
		callSafely(LudoriumEntity::uninitialize);
	}
}
