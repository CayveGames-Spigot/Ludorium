package me.cayve.ludorium.games.lobbies;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.cayve.ludorium.utils.entities.DisplayEntity;
import me.cayve.ludorium.utils.entities.ItemEntity;
import me.cayve.ludorium.utils.locational.Vector2D;

public class InteractionLobby extends GameLobby {

	public static class Token {
		public Location location;
		public ItemStack model;
		public Vector2D interactionBounds;
		
		public Token(Location location, ItemStack model, Vector2D interactionBounds) {
			this.location = location;
			this.model = model;
			this.interactionBounds = interactionBounds;
		}
	}
	
	private ArrayList<DisplayEntity<ItemDisplay>> displays = new ArrayList<>();
	
	public InteractionLobby(int minimum, int maximum, Token globalToken) {
		super(minimum, maximum);
		
		addDisplay(globalToken, -1);
		
		registerEvents();
	}
	
	public InteractionLobby(int minimum, int maximum, ArrayList<Token> tokens) {
		super(minimum, maximum);
		
		for (int i = 0; i < tokens.size(); i++)
			addDisplay(tokens.get(i), i);
		
		registerEvents();
	}
	
	private void registerEvents() {
		registerJoinListener((index) -> {displays.get(index).remove();});
		
		registerLeaveListener((index) -> {
			if (isEnabled())
				displays.get(index).spawn();
		});
	}
	
	@Override
	public void enable() {
		super.enable();
		
		for (DisplayEntity<?> display : displays)
			display.spawn();
	}
	
	@Override
	public void disable() {
		super.disable();
		
		for (DisplayEntity<?> display : displays)
			display.remove();
	}
	
	private void addDisplay(Token token, int lobbyPosition) {
		DisplayEntity<ItemDisplay> display = new ItemEntity(token.location, token.model);
		display.setInteraction(token.interactionBounds);
		display.registerOnInteractedWith((player) -> { onInteraction(player, lobbyPosition); });
		
		displays.add(display);
	}
	
	private void onInteraction(Player player, int index) {
		if (index == -1)
			attemptLobbyJoin(player.getUniqueId().toString());
		else
			attemptLobbyJoin(player.getUniqueId().toString(), index);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		for (DisplayEntity<?> display : displays)
			display.destroy();
	}
}
