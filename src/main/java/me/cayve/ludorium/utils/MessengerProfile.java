package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import me.cayve.ludorium.games.utils.PlayerProfileManager.ProfileComponent;
import me.cayve.ludorium.utils.ToolbarMessage.Message;
import me.cayve.ludorium.utils.events.Event.Subscriber;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.interfaces.Destroyable;

public class MessengerProfile implements Destroyable, ProfileComponent {

	private final SourceKey profileSourceKey;
	
	private final Event0 onDestroyEvent = new Event0();
	
	public MessengerProfile() { 
		this.profileSourceKey = new SourceKey(); 
		this.channels.put(AUDIENCE_ID, new ArrayList<>());
	}
	public MessengerProfile(SourceKey key) { 
		this.profileSourceKey = key; 
		this.channels.put(AUDIENCE_ID, new ArrayList<>());
	}
	
	private static final String AUDIENCE_ID = UUID.randomUUID().toString();
	
	//Channel ID / Listener ID list
	private final Map<String, List<String>> channels = new HashMap<String, List<String>>();
	private String[] channelIndexes; //Keeps track of the ordered indexes of the supplied channels
	
	public void setChannels(String[] channels) {
		this.channelIndexes = channels;
		
		for (String channel : channels)
			this.channels.put(channel, new ArrayList<>());
	}
	
	/**
	 * Subscribes to this messenger profile. If the listener has a unique channel, that channel will be subscribed to directly. Otherwise,
	 * the audience channel will be used instead.
	 * @param listenerID The ID to subscribe as
	 */
	public void subscribe(String listenerID) {
		subscribe(listenerID, listenerID);
	}
	
	/**
	 * Subscribes to this messenger profile to a specific channel index
	 * @param listenerID The ID to subscribe as
	 * @param channelIndex The channel to subscribe to
	 */
	public void subscribe(String listenerID, int channelIndex) {
		subscribe(listenerID, channelIndexes[channelIndex]);
	}
	
	/**
	 * Subscribes to this messenger profile to a specific channel ID
	 * @param listenerID The ID to subscribe as
	 * @param channelID The channel to subscribe to (if that channel does not exist, the audience channel will be used instead)
	 */
	public void subscribe(String listenerID, String channelID) {
		unsubscribe(listenerID);
		
		if (!channels.containsKey(channelID))
			channelID = AUDIENCE_ID;
		
		channels.get(channelID).add(listenerID);
	}
	
	/**
	 * Unsubscribes a listener from this profile
	 * @param listenerID The ID to unsubscribe as
	 */
	public void unsubscribe(String listenerID) {
		for (String channel : channels.keySet())
			channels.get(channel).remove(listenerID);
	}
	
	/**
	 * Sends a message to all channels
	 * @param toolbarFunction The ToolbarMessage function to call 
	 * @see ToolbarMessage
	 * @param message The message to send
	 */
	public void sendAll(TriFunction<String, SourceKey, Message, Message> toolbarFunction, Message message) {
		sendAll(null, toolbarFunction, message);
	}
	
	/**
	 * Sends a message to all channels
	 * @param sourceContext A context to apply to the message's source key
	 * @param toolbarFunction The ToolbarMessage function to call 
	 * @see ToolbarMessage
	 * @param message The message to send
	 */
	public void sendAll(String sourceContext, TriFunction<String, SourceKey, Message, Message> toolbarFunction, Message message) {
		for (String channel : channels.keySet())
			sendTo(channel, sourceContext, toolbarFunction, message);
	}
	
	/**
	 * Sends a message to ONLY the audience channel
	 * @param toolbarFunction The ToolbarMessage function to call 
	 * @see ToolbarMessage
	 * @param message The message to send
	 */
	public void sendAudience(TriFunction<String, SourceKey, Message, Message> toolbarFunction, Message message) {
		sendTo(AUDIENCE_ID, null, toolbarFunction, message);
	}
	
	/**
	 * Sends a message to ONLY the audience channel
	 * @param sourceContext A context to apply to the message's source key
	 * @param toolbarFunction The ToolbarMessage function to call 
	 * @see ToolbarMessage
	 * @param message The message to send
	 */
	public void sendAudience(String sourceContext, TriFunction<String, SourceKey, Message, Message> toolbarFunction, Message message) {
		sendTo(AUDIENCE_ID, sourceContext, toolbarFunction, message);
	}
	
	/**
	 * Sends a message to a specific channel index
	 * @param channelIndex The index of the channel to send to
	 * @param toolbarFunction The ToolbarMessage function to call 
	 * @see ToolbarMessage
	 * @param message The message to send
	 */
	public void sendTo(int channelIndex, TriFunction<String, SourceKey, Message, Message> toolbarFunction, Message message) {
		sendTo(channelIndexes[channelIndex], null, toolbarFunction, message);
	}
	
	/**
	 * Sends a message to a specific channel index
	 * @param channelIndex The index of the channel to send to
	 * @param sourceContext A context to apply to the message's source key
	 * @param toolbarFunction The ToolbarMessage function to call 
	 * @see ToolbarMessage
	 * @param message The message to send
	 */
	public void sendTo(int channelIndex, String sourceContext, TriFunction<String, SourceKey, Message, Message> toolbarFunction, Message message) {
		sendTo(channelIndexes[channelIndex], sourceContext, toolbarFunction, message);
	}
	
	/**
	 * Sends a message to a specific channel ID
	 * @param channelID The ID of the channel to send to
	 * @param sourceContext A context to apply to the message's source key
	 * @param toolbarFunction The ToolbarMessage function to call 
	 * @see ToolbarMessage
	 * @param message The message to send
	 */
	public void sendTo(String channelID, String sourceContext, TriFunction<String, SourceKey, Message, Message> toolbarFunction, Message message) {
		for (String listener : channels.get(channelID))
			toolbarFunction.apply(listener, profileSourceKey.withContext(sourceContext), message);
	}
	
	/**
	 * Clears all messages sourced from this profile for anyone listening
	 */
	public void clear() {
		ToolbarMessage.clearAllFromSource(profileSourceKey);
	}
	
	/**
	 * Clears all messages sourced from this profile, with context, for anyone listening
	 * @param context
	 */
	public void clearContext(String context) {
		ToolbarMessage.clearAllFromSource(profileSourceKey.withContext(context));
	}

	@Override
	public void destroy() {
		clear();
		
		onDestroyEvent.run();
	}

	@Override
	public Subscriber<Runnable> onDestroyed() { return onDestroyEvent.getSubscriber(); }
}
