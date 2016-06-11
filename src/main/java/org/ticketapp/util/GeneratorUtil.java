package org.ticketapp.util;

public final class GeneratorUtil {

	private static final int NUM_ALPHABETS = 26;

	/**
	 * Returns a string representation of the number in alphabet form.
	 * <p>
	 * Example: A, B, ... Y, Z, BA, BB...
	 * 
	 * @param number
	 *            input to be converted to String form
	 * @return String representation of <code>number</code>
	 */
	public static String getString(int number) {
		if (number <= 0) {
			throw new IllegalArgumentException(number + " is invalid. Should be > 0");
		}
		StringBuilder builder = new StringBuilder();
		int quotient = number;
		while (quotient > NUM_ALPHABETS - 1) {
			int remainder = quotient % NUM_ALPHABETS;
			builder.append(getChar(remainder));
			quotient = quotient / NUM_ALPHABETS;
		}
		builder.append(getChar(quotient));
		return builder.reverse().toString();
	}

	static char getChar(int number) {
		if (number < 0 || number > 26) {
			throw new IllegalArgumentException(number + " should be between 0 and 25");
		}
		return (char) ('A' + number);
	}
}
