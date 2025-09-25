package me.cayve.ludorium.utils.particles;

import java.util.function.Consumer;

import com.destroystokyo.paper.ParticleBuilder;

import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.animation.Animator;
import me.cayve.ludorium.utils.animation.animations.rigs.AnimatorRig;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.interfaces.Cancelable;
import me.cayve.ludorium.utils.locational.Transform;

public class ParticleStroke implements Cancelable{

	private AnimatorRig rig = new AnimatorRig(true);
	private ParticleBuilder build;
	private float timeIncrement = 0.1f;
	
	private Animator animator; //For translation animations, not design animations
	private Transform localPosition = new Transform();
	
	private Event0 onCancelEvent = new Event0();
	
	private Task refreshTimer;
	
	@SafeVarargs
	/**
	 * Creates a new stroke for a particle rig
	 * @param build The particle build
	 * @param position The LOCAL position of the stroke (usually 0,0 with a given offset and scale)
	 * @param rigAxis All the axis animations
	 */
	public ParticleStroke(ParticleBuilder build, Consumer<AnimatorRig>... rigAxis) {
		this.build = build;
		
		for (Consumer<AnimatorRig> rig : rigAxis)
			rig.accept(this.rig);
		
		refreshTimer = Timer.register(new Task().setDuration(1L));
	}
	
	public ParticleStroke setTimeIncrement(float increment) { this.timeIncrement = increment; return this; }
	public ParticleStroke setRefreshRate(long refreshRate) { refreshTimer.setDuration(refreshRate); return this; }
	
	public void setAnimator(Animator animator) { this.animator = animator; }
	public Transform getLocalPosition() { return localPosition; }
	
	public void setBuild(ParticleBuilder build) { this.build = build; }
	
	public ParticleBuilder[] evaluate() {
		if (!refreshTimer.isComplete())
			return new ParticleBuilder[0];
		refreshTimer.restart();
		
		int elementCount = (int) Math.floor(1f / timeIncrement);
		ParticleBuilder[] elements = new ParticleBuilder[elementCount];
		
		for (int i = 0; i < elementCount; i++) {
			ParticleBuilder copy = build.clone();
			
			//The scale of the stroke is applied here, all other scaling above this should avoid rescaling this
			Transform rigOffset = localPosition.clone().add(rig.evaluate(i * timeIncrement));
			copy.location(null, rigOffset.getX(), rigOffset.getY(), rigOffset.getZ());
	
			elements[i] = copy;
		}
		
		return elements;
	}

	@Override
	public void cancel() {
		if (animator != null)
			animator.cancel();
		
		refreshTimer.cancel();
		//Rig shouldn't need to be canceled since its a manual rig
	}

	@Override
	public Subscriber<Runnable> onCanceled() { return onCancelEvent.getSubscriber(); }
}
