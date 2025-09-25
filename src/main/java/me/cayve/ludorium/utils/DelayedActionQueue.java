package me.cayve.ludorium.utils;

import java.util.ArrayList;

import me.cayve.ludorium.utils.Timer.Task;

public class DelayedActionQueue {

	private record Pair(float actionDuration, Runnable action) {}
	
	private final ArrayList<Pair> queue = new ArrayList<>();
	private final Task timer = Timer.register(new Task().registerOnComplete(this::onComplete).pause());
	
	public void queue(float actionDuration, Runnable action) {
		queue.add(new Pair(actionDuration, action));
		
		if (timer.isPaused() || timer.isComplete())
			onComplete();
	}
	
	private void onComplete() {
		if (queue.isEmpty())
			return;
		
		timer.setDuration(queue.getFirst().actionDuration());
		timer.restart();
		
		queue.getFirst().action().run();
		queue.removeFirst();
	}
	
	public void destroy() {
		timer.cancel();
	}
}
