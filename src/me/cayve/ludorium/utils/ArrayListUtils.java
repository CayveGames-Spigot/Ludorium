package me.cayve.ludorium.utils;

import java.util.Iterator;
import java.util.function.Predicate;

public class ArrayListUtils<T> extends java.util.ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean containsIf(Predicate<T> predicate) {
		return find(predicate) != null;
	}
	
	public T find(Predicate<T> predicate) {
		Iterator<T> iterator = this.iterator();
		while (iterator.hasNext())
		{
			T next = iterator.next();
			if (predicate.test(next))
				return next;
		}
		return null;
	}
}
