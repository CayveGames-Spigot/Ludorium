package me.cayve.ludorium.games;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface GameDeclaration {
	/**
	 * Used in configuration paths and custom model IDs
	 * @return
	 */
	String prefix();
}
