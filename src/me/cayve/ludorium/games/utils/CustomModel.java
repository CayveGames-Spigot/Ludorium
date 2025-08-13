package me.cayve.ludorium.games.utils;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import me.cayve.ludorium.games.Game;
import me.cayve.ludorium.games.GameRegistrar;

public class CustomModel {
	
	public static ItemStack get(Class<? extends Game> type, String label) {
		return get(GameRegistrar.getPrefix(type) + "_" + label);
	}
	
	public static ItemStack get(String label) {
		ItemStack model = new ItemStack(Material.PAPER, 1);
		
		ItemMeta meta = model.getItemMeta();
	
		CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
		ArrayList<String> modelList = new ArrayList<String>();
		modelList.add("ludorium_" + label);
		
		modelData.setStrings(modelList);
		
		meta.setCustomModelDataComponent(modelData);
		
		model.setItemMeta(meta);
		
		return model;
	}
	
	public static boolean is(ItemStack item, Class<? extends Game> type, String label) {
		return is(item, GameRegistrar.getPrefix(type) + "_" + label);
	}
	
	public static boolean is(ItemStack item, String label) {
		if (item.getType() != Material.PAPER || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelDataComponent())
			return false;
		
		return item.getItemMeta().getCustomModelDataComponent().getStrings().get(0).equals("ludorium_" + label);
	}
}
