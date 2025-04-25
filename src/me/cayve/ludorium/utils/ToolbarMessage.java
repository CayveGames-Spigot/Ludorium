package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.cayve.ludorium.utils.Timer.Task;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

public class ToolbarMessage {

	public static class Message {
		
		public enum eType { MESSAGE, SUCCESS, WARNING, ERROR }
		
		private String message;
		private boolean isMuted;
		private boolean isPermanent;
		private boolean clearIfSkipped;
		private float duration = 3;
		private int priority = 0;
		private boolean refreshEveryTick;
		private boolean showDuration;
		private eType type;
		private UUID sourceKey;
		
		public Message(String message, UUID sourceKey) {
			this.message = message;
			this.sourceKey = sourceKey;
		}
		
		public Message setType(eType type) { this.type = type; return this; }
		public Message setMuted() { isMuted = true; return this; }
		public Message setPermanent() { isPermanent = true; return this; }
		public Message showDuration() { showDuration = true; return this; }
		public Message clearIfSkipped() { clearIfSkipped = true; return this; }
		public Message setDuration(float duration) { this.duration = duration; return this; }
		public Message setPriority(int priority) { this.priority = priority; return this; }
		public Message refreshEveryTick() { this.refreshEveryTick = true; return this; }
		
		public String getMessage() { return message; }
		public void updateMessage(String newMessage) { this.message = newMessage; }
		
		private ActiveMessage createActiveMessage() { return new ActiveMessage(this); }
	}
	
	//This acts as the actual message being displayed with dynamic duration
	//This allows for the same message to be reused
	private static class ActiveMessage {
		public Message template;
		
		private float duration;
		
		private boolean hasActivated = false;
		
		public ActiveMessage(Message template) {
			this.template = template;
		}
		
		public void update(float deltaTime) {
			if (!hasActivated) {
				hasActivated = true;
				
				//Duration needs to be applied here to allow for chaining: ".sendQueue().setDuration()"
				//Since sendImmediate and sendQueue create this object immediately
				duration = template.duration; 
			}
			
			duration += deltaTime;
		}
		
		/**
		 * Calculates the elapsed percentage time
		 * 8 seconds left on a 10 second display = 20%
		 */
		public float getElapsedPercent() 
		{
			if (!hasActivated) return 0;
			
			return 1 - (duration / template.duration); 
		}
	}
	
	private static HashMap<Player, ArrayList<ActiveMessage>> messageQueues;
	private static boolean initialized = false;
	private static Task refreshTimer, sendTimer;
	
	public static void initialize() {
		if (initialized) return;
		initialized = true;
		
		messageQueues = new HashMap<Player, ArrayList<ActiveMessage>>();
		
		refreshTimer = new Task().setRefreshRate(0).registerOnUpdate(() -> {
			organizeMap();
			
			for (Player player : messageQueues.keySet()) {
				ActiveMessage targetMessage = messageQueues.get(player).get(0);
				
				//If the message needs to update every tick (timers)
				//If the message has not been sent yet
				if (targetMessage.template.refreshEveryTick || !targetMessage.hasActivated)
					sendToolbarMessage(player, targetMessage);
				
				//Updates all active messages
				targetMessage.update(-(1 / 20f));
				
				//If the message is not permanent and ran out of time, remove it
				if (!targetMessage.template.isPermanent && targetMessage.duration <= 0)
					messageQueues.get(player).remove(targetMessage);
			}
		});

		sendTimer = new Task().setRefreshRate(1).registerOnUpdate(ToolbarMessage::forceUpdate);
		
		Timer.register(refreshTimer);
		Timer.register(sendTimer);
	}
	
	private static void organizeMap() {
		Iterator<Entry<Player, ArrayList<ActiveMessage>>> mapIterator = messageQueues.entrySet().iterator();
		
		while (mapIterator.hasNext()) {
			Entry<Player, ArrayList<ActiveMessage>> entry = mapIterator.next();
			
			Player player = entry.getKey();
			
			if (!messageQueues.get(player).isEmpty()) {
				ActiveMessage targetMessage = entry.getValue().get(0);
				
				for (int i = 0; i < messageQueues.get(player).size(); i++) {
					ActiveMessage curr = messageQueues.get(player).get(i);
					
					//If message does NOT have a lower priority value
					//AND
					//If first message is permanent and there is another, highest duration wins (causes cycling effect)
					//If first message is permanent and there is a non-permanent, replace (causes permanent messages to come last in queue)
					if (!(curr.template.priority < targetMessage.template.priority) &&
							((curr.template.isPermanent && targetMessage.template.isPermanent && curr.duration > targetMessage.duration) ||
							(targetMessage.template.isPermanent && !curr.template.isPermanent))) {
						{
							if (targetMessage.template.clearIfSkipped)
							{
								messageQueues.get(player).remove(i);
								i--;
							}
							
							targetMessage = curr;
							messageQueues.get(player).remove(targetMessage);
							messageQueues.get(player).add(0, targetMessage);
						}
					}
				}
			}

			//Clear the map of the player if they have no more message
			if (messageQueues.get(player).isEmpty())
				mapIterator.remove();
		}
	}
	
