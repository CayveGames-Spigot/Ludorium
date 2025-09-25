package me.cayve.ludorium.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArrayUtils {

	public static int wrap(int size, int index) {
		if (index >= 0)
			return index % size;
		else
			return (index % -size) + size;
	}
	
	public static <T> void forEachIndex(T[] array, Consumer<Integer> action) {
		for (int i = 0; i < array.length; i++)
			action.accept(i);
	}
	
	public static <T> void forEach(T[] array, Consumer<T> action) {
		for (int i = 0; i < array.length; i++)
			if (array[i] != null)
				action.accept(array[i]);
	}
	
	public static <T> T find(T[] array, Predicate<T> predicate) {
		for (int i = 0; i < array.length; i++)
			if (array[i] != null && predicate.test(array[i]))
				return array[i];
		return null;
	}
	
	public static <T> boolean contains(T[] array, Predicate<T> predicate) { return find(array, predicate) != null; }
	
	/**
	 * Maps an array to a new array type based on a mapping method
	 * @param <R>
	 * @param <T>
	 * @param array
	 * @param type
	 * @param mapMethod
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <R, T> R[] map(T[] array, Class<R> type, Function<T, R> mapMethod) {
		R[] mappedArray = (R[]) Array.newInstance(type, array.length);
		
		forEachIndex(array, (i) -> {
			if (array[i] != null)
				mappedArray[i] = mapMethod.apply(array[i]);
		});
		return mappedArray;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(ArrayList<T> list, Class<?> type) {
		T[] array = (T[]) Array.newInstance(type, list.size());
		
		for (int i = 0; i < list.size(); i++)
			array[i] = list.get(i);
		return array;
	}
}
