package me.cayve.ludorium.games.ludo;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.cayve.ludorium.commands.GameCommand;

public class LudoCommand extends GameCommand {

	public LudoCommand() {
		super("ludo", LudoBoard.class, LudoCreationWizard::new);
	}
	
	//Allows for any child argument to be suggested
	@Override
	protected void appendCreateArguments(ArgumentBuilder<CommandSourceStack, ?> root) 
	{ 		
		root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("6-player")
				.executes(ctx -> {
					((LudoCreationWizard)createWizard(ctx)).setSixPlayer().activateWizard();
					return 1;
				}));
	}
}
