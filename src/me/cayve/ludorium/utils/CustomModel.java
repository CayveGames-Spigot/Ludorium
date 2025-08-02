package me.cayve.ludorium.utils;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import me.cayve.ludorium.games.Game;
import me.cayve.ludorium.games.GameRegistrar;

public class CustomModel {
	
	public static ItemStack get(Class<? extends Game> type, String label) {
		ItemStack model = new ItemStack(Material.PAPER, 1);
		
		ItemMeta meta = model.getItemMeta();
	
		CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
		ArrayList<String> modelList = new ArrayList<String>();
		modelList.add("ludorium_" + GameRegistrar.getPrefix(type) + "_" + label);
		
		modelData.setStrings(modelList);
		
		meta.setCustomModelDataComponent(modelData);
		
		model.setItemMeta(meta);
		
		return model;
	}
}
