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
	private Vector3f gravity = new Vector3f(0, -.05f, 0);
	private float airDrag = .03f, surfaceDrag = .05f;
	private boolean grounded, atRest;
	
	private Task simulationTimer;
	
	public Rigidbody(Transform referenceTransform, Collider referenceCollider) {
		this.referenceTransform = referenceTransform;
		this.referenceCollider = referenceCollider;
		
		simulationTimer = new Task().setRefreshRate(1L).registerOnUpdate(this::calculate).pause();
		Timer.register(simulationTimer);
	}
	
	private void calculate() {
		float drag = (1 - (grounded ? surfaceDrag : airDrag));
		
		velocity.x *= drag;
		velocity.z *= drag;
		
		velocity.add(new Vector3f(gravity).mul(grounded ? 0 : 1));
		
		if (willCollide(new Vector3f(velocity.x, 0, 0)))
			velocity.x *= -.4f;
		if (willCollide(new Vector3f(0, 0, velocity.z)))
			velocity.z *= -.4f;
		
		if (!grounded && willCollide(new Vector3f(0, velocity.y, 0))) {
			grounded = true;
			velocity.y *= -.4f;
		} else
			grounded = false;
		
		referenceTransform.setLocation(referenceTransform.getLocation().add(Vector.fromJOML(velocity)));
		
		if (velocity.length() <= 0.1f && grounded)
		{
			if (!atRest) {
				atRest = true;
				velocity = new Vector3f(0,0,0);
				onRestEvent.run();
			}
		} else
			atRest = false;
	}
	
	private boolean willCollide(Vector3f velocityToCheck) {
		if (!referenceCollider.isEnabled())
			return false;
		
		return referenceTransform.getWorld().hasCollisionsIn(referenceCollider.getBoundingBox().shift(Vector.fromJOML(velocityToCheck)));
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

	public void disableOnRest() { onRestEvent.subscribe(this::disable, 1); }
	
	@Override public boolean isEnabled() { return !simulationTimer.isPaused(); }

	@Override public Subscriber<Runnable> onEnabled() { return onEnableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDisabled() { return onDisableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDestroyed() { return onDestroyEvent.getSubscriber(); }
	
	public Subscriber<Runnable> onRested() { return onRestEvent.getSubscriber(); }
}
