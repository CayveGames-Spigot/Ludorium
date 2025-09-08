package me.cayve.ludorium.utils.animations;

import java.util.concurrent.CopyOnWriteArrayList;

import me.cayve.ludorium.utils.animations.rigs.AnimatorRig;
import me.cayve.ludorium.utils.entities.DisplayEntity.EntityComponent;
import me.cayve.ludorium.utils.functionals.Event.Subscriber;
import me.cayve.ludorium.utils.functionals.Event0;
import me.cayve.ludorium.utils.interfaces.Cancelable;
import me.cayve.ludorium.utils.interfaces.Destroyable;
import me.cayve.ludorium.utils.locational.Transform;

public class Animator implements EntityComponent, Destroyable, Cancelable {
	
	private Transform originTransform, referenceTransform;
	
	private Event0 onCompleteEvent = new Event0();
	private Event0 onCancelEvent = new Event0();
	private Event0 onDestroyEvent = new Event0();
	
	private AnimatorRig defaultRig = new AnimatorRig();
	private CopyOnWriteArrayList<AnimatorRig> rigs = new CopyOnWriteArrayList<>();
	
	/**
	 * Animations work around an origin point, the reference is the actual position that will change
	 * @param originTransform
	 * @param referenceTransform
	 */
	public Animator(Transform originTransform, Transform referenceTransform) {
		this.originTransform = originTransform;
		this.referenceTransform = referenceTransform;
		
		play(defaultRig);
		rigs.remove(defaultRig);
	}
	
	private void update() {
		Transform offset = new Transform();
		
		offset.set(originTransform);
		
		rigs.forEach(x -> offset.add(x.evaluate()));
		
		referenceTransform.set(offset);
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
		
		referenceTransform.set(originTransform);
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
