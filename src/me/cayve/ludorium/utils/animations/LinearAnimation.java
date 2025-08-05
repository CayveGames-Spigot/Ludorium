package me.cayve.ludorium.utils.animations;

public class LinearAnimation extends Animation<Float> {

	private float min, max;
	
	public LinearAnimation(float min, float max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public Float evaluate(float time) {
		return ((max - min) * time) + min;
	}
	
}
