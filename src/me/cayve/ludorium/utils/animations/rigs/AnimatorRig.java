package me.cayve.ludorium.utils.animations.rigs;

import org.joml.Vector2f;
import org.joml.Vector3f;

import me.cayve.ludorium.utils.functionals.Event.Subscriber;
import me.cayve.ludorium.utils.animations.Animation;
import me.cayve.ludorium.utils.functionals.Event0;
import me.cayve.ludorium.utils.interfaces.Cancelable;
import me.cayve.ludorium.utils.locational.Transform;

public class AnimatorRig implements Cancelable {
	
	private Event0 onUpdateEvent = new Event0();
	private Event0 onCompleteEvent = new Event0();
	private Event0 onCancelEvent = new Event0();
	
	private Animation<Float> x, y, z, scale, pitch, yaw;
	private Animation<Vector3f> position;
	private Animation<Vector2f> rotation;
	private Animation<Transform> transform;
	
	/**
	 * Sets the transform animation. Overwrites all other animations
	 * @param animation
	 */
	public void setTransformAnimation(Animation<Transform> animation) {
		transform = animation;
		setAnimationListeners(animation);
		
		x = y = z = scale = pitch = yaw = null;
		position = null;
		rotation = null;
	}
	/**
	 * Sets the X-axis animation. Overwrites position animation
	 * @param animation
	 */
	public void setXAnimation(Animation<Float> animation) {
		x = animation;
		setAnimationListeners(animation);
		
		position = null;
	}
	
	/**
	 * Sets the Y-axis animation. Overwrites position animation
	 * @param animation
	 */
	public void setYAnimation(Animation<Float> animation) {
		y = animation;
		setAnimationListeners(animation);
		
		position = null;
	}
	
	/**
	 * Sets the Z-axis animation. Overwrites position animation
	 * @param animation
	 */
	public void setZAnimation(Animation<Float> animation) {
		z = animation;
		setAnimationListeners(animation);
		
		position = null;
	}
	
	/**
	 * Sets the position animation. Overwrites x, y, and z animations
	 * @param animation
	 */
	public void setPositionAnimation(Animation<Vector3f> animation) {
		position = animation;
		setAnimationListeners(animation);
		
		x = y = z = null;
	}
	
	/**
	 * Sets the scale animation
	 * @param animation
	 */
	public void setScaleAnimation(Animation<Float> animation) {
		scale = animation;
		setAnimationListeners(animation);
	}
	
	/**
	 * Sets the pitch animation. Overwrites rotation animation
	 * @param animation
	 */
	public void setPitchAnimation(Animation<Float> animation) {
		pitch = animation;
		setAnimationListeners(animation);
		
		rotation = null;
	}
	
	/**
	 * Sets the yaw animation. Overwrites rotation animation
	 * @param animation
	 */
	public void setYawAnimation(Animation<Float> animation) {
		yaw = animation;
		setAnimationListeners(animation);
		
		rotation = null;
	}
	
	/**
	 * Sets the rotation animation. Overwrites pitch and yaw animation
	 * @param animation
	 */
	public void setRotationAnimation(Animation<Vector2f> animation) {
		rotation = animation;
		setAnimationListeners(animation);
		
		pitch = yaw = null;
	}
	
	public Transform evaluate() {
		Transform offset = new Transform();
		
		//Default is set to 1, but this is offset so it needs to be 0
		offset.setScale(0);
		
		if (transform != null)
			offset = transform.evaluate();
		else
		{
			if (position != null)
				offset.setPosition(position.evaluate());
			else
			{
				if (x != null)
					offset.setX(x.evaluate());
				if (y != null)
					offset.setY(y.evaluate());
				if (z != null)
					offset.setZ(z.evaluate());
			}
			
			if (rotation != null)
				offset.setRotation(rotation.evaluate());
			else
			{
				if (pitch != null)
					offset.setPitch(pitch.evaluate());
				if (yaw != null)
					offset.setYaw(yaw.evaluate());
			}
			
			if (scale != null)
				offset.setScale(scale.evaluate());
		}
		
		return offset;
	}
	
	public boolean isAnimating() {
		boolean isAnimating = false;
		if (	(transform != null && !transform.isComplete()) ||
				(position != null && !position.isComplete()) ||
				(rotation != null && !rotation.isComplete()) ||
				(pitch != null && !pitch.isComplete()) ||
				(yaw != null && !yaw.isComplete()) ||
				(scale != null && !scale.isComplete()) ||
				(x != null && !x.isComplete()) ||
				(y != null && !y.isComplete()) ||
				(z != null && !z.isComplete()))
			isAnimating = true;
		return isAnimating;
	}
	
	private void onAnyAnimationComplete() {
		if (!isAnimating())
			onCompleteEvent.run();
	}
	
	private void setAnimationListeners(Animation<?> animation) {
		animation.registerListeners(onUpdateEvent, this::onAnyAnimationComplete);
	}
	
	/**
	 * Cancels all animations on all axes
	 */
	@Override
	public void cancel() {
		if (!isAnimating())
			return;
		
		if (x != null)
			x.destroy();
		if (y != null)
			y.destroy();
		if (z != null)
			z.destroy();
		if (scale != null)
			scale.destroy();
		if (pitch != null)
			pitch.destroy();
		if (yaw != null)
			yaw.destroy();
		if (position != null)
			position.destroy();
		if (rotation != null)
			rotation.destroy();
		if (transform != null)
			transform.destroy();
		
		onCancelEvent.run();
	}
	
	public Subscriber<Runnable> onUpdate() { return onUpdateEvent.getSubscriber(); }
	public Subscriber<Runnable> onCompleted() { return onCompleteEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onCanceled() { return onCancelEvent.getSubscriber(); }
}
