package me.cayve.ludorium.utils.animations;

public class LinearAnimation extends Animation<Float> {

	private float start, finish;
	
	public LinearAnimation(float start, float finish) {
		this.start = start;
		this.finish = finish;
	}
	
	@Override
	public Float evaluate(float time) {
		return ((finish - start) * time) + start;
	}
	
}
