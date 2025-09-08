package me.cayve.ludorium.utils.animations.rigs;

import me.cayve.ludorium.utils.animations.LinearAnimation;
import me.cayve.ludorium.utils.animations.OffsetAnimation;
import me.cayve.ludorium.utils.animations.SinWaveAnimation;

public class HoverAnimationRig extends AnimatorRig {
	
	/**
	 * Creates a rig for a default hover animation (similar to items hovering on the ground)
	 */
	public HoverAnimationRig() {
		construct(1, .3f, .2f, .4f);
	}
	
	/**
	 * Creates a rig for a hover animation (similar to items hovering on the ground)
	 * @param raisedOffset How high from the origin point the hover should be
	 * @param amplitude The amplitude of the hover wave effect
	 * @param waveSpeed The speed of the hover wave effect
	 * @param rotateSpeed The rotation speed
	 */
	public HoverAnimationRig(float raisedOffset, float amplitude, float waveSpeed, float rotateSpeed) {
		construct(raisedOffset, amplitude, waveSpeed, rotateSpeed);
	}
	
	private void construct(float raisedOffset, float amplitude, float waveSpeed, float rotateSpeed) {
		setYawAnimation(new LinearAnimation(0, 360).loops().setSpeed(rotateSpeed).randomize());
		setYAnimation(new OffsetAnimation(raisedOffset, new SinWaveAnimation(amplitude)).loops().setSpeed(waveSpeed).randomize());
	}
}
