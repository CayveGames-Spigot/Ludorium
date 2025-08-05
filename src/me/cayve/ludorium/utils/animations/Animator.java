package me.cayve.ludorium.utils.animations;

import java.util.function.Consumer;

import me.cayve.ludorium.utils.locational.Transform;
import me.cayve.ludorium.utils.locational.Vector2D;
import me.cayve.ludorium.utils.locational.Vector3D;

public class Animator {

	private Consumer<Transform> onUpdate;
	private Runnable onComplete, onCanceled;
	
	private Animation<Float> x, y, z, scale, pitch, yaw;
	private Animation<Vector3D> position;
	private Animation<Vector2D> rotation;
	private Animation<Transform> transform;
	
	public Animator(Consumer<Transform> onUpdate, Runnable onComplete, Runnable onCanceled) {
		this.onUpdate = onUpdate;
		this.onComplete = onComplete;
		this.onCanceled = onCanceled;
	}
	
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
	public void setPositionAnimation(Animation<Vector3D> animation) {
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
	public void setRotationAnimation(Animation<Vector2D> animation) {
		rotation = animation;
		setAnimationListeners(animation);
		
		pitch = yaw = null;
	}
	
	/**
	 * Cancels all animations on all axes
	 */
	public void cancelAnimations() {
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
		
		onCanceled.run();
	}
	
	private void onAnyAnimationUpdate() {
		Transform offset = new Transform();
		
		//Default is set to 1, but this is offset so it needs to be 0
		offset.scale = 0;
		
		if (transform != null)
			offset = transform.evaluate();
		else
		{
			if (position != null)
				offset.setPosition(position.evaluate());
			else
			{
				if (x != null)
					offset.x = x.evaluate();
				if (y != null)
					offset.y = y.evaluate();
				if (z != null)
					offset.z = z.evaluate();
			}
			
			if (rotation != null)
				offset.setRotation(rotation.evaluate());
			else
			{
				if (pitch != null)
					offset.pitch = pitch.evaluate();
				if (yaw != null)
					offset.yaw = yaw.evaluate();
			}
			
			if (scale != null)
				offset.scale = scale.evaluate();
		}
		
		onUpdate.accept(offset);
	}
	
	private void onAnyAnimationComplete() {
		if (!isAnimating())
			onComplete.run();
	}
	
	private void setAnimationListeners(Animation<?> animation) {
		animation.registerListeners(this::onAnyAnimationUpdate, this::onAnyAnimationComplete);
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
}
