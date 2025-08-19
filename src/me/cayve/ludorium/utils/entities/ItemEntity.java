package me.cayve.ludorium.utils.entities;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

public class ItemEntity extends DisplayEntity<ItemDisplay> {
	
	private ItemStack model;
	
	@SafeVarargs
	public ItemEntity(Location location, ItemStack model, 
			Function<DisplayEntity<BlockDisplay>, EntityComponent>... componentFactory) { 
		construct(location, model, null, componentFactory); 
	}
	@SafeVarargs
	public ItemEntity(Location location, ItemStack model, String displayID, 
			Function<DisplayEntity<BlockDisplay>, EntityComponent>... componentFactory) { 
		construct(location, model, displayID, componentFactory); 
	}
	
	@SafeVarargs
	protected final void construct(Location location, ItemStack model, String displayID, 
			Function<DisplayEntity<BlockDisplay>, EntityComponent>... componentFactory) {
		this.model = model;
		
		construct(location, model, displayID, componentFactory);
	}
	
	@Override
	public void enable() {
		super.enable();
		
		display.setItemStack(model);
	}
}
