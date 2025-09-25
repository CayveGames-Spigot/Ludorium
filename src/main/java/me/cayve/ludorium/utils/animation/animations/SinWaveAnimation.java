package me.cayve.ludorium.utils.animation.animations;

import me.cayve.ludorium.LudoriumException;
import me.cayve.ludorium.utils.animation.Animation;

public class SinWaveAnimation extends Animation<Float> {
	
	private float amplitude = 1;
	private float timeDelay = 1;
	
	/**
	 * @param amplitude Height of the waves
	 * @param timeDelay (0-1) (0, faster time between peaks. 1 slower time between peaks)
	 */
	public SinWaveAnimation(float amplitude, float timeDelay) {
		if (timeDelay < 0 || timeDelay > 1)
			throw new LudoriumException("SinWaveAnimation timeDelay is invalid: " + timeDelay);
		
		this.amplitude = amplitude;
		this.timeDelay = (timeDelay * 5) + 0.2f; //This puts it in a valid range. (0-1) just makes it more easy for designers
	}
	
	/**
	 * @param amplitude Height of the waves
	 */
	public SinWaveAnimation(float amplitude) {
		this.amplitude = amplitude;
	}
	
	/**
	 * Default sine wave
	 */
	public SinWaveAnimation() {
		amplitude = 1;
		timeDelay = 1;
	}
	
	private static final double CONSTANT = 4 / Math.pow(Math.PI, 2);
	
	@Override
	public Float evaluate(float time) {
		if (time < 0 || time > 1)
			throw new LudoriumException("Animation time is invalid: " + time);
		
		double x = time * 2 * Math.PI;
		
		if (time <= .5)
			return (float) (amplitude * Math.pow(CONSTANT * (x * (Math.PI - x)), timeDelay));
		else
			return (float) (-amplitude * Math.pow(CONSTANT * (x - Math.PI) * (2 * Math.PI - x), timeDelay));
	}

}
