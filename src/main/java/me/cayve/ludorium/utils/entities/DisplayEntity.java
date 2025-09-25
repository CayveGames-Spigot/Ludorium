package me.cayve.ludorium.utils.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.event.Listener;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event1;
import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.interfaces.Destroyable;
import me.cayve.ludorium.utils.interfaces.Toggleable;
import me.cayve.ludorium.utils.locational.Transform;

public class DisplayEntity<T extends Display> implements Destroyable, Toggleable, Listener {
	
	public static interface EntityComponent {}
	
	protected T display;
	private String displayID = UUID.randomUUID().toString();
	
	protected Transform position = new Transform();
	protected Vector3f pivot = new Vector3f(0,0,0);
	private Class<T> type;

	private Map<Class<? extends EntityComponent>, EntityComponent> components = new HashMap<>();
	
	private Event1<DisplayEntity<T>> onDestroyEvent = new Event1<DisplayEntity<T>>();
	private Event0 onEnableEvent = new Event0();
	private Event0 onDisableEvent = new Event0();
	
	protected DisplayEntity() { }
	
	@SafeVarargs
	public DisplayEntity(Class<T> type, Location location, Function<DisplayEntity<T>, EntityComponent>... componentFactory) { 
		construct(type, location, componentFactory); 
	}
	@SafeVarargs
	public DisplayEntity(Class<T> type, Location location, String displayID, Function<DisplayEntity<T>, EntityComponent>... componentFactory) { 
		construct(type, location, displayID, componentFactory); 
	}
	
	@SafeVarargs
	protected final void construct(Class<T> type, Location location, Function<DisplayEntity<T>, EntityComponent>... componentFactory) { 
		construct(type, location, null, componentFactory); 
	}

	@SafeVarargs
	protected final void construct(Class<T> type, Location location, String displayID, Function<DisplayEntity<T>, EntityComponent>... componentFactory) {
		if (type == null) return;
		
		this.type = type;
		
		this.position.setLocation(location);
		
		if (displayID != null)
			this.displayID = displayID;
		
		for (Function<DisplayEntity<T>, EntityComponent> factory : componentFactory)
		{
			EntityComponent component = factory.apply(this);
			
			if (!this.components.containsKey(component.getClass()))
				this.components.put(component.getClass(), component);
		}

		position.onUpdated().subscribe(this::updatePosition);
		
		enable();
	}
	
	public String getID() { return displayID; }
	
	/**
	 * Returns the mutable location of the display
	 * @return
	 */
	public Transform getPositionTransform() { return position; }
	
	/**
	 * Sets the pivot point of this display
	 * @param pivot
	 */
	public void setPivot(Vector3f pivot) { 
		pivot.mul(-1); //Flip it because it's pivot -> offset
		this.pivot = pivot;
	}

	private void updatePosition() {
		if (!isEnabled()) return;

		display.teleport(position.getLocation().add(Vector.fromJOML(pivot)));
		display.setRotation(position.getYaw(), position.getPitch());
		
		Transformation displayTransformation = display.getTransformation();
		displayTransformation.getScale().set(position.getScale());
		display.setTransformation(displayTransformation);
	}
	
	/**
	 * @return The entity's display
	 */
	public T get() { return display; }
	
	public <C extends EntityComponent> C getComponent(Class<C> type) {
		if (!components.containsKey(type))
			return null;
		return type.cast(components.get(type));
	}

	private <C> void forEachComponentWith(Class<C> type, Consumer<C> action) {
		for (EntityComponent component : components.values()) {
			if (!type.isInstance(component)) continue;
			
			action.accept(type.cast(component));
		}
	}
	
	/**
	 * Spawns this entity's display
	 */
	@Override
	public void enable() {
		disable();
			
		display = LudoriumEntity.spawn(position.getLocation(), type);
		
		forEachComponentWith(Toggleable.class, x -> x.enable());
		
		onEnableEvent.run();
	}
	
	/**
	 * Removes this entity's display
	 */
	@Override
	public void disable() {
		if (!isEnabled())
			return;
		
		LudoriumEntity.remove(display);
		display = null;
		
		forEachComponentWith(Toggleable.class, x -> x.disable());
		
		onDisableEvent.run();
	}

	/**
	 * Destroys and cleans up this entity
	 */
	@Override
	public void destroy() {
		disable();
		
		forEachComponentWith(Destroyable.class, x -> x.destroy());
		
		onDestroyEvent.accept(this);
	}
	
	@Override public boolean isEnabled() { return display != null; }
	
	@Override public Subscriber<Runnable> onDisabled() { return onDisableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onEnabled() { return onEnableEvent.getSubscriber(); }
	@Override public Subscriber<Consumer<DisplayEntity<T>>> onDestroyed() { return onDestroyEvent.getSubscriber(); }
}
