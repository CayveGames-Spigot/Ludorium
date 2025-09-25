package me.cayve.ludorium.utils.particles;

import com.destroystokyo.paper.ParticleBuilder;

import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.entities.DisplayEntity.EntityComponent;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.interfaces.Cancelable;
import me.cayve.ludorium.utils.interfaces.Destroyable;
import me.cayve.ludorium.utils.interfaces.Toggleable;
import me.cayve.ludorium.utils.locational.Transform;

public class ParticleEmitter implements Cancelable, Destroyable, Toggleable, EntityComponent {

	private Transform referenceTransform;
	
	private ParticleRig rig;
	
	private Event0 onDestroyEvent = new Event0();
	private Event0 onEnableEvent = new Event0();
	private Event0 onDisableEvent = new Event0();
	private Event0 onCancelEvent = new Event0();
	
	private Task timer;
	
	public ParticleEmitter(Transform referenceTransform) {
		this.referenceTransform = referenceTransform;
		
		timer = Timer.register(new Task().setRefreshRate(1L)
				.registerOnUpdate(this::update)).setPriority(-1);
		
		referenceTransform.onUpdated().subscribe(() -> rig.getLocalPosition().setScale(referenceTransform.getScale()));
	}
	
	private void update() {
		if (rig == null)
			return;
		
		for (ParticleBuilder particle : rig.evaluate()) {
			//particle.location() up until here represents the offset the particle will have from the reference location
			Transform particleTransform = referenceTransform.clone().applyOffsets().setOffset(particle.location().toVector().toVector3f());
			particle.location(particleTransform.getLocation()).spawn();
		}
	}
	
	public void play(ParticleRig rig) {
		this.rig = rig;
		rig.getLocalPosition().setScale(referenceTransform.getScale());
		
		enable();
	}
	
	@Override
	public boolean isEnabled() { return !timer.isPaused(); }

	@Override
	public void enable() {
		timer.restart();
		
		onEnableEvent.run();
	}

	@Override
	public void disable() {
		timer.pause();
		
		onDisableEvent.run();
	}

	@Override
	public void destroy() {
		cancel();
		
		timer.cancel();
		
		onDestroyEvent.run();
	}

	@Override
	public void cancel() {
		if (rig != null)
			rig.cancel();
		timer.pause();
		
		onCancelEvent.run();
	}
	
	@Override public Subscriber<Runnable> onEnabled() { return onEnableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDisabled() { return onDisableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDestroyed() { return onDestroyEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onCanceled() { return onCancelEvent.getSubscriber(); }

}
