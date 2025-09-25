package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.cayve.ludorium.utils.Timer.Task;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ToolbarMessage {

	public static final float DEFAULT_MESSAGE_DURATION = 3f;
	public static class Message {
		
		public enum eType { MESSAGE, SUCCESS, WARNING, ERROR }
		
		private Function<Player, Component> message;
		private boolean isMuted;
		private boolean isPermanent;
		private boolean clearIfSkipped;
		private float duration = DEFAULT_MESSAGE_DURATION;
		private Task linkedTimer;
		private int priority = 0;
		private boolean refreshEveryTick;
		private boolean showDuration;
		private float showDurationDelay = 0;
		private eType type = eType.MESSAGE;
		private SourceKey sourceKey;
		
		/**
		 * Message object that will be sent to a viewer
		 * @param message Function to retrieve the message contents based on the viewer of the message
		 */
		public Message(Function<Player, Component> message) {
			this.message = message;
		}
		
		/**
		 * @param percentDelay Delay showing the duration until it is complete by this percentage.
		 * For example, if set to .25f, the progress bar will not appear until 25% of the duration has passed<p>
		 * The progress bar will not jump to 25%, for example, but rather it will represent the last 75% instead
		 */
		public Message showDuration(float percentDelay) { showDurationDelay = percentDelay; showDuration(); return this; }
		public Message showDuration() { showDuration = true; refreshEveryTick(); return this; }
		public Message setType(eType type) { this.type = type; return this; }
		public Message setMuted() { isMuted = true; return this; }
		public Message setPermanent() { isPermanent = true; return this; }
		public Message clearIfSkipped() { clearIfSkipped = true; return this; }
		public Message setDuration(float duration) { this.duration = duration; return this; }
		public Message setPriority(int priority) { this.priority = priority; return this; }
		public Message refreshEveryTick() { this.refreshEveryTick = true; return this; }
		public Message linkDurationToTimer(Task timer) { linkedTimer = timer; setDuration(timer.getSecondsLeft()); return this; }
		private Message setSource(SourceKey sourceKey) { this.sourceKey = sourceKey; return this; }
		
		public void updateMessage(Function<Player, Component> newMessage) { this.message = newMessage; }
		
		private ActiveMessage createActiveMessage() { return new ActiveMessage(this); }
	}
	
	//This acts as the actual message being displayed with dynamic duration
	//This allows for the same message to be reused
	private static class ActiveMessage {
		public Message template;
		
		private float duration;
		
		private boolean hasActivated = false;
		
		private boolean requestsImmediate;
		
		public ActiveMessage(Message template) {
			this.template = template;
			duration = template.duration;
		}
		
		public void update(float deltaTime) {
			if (!hasActivated)
				hasActivated = true;
			
			if (template.linkedTimer == null)
				duration += deltaTime;
			else
				duration = template.linkedTimer.getSecondsLeft();
		}
		
		/**
		 * Calculates the duration to display based (0-1) on
		 * how much time is left and the duration delay percentage
		 */
		public float getDurationDisplay() 
		{
			if (!hasActivated) return 1;
			
			return (duration) / ((1 - template.showDurationDelay) * template.duration); 
		}
		
		public boolean shouldDisplayDuration() {
			return template.showDuration && 1 - (duration / template.duration) >= template.showDurationDelay;
		}
	}
	
	private static HashMap<String, ArrayList<ActiveMessage>> messageQueues;
	private static boolean initialized = false;
	private static Task refreshTimer, sendTimer;
	
	public static void initialize() {
		if (initialized) return;
		initialized = true;
		
		messageQueues = new HashMap<String, ArrayList<ActiveMessage>>();
		
		refreshTimer = new Task().setRefreshRate(0).registerOnUpdate(() -> {
			organizeMap();
			
			for (String playerID : messageQueues.keySet()) {
				ActiveMessage targetMessage = messageQueues.get(playerID).get(0);
				
				//If the message needs to update every tick (timers)
				//If the message has not been sent yet
				if (targetMessage.template.refreshEveryTick || !targetMessage.hasActivated)
					sendToolbarMessage(playerID, targetMessage);
				
				//Updates all active messages
				targetMessage.update(-(1 / 20f));
				
				//If the message is not permanent and ran out of time, remove it
				if (!targetMessage.template.isPermanent && targetMessage.duration <= 0)
					messageQueues.get(playerID).remove(targetMessage);
			}
		});

		sendTimer = new Task().setRefreshRate(1).registerOnUpdate(ToolbarMessage::forceUpdate);
		
		Timer.register(refreshTimer);
		Timer.register(sendTimer);
	}
	
	private static void organizeMap() {
		Iterator<Entry<String, ArrayList<ActiveMessage>>> mapIterator = messageQueues.entrySet().iterator();
		
		while (mapIterator.hasNext()) {
			Entry<String, ArrayList<ActiveMessage>> entry = mapIterator.next();
			
			String playerID = entry.getKey();
			
			if (!messageQueues.get(playerID).isEmpty()) {
				//Retrieve the first message of the queue
				ActiveMessage targetMessage = entry.getValue().get(0);
				
				//Test if any other messages in the queue have a higher priority
				for (int i = 1; i < messageQueues.get(playerID).size(); i++) {
					ActiveMessage curr = messageQueues.get(playerID).get(i);
					
					if (hasPriorityOver(curr, targetMessage)) {
						if (targetMessage.template.clearIfSkipped && targetMessage.hasActivated)
						{
							messageQueues.get(playerID).remove(i);
							i--;
						}
						
						targetMessage = curr;
						messageQueues.get(playerID).remove(targetMessage);
						messageQueues.get(playerID).add(0, targetMessage);
					}
					//If a message fails to have higher priority and is clearIfSkipped
					//AND
					//Has been activated OR requests immediate
					else if (curr.template.clearIfSkipped && (curr.hasActivated || curr.requestsImmediate)) {
						messageQueues.get(playerID).remove(i);
						i--;
					}
				}
				
				//If target requested immediate, it no longer should request it
				targetMessage.requestsImmediate = false;
			}

			//Clear the map of the player if they have no more message
			if (messageQueues.get(playerID).isEmpty())
				mapIterator.remove();
		}
	}
	
	private static boolean hasPriorityOver(ActiveMessage toTest, ActiveMessage original) {
		//If message does NOT have a lower priority value
		//AND
		//If first message is permanent and there is another, highest duration wins (causes cycling effect)
		//If first message is permanent and there is a non-permanent, replace (causes permanent messages to come last in queue)
		
		//OR
		
		//If both messages are non-permanent
		//AND
		//If the message has a higher priority
		return (!(toTest.template.priority < original.template.priority) &&
				((toTest.template.isPermanent && original.template.isPermanent && toTest.duration > original.duration) ||
				(original.template.isPermanent && !toTest.template.isPermanent))) ||
				(!toTest.template.isPermanent && !original.template.isPermanent &&
				toTest.template.priority > original.template.priority);
	}
	
	/**
	 * Forces a message to update if its currently being displayed.
	 * Normal updates occur every ~1 second (unless refreshEveryTick() is enabled on the message).
	 * All instances of this message will update
	 * @param message The message to update
	 */
	public static void forceUpdate(Message message) {
		for (String playerID : messageQueues.keySet()) {
			if (messageQueues.get(playerID).get(0).template.equals(message))
				sendToolbarMessage(playerID, messageQueues.get(playerID).get(0));
		}
	}
	
	/**
	 * Forces all messages to be updated immediately.
	 */
	public static void forceUpdate() {
		organizeMap();
		
		for (String playerID : messageQueues.keySet()) {
			ActiveMessage targetMessage = messageQueues.get(playerID).get(0);
			
			//Re-send all activated messages (ignore constant refresh, other task handles those)
			if (targetMessage.hasActivated && !targetMessage.template.refreshEveryTick)
				sendToolbarMessage(playerID, targetMessage);
		}
	}
	
	/**
	 * Clears all messages with the given source key
	 * @param sourceKey The source key to compare
	 */
	public static void clearAllFromSource(SourceKey sourceKey) {
		clearIf((player, testMessage) -> testMessage.sourceKey != null && sourceKey.equals(testMessage.sourceKey));
	}
	
	/**
	 * Clears all messages for a player from a given source
	 * @param playerID The player to clear the messages for
	 * @param sourceKey The source key to clear
	 */
	public static void clearPlayerFromSource(String playerID, SourceKey sourceKey) {
		clearIf((testPlayer, testMessage) -> 
			testMessage.sourceKey != null && sourceKey.equals(testMessage.sourceKey) && 
			playerID.equals(testPlayer));
	}
	
	/**
	 * Clears a specific message. If the message was queued multiple times, it will clear all of them
	 * @param message The message to clear
	 */
	public static void clearMessage(Message message) {
		clearIf((player, testMessage) -> testMessage.equals(message));
	}
	
	/**
	 * Clears a message based on given predicate
	 * @param predicate The predicate to evaluate
	 */
	public static void clearIf(BiPredicate<String, Message> predicate) {
		boolean update = false;

		Iterator<String> playerIterator = messageQueues.keySet().iterator();
		while (playerIterator.hasNext()) {
			String playerID = playerIterator.next();
			//Go top down to avoid list removal issues (multiple of the same message can also exist)
			int messageIndex = messageQueues.get(playerID).size() - 1;
			while (messageIndex >= 0) {
				if (predicate.test(playerID, messageQueues.get(playerID).get(messageIndex).template))
				{
					messageQueues.get(playerID).remove(messageIndex);
					
					if (messageQueues.get(playerID).isEmpty())
						playerIterator.remove();
				}
				if (messageIndex == 0) //If the message is currently displayed, clear it
					update = true;
				messageIndex--;
			}
		}
		
		if (update)
			forceUpdate();
	}
	
	/**
	 * Clears all messages a player has
	 * @param playerID The player to clear
	 */
	public static void clearPlayer(String playerID) {
		if (messageQueues.containsKey(playerID))
			messageQueues.remove(playerID);
		
		sendToolbarMessage(playerID, null);
	}
	
	/**
	 * Quick way to clear all a player's messages from source and immediately send a new message. 
	 * Used if a source will only ever be sending one message at a time.
	 * Keep in mind other sources might still have messages associated with the player
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message Function to retrieve the message contents based on the viewer of the message
	 * @return self
	 */
	public static Message clearSourceAndSendImmediate(String playerID, SourceKey source, Function<Player, Component> message) {
		return clearSourceAndSendImmediate(playerID, source, new Message(message));
	}
	
	/**
	 * Quick way to clear all a player's messages from source and immediately send a new message. 
	 * Used if a source will only ever be sending one message at a time.
	 * Keep in mind other sources might still have messages associated with the player
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message clearSourceAndSendImmediate(String playerID, SourceKey source, Message message) {
		clearPlayerFromSource(playerID, source);
		
		return sendImmediate(playerID, source, message);
	}
	
	/**
	 * Simply adds to front of queue, if a higher priority exists it will not appear yet
	 * @param playerID The player to send the message to
	 * @param message Function to retrieve the message contents based on the viewer of the message
	 * @return self
	 */
	public static Message sendImmediate(String playerID, Function<Player, Component> message) {
		return sendImmediate(playerID, null, message);
	}
	
	/**
	 * Simply adds to front of queue, if a higher priority exists it will not appear yet
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message Function to retrieve the message contents based on the viewer of the message
	 * @return self
	 */
	public static Message sendImmediate(String playerID, SourceKey source, Function<Player, Component> message) {
		return sendImmediate(playerID, source, new Message(message));
	}
	
	/**
	 * Simply adds to front of queue, if a higher priority exists it will not appear yet
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message sendImmediate(String playerID, SourceKey source, Message message) {
		if (!messageQueues.containsKey(playerID))
			messageQueues.put(playerID, new ArrayList<ActiveMessage>());
		
		ActiveMessage messageObj = message.setSource(source).createActiveMessage();
		
		messageObj.requestsImmediate = true;
		
		messageQueues.get(playerID).add(0, messageObj);
		
		return messageObj.template;
	}
	
	/**
	 * Quick way to clear all a player's messages from source and queue a new message. 
	 * Used if a source will only ever be sending one message at a time.
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message Function to retrieve the message contents based on the viewer of the message
	 * @return self
	 */
	public static Message clearSourceAndSendQueue(String playerID, SourceKey source, Function<Player, Component> message) {
		return clearSourceAndSendQueue(playerID, source, new Message(message));
	}
	
	/**
	 * Quick way to clear all a player's messages from source and queue a new message. 
	 * Used if a source will only ever be sending one message at a time.
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message clearSourceAndSendQueue(String playerID, SourceKey source, Message message) {
		clearPlayerFromSource(playerID, source);
		
		return sendQueue(playerID, source, message);
	}
	
	/**
	 * Adds to back of queue, if has higher priority than others it will move further up
	 * @param playerID The player to send the message to
	 * @param message Function to retrieve the message contents based on the viewer of the message
	 * @return self
	 */
	public static Message sendQueue(String playerID, Function<Player, Component> message) {
		return sendQueue(playerID, null, message);
	}
	
	/**
	 * Adds to back of queue, if has higher priority than others it will move further up
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message Function to retrieve the message contents based on the viewer of the message
	 * @return self
	 */
	public static Message sendQueue(String playerID, SourceKey source, Function<Player, Component> message) {
		return sendQueue(playerID, source, new Message(message));
	}
	
	/**
	 * Adds to back of queue, if has higher priority than others it will move further up
	 * @param playerID The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message sendQueue(String playerID, SourceKey source, Message message) {
		if (!messageQueues.containsKey(playerID))
			messageQueues.put(playerID, new ArrayList<ActiveMessage>());
		
		messageQueues.get(playerID).add(message.setSource(source).createActiveMessage());
		
		return message;
	}
	
	private static void sendToolbarMessage(String playerID, ActiveMessage message) {
		Player player = Bukkit.getOfflinePlayer(UUID.fromString(playerID)).getPlayer();
		
		if (player == null || !player.isOnline()) return;
		
		Component messageComponent = message == null ? Component.empty() : message.template.message.apply(player);

		if (message != null) {
			if (message.shouldDisplayDuration())
				messageComponent = 
					Component.text(ProgressBar.newBuild().reverse().barCount(10).customCharacters('|', '.').generate(message.getDurationDisplay()))
					.append(Component.text(" "))
					.append(messageComponent)
					.append(Component.text(" "))
					.append(Component.text(ProgressBar.newBuild().barCount(10).customCharacters('|', '.').generate(message.getDurationDisplay())));
			
			NamedTextColor textColor = switch(message.template.type) {
				case Message.eType.ERROR -> NamedTextColor.RED;
				case Message.eType.WARNING -> NamedTextColor.GOLD;
				case Message.eType.SUCCESS -> NamedTextColor.DARK_GREEN;
				default -> NamedTextColor.WHITE;
			};
			
			messageComponent = messageComponent.color(textColor);
			
			if (!message.hasActivated && !message.template.isMuted) 
			{
				if (message.template.type == Message.eType.ERROR)
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 0.5f);
				else if (message.template.type == Message.eType.WARNING)
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 0.75f);
				else if (message.template.type == Message.eType.SUCCESS)
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 2);
				else
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 1);
			}
		
		}
		
		player.sendActionBar(messageComponent);
	}
}
