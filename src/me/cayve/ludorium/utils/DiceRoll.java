package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class DiceRoll {

	/**
	 * Gives the player dice to roll
	 * @param player The player to roll
	 * @param gameKey The game's key for the player state manager inventory management
	 * @param diceCount The amount of dice to roll
	 * @param callback Callback containing array with roll results (with length of diceCount)
	 */
	public void playerRoll(String playerID, String gameKey, int diceCount, Consumer<ArrayList<Integer>> callback) {
		ArrayList<Integer> rolls = new ArrayList<>();
		
		for (int i = 0; i < diceCount; i++)
			rolls.add(new Random().nextInt(6));
		
		callback.accept(rolls);
	}
	
	public void forceRoll() {
		
	}
	public void destroy() {
		
	}
}
