package me.cayve.ludorium.utils;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public class CustomModel {
	
	public static ItemStack get(String label) {
		ItemStack model = new ItemStack(Material.PAPER, 1);
		
		ItemMeta meta = model.getItemMeta();
	
		CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
		ArrayList<String> modelList = new ArrayList<String>();
		modelList.add("LUDORIUM_" + label);
		
		modelData.setStrings(modelList);
		
		meta.setCustomModelDataComponent(modelData);
		
		model.setItemMeta(meta);
		
		return model;
	}
}
