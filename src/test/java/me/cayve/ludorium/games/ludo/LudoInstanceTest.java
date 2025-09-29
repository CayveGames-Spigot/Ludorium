package me.cayve.ludorium.games.ludo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.util.Vector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import me.cayve.ludorium.games.events.GameFinishEvent;
import me.cayve.ludorium.games.events.InstanceEvent;
import me.cayve.ludorium.games.events.TokenMoveEvent;
import me.cayve.ludorium.utils.ArrayListUtils;

/**
 * Unit Testing for the LudoInstance functionality
 */
public class LudoInstanceTest {

	/**
	 * Creates a default (8-tile) LudoMap with no safe spaces
	 */
	private LudoMap createDefaultMap() {
		return createMapWithSafeSpaces(new Integer[0]);
	}
	
	/**
	 * Creates a default LudoMap with custom safe spaces
	 */
	private LudoMap createMapWithSafeSpaces(Integer[] safeSpaces) {
		return new LudoMap(new Vector[] {
			    new Vector(0.0, 0.0, 0.0),
			    new Vector(0.0, 0.0, -4.0),
			    new Vector(-4.0, 0.0, -6.0),
			    new Vector(0.0, 0.0, -6.0),
			    new Vector(2.0, 0.0, -10.0),
			    new Vector(2.0, 0.0, -6.0),
			    new Vector(6.0, 0.0, -4.0),
			    new Vector(2.0, 0.0, -4.0),
			    new Vector(1.0, 0.0, -1.0),
			    new Vector(1.0, 0.0, -2.0),
			    new Vector(1.0, 0.0, -3.0),
			    new Vector(1.0, 0.0, -4.0),
			    new Vector(-3.0, 0.0, -5.0),
			    new Vector(-2.0, 0.0, -5.0),
			    new Vector(-1.0, 0.0, -5.0),
			    new Vector(0.0, 0.0, -5.0),
			    new Vector(1.0, 0.0, -9.0),
			    new Vector(1.0, 0.0, -8.0),
			    new Vector(1.0, 0.0, -7.0),
			    new Vector(1.0, 0.0, -6.0),
			    new Vector(5.0, 0.0, -5.0),
			    new Vector(4.0, 0.0, -5.0),
			    new Vector(3.0, 0.0, -5.0),
			    new Vector(2.0, 0.0, -5.0),
			    new Vector(-1.5, 0.0, -0.5),
			    new Vector(-2.5, 0.0, -0.5),
			    new Vector(-1.5, 0.0, -1.5),
			    new Vector(-2.5, 0.0, -1.5),
			    new Vector(-1.5, 0.0, -7.5),
			    new Vector(-2.5, 0.0, -7.5),
			    new Vector(-1.5, 0.0, -8.5),
			    new Vector(-2.5, 0.0, -8.5),
			    new Vector(5.5, 0.0, -7.5),
			    new Vector(4.5, 0.0, -7.5),
			    new Vector(5.5, 0.0, -8.5),
			    new Vector(4.5, 0.0, -8.5),
			    new Vector(5.5, 0.0, -0.5),
			    new Vector(4.5, 0.0, -0.5),
			    new Vector(5.5, 0.0, -1.5),
			    new Vector(4.5, 0.0, -1.5)
			}, safeSpaces, null, 8, false);
	}
	
	/**
	 * Creates a Ludo party "lobby" with the specified positions
	 */
	private ArrayList<Integer> createParty(Integer... color) {
		return new ArrayList<Integer>(Arrays.asList(color));
	}
	
	/**
	 * Applies lists of roll, piece selection, and tile selection in that order.
	 * A single entry will be applied in order before incrementing to the next entry.
	 */
	private void applyMoves(LudoInstance instance, int[] rolls, String[] selectedPieces, int[] selectedTiles) {
		int j = 0;
		while (!(j >= rolls.length && j >= selectedPieces.length && j >= selectedTiles.length)) {
			if (j < rolls.length) {
				instance.roll(rolls[j]);
			}
			if (j < selectedPieces.length) {
				instance.selectPiece(selectedPieces[j]);
			}
			if (j < selectedTiles.length) {
				instance.selectTile(selectedTiles[j]);
			}
			j++;
		}
	}
	
