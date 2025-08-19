package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.utils.functionals.Event0;

public class Timer {

	public static class Task {
		private Event0 onComplete = new Event0();
		private Event0 onUpdate = new Event0();
		private Event0 onCanceled = new Event0();
		
		private long duration = -1;
		private long refreshRate = -1;
		
		private long timeUntilCompletion = -1;
		private long timeUntilNextRefresh = -1;
		
		private boolean isComplete;
		private boolean isPaused;
		private boolean isCanceled;
		private boolean refreshOnStart;
		
		private String sourceKey;
		
		public Task() { this.sourceKey = UUID.randomUUID().toString(); }
		public Task(String sourceKey) { this.sourceKey = sourceKey; }
		
		public Task skip(long ticks) {
			timeUntilCompletion -= ticks;
			timeUntilNextRefresh -= ticks;
			return this;
		}
		
		public Task skip(float seconds) {
			return skip((long)Math.round(seconds * 20));
		}
		
		public Task skip(int seconds) {
			return skip(seconds * 20l);
		}
		
		public Task setDuration(long ticks) {
			this.duration = ticks;
			this.timeUntilCompletion = this.duration;
			return this;
		}
		
		public Task setDuration(float seconds) {
			return setDuration((long)Math.round(seconds * 20));
		}
		
		public Task setDuration(int seconds) {
			return setDuration(seconds * 20l);
		}
		
		public Task setRefreshRate(long ticks) {
			this.refreshRate = ticks;
			this.timeUntilNextRefresh = this.refreshRate;
			return this;
		}
		
		public Task setRefreshRate(float seconds) {
			return setRefreshRate((long)Math.round(seconds * 20));
		}
		
		public Task setRefreshRate(int seconds) {
			return setRefreshRate(seconds * 20l);
		}
	
		public Task registerOnComplete(Runnable listener) {
			onComplete.subscribe(listener);
			return this;
		}
		
		public Task registerOnUpdate(Runnable listener) {
			onUpdate.subscribe(listener);
			return this;
		}
		
		public Task registerOnCanceled(Runnable listener) {
			onCanceled.subscribe(listener);
			return this;
		}
		
		public Task refreshOnStart() { 
			refreshOnStart = true;
			return this;
		}
		
		public Task unpause() {
			isPaused = false;
			return this;
		}
		
		public Task pause() {
			isPaused = true;
			return this;
		}
		
		public void cancel() {
			isCanceled = true;
			onCanceled.run();
		}
		
		public void restart() {
			timeUntilNextRefresh = refreshRate;
			timeUntilCompletion = duration;
			
			isPaused = false;
			isComplete = false;	
		}
		
		public boolean isComplete() { return isComplete; }
		public boolean isPaused() { return isPaused; }
		public float getPercentTimeLeft() { return timeUntilCompletion / (float) duration; }
		public float getPercentTimeCompleted() { return 1 - getPercentTimeLeft(); }
		public long getTicksLeft() { return timeUntilCompletion; }
		public int getWholeSecondsLeft() { return Math.round(timeUntilCompletion / 20f); }
		public float getSecondsLeft() { return timeUntilCompletion / 20f; }

		/**
		 * Called once per tick
		 */
		private void update() {
			if (isComplete || isCanceled || isPaused) return;

			if (refreshOnStart && duration == timeUntilCompletion)
				onUpdate.run();
			
			if (duration != -1) timeUntilCompletion--;
			if (refreshRate != -1) timeUntilNextRefresh--;
			
			if (refreshRate != -1 && timeUntilNextRefresh <= 0)
			{
				timeUntilNextRefresh = refreshRate;
				onUpdate.run();
			}
			
			if (duration != -1 && timeUntilCompletion <= 0) {
				isComplete = true;
				onComplete.run();
			}
		}
	}
	
	private static ArrayList<Task> tasks = new ArrayList<>();
	private static ArrayList<Task> toBeAdded = new ArrayList<>();
	
	public static void initialize() {
		new BukkitRunnable() {
			public void run() {
				//Separate the addition of tasks to avoid ConcurrentModificationException
				for (Task task : toBeAdded)
					tasks.add(task);
				toBeAdded.clear();
				
				Iterator<Task> taskIterator = tasks.iterator();
				while (taskIterator.hasNext()) {
					Task task = taskIterator.next();
					
					if (task.isCanceled)
						taskIterator.remove();
					else if (!task.isPaused && !task.isComplete)
						task.update();
				}
			}
		}.runTaskTimer(LudoriumPlugin.getPlugin(), 0, 1L);
	}
	
	public static void clearAllTasks() {
		for (Task task : tasks)
			task.cancel();
	}
	
	public static void cancelAllWithKey(String sourceKey) {
		for (Task task : tasks)
			if (task.sourceKey.equals(sourceKey))
				task.cancel();
	}
	
	/**
	 * Registers a new task to the timer
	 * @param task
	 */
	public static Task register(Task task) {
		toBeAdded.add(task);
		return task;
	}
}
