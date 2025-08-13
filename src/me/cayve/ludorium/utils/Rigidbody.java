package me.cayve.ludorium.utils;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.functionals.MultiConsumer;
import me.cayve.ludorium.utils.functionals.MultiRunnable;
import me.cayve.ludorium.utils.locational.Vector3D;

public class Rigidbody {

	private MultiRunnable onRestEvent = new MultiRunnable(); //Called when Rigidbody comes to rest
	private MultiConsumer<Location> onUpdateEvent = new MultiConsumer<>(); //Called when Rigidbody updates
	
	private BoundingBox collider;
	private Location position;
	
	private Vector3D velocity = new Vector3D(0, 0, 0);
	private Vector3D gravity = new Vector3D(0, -.03f, 0);
	private float airDrag = 0, surfaceDrag = 0;
	
	private Task simulationTimer;
	
	public Rigidbody(BoundingBox collider) {
		this.collider = collider;
		
		simulationTimer = new Task().setRefreshRate(1L).registerOnUpdate(this::calculate).pause();
		Timer.register(simulationTimer);
	}
	
	private void calculate() {
		
	}
	
	public void enable() {
		simulationTimer.restart();
	}
	
	public void disable() {
		simulationTimer.pause();
	}
	
	public void destroy() {
		if (simulationTimer != null)
			simulationTimer.cancel();
	}
	
	public void setGravity(Vector3D gravity) { this.gravity = gravity; }
	public void setVelocity(Vector3D velocity) { this.velocity = velocity; }
	public void setAirDrag(float drag) { this.airDrag = drag; }
	public void setSurfaceDrag(float drag) { this.surfaceDrag = drag; }
	
	public void registerOnRestEvent(Runnable listener) { onRestEvent.add(listener); }
	public void registerOnUpdateEvent(Consumer<Location> listener) { onUpdateEvent.add(onUpdateEvent); }
}
