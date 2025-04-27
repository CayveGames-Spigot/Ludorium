package me.cayve.ludorium.utils.locational;

import java.lang.reflect.Array;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Grid2D<T> {

	private final T[][] data;
	private final int width;
	private final int height;
	
	@SuppressWarnings("unchecked")
	public Grid2D(Class<T> type, int width, int height) {
		this.width = width;
		this.height = height;
		
		data = (T[][]) Array.newInstance(type, width, height);
	}
	
	public T get(int x, int y) {
		if (x < 0 || y < 0 || x >= width || y >= height)
			return null;
		return data[x][y];
	}
	
	public void set(int x, int y, T element) {
		data[x][y] = element;
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	/**
	 * Runs an action at each coordinate (even if its null)
	 * @param action
	 */
	public void forEachIndex(BiConsumer<Integer, Integer> action) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				action.accept(x, y);
			}
		}
	}
	
	/**
	 * Runs an action on each existing element
	 * @param action
	 */
	public void forEach(Consumer<T> action) {
		forEachIndex((x, y) -> {
			T element = get(x, y);
			
			if (element != null)
				action.accept(element);
		});
	}
	
	/**
	 * Maps the grid to a new grid type based on a map method
	 * @param <R>
	 * @param type
	 * @param mapMethod
	 * @return
	 */
	public <R> Grid2D<R> map(Class<R> type, Function<T, R> mapMethod) {
		Grid2D<R> newGrid = new Grid2D<R>(type, width, height);
		forEachIndex((x, y) -> newGrid.set(x, y, mapMethod.apply(get(x, y))));
		return newGrid;
	}
	
	public T[][] toArray() { return data; }
}