	/**
	 * Forces a message to update if its currently being displayed.
	 * Normal updates occur every ~1 second (unless refreshEveryTick() is enabled on the message).
	 * All instances of this message will update
	 * @param message The message to update
	 */
	public static void forceUpdate(Message message) {
		for (Player player : messageQueues.keySet()) {
			if (messageQueues.get(player).get(0).template.equals(message))
				sendToolbarMessage(player, messageQueues.get(player).get(0));
		}
	}
	
	/**
	 * Forces all messages to be updated immediately.
	 */
	public static void forceUpdate() {
		organizeMap();
		
		for (Player player : messageQueues.keySet()) {
			ActiveMessage targetMessage = messageQueues.get(player).get(0);
			
			//Re-send all activated messages (ignore constant refresh, other task handles those)
			if (targetMessage.hasActivated && !targetMessage.template.refreshEveryTick)
				sendToolbarMessage(player, targetMessage);
		}
	}
	
	/**
	 * Clears all messages with the given source key
	 * @param sourceKey The source key to compare
	 */
	public static void clearAllFromSource(UUID sourceKey) {
		clearIf(testMessage -> testMessage.sourceKey != null && testMessage.sourceKey.equals(sourceKey));
	}
	
	/**
	 * Clears a specific message. If the message was queued multiple times, it will clear all of them
	 * @param message The message to clear
	 */
	public static void clearMessage(Message message) {
		clearIf(testMessage -> testMessage.equals(message));
	}
	
	/**
	 * Clears a message based on given predicate
	 * @param predicate The predicate to evaluate
	 */
	public static void clearIf(Predicate<Message> predicate) {
		boolean update = false;
		for (Player player : messageQueues.keySet()) {
			//Go top down to avoid list removal issues (multiple of the same message can also exist)
			int messageIndex = messageQueues.get(player).size() - 1;
			while (messageIndex >= 0) {
				if (predicate.test(messageQueues.get(player).get(messageIndex).template))
				{
					messageQueues.get(player).remove(messageIndex);
					
					if (messageQueues.get(player).isEmpty())
						messageQueues.remove(player);
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
	 * @param player The player to clear
	 */
	public static void clearPlayer(Player player) {
		if (messageQueues.containsKey(player))
			messageQueues.remove(player);
		
		sendToolbarMessage(player, null);
	}
	
	/**
	 * Quick way to clear all from source and queue a new message. 
	 * Used if a source will only ever be sending one message at a time.
	 * @param player The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message clearSourceAndSend(Player player, UUID source, String message) {
		clearAllFromSource(source);
		
		return sendQueue(player, source, message);
	}
	
	/**
	 * Quick way to clear all from source and immediately send a new message. 
	 * Used if a source will only ever be sending one message at a time.
	 * Keep in mind other sources might still have messages associated with the player
	 * @param player The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message clearSourceAndSendImmediate(Player player, UUID source, String message) {
		clearAllFromSource(source);
		
		return sendImmediate(player, source, message);
	}
	
	/**
	 * Simply adds to front of queue, if a higher priority exists it will not appear yet
	 * @param player The player to send the message to
	 * @param message The message to send
	 * @return self
	 */
	public static Message sendImmediate(Player player, String message) {
		return sendImmediate(player, null, message);
	}
	
	/**
	 * Adds to back of queue, if has higher priority than others it will move further up
	 * @param player The player to send the message to
	 * @param message The message to send
	 * @return self
	 */
	public static Message sendQueue(Player player, String message) {
		return sendQueue(player, null, message);
	}
	
	/**
	 * Simply adds to front of queue, if a higher priority exists it will not appear yet
	 * @param player The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message sendImmediate(Player player, UUID source, String message) {
		if (!messageQueues.containsKey(player))
			messageQueues.put(player, new ArrayList<ActiveMessage>());
		
		ActiveMessage messageObj = new Message(message, source).createActiveMessage();
		
		//If the active message is clearIfSkipped, skip it
		if (messageQueues.get(player).size() != 0 && messageQueues.get(player).get(0).template.clearIfSkipped)
			messageQueues.get(player).remove(0);
		
		messageQueues.get(player).add(0, messageObj);
		
		return messageObj.template;
	}
	
	/**
	 * Adds to back of queue, if has higher priority than others it will move further up
	 * @param player The player to send the message to
	 * @param source The source the message came from
	 * @param message The message to send
	 * @return self
	 */
	public static Message sendQueue(Player player, UUID source, String message) {
		if (!messageQueues.containsKey(player))
			return sendImmediate(player, source, message);
		
		Message messageObj = new Message(message, source);
		messageQueues.get(player).add(messageObj.createActiveMessage());
		
		return messageObj;
	}
	
	private static void sendToolbarMessage(Player player, ActiveMessage message) {
		if (player == null || !player.isOnline()) return;
		
		String rawMessage = message == null ? "" : message.template.message;
		
		if (message != null)
		{
			rawMessage = (message.template.type == Message.eType.ERROR ? ChatColor.RED :
						message.template.type == Message.eType.WARNING ? ChatColor.GOLD :
						message.template.type == Message.eType.SUCCESS ? ChatColor.DARK_GREEN : 
						ChatColor.WHITE) + rawMessage + 
						(message.template.showDuration ? " " + ProgressBar.generate(message.getElapsedPercent()) : "");
		}
		
		if (message != null && !message.hasActivated && !message.template.isMuted) 
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
		
		player.sendActionBar(Component.text(rawMessage));
	}
}
