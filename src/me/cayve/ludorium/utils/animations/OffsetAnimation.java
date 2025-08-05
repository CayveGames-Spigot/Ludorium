package me.cayve.ludorium.utils.animations;

public class OffsetAnimation extends Animation<Float> {

	private float offset;
	private Animation<Float> animation;
	
	public OffsetAnimation(float offset, Animation<Float> animation) {
		this.offset = offset;
		this.animation = animation;
	}
	
	@Override
	public Float evaluate(float time) {
		return offset + animation.evaluate(time);
	}
	
}
