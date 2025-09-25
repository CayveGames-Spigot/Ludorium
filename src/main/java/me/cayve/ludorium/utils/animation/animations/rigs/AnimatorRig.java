package me.cayve.ludorium.utils.animation.animations.rigs;

import org.joml.Vector2f;
import org.joml.Vector3f;

import me.cayve.ludorium.utils.animation.Animation;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event.Subscriber;
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
	
	private boolean isManual = false;
	
	/**
	 * Creates an animator rig set up for automatic evaluation
	 */
	public AnimatorRig() {}
	
	/**
	 * Creates an animator rig and sets the evaluation method
	 * @param isManual If true, animations will not play automatically. Speed and duration will be obsolete
	 */
	public AnimatorRig(boolean isManual) { this.isManual = isManual; }
	
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
	
	/**
	 * Evaluates the current rig based on all animation durations - AUTOMATIC MODE ONLY
	 * @return
	 */
	public Transform evaluate() {
		Transform offset = new Transform(true);
		
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
			
			if (this.scale != null)
				offset.setScale(this.scale.evaluate());
		}
		
		return offset;
	}
	
	/**
	 * Evaluates the current rig based on the given time, used in manual mode
	 * @param time
	 * @return
	 */
	public Transform evaluate(float time) {
		//This method needs to be copied since each animation has its own time variable in automatic mode.
		//Having evaluate() internally call this method would not work
		
		Transform offset = new Transform(true);
		
		if (transform != null)
			offset = transform.evaluate(time);
		else
		{
			if (position != null)
				offset.setPosition(position.evaluate(time));
			else
			{
				if (x != null)
					offset.setX(x.evaluate(time));
				if (y != null)
					offset.setY(y.evaluate(time));
				if (z != null)
					offset.setZ(z.evaluate(time));
			}
			
			if (rotation != null)
				offset.setRotation(rotation.evaluate(time));
			else
			{
				if (pitch != null)
					offset.setPitch(pitch.evaluate(time));
				if (yaw != null)
					offset.setYaw(yaw.evaluate(time));
			}
			
			if (scale != null)
				offset.setScale(scale.evaluate(time));
		}
		
		return offset;
	}
	
	/**
	 * Whether any animation in this rig is still animating - AUTOMATIC MODE ONLY
	 * @return
	 */
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
		if (!isManual)
			animation.registerListeners(onUpdateEvent, this::onAnyAnimationComplete);
	}
	
	/**
	 * Cancels all animations on all axes - AUTOMATIC MODE ONLY
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
	
	/**
	 * Called on any animation update - AUTOMATIC MODE ONLY
	 * @return
	 */
	public Subscriber<Runnable> onUpdate() { return onUpdateEvent.getSubscriber(); }
	/**
	 * Called when entire rig is complete - AUTOMATIC MODE ONLY
	 * @return
	 */
	public Subscriber<Runnable> onCompleted() { return onCompleteEvent.getSubscriber(); }
	/**
	 * Called when entire rig is canceled - AUTOMATIC MODE ONLY
	 * @return
	 */
	@Override public Subscriber<Runnable> onCanceled() { return onCancelEvent.getSubscriber(); }
}
