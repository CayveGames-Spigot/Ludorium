package me.cayve.ludorium.utils;

import java.util.function.Consumer;

import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.BoundingBox;
import org.joml.Vector2f;

import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.entities.DisplayEntity.EntityComponent;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event1;
import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.entities.LudoriumEntity;
import me.cayve.ludorium.utils.interfaces.Destroyable;
import me.cayve.ludorium.utils.interfaces.Interactable;
import me.cayve.ludorium.utils.interfaces.Toggleable;
import me.cayve.ludorium.utils.locational.Transform;

public class Collider implements EntityComponent, Destroyable, Listener, Toggleable, Interactable {

	private Transform referenceTransform;
	
	private Interaction interaction;
	private Vector2f interactionBounds = new Vector2f(1,1);
	
	private Event1<Player> onInteractEvent = new Event1<>();
	private Event0 onDestroyEvent = new Event0();
	private Event0 onEnableEvent = new Event0();
	private Event0 onDisableEvent = new Event0();
	
	public Collider(Transform referenceTransform) {
		construct(referenceTransform, null);
	}
	
	public Collider(Transform referenceTransform, Vector2f interactionBounds) {
		construct(referenceTransform, interactionBounds);
	}
	
	protected void construct(Transform referenceTransform, Vector2f interactionBounds) {
		this.referenceTransform = referenceTransform;
		this.referenceTransform.onUpdated().subscribe(this::update);
		
		if (interactionBounds != null)
			setBounds(interactionBounds);
	}
	
	public void setBounds(Vector2f interactionBounds) {
		this.interactionBounds = interactionBounds;
		
		refreshBounds();
	}
	
	public BoundingBox getBoundingBox() { return interaction.getBoundingBox(); }
	
	private void refreshBounds() {
		if (interaction != null) {
			interaction.setInteractionWidth(interactionBounds.x);
			interaction.setInteractionHeight(interactionBounds.y);
		}
	}
	
	@Override
	public void enable() {
		if (interaction != null)
			disable();
		
		interaction = LudoriumEntity.spawn(referenceTransform.getLocation(), Interaction.class);
		
		refreshBounds();
		
		LudoriumPlugin.registerEvent(this);
		
		onEnableEvent.run();
	}
	
	@Override
	public void disable() {
		if (interaction != null)
		{
			LudoriumEntity.remove(interaction);
			interaction = null;
			
			HandlerList.unregisterAll(this);
		}
		
		onDisableEvent.run();
	}
	
	@Override
	public void destroy() {
		if (interaction != null)
			interaction.remove();
		
		onDestroyEvent.run();
	}
	
	public void update() {
		if (interaction == null) return;
		interaction.teleport(referenceTransform.getLocation());
		interaction.setRotation(referenceTransform.getYaw(), referenceTransform.getPitch());
		
		interaction.setInteractionWidth(interactionBounds.x * referenceTransform.getScale());
		interaction.setInteractionHeight(interactionBounds.y * referenceTransform.getScale());
	}
	
	@EventHandler
	/**
	 * The interaction event trigger
	 * @param event
	 */
	public void onInteractedWith(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof Interaction) ||
			!interaction.equals(event.getRightClicked())) 
				return;

		onInteractEvent.accept(event.getPlayer());
	}
	
	@Override public boolean isEnabled() { return interaction != null; }

	@Override public Subscriber<Runnable> onEnabled() { return onEnableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDisabled() { return onDisableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDestroyed() { return onDestroyEvent.getSubscriber(); }
	@Override public Subscriber<Consumer<Player>> onInteracted() { return onInteractEvent.getSubscriber(); }
}
