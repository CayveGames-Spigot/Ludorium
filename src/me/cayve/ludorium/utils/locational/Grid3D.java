package me.cayve.ludorium.utils.locational;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.TriConsumer;

public class Grid3D<T> {

	private final T[][][] data;
	private final int width;
	private final int height;
	private final int depth;
	
	@SuppressWarnings("unchecked")
	public Grid3D(Class<T> type, int width, int height, int depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		data = (T[][][]) Array.newInstance(type, width, height, depth);
	}
	
	public T get(int x, int y, int z) {
		return data[x][y][z];
	}
	
	public void set(int x, int y, int z, T element) {
		data[x][y][z] = element;
	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getDepth() { return depth; }
	
	/**
	 * Runs an action at each coordinate (even if its null)
	 * @param action
	 */
	public void forEach(TriConsumer<Integer, Integer, Integer> action) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < depth; z++) {
					action.accept(x, y, z);
				}
			}
		}
	}
	
	/**
	 * Runs an action on each existing element
	 * @param action
	 */
	public void forEach(Consumer<T> action) {
		forEach((x, y, z) -> {
			T element = get(x, y, z);
			
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
	public <R> Grid3D<R> map(Class<R> type, Function<T, R> mapMethod) {
		Grid3D<R> newGrid = new Grid3D<R>(type, width, height, depth);
		forEach((x, y, z) -> newGrid.set(x, y, z, mapMethod.apply(get(x, y, z))));
		return newGrid;
	}
	
	public T[][][] toArray() { return data; }
}
