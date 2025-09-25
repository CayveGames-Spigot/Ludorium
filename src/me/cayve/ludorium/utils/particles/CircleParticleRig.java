package me.cayve.ludorium.utils.particles;

import com.destroystokyo.paper.ParticleBuilder;

import me.cayve.ludorium.utils.animations.FunctionalAnimation;
import me.cayve.ludorium.utils.animations.SinWaveAnimation;

public class CircleParticleRig extends ParticleRig {

	public CircleParticleRig(ParticleBuilder build, float radius) {
		addStroke(new ParticleStroke(build,
				rig -> rig.setXAnimation(new FunctionalAnimation(x -> (float)(radius*Math.cos(x * Math.PI)), new SinWaveAnimation())),
				rig -> rig.setZAnimation(new FunctionalAnimation(x -> (float)(radius*Math.sin(x * Math.PI)), new SinWaveAnimation())))
				.setTimeIncrement(.01f));
	}
}
