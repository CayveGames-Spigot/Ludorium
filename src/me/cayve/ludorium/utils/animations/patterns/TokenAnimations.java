package me.cayve.ludorium.utils.animations.patterns;

import org.bukkit.Location;

import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.animations.LinearAnimation;
import me.cayve.ludorium.utils.animations.SinWaveAnimation;
import me.cayve.ludorium.utils.functionals.Event.Subscription;

public class TokenAnimations {
	
	/**
	 * Animates a token through a path using jumping animations. (Like jumping on each space to your target)
	 * @param tokenAnimator The animator of the token
	 * @param path The path to follow. First element = origin, Last element = target.
	 * @param jumpCallbacks Callbacks after each jump (index 0 corresponds to the end of the first jump)
	 * @param duration How long it takes to get to the target
	 * @param amplitude How high up the token goes while jumping
	 */
	public static void jumpTo(Animator tokenAnimator, Location[] path, Runnable[] jumpCallbacks, float duration, float amplitude) {
		final int jumpCount = path.length;
		float jumpDuration = duration / jumpCount;
		
		Runnable nestedComplete = null;
		
		for (int i = jumpCount - 1; i > 0; i++) {
			final int index = i;
			final Runnable finalizedIteration = nestedComplete;
			
			nestedComplete = () -> {
				if (jumpCallbacks[index] != null)
					jumpCallbacks[index].run();
				
				if (index != jumpCount - 1)
					applyJumpAnimations(finalizedIteration, tokenAnimator, path[index], path[index + 1], jumpDuration, amplitude);
			};
		}
		
		applyJumpAnimations(nestedComplete, tokenAnimator, path[0], path[1], jumpDuration, amplitude);
	}
	
	private static void applyJumpAnimations(Runnable onComplete, Animator animator, Location origin, Location target, 
			float jumpDuration, float amplitude) {
		animator.setXAnimation(new LinearAnimation((float)origin.getX(), (float)target.getX()).setDuration(jumpDuration));
		animator.setYAnimation(new SinWaveAnimation(amplitude).subanim(0, .5f).setDuration(jumpDuration));
		animator.setZAnimation(new LinearAnimation((float)origin.getZ(), (float)target.getZ()).setDuration(jumpDuration));
		
		Subscription subscription = animator.onCompleted().subscribe(onComplete, true);
		animator.onCanceled().subscribe(subscription::close, true);
	}
}
