package me.cayve.ludorium.utils;

public class StringUtils {

	public static String replaceAt(String string, int index, char replacement) {
		String result = string.substring(0, index) + replacement;
		if (index != string.length() - 1)
			result += string.substring(index + 1);
		return result;
	}
	
	public static String replaceLast(String string, char regex, char replacement) {
		return replaceAt(string, string.lastIndexOf(regex), replacement);
	}
}
