package me.cayve.ludorium.games;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;

import me.cayve.ludorium.main.LudoriumException;
import me.cayve.ludorium.utils.Config;

public class GameRegistrar {
	private static Map<Game, String> registeredGames = new HashMap<>();
	
	public static void registerGames() {
		Logger logger = Logger.getLogger("me.cayve.shaded.reflections");
		logger.setLevel(Level.OFF);
		
		for (Handler handler : logger.getHandlers())
			logger.removeHandler(handler);
		
		Reflections reflections = new Reflections("me.cayve.ludorium");
		
		Set<Class<?>> gameDeclarations = reflections.getTypesAnnotatedWith(GameDeclaration.class);
		
		for (Class<?> declaration : gameDeclarations) {
			//Check if annotation is paired with Game implementation
			if (!Game.class.isAssignableFrom(declaration))
				throw new LudoriumException("Class " + declaration.getName() + " declares a game type (@GameDeclaration) but does not implement type Game");
			
			GameDeclaration annotation = declaration.getAnnotation(GameDeclaration.class);
			String prefix = annotation.prefix();
			
			//Check that this game should be enabled at all
			if (!Config.getBoolean(Config.getRootPath() + prefix + ".enabled"))
				continue;
			
			//Attempt to instantiate the new game declaration
			try {
				Game newGame = (Game) declaration.getDeclaredConstructor().newInstance();
				
				registeredGames.put(newGame, prefix);
				newGame.initialize();
			} catch (Exception e) {
				e.printStackTrace();
				throw new LudoriumException("Failed to instantiate game class: " + declaration.getName());
			}
		}
	}
	
	public static void forEachGame(Consumer<Game> action) {
		for (Game game : registeredGames.keySet())
			action.accept(game);
	}
	
	public static String getPrefix(Class<? extends Game> type) {
		for (Entry<Game, String> game : registeredGames.entrySet())
			if (game.getKey().getClass().isAssignableFrom(type))
				return game.getValue();
		return null;
	}
}