	/**
	 * Quick way to create a piece ID
	 */
	private String piece(int color, int index) { return color + "-" + index; }
	
	/**
	 * Converts a board state to a printable string, with indexes labeled
	 */
	private String boardToString(String[] board) {
		String[] boardCopy = board.clone();
		for (int i = 0; i < board.length; i++) {
			if (!board[i].isEmpty())
				boardCopy[i] = board[i] + " (" + i + ")";
		}
		return Arrays.toString(boardCopy);
	}
	
	/*private void testSolo_NoLineupPathing(int color) {
		
	}
	
	private void testSolo_NoLineup(int color) {
		
	}
	
	private void testSolo_NoLineupWin(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 }, 
				new String[] { 
						piece(color,0), piece(color,0), piece(color,0),
						piece(color,1), piece(color,1), piece(color,1),
						piece(color,2), piece(color,2), piece(color,2),
						piece(color,3), piece(color,3), piece(color,3) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 3),
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 2),
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 1),
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 0)});
		
		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		
		expected[map.getHomeIndex(color, 3)] = piece(color,0);
		expected[map.getHomeIndex(color, 2)] = piece(color,1);
		expected[map.getHomeIndex(color, 1)] = piece(color,2);
		expected[map.getHomeIndex(color, 0)] = piece(color,3);
		
		GameFinishEvent event = ArrayListUtils.findOfType(instance.getLogger().getFullLog(), GameFinishEvent.class);
		
		assertEquals(color, event.getPlayerTurn());
		assertArrayEquals(expected, instance.getBoardState(), "Full board result: " + boardToString(instance.getBoardState()));
	}
	
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	private void testDuo_SafeSpaceCapture(int color) {
		int secondColor = (color + 1) % 4;
		LudoMap map = createMapWithSafeSpaces(new Integer[] { 3 });
		LudoInstance instance = new LudoInstance(createParty(secondColor, color), map, true, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 1, 6, 3 }, 
				new String[] { 
						piece(secondColor,0), piece(secondColor,0),
						piece(color,0), piece(color,0)}, 
				new int[] { 
						map.getTileIndex(map.getStartTile(secondColor)), 
						map.getTileIndex(map.getStartTile(secondColor) + 1), 
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 3)});

		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		
		expected[map.getStartTile(color)] = piece(color,0);
		expected[map.getTileIndex(map.getStartTile(secondColor) + 1)] = piece(secondColor,0);
		
		expected[map.getStarterIndex(color, 1)] = piece(color,1);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getStarterIndex(secondColor, 1)] = piece(secondColor,1);
		expected[map.getStarterIndex(secondColor, 2)] = piece(secondColor,2);
		expected[map.getStarterIndex(secondColor, 3)] = piece(secondColor,3);
		
		assertArrayEquals(expected, instance.getBoardState(), "Full board result: " + boardToString(instance.getBoardState()));
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testDuo_ForceCaptureBlunder(int color) {
		int secondColor = (color + 1) % 4;
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color, secondColor), map, false, false, true, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 1, 6, 1, 6, 2 }, 
				new String[] { 
						piece(color,0), piece(color,0),
						piece(secondColor,0), piece(secondColor,0),
						piece(color,1), piece(color,1)}, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 1), 
						
						map.getTileIndex(map.getStartTile(secondColor)), 
						map.getTileIndex(map.getStartTile(secondColor) + 1), 

						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(secondColor))});

		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		
		expected[map.getStartTile(color)] = piece(color,1);
		expected[map.getTileIndex(map.getStartTile(secondColor) + 1)] = piece(secondColor,0);
		
		expected[map.getStarterIndex(color, 0)] = piece(color,0);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getStarterIndex(secondColor, 1)] = piece(secondColor,1);
		expected[map.getStarterIndex(secondColor, 2)] = piece(secondColor,2);
		expected[map.getStarterIndex(secondColor, 3)] = piece(secondColor,3);
		
		assertArrayEquals(expected, instance.getBoardState(), "Full board result: " + boardToString(instance.getBoardState()));
	}*/
	
