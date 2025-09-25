package me.cayve.ludorium.utils.animation.animations;

import me.cayve.ludorium.utils.animation.Animation;

public class LinearAnimation extends Animation<Float> {

	private float start, finish;
	
	public LinearAnimation() {
		this.start = 0;
		this.finish = 1;
	}
	public LinearAnimation(float start, float finish) {
		this.start = start;
		this.finish = finish;
	}
	
	@Override
	public Float evaluate(float time) {
		return ((finish - start) * time) + start;
	}
	
}
