package me.cayve.ludorium.utils.animations;

import me.cayve.ludorium.main.LudoriumException;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;

public abstract class Animation<T> {
	
	private Task task;

	private boolean loop;
	private float startTime = 0, endTime = 1;
	private float speed = 1, duration = 1;
	
	/**
	 * Sets the speed of this animation
	 * @param speed
	 * @return
	 */
	public Animation<T> setSpeed(float speed) { this.speed = speed; return this; }
	
	/**
	 * Sets the duration of this animation. Setting this too low might cause visual bugs since
	 * this value will be converted into ticks
	 * @param duration
	 * @return
	 */
	public Animation<T> setDuration(float duration) { this.duration = duration; return this; }
	
	/**
	 * Sets this animation to loop
	 * @return self
	 */
	public Animation<T> loops() { loop = true; return this; }
	/**
	 * Sets this animation to be a sub-animation of the base.
	 * (e.x. Parabola of a sin wave)
	 * @param startTime The start time of the base animation (0.0-endTime)
	 * @param endTime The end time of the base animation (startTime-1.0)
	 * @return self
	 */
	public Animation<T> subanim(float startTime, float endTime) 
	{
		if (startTime < 0 || endTime > 1 || endTime <= startTime || startTime >= endTime)
			throw new LudoriumException("Animation: Invalid sub-animation time (" + startTime + "-" + endTime + ")");
		
		this.startTime = startTime; 
		this.endTime = endTime; 
		return this; 
	}
	
	/**
	 * The animation state at a given time (0-1)
	 * @param time The time of the animation state to evaluate (between 0-1)
	 * @return The animation state
	 */
	public abstract T evaluate(float time);
	
	/**
	 * @return The animation state at the current time
	 */
	public T evaluate() { return evaluate(((endTime - startTime) * (task == null ? 0 : task.getPercentTimeCompleted())) + startTime); }
	
	public boolean isComplete() { return task != null && task.isComplete(); }
	/**
	 * Registers listeners and actually starts the animation
	 * @param onUpdate is called once every animation refresh (animation refresh rates vary)
	 * @param onComplete is called at the end of the animation (looping animations will never call this)
	 */
	public void registerListeners(Runnable onUpdate, Runnable onComplete) {
		if (task != null)
			task.cancel();
		
		//If speed is set to 0, the animation will never complete
		if (speed == 0)
			return;
		
		task = Timer.register(new Task().setRefreshRate(0).setDuration(duration / speed)
				.registerOnUpdate(() -> {
					if (onUpdate != null)
						onUpdate.run();
				})
				.registerOnComplete(() -> {
					if (loop) {
						task.restart();
						return;
					}
					
					if (onComplete != null)
						onComplete.run();
				}));
	}
	
	public void destroy() {
		if (task != null)
			task.cancel();
	}
}
