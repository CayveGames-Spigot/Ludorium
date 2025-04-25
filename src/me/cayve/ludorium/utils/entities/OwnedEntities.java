package me.cayve.ludorium.utils.entities;

import java.util.ArrayList;

public class OwnedEntities {

	private ArrayList<DisplayEntity<?>> ownedEntities = new ArrayList<DisplayEntity<?>>();
	
	public void registerEntity(DisplayEntity<?> entity) {
		ownedEntities.add(entity);
		
		entity.registerOnDestroy(x -> ownedEntities.remove(x));
	}
	
	public void registierEntities(ArrayList<DisplayEntity<?>> entities) {
		for (DisplayEntity<?> entity : entities)
			registerEntity(entity);
	}
	
	public void destroy() {
		while (ownedEntities.size() > 0) {
			ownedEntities.get(0).destroy();
			//Entities owned should automatically remove themselves from the list if destroyed
		}
	}
}
