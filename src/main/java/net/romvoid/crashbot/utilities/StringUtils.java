package net.romvoid.crashbot.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.var;

public class StringUtils {

	public static final Pattern SPLIT_PATTERN = Pattern.compile("\\s+");
	public static final String[] EMPTY_ARRAY = new String[0];

	public static String[] advancedSplitArgs(String args, int expectedArgs) {

		List<String> result = new ArrayList<>();
		var inBlock = false;
		var currentBlock = new StringBuilder();

		for (int i = 0; i < args.length(); i++) {
			var currentChar = args.charAt(i);
			if ((currentChar == '"' || currentChar == '“' || currentChar == '”')
					&& (i == 0 || args.charAt(i - 1) != '\\' || args.charAt(i - 2) == '\\')) {
				inBlock = !inBlock;
			}
			if (inBlock) {
				currentBlock.append(currentChar);
			} else if (Character.isSpaceChar(currentChar)) {
				if (currentBlock.length() != 0) {
					if (((currentBlock.charAt(0) == '"' || currentBlock.charAt(0) == '“')
							&& (currentBlock.charAt(currentBlock.length() - 1) == '"'
									|| currentBlock.charAt(currentBlock.length() - 1) == '”'))) {
						currentBlock.deleteCharAt(0);
						currentBlock.deleteCharAt(currentBlock.length() - 1);
					}
					result.add(advancedSplitArgsUnbox(currentBlock.toString()));
					currentBlock = new StringBuilder();
				}
			} else {
				currentBlock.append(currentChar);
			}
		}

		if (currentBlock.length() != 0) {
			if ((currentBlock.charAt(0) == '"' || currentBlock.charAt(0) == '“')
					&& (currentBlock.charAt(currentBlock.length() - 1) == '"'
							|| currentBlock.charAt(currentBlock.length() - 1) == '”')) {
				currentBlock.deleteCharAt(0);
				currentBlock.deleteCharAt(currentBlock.length() - 1);
			}

			result.add(advancedSplitArgsUnbox(currentBlock.toString()));
		}

		var raw = result.toArray(EMPTY_ARRAY);

		if (expectedArgs < 1) {
			return raw;
		}

		return normalizeArray(raw, expectedArgs);
	}

	public static String limit(String value, int length) {
		var buf = new StringBuilder(value);

		if (buf.length() > length) {
			buf.setLength(length - 3);
			buf.append("...");
		}

		return buf.toString();
	}

	/**
	 * Normalize an {@link String} Array.
	 *
	 * @param raw          the String array to be normalized
	 * @param expectedSize the final size of the Array.
	 * @return {@link String}[] with the size of expectedArgs
	 */
	public static String[] normalizeArray(String[] raw, int expectedSize) {
		var normalized = new String[expectedSize];

		Arrays.fill(normalized, "");
		for (int i = 0; i < normalized.length; i++) {
			if (i < raw.length && raw[i] != null && !raw[i].isEmpty()) {
				normalized[i] = raw[i];
			}
		}

		return normalized;
	}

	public static Map<String, String> parseArguments(String[] args) {
		Map<String, String> options = new HashMap<>();

		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].charAt(0) == '-' || args[i].charAt(0) == '/') // This start with - or /
				{
					args[i] = args[i].substring(1);
					if (i + 1 >= args.length || args[i + 1].charAt(0) == '-' || args[i + 1].charAt(0) == '/') { // Next
																												// start
																												// with
																												// - (or
																												// last
																												// arg)
						options.put(args[i], "null");
					} else {
						options.put(args[i], args[i + 1]);
						i++;
					}
				} else {
					options.put(null, args[i]);
				}
			}

			return options;
		} catch (Exception e) {
			return new HashMap<>();
		}
	}

	/**
	 * Enhanced {@link String#split(String, int)} with SPLIT_PATTERN as the Pattern
	 * used.
	 *
	 * @param args         the {@link String} to be split.
	 * @param expectedArgs the size of the returned array of Non-null
	 *                     {@link String}s
	 * @return a {@link String}[] with the size of expectedArgs
	 */
	public static String[] splitArgs(String args, int expectedArgs) {
		var raw = SPLIT_PATTERN.split(args, expectedArgs);

		if (expectedArgs < 1) {
			return raw;
		}

		return normalizeArray(raw, expectedArgs);
	}

	private static String advancedSplitArgsUnbox(String s) {
		return s.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\",
				"\\");
	}

}