	/**
	 * Tests the pathing for a piece getting captured
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testDuo_PathingTileToStarter(int color) {
		int secondColor = (color + 1) % 4;
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color, secondColor), map, false, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 2, 6 }, 
				new String[] { 
						piece(color,0), piece(color,0), piece(secondColor,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 2), 
						map.getTileIndex(map.getStartTile(secondColor))});
		
		Integer[] expected = new Integer[] {
				map.getStartTile(secondColor),
				map.getStarterIndex(color, 0) 
		};
		
		TokenMoveEvent lastEvent = null, secondToLast = null;
		for (InstanceEvent event : instance.getLogger().getFullLog()) {
			if (event instanceof TokenMoveEvent tokenMove)
			{
				secondToLast = lastEvent;
				lastEvent = tokenMove;
			}		
		}
		
		assertArrayEquals(expected, secondToLast.getPath(), "Actual path: " + Arrays.toString(secondToLast.getPath()));
	}
	
	/**
	 * Tests the pathing for a piece going from the home tile to the next home tile
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_PathingHomeToNext(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6, 2, 1 }, 
				new String[] { 
						piece(color,0), piece(color,0), piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 0), 
						map.getHomeIndex(color, 1)});
		
		Integer[] expected = new Integer[] {
				map.getHomeIndex(color, 0),
				map.getHomeIndex(color, 1)};
		
		TokenMoveEvent lastEvent = null;
		for (InstanceEvent event : instance.getLogger().getFullLog()) {
			if (event instanceof TokenMoveEvent tokenMove)
				lastEvent = tokenMove;
				
		}
		assertArrayEquals(expected, lastEvent.getPath(), "Actual path: " + Arrays.toString(lastEvent.getPath()));
	}
	
	/**
	 * Tests the pathing for a piece going from tile to the next tile
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_PathingTileToNext(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 1 }, 
				new String[] { 
						piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 1)});
		
		Integer[] expected = new Integer[] {
				map.getTileIndex(map.getStartTile(color)), 
				map.getTileIndex(map.getStartTile(color) + 1)};
		
		TokenMoveEvent lastEvent = null;
		for (InstanceEvent event : instance.getLogger().getFullLog()) {
			if (event instanceof TokenMoveEvent tokenMove)
				lastEvent = tokenMove;
				
		}
		assertArrayEquals(expected, lastEvent.getPath(), "Actual path: " + Arrays.toString(lastEvent.getPath()));
	}
	
	/**
	 * Tests the pathing for a piece going from the board tile to a home tile
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_PathingTileToHome(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, true, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6, 5 }, 
				new String[] { 
						piece(color,0), piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 3)});
		
		Integer[] expected = new Integer[] {
				map.getTileIndex(map.getStartTile(color) + 6),
				map.getTileIndex(map.getStartTile(color) + 7),
				map.getHomeIndex(color, 0),
				map.getHomeIndex(color, 1),
				map.getHomeIndex(color, 2),
				map.getHomeIndex(color, 3)};
		
		TokenMoveEvent lastEvent = null;
		for (InstanceEvent event : instance.getLogger().getFullLog()) {
			if (event instanceof TokenMoveEvent tokenMove)
				lastEvent = tokenMove;
				
		}
		assertArrayEquals(expected, lastEvent.getPath(), "Actual path: " + Arrays.toString(lastEvent.getPath()));
	}
	
	/**
	 * Tests the pathing of a piece moving on the board tiles
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_PathingTileToTile(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, true, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6 }, 
				new String[] { 
						piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6)});
		
		Integer[] expected = new Integer[] {
				map.getTileIndex(map.getStartTile(color)),
				map.getTileIndex(map.getStartTile(color) + 1),
				map.getTileIndex(map.getStartTile(color) + 2),
				map.getTileIndex(map.getStartTile(color) + 3),
				map.getTileIndex(map.getStartTile(color) + 4),
				map.getTileIndex(map.getStartTile(color) + 5),
				map.getTileIndex(map.getStartTile(color) + 6) };
		
		TokenMoveEvent lastEvent = null;
		for (InstanceEvent event : instance.getLogger().getFullLog()) {
			if (event instanceof TokenMoveEvent tokenMove)
				lastEvent = tokenMove;
				
		}
		assertArrayEquals(expected, lastEvent.getPath(), "Actual path: " + Arrays.toString(lastEvent.getPath()));
	}
	
	/**
	 * Tests the pathing of a piece leaving the starter area and entering the board
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_PathingStarterToTile(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, true, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6 }, 
				new String[] { 
						piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color))});

		Integer[] expected = new Integer[] { 
				map.getStarterIndex(color, 0), 
				map.getStartTile(color) 
		};
		
		TokenMoveEvent lastEvent = null;
		for (InstanceEvent event : instance.getLogger().getFullLog()) {
			if (event instanceof TokenMoveEvent tokenMove)
				lastEvent = tokenMove;
				
		}
		assertArrayEquals(expected, lastEvent.getPath(), "Actual path: " + Arrays.toString(lastEvent.getPath()));
	}
	
	/**
	 * Tests whether a piece is able to skip over another piece that is in the home stretch
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_HomeSkip(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, true, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6, 2, 6, 6, 3 }, 
				new String[] { 
						piece(color,0), piece(color,0), piece(color,0),
						piece(color,1), piece(color,1), piece(color,1) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 0),
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 1)});

		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		
		expected[map.getHomeIndex(color, 0)] = piece(color,0);
		
		expected[map.getTileIndex(map.getStartTile(color) + 6)] = piece(color,1);
		
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		assertArrayEquals(expected, instance.getBoardState(), "Full board result: " + boardToString(instance.getBoardState()));
	}
	
	/**
	 * Tests if a player wins by lining their pieces up during the game rule homeLineup
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_HomeLineupWin(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, true, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6, 5, 6, 6, 4, 6, 6, 3, 6, 6, 2 }, 
				new String[] { 
						piece(color,0), piece(color,0), piece(color,0),
						piece(color,1), piece(color,1), piece(color,1),
						piece(color,2), piece(color,2), piece(color,2),
						piece(color,3), piece(color,3), piece(color,3) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 3),
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 2),
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 1),
						
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 0)});
		
		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		
		expected[map.getHomeIndex(color, 3)] = piece(color,0);
		expected[map.getHomeIndex(color, 2)] = piece(color,1);
		expected[map.getHomeIndex(color, 1)] = piece(color,2);
		expected[map.getHomeIndex(color, 0)] = piece(color,3);
		
		GameFinishEvent event = ArrayListUtils.findOfType(instance.getLogger().getFullLog(), GameFinishEvent.class);
		
		assertEquals(color, event.getPlayerTurn());
		assertArrayEquals(expected, instance.getBoardState(), "Full board result: " + boardToString(instance.getBoardState()));
	}
	
	/**
	 * Tests if a piece can capture an opponents piece
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testDuo_F63_S61_Capture(int color) {
		int secondColor = (color + 1) % 4;
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color, secondColor), map, false, false, false, 2, false);
		instance.start(false);
		
		applyMoves(instance, 
				new int[] { 6, 3, 6, 1 }, 
				new String[] { piece(color,0), piece(color,0), piece(secondColor,0), piece(secondColor,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(secondColor) + 1), 
						map.getTileIndex(map.getStartTile(secondColor)),
						map.getTileIndex(map.getStartTile(secondColor) + 1)});

		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		expected[map.getStarterIndex(color, 0)] = piece(color,0);
		expected[map.getStarterIndex(color, 1)] = piece(color,1);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getStarterIndex(secondColor, 1)] = piece(secondColor,1);
		expected[map.getStarterIndex(secondColor, 2)] = piece(secondColor,2);
		expected[map.getStarterIndex(secondColor, 3)] = piece(secondColor,3);
		
		expected[map.getTileIndex(map.getStartTile(secondColor) + 1)] = piece(secondColor,0);
		
		assertArrayEquals(expected, instance.getBoardState(), "Full board result: " + boardToString(instance.getBoardState()));
	}
	
	/**
	 * Tests if a piece sitting on an opponent's start tile can be captured when the opponent leaves the starter
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testDuo_F62_S6_Capture(int color) {
		int secondColor = (color + 1) % 4;
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color, secondColor), map, false, false, false, 2, false);
		instance.start(false);
		
		applyMoves(instance, 
				new int[] { 6, 2, 6 }, 
				new String[] { piece(color,0), piece(color,0), piece(secondColor,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 2), 
						map.getTileIndex(map.getStartTile(secondColor)) });
		
		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		expected[map.getStarterIndex(color, 0)] = piece(color,0);
		expected[map.getStarterIndex(color, 1)] = piece(color,1);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getStarterIndex(secondColor, 1)] = piece(secondColor,1);
		expected[map.getStarterIndex(secondColor, 2)] = piece(secondColor,2);
		expected[map.getStarterIndex(secondColor, 3)] = piece(secondColor,3);
		
		expected[map.getTileIndex(map.getStartTile(secondColor))] = piece(secondColor,0);
		
		assertArrayEquals(expected, instance.getBoardState(), "Full board result: " + boardToString(instance.getBoardState()));
	}
	
	/**
	 * Tests if a piece can be moved from the starter and around the board
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_F66(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, false, false, 2, false);
		instance.start(false);
		
		applyMoves(instance, 
				new int[] { 6, 6 }, 
				new String[] { piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6) });

		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		expected[map.getStarterIndex(color, 1)] = piece(color,1);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getTileIndex(map.getStartTile(color) + 6)] = piece(color,0);
		
		assertArrayEquals(expected, instance.getBoardState(), "Actual piece location: " + instance.getPieceIndex(piece(color,0)));
	}
	
	/**
	 * Tests if a piece can be moved from the starter all the way to the home stretch
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_F662(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6, 2 }, 
				new String[] { piece(color,0), piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6), 
						map.getHomeIndex(color, 0) });
		
		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		expected[map.getStarterIndex(color, 1)] = piece(color,1);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getHomeIndex(color, 0)] = piece(color,0);
		
		assertArrayEquals(expected, instance.getBoardState(), "Actual piece location: " + instance.getPieceIndex(piece(color,0)));
	}
	
	/**
	 * Tests if a piece can be moved from the starter all the way to the home stretch
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_F666(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 6, 1, 6 }, 
				new String[] { piece(color,0), piece(color,0), piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 6),
						map.getTileIndex(map.getStartTile(color) + 7),
						map.getHomeIndex(color, 5) });
		
		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		expected[map.getStarterIndex(color, 1)] = piece(color,1);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getTileIndex(map.getStartTile(color) + 7)] = piece(color,0);
		
		assertArrayEquals(expected, instance.getBoardState(), "Actual piece location: " + instance.getPieceIndex(piece(color,0)));
	}
	
	/**
	 * Tests if a piece can be moved from the starter all the way to the home stretch, using a different combination
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 3 })
	void testSolo_F626(int color) {
		LudoMap map = createDefaultMap();
		LudoInstance instance = new LudoInstance(createParty(color), map, false, false, false, 2, false);
		instance.start(false);

		applyMoves(instance, 
				new int[] { 6, 2, 6 }, 
				new String[] { piece(color,0), piece(color,0), piece(color,0) }, 
				new int[] { 
						map.getTileIndex(map.getStartTile(color)), 
						map.getTileIndex(map.getStartTile(color) + 2), 
						map.getHomeIndex(color, 0) });
		
		String[] expected = new String[map.getMapSize()];
		Arrays.fill(expected, "");
		expected[map.getStarterIndex(color, 1)] = piece(color,1);
		expected[map.getStarterIndex(color, 2)] = piece(color,2);
		expected[map.getStarterIndex(color, 3)] = piece(color,3);
		
		expected[map.getHomeIndex(color, 0)] = piece(color,0);
		
		assertArrayEquals(expected, instance.getBoardState(), "Actual piece location: " + instance.getPieceIndex(piece(color,0)));
	}
}
