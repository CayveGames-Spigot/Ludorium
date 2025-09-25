package me.cayve.ludorium.utils.particles;

import java.util.ArrayList;

import com.destroystokyo.paper.ParticleBuilder;

import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.animation.Animator;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.interfaces.Cancelable;
import me.cayve.ludorium.utils.locational.Transform;

public class ParticleRig implements Cancelable {
	
	protected ArrayList<ParticleStroke> strokes = new ArrayList<>();
	
	private Animator animator; //For translation animations, not design animations
	private Transform localPosition = new Transform();
	
	private Event0 onCancelEvent = new Event0();
	
	private Task refreshTimer;
	
	public ParticleRig() {
		refreshTimer = Timer.register(new Task().setDuration(1L));
		
		//Update the scaling of the strokes if the rig scale changes
		localPosition.onUpdated().subscribe(() -> {
			strokes.forEach(x -> x.getLocalPosition().setScale(localPosition.getScale()));
		});
	}
	
	public ParticleRig setAnimator(Animator animator) { 
		this.animator = animator; 
		return this;
	}
	
	public Transform getLocalPosition() { return localPosition; }
	
	public ParticleRig setRefreshRate(long refreshRate) { refreshTimer.setDuration(refreshRate); return this; }
	public ParticleRig overrideStrokeBuild(int strokeIndex, ParticleBuilder build) 
	{ 
		strokes.get(strokeIndex).setBuild(build); 
		return this;
	}
	
	public ParticleRig addStroke(ParticleStroke stroke) {
		strokes.add(stroke);
		
		stroke.getLocalPosition().setScale(localPosition.getScale());
		return this;
	}
	
	public ParticleBuilder[] evaluate() {
		if (!refreshTimer.isComplete())
			return new ParticleBuilder[0];
		refreshTimer.restart();
		
		ArrayList<ParticleBuilder> particles = new ArrayList<>();
		
		for (ParticleStroke stroke : strokes) {
			for (ParticleBuilder element : stroke.evaluate()) {

				//Only the position is scaled here, leaving the stroke scaling alone
				element.location(null,
						element.location().getX() + localPosition.getX(), 
						element.location().getY() + localPosition.getY(), 
						element.location().getZ() + localPosition.getZ());
				particles.add(element);
			}
		}

		return particles.toArray(new ParticleBuilder[0]);
	}
	
	@Override
	public void cancel() {
		if (animator != null)
			animator.cancel();
		
		for (ParticleStroke stroke : strokes)
			stroke.cancel();
		
		refreshTimer.cancel();
	}

	@Override
	public Subscriber<Runnable> onCanceled() { return onCancelEvent.getSubscriber(); }
}
