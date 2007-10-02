/* ===================================================================
 * StringUtil.java
 * 
 * Copyright (c) 2002-2003 Matt Magoffin.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * =================================================================== 
 * $Id: StringUtil.java,v 1.1 2006/07/10 04:22:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for dealing with String objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 */
public final class StringUtil {

	/**
	 * Constant to parse an Integer in the {@link #parseNumber(String, char)}
	 * method.
	 */
	public static final char INTEGER = 'i';

	/**
	 * Constant to parse a Long in the {@link #parseNumber(String, char)}
	 * method.
	 */
	public static final char LONG = 'l';

	/**
	 * Constant to parse a Short in the {@link #parseNumber(String, char)}
	 * method.
	 */
	public static final char SHORT = 's';

	/** White space characters for {@link #normalizeWhitespace(String)}. */
	private final static char[] WHITESPACE_CHARS = { ' ', '\t', '\n', '\r' };

	/** Log category. */
	private final static Log LOG = LogFactory.getLog(StringUtil.class);

	/**
	 * Capitalize the first character in the given string.
	 * 
	 * @param str input string
	 * @return <var>str</var> with first letter capitalized
	 */
	public static String capFirstChar(String str) {
		if (str == null || str.length() < 1
				|| Character.isUpperCase(str.charAt(0))) {
			return str;
		}
		char[] chars = str.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	/**
	 * Return the number of occurances of a character in a string.
	 * 
	 * @param str string to test in
	 * @param testChar character to test for
	 * @return count of occurances of <var>testChar</var> in <var>str</var>
	 */
	public static int countOf(String str, char testChar) {
		int count = 0;
		if (str != null) {
			char[] array = str.toCharArray();
			for (int i = 0; i < array.length; i++) {
				if (array[i] == testChar)
					count++;
			}
		}
		return count;
	}

	/**
	 * Format a Long as a phone number string.
	 * 
	 * @param number the number to format as a phone number
	 * @param extension
	 * @return number formatted as a string, or <em>null</em> if number can't
	 *         be formated as a string
	 */
	public static String formatPhone(Long number, Short extension) {
		if (number == null)
			return null;
		return formatPhone(number.toString(), extension);
	}

	/**
	 * Format a string of digits as a phone number string.
	 * 
	 * <p>
	 * The format is <code>555-555-5555x5</code>.
	 * </p>
	 * 
	 * @param numberString  the number to format as a phone number
	 * @param extension an optional extension
	 * @return number formatted as a string, or <em>null</em> if number can't
	 *         be formated as a string
	 */
	public static String formatPhone(String numberString, Object extension) {
		if (numberString == null)
			return null;
		if (numberString.length() > 9) {
			// format as 111-555-6666
			return numberString.substring(0, 3) + "-"
					+ numberString.substring(3, 6) + "-"
					+ numberString.substring(6)
					+ (extension == null ? "" : "x" + extension.toString());
		} else if (numberString.length() > 6) {
			// format as 555-6666
			return numberString.substring(0, 3) + "-"
					+ numberString.substring(3)
					+ (extension == null ? "" : "x" + extension.toString());
		}
		// hmmm... return nothing
		return null;
	}

	/**
	 * Get a boolean value from a String.
	 * 
	 * <p>
	 * The following values are considered <em>true</em>:
	 * </p>
	 * 
	 * <ul>
	 * <li>1</li>
	 * <li>t</li>
	 * <li>true</li>
	 * <li>y</li>
	 * <li>yes</li>
	 * </ul>
	 * 
	 * <p>
	 * All other values (or a missing value) is considered <em>false</em>.
	 * </p>
	 * 
	 * @return java.lang.Boolean
	 * @param s the parsed boolean value
	 */
	public static boolean parseBoolean(String s) {
		if (s == null)
			return false;
		boolean result = false;
		s = s.toLowerCase();
		if (s.equals("true") || s.equals("yes") || s.equals("y")
				|| s.equals("t") || s.equals("1")) {
			result = true;
		}
		return result;
	}

	/**
	 * Parse all digits out of a string.
	 * 
	 * <p>
	 * Method allows a negative character (<code>-</code>) to be the first
	 * "digit".
	 * </p>
	 * 
	 * @param text the String to parse digits out of
	 * @return the digits parsed out of the String, or <em>null</em> if no
	 *         digits found in string
	 */
	public static String parseDigits(String text) {
		if (text == null)
			return null;
		char[] chars = text.toCharArray();
		StringBuffer buf = new StringBuffer();
		int i = 0;
		while (i < chars.length) {
			if (Character.isDigit(chars[i])
					|| (buf.length() < 1 && chars[i] == '-')) {
				buf.append(chars[i]);
			}
			i++;
		}
		if (buf.length() > 0) {
			return buf.toString();
		}
		return null;
	}

	/**
	 * Parse an Integer out of a String.
	 * 
	 * <p>
	 * This method will extract all digits out of a String and convert that to
	 * an Integer.
	 * </p>
	 * 
	 * @param number the String to parse into an Integer
	 * @return the converted Integer, or <em>null</em> if can't be converted
	 */
	public static Integer parseInteger(String number) {
		return (Integer) parseNumber(number, INTEGER);
	}

	/**
	 * Parse an Long out of a String.
	 * 
	 * <p>
	 * This method will extract all digits out of a String and convert that to
	 * an Long.
	 * </p>
	 * 
	 * @param number  the String to parse into an Long
	 * @return the converted Long, or <em>null</em> if can't be converted
	 */
	public static Long parseLong(String number) {
		return (Long) parseNumber(number, LONG);
	}

	/**
	 * Parse a Number out of a String.
	 * 
	 * <p>
	 * This method will extract all digits out of a String and convert that to a
	 * Number of the specified type.
	 * </p>
	 * 
	 * @param number the String to parse digits out of
	 * @param type  the type of Number object desired
	 * @return the converted Number, or <em>null</em> if can't be converted to
	 *         a Number
	 */
	public static Object parseNumber(String number, char type) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Parsing "
					+ (type == 'l' ? "Long" : (type == 's' ? "Short"
							: "Integer")) + " from " + number);
		}
		if (number == null)
			return null;
		String numStr = parseDigits(number);
		if (numStr != null && numStr.length() > 0) {
			try {
				switch (type) {
					case 'l':
						return Long.valueOf(numStr);
					case 's':
						return Short.valueOf(numStr);
					default:
						return Integer.valueOf(numStr);
				}
			} catch (NumberFormatException e) {
				LOG.warn("Can't convert " + numStr + " to a Number");
			}
		}
		return null;
	}

	/**
	 * Parse an Short out of a String.
	 * 
	 * <p>
	 * This method will extract all digits out of a String and convert that to
	 * an Short.
	 * </p>
	 * 
	 * @param number the String to parse into an Short
	 * @return the converted Short, or <em>null</em> if can't be converted
	 */
	public static Short parseShort(String number) {
		return (Short) parseNumber(number, SHORT);
	}

	/**
	 * Replaces multiple occurances of a string within another string.
	 * 
	 * Examples:
	 * <ul>
	 * <li>
	 * <p>
	 * replace("abcdef", "abc", "xyz") returns "xyzdef".
	 * <li>
	 * <p>
	 * replace("abcdef", "bc", "xyz") returns "axyzdef".
	 * <li>
	 * <p>
	 * replace("abcdef", "efg", "xyz") returns "abcdef".
	 * <li>
	 * <p>
	 * replace("abcdefbcgbc", "bc", "x") returns "axdefxgx".
	 * </ul>
	 * 
	 * @param inputString The string where characters are to be replaced.
	 * @param searchString The string to search for in pInputString.
	 * @param replaceString  The string to replace pSearchString with.
	 * @return The resulting string
	 */
	public static String replace(String inputString, String searchString,
			String replaceString) {
		if (inputString == null) {
			throw new IllegalArgumentException(
					"StringUtil.replace: pInputString is null!");
		}
		if (searchString == null) {
			throw new IllegalArgumentException(
					"StringUtil.replace: pSearchString is null!");
		}
		if (replaceString == null) {
			throw new IllegalArgumentException(
					"StringUtil.replace: pReplaceString is null!");
		}

		int inputStringLength = inputString.length();
		int searchStringLength = searchString.length();

		// If either input string or search string are empty, return original
		// string
		if ((inputStringLength == 0) || (searchStringLength == 0)) {
			return inputString;
		}

		int pos = inputString.lastIndexOf(searchString);

		// if the search string not found, return original string
		if (pos == -1) {
			return inputString;
		}

		StringBuffer sb = new StringBuffer(inputString);

		while (pos != -1) {
			sb.replace(pos, pos + searchStringLength, replaceString);
			pos = inputString.lastIndexOf(searchString, pos - 1);
		}

		return sb.toString();
	}

	/**
	 * Convert an array to a String.
	 * 
	 * @return java.lang.String
	 * @param array the array to join
	 */
	public static String valueOf(Object[][] array) {
		StringBuffer buf = new StringBuffer("[");
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					buf.append(",");
				}
				buf.append("[");
				if (array[i] != null) {
					for (int j = 0; j < array[i].length; j++) {
						if (j > 0) {
							buf.append(",");
						}
						buf.append(array[i][j]);
					}
				}
				buf.append("]");
			}
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Convert an array to a String.
	 * 
	 * @return java.lang.String
	 * @param array the array to join
	 */
	public static String valueOf(Object[] array) {
		StringBuffer buf = new StringBuffer("[");
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					buf.append(",");
				}
				buf.append(array[i]);
			}
		}
		buf.append("]");
		return buf.toString();
	}

	/**
	 * Convert an array to a String.
	 * 
	 * @param array java.lang.Object[]
	 * @param delim the delimiter to join the array with
	 * @param start the starting array element
	 * @param end  the ending array element
	 * @return java.lang.String
	 */
	public static String valueOf(Object[] array, String delim, String start,
			String end) {
		StringBuffer buf = new StringBuffer();
		if (start != null) {
			buf.append(start);
		}
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (i > 0 && delim != null) {
					buf.append(delim);
				}
				buf.append(array[i]);
			}
		}
		if (end != null) {
			buf.append(end);
		}
		return buf.toString();
	}

	/**
	 * Round a double to a specified number of decimal places and return as a
	 * String.
	 * 
	 * @param num the number to round
	 * @param places the maximum number of decimal places to use
	 * @return a String version of the rounded number
	 */
	public static String roundDecimal(double num, int places) {
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(places);
		try {
			return format.format(num);
		} catch (Exception e) {
			LOG.warn("Unable to round decimal " + num + ": " + e.toString());
			return String.valueOf(num);
		}
	}

	/**
	 * Return the substring from <var>str</var> after <var>match</var>.
	 * 
	 * @param str the source string
	 * @param match the substring to match
	 * @return a substring remaining after the last <var>match</var>
	 */
	public static String substringAfter(String str, char match) {
		int idx = str.lastIndexOf(match);
		if (idx < 0 || (idx + 1) >= str.length()) {
			return null;
		}
		return str.substring(idx + 1);
	}

	/**
	 * Return the substring from <var>str</var> after <var>match</var>.
	 * 
	 * @param str the source string
	 * @param match the substring to match
	 * @return a substring remaining after the last <var>match</var>
	 */
	public static String substringAfter(String str, String match) {
		int idx = str.lastIndexOf(match);
		int matchLength = match.length();
		if (idx < 0 || (idx + matchLength) >= str.length()) {
			return null;
		}
		return str.substring(idx + matchLength);
	}

	/**
	 * Return the substring from <var>str</var> after the first <var>match</var>.
	 * 
	 * @param str the source string
	 * @param match the substring to match
	 * @return a substring remaining after the first <var>match</var>
	 */
	public static String substringAfterFirst(String str, char match) {
		int idx = str.indexOf(match);
		if (idx < 0 || (idx + 1) >= str.length()) {
			return null;
		}
		return str.substring(idx + 1);
	}

	/**
	 * Return the substring from <var>str</var> after the first<var>match</var>.
	 * 
	 * @param str the source string
	 * @param match  the substring to match
	 * @return a substring remaining after the first <var>match</var>
	 */
	public static String substringAfterFirst(String str, String match) {
		int idx = str.indexOf(match);
		int matchLength = match.length();
		if (idx < 0 || (idx + matchLength) >= str.length()) {
			return null;
		}
		return str.substring(idx + matchLength);
	}

	/**
	 * Return the substring from <var>str</var> before <var>match</var>.
	 * 
	 * @param str the source string
	 * @param match the substring to match
	 * @return a substring starting before the first <var>match</var>
	 */
	public static String substringBefore(String str, char match) {
		int idx = str.indexOf(match);
		if (idx < 1) {
			return null;
		}
		return str.substring(0, idx);
	}

	/**
	 * Return the substring from <var>str</var> before <var>match</var>.
	 * 
	 * @param str the source string
	 * @param match the substring to match
	 * @return a substring starting before the first <var>match</var>
	 */
	public static String substringBefore(String str, String match) {
		int idx = str.indexOf(match);
		if (idx < 1) {
			return null;
		}
		return str.substring(0, idx);
	}

	/**
	 * Return the substring from <var>str</var> before the last <var>match</var>.
	 * 
	 * @param str the source string
	 * @param match the substring to match
	 * @return a substring starting before the last <var>match</var>
	 */
	public static String substringBeforeLast(String str, char match) {
		int idx = str.lastIndexOf(match);
		if (idx < 1) {
			return null;
		}
		return str.substring(0, idx);
	}

	/**
	 * Return the substring from <var>str</var> before the last <var>match</var>.
	 * 
	 * @param str the source string
	 * @param match the substring to match
	 * @return a substring starting before the last <var>match</var>
	 */
	public static String substringBeforeLast(String str, String match) {
		int idx = str.lastIndexOf(match);
		if (idx < 1) {
			return null;
		}
		return str.substring(0, idx);
	}

	/**
	 * Trim trailing and leading whitespace from a string, returning
	 * <em>null</em> if the trimmed string is empty.
	 * 
	 * @param str the string to trim
	 * @return the trimmed string, or <em>null</em>
	 */
	public static String trimToNull(String str) {
		if (str == null || str.length() < 1) {
			return null;
		}
		String trimmed = str.trim();
		if (trimmed.length() < 1) {
			return null;
		}
		return trimmed;
	}

	/**
	 * Normalize the white space in a String.
	 * 
	 * <p>
	 * This method will remove leading/trailing whitespace as well as turn
	 * strings of white space into a single space character. If the string is
	 * nothing but whitespace, <em>null</em> will be returned.
	 * </p>
	 * 
	 * @param str the String to normalize
	 * @return normalized String
	 */
	public static String normalizeWhitespace(String str) {
		if (str == null || str.length() < 1) {
			return null;
		}
		StringBuffer buf = null; // try not to copy string if don't have to
		char[] chars = str.toCharArray();
		int i = 0;
		int j = 0;
		boolean addSpace = true;

		OUTER: for (i = 0; i < chars.length; i++) {
			for (j = 0; j < WHITESPACE_CHARS.length; j++) {
				if (chars[i] == WHITESPACE_CHARS[j]) {
					if (addSpace) {
						if (buf != null) {
							buf.append(' ');
						}
						addSpace = false;
					} else {
						// double white space
						if (buf == null) {
							// copy current into buf
							buf = new StringBuffer();
							for (int k = 0; k < i; k++) {
								buf.append(chars[k]);
							}
						}
					}
					continue OUTER;
				}
			}
			addSpace = true; // not a whitespace char, so add next one
			if (buf != null) {
				buf.append(chars[i]);
			}
		}
		String result = buf == null ? str.trim() : buf.toString().trim();
		return result.length() < 1 ? null : result;
	}

	/**
	 * Normalize the white space on each String in an array.
	 * 
	 * @param strs array of String objects
	 * @return the normalized string
	 * @see #normalizeWhitespace(String)
	 */
	public static String[] normalizeWhitespace(String[] strs) {
		if (strs == null || strs.length < 1) {
			return strs;
		}
		for (int i = 0; i < strs.length; i++) {
			strs[i] = normalizeWhitespace(strs[i]);
		}
		return strs;
	}

	/**
	 * Escape any single quote characters that are included in the specified
	 * message string.
	 * 
	 * <p>
	 * Adapted from org.apache.struts.util.MessageResources#escape(String)
	 * </p>
	 * 
	 * @param string The string to be escaped
	 * @return the escaped string
	 */
	public static String escapeSingleQuotes(String string) {
		if ((string == null) || (string.indexOf('\'') < 0))
			return (string);
		int n = string.length();
		StringBuffer sb = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			char ch = string.charAt(i);
			if (ch == '\'')
				sb.append('\'');
			sb.append(ch);
		}
		return (sb.toString());
	}

}
