package me.cayve.ludorium.utils;

import java.util.regex.Pattern;

public class ProgressBar {

	private static final int DEFAULT_BAR_COUNT = 4;
	private static final String DEFAULT_FORMAT = "[%i%o]";
	
	/**
	 * Generates a progress bar string
	 * @param progress how far along the progress bar should be (0-1)
	 * @return progress bar string
	 */
	public static String generate(float progress) {
		return generate(Math.clamp(progress, 0, 1), DEFAULT_BAR_COUNT);
	}
	
	/**
	 * Generates a progress bar string
	 * @param progress how far along the progress bar should be (0-1)
	 * @param barCount how many bars the progress bar should have
	 * @return progress bar string
	 */
	public static String generate(float progress, int barCount) {
		return generate(Math.clamp(progress, 0, 1), barCount, DEFAULT_FORMAT);
	}
	
	/**
	 * Generates a progress bar string
	 * @param progress how far along the progress bar should be (0-1)
	 * @param format the format the progress bar should be. Should contain 1 "%i" (complete) and 1 "%o" (incomplete). Example: "[%i %o]" will output "[■ ■ □ □]"
	 * @return progress bar string
	 */
	public static String generate(float progress, String format) {
		return generate(Math.clamp(progress, 0, 1), DEFAULT_BAR_COUNT, format);
	}
	
	/**
	 * Generates a progress bar string
	 * @param progress how far along the progress bar should be (0-1)
	 * @param barCount how many bars the progress bar should have
	 * @param format the format the progress bar should be. Should contain 1 "%i" (complete) and 1 "%o" (incomplete). Example: "[%i %o]" will output "[■ ■ □ □]"
	 * @return progress bar string
	 */
	public static String generate(float progress, int barCount, String format) {
		return generate(Math.round(Math.clamp(progress, 0, 1) * barCount), barCount, format);
	}
	
	/**
	 * Generates a progress bar string
	 * @param barsCompleted how many bars will be completed
	 * @return progress bar string
	 */
	public static String generate(int barsCompleted) {
		return generate(Math.clamp(barsCompleted, 0, DEFAULT_BAR_COUNT), DEFAULT_BAR_COUNT);
	}
	
	/**
	 * Generates a progress bar string
	 * @param barsCompleted how many bars will be completed
	 * @param barCount how many bars the progress bar should have
	 * @return progress bar string
	 */
	public static String generate(int barsCompleted, int barCount) {
		return generate(Math.clamp(barsCompleted, 0, DEFAULT_BAR_COUNT), barCount, DEFAULT_FORMAT);
	}
	
	/**
	 * Generates a progress bar string
	 * @param barsCompleted how many bars will be completed
	 * @param format the format the progress bar should be. Should contain 1 "%i" (complete) and 1 "%o" (incomplete). Example: "[%i %o]" will output "[■ ■ □ □]"
	 * @return progress bar string
	 */
	public static String generate(int barsCompleted, String format) {
		return generate(Math.clamp(barsCompleted, 0, DEFAULT_BAR_COUNT), DEFAULT_BAR_COUNT, format);
	}
	
	/**
	 * Generates a progress bar string
	 * @param barsCompleted how many bars will be completed
	 * @param barCount how many bars the progress bar should have
	 * @param format the format the progress bar should be. Should contain 1 "%i" (complete) and 1 "%o" (incomplete). Example: "[%i %o]" will output "[■ ■ □ □]"
	 * @return progress bar string
	 */
	public static String generate(int barsCompleted, int barCount, String format) {
		String progressBar = generateString(barCount, format);
		for (int i = 0; i < barsCompleted; i++)
			progressBar = progressBar.replaceFirst(Pattern.quote("□"), "■");
		return progressBar;
	}
	
	private static String generateString(int barCount, String format) {
		String dilimeter = format.substring(format.indexOf("%i") + 2, format.indexOf("%o"));
		String rawBar = (dilimeter + "□").repeat(barCount).replaceFirst(Pattern.quote(dilimeter), "");
		return format.replace("%i" + dilimeter + "%o", rawBar);
	}
}
