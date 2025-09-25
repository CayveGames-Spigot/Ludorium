package me.cayve.ludorium.utils.entities;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import me.cayve.ludorium.LudoriumException;
import me.cayve.ludorium.LudoriumPlugin;

public class LudoriumEntity implements Listener {
	
	private static ArrayList<Entity> entities = new ArrayList<>();
	private static NamespacedKey pluginKey = new NamespacedKey(LudoriumPlugin.getPlugin(), "Ludorium");
	
	/**
	 * Spawns a new entity and tags it to be owned and tracked by this plugin
	 * @param location The location to spawn the entity
	 * @param type The type of entity to spawn
	 * @return
	 */
	public static <T extends Entity> T spawn(Location location, Class<T> type) {
		T entity = location.getWorld().spawn(location, type);
		
		entity.getPersistentDataContainer().set(pluginKey, PersistentDataType.INTEGER, 1);
		entities.add(entity);
		
		return entity;
	}
	
	/**
	 * Removes a tagged entity from the world
	 * @param entity The entity to remove
	 * @throws EntityNotTaggedException 
	 */
	public static void remove(Entity entity) {
		if (!entity.getPersistentDataContainer().has(pluginKey))
			throw new LudoriumException("Entity not tagged properly.");
		if (!entities.contains(entity))
			throw new LudoriumException("Entity has already been removed (or not spawned properly).");
		
		entities.remove(entity);
		entity.remove();
	}
	
	public static void initialize() {
		LudoriumPlugin.registerEvent(new LudoriumEntity());
		
		for (World world : Bukkit.getWorlds()) {
			
			//Iterate through all entities, if there exists an entity that is tagged by this plugin, but we aren't tracking it, remove it
			//This implies another plugin saved the entity and respawned it on restart
			Iterator<Entity> entityIterator = world.getEntities().iterator();
			while (entityIterator.hasNext()) {
				Entity entity = entityIterator.next();
				if (entity.getPersistentDataContainer().has(pluginKey) && !entities.contains(entity))
					entity.remove();
			}
		}
	}
	
	public static void uninitialize() {
		for (Entity entity : entities)
			remove(entity);
	}
	
	//If an entity is spawned, is tagged by this plugin, but we aren't tracking it, remove it
	//This implies another plugin spawned the entity
	@EventHandler
	private void onEntitySpawn(EntitySpawnEvent event) {
		if (!entities.contains(event.getEntity())) {
			if (event.getEntity().getPersistentDataContainer().has(pluginKey))
			{
				event.setCancelled(true);
				event.getEntity().remove();
			}
		}
	}
	
	@EventHandler
	private void onEntityRemove(EntityRemoveEvent event) {
		//If the list contains the tagged entity thats being removed, that means .remove() was used elsewhere
		//You could certainly handle the issue here, but the exception serves as a notice of incorrect usage
		if (event.getEntity().getPersistentDataContainer().has(pluginKey) && entities.contains(event.getEntity())) 
		{
			//If we're in developer mode, throw an exception
			if (LudoriumPlugin.isDeveloperMode())
				throw new LudoriumException("Entity not removed properly.");
			else //Otherwise just handle it
				remove(event.getEntity());
		}
	}
}
