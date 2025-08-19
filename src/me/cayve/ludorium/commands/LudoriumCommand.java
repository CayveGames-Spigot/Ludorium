package me.cayve.ludorium.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.cayve.ludorium.games.boards.BoardList;
import me.cayve.ludorium.main.LudoriumPlugin;
import me.cayve.ludorium.ymls.TextYml;

public class LudoriumCommand {
	
	private static ArrayList<GameCommand> registeredGames;
	
	public static void registerGame(GameCommand gameCommand) {
		if (registeredGames == null)
			registeredGames = new ArrayList<GameCommand>();
		
		registeredGames.add(gameCommand);
	}
	
	public static LiteralCommandNode<CommandSourceStack> build() {
		LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.<CommandSourceStack>literal("ludorium")
			.then(LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
					.requires(sender -> sender.getSender().hasPermission("ludorium.admin") || sender.getSender().isOp())
					.executes(ctx -> {
						CommandSender sender = ctx.getSource().getSender();
						
						LudoriumPlugin.getPlugin().reloadPlugin();
						sender.sendMessage(TextYml.getText((sender instanceof Player ? (Player) sender : null),
								"commands.reload"));
						return 1;
					}))
			.then(LiteralArgumentBuilder.<CommandSourceStack>literal("leave")
					.executes(ctx -> {
						String playerID = ((Player)ctx.getSource().getSender()).getUniqueId().toString();
						BoardList.forEach(x -> {
							if (x.getLobby().hasPlayer(playerID))
								x.getLobby().attemptLobbyLeave(playerID);
						});
						
						return 1;
					}));
		
		for (GameCommand gameCommand : registeredGames)
			root.then(gameCommand.build());
		
		return root.build();
	}
}
