package me.cayve.ludorium.utils.animation.animations.rigs;

import org.bukkit.Location;

import me.cayve.ludorium.utils.animation.animations.LinearAnimation;
import me.cayve.ludorium.utils.animation.animations.SinWaveAnimation;
import me.cayve.ludorium.utils.events.Event.Subscription;

public class PathingAnimationRig extends AnimatorRig {

	/**
	 * Creates a rig to animate a token through a path using jumping animations. (Like jumping on each space to your target)
	 * @param path The path to follow. First element = origin, Last element = target.
	 * @param jumpCallbacks Callbacks after each jump (index 0 corresponds to the end of the first jump)
	 * @param jumpDuration How long it takes per jump
	 * @param amplitude How high up the token goes while jumping
	 */
	public PathingAnimationRig(Location[] path, Runnable[] jumpCallbacks, float jumpDuration, float amplitude) {
		final int jumpCount = path.length;
		
		Runnable nestedComplete = null;
		for (int i = jumpCount - 1; i > 0; i--) {
			final int index = i;
			final Runnable finalizedIteration = nestedComplete;

			nestedComplete = () -> {
				if (jumpCallbacks[index] != null)
					jumpCallbacks[index].run();
				
				if (index != jumpCount - 1)
					applyJumpAnimations(finalizedIteration, path[index], path[index + 1], jumpDuration, amplitude);
			};
		}
		
		applyJumpAnimations(nestedComplete, path[0], path[1], jumpDuration, amplitude);
	}
	
	private void applyJumpAnimations(Runnable onComplete, Location origin, Location target, 
			float jumpDuration, float amplitude) {

		setXAnimation(new LinearAnimation(0, (float)target.getX() - (float)origin.getX()).setDuration(jumpDuration));
		setYAnimation(new SinWaveAnimation(amplitude).subanim(0, .5f).setDuration(jumpDuration));
		setZAnimation(new LinearAnimation(0, (float)target.getZ() - (float)origin.getZ()).setDuration(jumpDuration));
		
		Subscription subscription = onCompleted().subscribe(onComplete, true);
		onCanceled().subscribe(subscription::close, true);
	}
}
