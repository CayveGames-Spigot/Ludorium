package me.cayve.ludorium.games.ludo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.util.Vector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class LudoMapTest {
	private LudoMap createDefaultMap() {
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
			}, new Integer[0], null, 8, false);
	}
	
	@ParameterizedTest
	@CsvSource({
        "0, 7",
        "1, 1",
        "2, 3",
        "3, 5"
    })
	void testEndTile(int color, int expected) {
		assertEquals(expected, createDefaultMap().getEndTile(color));
	}
	
	@ParameterizedTest
	@CsvSource({
        "0, 0",
        "1, 2",
        "2, 4",
        "3, 6"
    })
	void testStartTile(int color, int expected) {
		assertEquals(expected, createDefaultMap().getStartTile(color));
	}
	
	@ParameterizedTest
	@CsvSource({
        "0, 0, 8",
        "0, 1, 9",
        "0, 2, 10",
        "0, 3, 11",
        "1, 0, 12",
        "1, 1, 13",
        "1, 2, 14",
        "1, 3, 15",
        "2, 0, 16",
        "2, 1, 17",
        "2, 2, 18",
        "2, 3, 19",
        "3, 0, 20",
        "3, 1, 21",
        "3, 2, 22",
        "3, 3, 23",
    })
	void testHomeTile(int color, int index, int expected) {
		assertEquals(expected, createDefaultMap().getHomeIndex(color, index));
	}
	
	@ParameterizedTest
	@CsvSource({
        "0, 0",
        "1, 1",
        "2, 2",
        "3, 3",
        "4, 4",
        "5, 5",
        "6, 6",
        "7, 7",
        "8, 0",
        "9, 1",
        "10, 2",
        "11, 3",
        "12, 4",
        "13, 5",
        "14, 6",
        "15, 7",
    })
	void testTile(int index, int expected) {
		assertEquals(expected, createDefaultMap().getTileIndex(index));
	}
	
	@ParameterizedTest
	@CsvSource({
        "0, 0, 24",
        "0, 1, 25",
        "0, 2, 26",
        "0, 3, 27",
        "1, 0, 28",
        "1, 1, 29",
        "1, 2, 30",
        "1, 3, 31",
        "2, 0, 32",
        "2, 1, 33",
        "2, 2, 34",
        "2, 3, 35",
        "3, 0, 36",
        "3, 1, 37",
        "3, 2, 38",
        "3, 3, 39",
    })
	void testStarterIndex(int color, int index, int expected) {
		assertEquals(expected, createDefaultMap().getStarterIndex(color, index));
	}
}
