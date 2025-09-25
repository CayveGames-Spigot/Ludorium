package me.cayve.ludorium.utils;

import java.util.regex.Pattern;

public class ProgressBar {

	public static class Builder {
		private record Pair(char filled, char unfilled) {}
		
		private boolean reverse = false;
		private int barCount = 4;
		private String format = "[%i%o]";
		private Pair fillCharacters = new Pair('■', '□');
		
		/**
		 * @param barCount how many bars the progress bar should have
		 */
		public Builder barCount(int barCount) { this.barCount = barCount; return this; }
		/**
		 * @param format the format the progress bar should be. Should contain 1 "%i" (complete) and 1 "%o" (incomplete).<p> Example: "[%i %o]" will output "[■ ■ □ □]"
		 */
		public Builder format(String format) { this.format = format; return this; }
		public Builder customCharacters(char filled, char unfilled) { this.fillCharacters = new Pair(filled, unfilled); return this; }
		public Builder reverse() { this.reverse = true; return this; }
		
		/**
		 * Generates a progress bar string
		 * @param progress how far along the progress bar should be (0-1)
		 * @return progress bar string
		 */
		public String generate(float progress) {
			return generate(Math.round(Math.clamp(progress, 0, 1) * barCount));
		}
		
		/**
		 * Generates a progress bar string
		 * @param barsCompleted how many bars will be completed
		 * @return progress bar string
		 */
		public String generate(int barsCompleted) {
			String progressBar = generateString(barCount, format, fillCharacters);
			for (int i = 0; i < barsCompleted; i++)
			{
				if (reverse)
					progressBar = StringUtils.replaceLast(progressBar, fillCharacters.unfilled(), fillCharacters.filled());
				else
					progressBar = progressBar.replaceFirst(Pattern.quote(fillCharacters.unfilled() + ""), fillCharacters.filled() + "");
			}

			return progressBar;
		}
		
		private static String generateString(int barCount, String format, Pair fillCharacters) {
			String dilimeter = format.substring(format.indexOf("%i") + 2, format.indexOf("%o"));
			String rawBar = (dilimeter + fillCharacters.unfilled()).repeat(barCount).replaceFirst(Pattern.quote(dilimeter), "");
			return format.replace("%i" + dilimeter + "%o", rawBar);
		}
	}
	
	public static Builder newBuild() { return new Builder(); }
}
