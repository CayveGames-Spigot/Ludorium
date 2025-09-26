package me.cayve.ludorium.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ArrayListUtils {

	public static <T> boolean containsIf(ArrayList<T> list, Predicate<T> predicate) {
		return find(list, predicate) != null;
	}
	
	public static <T> T findOfType(ArrayList<?> list, Class<T> type) {
		return type.cast(find(list, x -> x.getClass().isAssignableFrom(type)));
	}
	public static <T> T find(ArrayList<T> list, Predicate<T> predicate) {
		Iterator<T> iterator = list.iterator();
		while (iterator.hasNext())
		{
			T next = iterator.next();
			if (predicate.test(next))
				return next;
		}
		return null;
	}
	
	public static <T> boolean runIfFound(ArrayList<T> list, Predicate<T> predicate, Consumer<T> action) {
		T result = find(list, predicate);
		if (result == null) return false;
		action.accept(result);
		return true;
	}
}
