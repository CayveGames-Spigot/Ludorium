package me.cayve.ludorium.commands;

import java.util.function.Supplier;

import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.cayve.ludorium.games.GameBase;
import me.cayve.ludorium.games.wizards.GameCreationWizard;
import me.cayve.ludorium.ymls.TextYml;

public class GameCommand {

	private String label;
	private Class<? extends GameBase> type;
	private Supplier<GameCreationWizard> wizardFactory;
	
	/**
	 * Supplies the command information for command building
	 * @param label the label of the game for the sub-command
	 * @param type where the final instance will be sorted
	 * @param wizardFactory supplier for the wizard instance
	 */
	public GameCommand(String label, Class<? extends GameBase> type, Supplier<GameCreationWizard> wizardFactory) {
		this.label = label;
		this.type = type;
		this.wizardFactory = wizardFactory;
	}
	
	/**
	 * Builds the game command
	 */
	public LiteralCommandNode<CommandSourceStack> build() {
		LiteralArgumentBuilder<CommandSourceStack> subcommand = LiteralArgumentBuilder.literal(label);
		
		subcommand.then(buildCreate());
		subcommand.then(buildDelete());
		subcommand.then(buildList());
		
		appendOthers(subcommand);
		
		subcommand.requires(sender -> sender.getSender().hasPermission("ludorium.admin") || sender.getSender().isOp());
		
		return subcommand.build();
	}
	
	//Builds the game instance create command
	private LiteralCommandNode<CommandSourceStack> buildCreate() {
		LiteralArgumentBuilder<CommandSourceStack> create = LiteralArgumentBuilder.literal("create");
		
		ArgumentBuilder<CommandSourceStack, ?> instanceName = RequiredArgumentBuilder.argument("instance name", StringArgumentType.word());

		//If there are any arguments to be added (/ludorium ludo create Test manual)
		appendCreateArguments(instanceName);
		
		create.then(instanceName.build());
		
		create.requires(sender -> sender.getSender() instanceof Player && !GameCreationWizard.isInWizard((Player) sender.getSender()));
		create.executes(ctx -> {
			createWizard(ctx).activateWizard();
			return 1;
		});
		
		return create.build();
	}
	
	//Builds the create command arguments (in children)
	protected void appendCreateArguments(ArgumentBuilder<CommandSourceStack, ?> root) { }
		
	
	//Builds the game instance deletion command
	private LiteralCommandNode<CommandSourceStack> buildDelete() {
		return LiteralArgumentBuilder.<CommandSourceStack>literal("delete")
				.then(RequiredArgumentBuilder.argument("instance name", StringArgumentType.word()))
				.executes(ctx -> 
				{
					String instanceName = ctx.getArgument("instance name", String.class);
					
					if (GameBase.deleteGameInstance(instanceName, type))
						ctx.getSource().getSender().sendMessage(TextYml.getText("commands.instanceDeleteSuccess")
								.replace("<name>", instanceName).replace("<game>", label));
					else //Failed to delete instance
						ctx.getSource().getSender().sendMessage(TextYml.getText("commands.instanceDeleteFailure")
								.replace("<name>", instanceName).replace("<game>", label));
					return 1;
				}).build();
	}
	
	//Builds the game instance list command
	private LiteralCommandNode<CommandSourceStack> buildList() {
		return LiteralArgumentBuilder.<CommandSourceStack>literal("list").executes(ctx -> 
		{ 
			ctx.getSource().getSender().sendMessage(TextYml.getText(
					GameBase.getInstanceList(type).size() == 0 ? "commands.noGameInstances" : "commands.instanceListHeader")
					.replace("<game>", label));
			return 1;
		}).build();
	}
	
	/**
	 * Allows for any other subcommand to be added to the game command
	 * @param command the root command
	 */
	protected void appendOthers(ArgumentBuilder<CommandSourceStack, ?> root) { }
	
	//Creates a new instances of the wizard using the factory based on a command context
	protected GameCreationWizard createWizard(CommandContext<CommandSourceStack> ctx) {
		return wizardFactory.get().apply(ctx.getArgument("instance name", String.class), (Player) ctx.getSource().getSender());
	}
}
