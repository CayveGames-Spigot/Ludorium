package me.cayve.ludorium.utils.animations.patterns;

import me.cayve.ludorium.utils.ArrayUtils;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.SinWaveAnimation;

public class ArrayAnimations {

	/**
	 * Creates a wave effect down an array
	 * @param sourceKey The task source key
	 * @param animators
	 * @param duration How long, from first start until last animation completes, does the animation take
	 * @param overlap (0-1) What percent should the animations overlap (0 = no overlap, evenly spaced apart. 1 = fully overlapped, all animations trigger at the start)
	 * @param amplitude Height of the waves (from @SinWaveAnimation)
	 * @param timeDelay (0-1) (0, faster time between peaks. 1 slower time between peaks) (from @SinWaveAnimation)
	 */
	public static void wave(String sourceKey, Animator[] animators, float duration, float overlap, float amplitude, float timeDelay) {
		wave(sourceKey, animators, 0, duration, overlap, amplitude, timeDelay);
	}
	/**
	 * Creates a wave effect down an array
	 * @param sourceKey The task source key
	 * @param animators
	 * @param startIndex Where to start the wave
	 * @param duration How long, from first start until last animation completes, does the animation take
	 * @param overlap (0-1) What percent should the animations overlap (0 = no overlap, evenly spaced apart. 1 = fully overlapped, all animations trigger at the start)
	 * @param amplitude Height of the waves (from @SinWaveAnimation)
	 * @param timeDelay (0-1) (0, faster time between peaks. 1 slower time between peaks) (from @SinWaveAnimation)
	 */
	public static void wave(String sourceKey, Animator[] animators, int startIndex, float duration, float overlap, float amplitude, float timeDelay)
	{
		wave(sourceKey, animators, startIndex, 1, duration, overlap, amplitude, timeDelay);
	}
	
	/**
	 * Creates a wave effect down an array
	 * @param sourceKey The task source key
	 * @param animators
	 * @param startIndex Where to start the wave
	 * @param direction (-1, 0, 1) (-1 = Backwards) (0 = Both directions) (1 = Forward)
	 * @param duration How long, from first start until last animation completes, does the animation take
	 * @param overlap (0-1) What percent should the animations overlap (0 = no overlap, evenly spaced apart. 1 = fully overlapped, all animations trigger at the start)
	 * @param amplitude Height of the waves (from @SinWaveAnimation)
	 * @param timeDelay (0-1) (0, faster time between peaks. 1 slower time between peaks) (from @SinWaveAnimation)
	 */
	public static void wave(String sourceKey, Animator[] animators, int startIndex, int direction, float duration, float overlap, float amplitude, float timeDelay) {
		int loopSize = direction == 0 ? (int)Math.ceil(animators.length / 2f) : animators.length;
		
		float evenAnimDur = duration / animators.length;
		for (int i = 0; i < loopSize; i++) {
			//If direction is both ways, do this logic twice
			for (int k = 0; k < (direction == 0 ? 2 : 1); k++) {
				final int index = ArrayUtils.wrap(animators.length, startIndex + 
						(i * (direction == -1 || direction == 0 && k == 1 ? -1 : 1))); //Flip the index direction if going reverse
				
				Timer.register(new Task(sourceKey).setDuration(i * evenAnimDur * (1 - overlap)).registerOnComplete(() -> {
					animators[index].getDefaultRig().setYAnimation(new SinWaveAnimation(amplitude, timeDelay)
							.setDuration(evenAnimDur + ((duration - evenAnimDur) * overlap)));
				}));
			}
		}
	}
}
