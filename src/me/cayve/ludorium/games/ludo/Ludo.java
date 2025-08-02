package me.cayve.ludorium.games.ludo;

import me.cayve.ludorium.commands.LudoriumCommand;
import me.cayve.ludorium.games.Game;
import me.cayve.ludorium.games.GameDeclaration;
import me.cayve.ludorium.games.boards.BoardList;

@GameDeclaration(prefix = "ludo")
public class Ludo implements Game {

	@Override
	public void initialize() {
		LudoriumCommand.registerGame(new LudoCommand());
	}

	@Override
	public void save() {
		LudoYml.saveBoards(BoardList.getInstanceList(LudoBoard.class));
	}

	@Override
	public void load() {
		BoardList.destroyAllOfType(LudoBoard.class);
		
		LudoYml.loadBoards();
	}
}
