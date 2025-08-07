package me.cayve.ludorium.games.ludo;

import java.util.ArrayList;

import org.bukkit.Location;

import me.cayve.ludorium.actions.SelectBlocksAction;
import me.cayve.ludorium.actions.SelectRegionAction;
import me.cayve.ludorium.games.boards.BoardList;
import me.cayve.ludorium.games.wizards.GameCreationWizard;
import me.cayve.ludorium.utils.StateMachine;
import me.cayve.ludorium.utils.ToolbarMessage;
import me.cayve.ludorium.utils.locational.Region;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class LudoCreationWizard extends GameCreationWizard {

	private class ConstructionMap {
		private ArrayList<Location> 			tiles = new ArrayList<>();
		private ArrayList<ArrayList<Location>> 	homes = new ArrayList<>();
		private ArrayList<Region> 				starters = new ArrayList<>();
		private ArrayList<Location>				safeSpaces = new ArrayList<>();
		
		public void setTiles(ArrayList<Location> tiles) { this.tiles = tiles; }
		
		public void addHomeSet(ArrayList<Location> homeSet) { this.homes.add(homeSet); }
		public void removeLastHomeSet() { homes.removeLast(); }
		
		public void addStarterRegion(Region region) { this.starters.add(region); }
		public void removeLastStarterRegion() { starters.removeLast(); }
		
		public void setSafeSpaces(ArrayList<Location> safeSpaces) { this.safeSpaces = safeSpaces; }
		
		public LudoMap constructMap() {
			return new LudoMap(tiles, homes, starters, safeSpaces);
		}
	}
	//Manual mode will force the wizard to select every board space 
	//Otherwise, the board will attempt to find the spaces automatically
	private boolean isSixPlayer;
	
	private ConstructionMap map = new ConstructionMap();
	
	public LudoCreationWizard() {
		super();
		
		createStates();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		stateMachine.complete();
	}
	
	public LudoCreationWizard setSixPlayer() {
		if (!stateMachine.hasStarted())
			this.isSixPlayer = true;
		
		return this;
	}
	
	private void promptColorOrderVerification() {
		Component colors = Component.empty();
		
		//Replaces color with the ordered RED->YELLOW->GREEN->BLUE and adds the last two if six players
		for (int i = (isSixPlayer ? 6 : 4) - 1; i >= 0; i++)
		{
			colors.append(Component.text("■").color(TextYml.getText(player, "words.colors." + LudoBoard.COLOR_ORDER[i]).color()));
			
			if (i != 0)
				colors.append(Component.text("->"));
		}

		ToolbarMessage.sendQueue(player, tsk, TextYml.getText(player, "wizards.ludo.colorVerification", TextYml.tag("colors", colors)))
			.setDuration(12).showDuration().clearIfSkipped();
	}
	
	private void createStates() {
		stateMachine = new StateMachine()
			/*
			 * Select tiles
			 */
			.newState()
				.registerProgress(this::promptColorOrderVerification)
				.registerAction(() -> { //Started manual mode
					ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText(player, "wizards.ludo.selectTiles",
							Placeholder.parsed("color", 
									((TextComponent)TextYml.getText(player, "words.colors." + LudoBoard.COLOR_ORDER[0])).content().toUpperCase())))
					.setPermanent();
					
					setNewAction(new SelectBlocksAction(player, -1, false, this::onCompletedAction, this::onCanceledAction));
				})
				.registerComplete(() -> {
					map.setTiles(((SelectBlocksAction) currentAction).getLocations());
				}).buildState()
				
			/*
			 * Select homes
			 */
			.newState("HOME SELECTION")
				.registerRegress(() -> {
					map.removeLastHomeSet();
				})
				.registerAction(() -> {
					ToolbarMessage.clearSourceAndSend(player, stateTsk, TextYml.getText(player, "wizards.ludo.selectHome",
							Placeholder.parsed("color", 
									((TextComponent)TextYml.getText(player, "words.colors." 
												+ LudoBoard.COLOR_ORDER[stateMachine.contextualIndex("homeColor")])).content().toUpperCase())))
							.setPermanent();
					 
					setNewAction(new SelectBlocksAction(player, 4, false, this::onCompletedAction, this::onCanceledAction));
				})
				.registerComplete(() -> {
					ToolbarMessage.clearAllFromSource(stateTsk);
					
					map.addHomeSet(((SelectBlocksAction) currentAction).getLocations());
				}).buildState()
				
			.copyState("HOME SELECTION").buildState()
			.copyState("HOME SELECTION").buildState()
			.copyState("HOME SELECTION").buildState();
		
		if (isSixPlayer)
			stateMachine
				.copyState("HOME SELECTION").buildState()
				.copyState("HOME SELECTION").buildState();
		
			/*
			 * Select starter region
			 */
			stateMachine.newState("STARTER REGION")
				.registerRegress(() -> {
					map.removeLastStarterRegion();
				})
				.registerAction(() -> {
					setNewAction(new SelectRegionAction(player, 
							TextYml.getText(player, "wizards.ludo.selectStarter",
								Placeholder.parsed("color", ((TextComponent)TextYml.getText(player, "words.colors." 
										+ LudoBoard.COLOR_ORDER[stateMachine.contextualIndex("starterColor")])).content().toUpperCase())),
							this::onCompletedAction, this:: onCanceledAction));
				})
				.registerComplete(() -> {
					map.addStarterRegion(((SelectRegionAction) currentAction).getRegion());
				}).buildState()
				
			.copyState("STARTER REGION").buildState()
			.copyState("STARTER REGION").buildState()
			.copyState("STARTER REGION").buildState();
		
		if (isSixPlayer)
			stateMachine
				.copyState("STARTER REGION").buildState()
				.copyState("STARTER REGION").buildState();

			stateMachine.newState()
					.registerAction(this::createGame).buildState();
	}
	
	private void createGame() {
		
		BoardList.add(new LudoBoard(instanceName, map.constructMap(), map.tiles.getFirst()));
		
		completeWizard();
	}
}
