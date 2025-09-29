package me.cayve.ludorium.utils.animation.animations.rigs;

import org.bukkit.Location;
import org.joml.Vector3f;

import me.cayve.ludorium.utils.animation.animations.LinearAnimation;
import me.cayve.ludorium.utils.animation.animations.SinWaveAnimation;
import me.cayve.ludorium.utils.events.Event.Subscription;
import me.cayve.ludorium.utils.locational.Transform;

public class PathingAnimationRig extends AnimatorRig {

	//Required to keep track of the current offset this rig is providing
	//in order to seamlessly jump the pieces
	private Transform currentOffset = new Transform();
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
		//Ignore the last loop (element 0, >=) because its handled after
		for (int i = jumpCount - 1; i > 0; i--) {
			final int index = i;
			final Runnable finalizedIteration = nestedComplete;

			nestedComplete = () -> {
				if (jumpCallbacks[index] != null)
					jumpCallbacks[index].run();
				
				if (index != jumpCount - 1)
					applyJumpAnimations(finalizedIteration, index == jumpCount - 2,
							path[index], path[index + 1], jumpDuration, amplitude);
			};
		}
		
		applyJumpAnimations(nestedComplete, jumpCount == 2, path[0], path[1], jumpDuration, amplitude);
	}
	
	private void applyJumpAnimations(Runnable onComplete, boolean lastJump, Location origin, Location target, 
			float jumpDuration, float amplitude) {

		setXAnimation(new LinearAnimation(currentOffset.getX(), currentOffset.getX() + (float)target.getX() - (float)origin.getX())
				.setDuration(jumpDuration));
		setYAnimation(new SinWaveAnimation(amplitude).subanim(0, .5f).setDuration(jumpDuration));
		setZAnimation(new LinearAnimation(currentOffset.getZ(), currentOffset.getZ() + (float)target.getZ() - (float)origin.getZ())
				.setDuration(jumpDuration));
		
		//Update the current offset to add the movement of the piece
		currentOffset.setPosition(currentOffset.getPosition().add(new Vector3f(
				(float)target.getX() - (float)origin.getX(),
				0,
				(float)target.getZ() - (float)origin.getZ())));
		
		Subscription subscription = onCompleted().subscribe(() ->
		{
			if (!lastJump)
				onCompleted().setCanceled(true);
			onComplete.run();
		}, true, 1);
		onCanceled().subscribe(subscription::close, true);
	}
}
