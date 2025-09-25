package me.cayve.ludorium.commands;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.cayve.ludorium.games.GameCreationWizard;
import me.cayve.ludorium.games.boards.BoardList;
import me.cayve.ludorium.games.boards.GameBoard;
import me.cayve.ludorium.ymls.TextYml;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class GameCommand {

	private String label;
	private Class<? extends GameBoard> type;
	private Supplier<GameCreationWizard> wizardFactory;
	
	/**
	 * Supplies the command information for command building
	 * @param label the label of the game for the sub-command
	 * @param type where the final instance will be sorted
	 * @param wizardFactory supplier for the wizard instance
	 */
	public GameCommand(String label, Class<? extends GameBoard> type, Supplier<GameCreationWizard> wizardFactory) {
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

		instanceName.executes(ctx -> {
			createWizard(ctx).activateWizard();
			return 1;
		});
		
		//If there are any arguments to be added (/ludorium ludo create Test manual)
		appendCreateArguments(instanceName);
		
		create.then(instanceName.build());
		
		create.requires(sender -> sender.getSender() instanceof Player);
		
		return create.build();
	}
	
	//Builds the create command arguments (in children)
	protected void appendCreateArguments(ArgumentBuilder<CommandSourceStack, ?> root) { }
		
	
	//Builds the game instance deletion command
	private LiteralCommandNode<CommandSourceStack> buildDelete() {
		return LiteralArgumentBuilder.<CommandSourceStack>literal("delete")
				.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("instance name", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (GameBoard board : BoardList.getInstanceListOfType(type))
								builder.suggest(board.getName());
							return builder.buildFuture();
						})
						.executes(ctx -> 
						{
							String instanceName = ctx.getArgument("instance name", String.class);
							CommandSender sender = ctx.getSource().getSender();
							
							if (BoardList.remove(instanceName, type))
								sender.sendMessage(TextYml.getText((sender instanceof Player ? (Player) sender : null),
										"commands.instanceDeleteSuccess",
										Placeholder.parsed("name", instanceName),
										Placeholder.parsed("game", label)));
							else //Failed to delete instance
								sender.sendMessage(TextYml.getText((sender instanceof Player ? (Player) sender : null),
										"commands.instanceDeleteFailure",
										Placeholder.parsed("name", instanceName),
										Placeholder.parsed("game", label)));
							return 1;
						}).build())
					.build();
	}
	
	//Builds the game instance list command
	private LiteralCommandNode<CommandSourceStack> buildList() {
		return LiteralArgumentBuilder.<CommandSourceStack>literal("list").executes(ctx -> 
		{ 
			CommandSender sender = ctx.getSource().getSender();
			
			ArrayList<String> nameList = new ArrayList<>();
			BoardList.getInstanceListOfType(type).forEach(x -> nameList.add(x.getName()));
			
			sender.sendMessage(TextYml.getText((sender instanceof Player ? (Player) sender : null),
					nameList.size() == 0 ? "commands.noGameInstances" : "commands.instanceListHeader",
							Placeholder.parsed("game", label)));
			for (String board : nameList)
				sender.sendMessage(board);
			
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
