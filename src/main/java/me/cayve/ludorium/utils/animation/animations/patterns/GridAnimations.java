package me.cayve.ludorium.utils.animation.animations.patterns;

import org.joml.Vector2i;

import me.cayve.ludorium.utils.SourceKey;
import me.cayve.ludorium.utils.Timer;
import me.cayve.ludorium.utils.Timer.Task;
import me.cayve.ludorium.utils.animation.Animator;
import me.cayve.ludorium.utils.animation.animations.SinWaveAnimation;
import me.cayve.ludorium.utils.locational.Grid;
import me.cayve.ludorium.utils.locational.eDirection;

public class GridAnimations {

	/**
	 * Utilizes @SinWaveAnimation to create a wave effect
	 * @param sourceKey The task source key
	 * @param animators The grid of animators
	 * @param direction The direction the wave starts from. (North-East simply means the 0,0 of the grid array)
	 * @param duration How long, from first start until last animation completes, does the animation take
	 * @param overlap (0-1) What percent should the animations overlap (0 = no overlap, evenly spaced apart. 1 = fully overlapped, all animations trigger at the start)
	 * @param amplitude Height of the waves (from @SinWaveAnimation)
	 * @param timeDelay (0-1) (0, faster time between peaks. 1 slower time between peaks) (from @SinWaveAnimation)
	 */
	public static void wave(SourceKey sourceKey, Grid<Animator> animators, eDirection side, float duration, float overlap, float amplitude, float timeDelay) {
		Vector2i dV = side.getVector();
		
		if (side.isCardinal()) { //Not diagonal
			int height = animators.getHeight();
			int width = animators.getWidth();
			
			boolean isColWave = dV.x != 0;
			
			float evenAnimDur = duration / ((isColWave ? width : height));
			
			//Loops through either rows or cols
			for (int wave = 0; wave < (isColWave ? width : height); wave++) {
				final int currentWave = wave; //Allows use in lambda
				
				Timer.register(new Task(sourceKey).setDuration(wave * evenAnimDur * (1 - overlap)).registerOnComplete(() -> {
					//Loops through opposite direction
					for (int i = 0; i < (isColWave ? height : width); i++) {
						//If the wave is with rows, x is i
						//If the wave is with cols, x is wave (adjusted for direction)
						int x = !isColWave ? i : (dV.x < 0) ? currentWave : Math.max(width - currentWave - 1, 0);
						int y = isColWave ? i : (dV.y < 0) ? currentWave : Math.max(height - currentWave - 1, 0);

						if (animators.get(x, y) == null) continue;
						
						animators.get(x, y).getDefaultRig().setYAnimation(new SinWaveAnimation(amplitude, timeDelay)
								.setDuration(evenAnimDur + ((duration - evenAnimDur) * overlap)));
					}
				}));
				
			}
			
		} else { //Diagonal
			int waveCount = animators.getWidth() + animators.getHeight() - 1;

			float evenAnimDur = duration / waveCount;
			
			//The most amount of elements in a single diagonal can only be the minimum of the lengths
			int waveElements = Math.min(animators.getWidth(), animators.getHeight());
			
			for (int wave = 0; wave < waveCount; wave++) {
				final int currentWave = wave; //Allows use in lambda
				
				Timer.register(new Task(sourceKey).setDuration(wave * evenAnimDur * (1 - overlap)).registerOnComplete(() -> {
					for (int i = 0; i < waveElements; i++) {

						//Calculate x and y position of this wave element
						int x = waveElements - i - 1;
						int y = currentWave - waveElements + i + 1;
						
						//If the minimum was the height, swap the x and y
						if (animators.getWidth() > animators.getHeight())
						{
							int t = x;
							x = y;
							y = t;
						}
						
						//Orient the x and y based on wave direction
						x = dV.x < 0 ? x : animators.getWidth() - x - 1;
						y = dV.y < 0 ? y : animators.getHeight() - y - 1;

						//.get handles out of bounds
						if (animators.get(x, y) == null) continue;

						animators.get(x, y).getDefaultRig().setYAnimation(new SinWaveAnimation(amplitude, timeDelay)
								.setDuration(evenAnimDur + ((duration - evenAnimDur) * overlap)));
					}
				}));
			}
		}
	}
	
}
