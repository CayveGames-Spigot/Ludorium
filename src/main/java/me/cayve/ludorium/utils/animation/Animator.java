package me.cayve.ludorium.utils.animation;

import java.util.concurrent.CopyOnWriteArrayList;

import me.cayve.ludorium.utils.animation.animations.rigs.AnimatorRig;
import me.cayve.ludorium.utils.entities.DisplayEntity.EntityComponent;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.interfaces.Cancelable;
import me.cayve.ludorium.utils.interfaces.Destroyable;
import me.cayve.ludorium.utils.locational.Transform;

public class Animator implements EntityComponent, Destroyable, Cancelable {
	
	private Transform position;
	
	private Event0 onCompleteEvent = new Event0();
	private Event0 onCancelEvent = new Event0();
	private Event0 onDestroyEvent = new Event0();
	
	private AnimatorRig defaultRig = new AnimatorRig();
	private CopyOnWriteArrayList<AnimatorRig> rigs = new CopyOnWriteArrayList<>();
	
	/**
	 * Animator will apply animations to the offset of the transform
	 * @param positionTransform
	 */
	public Animator(Transform positionTransform) {
		this.position = positionTransform;
		
		play(defaultRig);
		rigs.remove(defaultRig);
	}
	
	private void update() {
		Transform offset = new Transform(true);
			
		offset.add(defaultRig.evaluate());
		rigs.forEach(x -> offset.add(x.evaluate()));
		
		position.setOffset(offset);
	}
	
	private boolean isAnyRigAnimating() {
		for (AnimatorRig rig : rigs)
			if (rig.isAnimating())
				return true;
		
		return defaultRig.isAnimating();
	}
	
	public AnimatorRig getDefaultRig() { return defaultRig; }
	
	public void play(AnimatorRig rig) {
		rigs.add(rig);
		
		rig.onUpdate().subscribe(this::update);
		rig.onCanceled().subscribe(() -> onRigCancel(rig));
		rig.onCompleted().subscribe(() -> onRigComplete(rig));
	}
	
	private void onRigComplete(AnimatorRig rig) {
		rigs.remove(rig);
		
		if (!isAnyRigAnimating())
			onCompleteEvent.run();
	}
	
	private void onRigCancel(AnimatorRig rig) {
		rigs.remove(rig);
		
		if (!isAnyRigAnimating())
			cancel();
	}
	
	public void cancelRigType(Class<? extends AnimatorRig> rigType) {
		for (AnimatorRig rig : rigs) {
			if (rig.getClass().equals(rigType))
				rig.cancel();
		}
		
		if (defaultRig.getClass().equals(rigType))
			defaultRig.cancel();
	}
	
	@Override
	public void cancel() {
		defaultRig.cancel();
		rigs.forEach((x) -> x.cancel());
		rigs.clear();
		
		onCancelEvent.run();
		
		position.resetOffsets();
	}
	
	@Override
	public void destroy() { 
		cancel(); 
		
		onDestroyEvent.run();
	}
	
	public Subscriber<Runnable> onCompleted() { return onCompleteEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onCanceled() { return onCancelEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onDestroyed() { return onDestroyEvent.getSubscriber(); }
}
