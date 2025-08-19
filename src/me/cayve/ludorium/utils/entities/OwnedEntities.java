package me.cayve.ludorium.utils.entities;

import java.util.ArrayList;

import me.cayve.ludorium.utils.functionals.Event.Subscriber;
import me.cayve.ludorium.utils.functionals.Event0;
import me.cayve.ludorium.utils.interfaces.Destroyable;

public class OwnedEntities implements Destroyable {

	private Event0 onDestroyEvent = new Event0();
	
	private ArrayList<DisplayEntity<?>> ownedEntities = new ArrayList<DisplayEntity<?>>();
	
	public void registerEntity(DisplayEntity<?> entity) {
		ownedEntities.add(entity);
		
		entity.onDestroyed().subscribe(x -> ownedEntities.remove(x));
	}
	
	public void registierEntities(ArrayList<DisplayEntity<?>> entities) {
		for (DisplayEntity<?> entity : entities)
			registerEntity(entity);
	}
	
	@Override
	public void destroy() {
		while (ownedEntities.size() > 0) {
			ownedEntities.get(0).destroy();
			//Entities owned should automatically remove themselves from the list if destroyed
		}
		
		onDestroyEvent.run();
	}
	
	@Override public Subscriber<Runnable> onDestroyed() { return onDestroyEvent.getSubscriber(); }
}
