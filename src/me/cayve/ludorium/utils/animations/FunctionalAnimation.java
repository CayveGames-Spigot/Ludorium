package me.cayve.ludorium.utils.animations;

import java.util.function.Function;

public class FunctionalAnimation extends Animation<Float> {

	private Function<Float, Float> action;
	private Animation<Float> animation;
	
	public FunctionalAnimation(Function<Float, Float> action, Animation<Float> animation) {
		this.action = action;
		this.animation = animation;
	}

	@Override
	public Float evaluate(float time) {
		return action.apply(animation.evaluate(time));
	}

	@Override
	public void destroy() {
		super.destroy();
		
		animation.destroy();
	}
}
