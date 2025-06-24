package me.cayve.ludorium.utils.entities;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

public class ItemEntity extends DisplayEntity<ItemDisplay> {
	
	private ItemStack model;
	
	public ItemEntity(Location location, ItemStack model) {
		super(ItemDisplay.class, location);
		
		this.model = model;
	}
	
	@Override
	public ItemDisplay spawn(boolean rawSpawn) {
		super.spawn(rawSpawn);
		
		display.setItemStack(model);
		
		return display;
	}
}
