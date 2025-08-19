package me.cayve.ludorium.utils;

import org.bukkit.util.Vector;
import org.joml.Vector3f;

import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.entities.DisplayEntity.EntityComponent;
import me.cayve.ludorium.utils.functionals.Event.Subscriber;
import me.cayve.ludorium.utils.functionals.Event0;
import me.cayve.ludorium.utils.interfaces.Destroyable;
import me.cayve.ludorium.utils.interfaces.Toggleable;
import me.cayve.ludorium.utils.locational.Transform;

public class Rigidbody implements EntityComponent, Destroyable, Toggleable {

	private Transform referenceTransform;
	private Collider referenceCollider;
	
	private Event0 onDestroyEvent = new Event0();
	private Event0 onEnableEvent = new Event0();
	private Event0 onDisableEvent = new Event0();
	private Event0 onRestEvent = new Event0(); //Called when Rigidbody comes to rest
	
	private Vector3f velocity = new Vector3f(0, 0, 0);
	private Vector3f gravity = new Vector3f(0, -.03f, 0);
	private float airDrag = 0, surfaceDrag = 0;
	private boolean grounded, atRest;
	
	private Task simulationTimer;
	
	public Rigidbody(Transform referenceTransform, Collider referenceCollider) {
		this.referenceTransform = referenceTransform;
		this.referenceCollider = referenceCollider;
		
		simulationTimer = new Task().setRefreshRate(1L).registerOnUpdate(this::calculate).pause();
		Timer.register(simulationTimer);
	}
	
	private void calculate() {
		velocity = velocity.mul(grounded ? surfaceDrag : airDrag).add(gravity);
		
		referenceTransform.setLocation(referenceTransform.getLocation().add(Vector.fromJOML(velocity)));
		
		if (referenceCollider.isEnabled() && referenceTransform.getWorld().hasCollisionsIn(referenceCollider.getBoundingBox())) {
			grounded = true;
			velocity.y *= -1;
		} else
			grounded = false;
		
		if (velocity.equals(0, 0, 0))
		{
			if (!atRest) {
				atRest = true;
				onRestEvent.run();
			}
		} else
			atRest = false;
	}
	
	@Override
	public void enable() {
		simulationTimer.restart();
		onEnableEvent.run();
	}
	
	@Override
	public void disable() {
		simulationTimer.pause();
		onDisableEvent.run();
	}
	
	@Override
	public void destroy() {
		if (simulationTimer != null)
			simulationTimer.cancel();
		onDestroyEvent.run();
	}
	
	public void setGravity(Vector3f gravity) { this.gravity = gravity; }
	public void setVelocity(Vector3f velocity) { this.velocity = velocity; }
	public void setAirDrag(float drag) { this.airDrag = drag; }
	public void setSurfaceDrag(float drag) { this.surfaceDrag = drag; }

	public void disableOnRest() { onRestEvent.subscribe(this::disable); }
	
	@Override public boolean isEnabled() { return !simulationTimer.isPaused(); }

	@Override public Subscriber<Runnable> onEnabled() { return onEnableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDisabled() { return onDisableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDestroyed() { return onDestroyEvent.getSubscriber(); }
	
}
