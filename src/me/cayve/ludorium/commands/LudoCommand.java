package me.cayve.ludorium.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.cayve.ludorium.games.boards.LudoBoard;
import me.cayve.ludorium.games.wizards.LudoCreationWizard;

public class LudoCommand extends GameCommand {

	public LudoCommand() {
		super("ludo", LudoBoard.class, LudoCreationWizard::new);
	}
	
	//Allows for any child argument to be suggested
	@Override
	protected void appendCreateArguments(ArgumentBuilder<CommandSourceStack, ?> root) 
	{ 
		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("manual")
				.executes(ctx -> {
					((LudoCreationWizard)createWizard(ctx)).setManualMode().activateWizard();
					return 1;
				}));
		
		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("6-player")
				.executes(ctx -> {
					((LudoCreationWizard)createWizard(ctx)).setSixPlayer().setManualMode().activateWizard();
					return 1;
				}));
	}
}
