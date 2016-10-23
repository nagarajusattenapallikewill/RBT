/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

/**
 * Provides the utility methods for the string operations.
 * 
 * @author vinayasimha.patil
 */
public class StringUtil
{
	/**
	 * Parses the string argument as a signed decimal integer. If it is not
	 * valid number then returns -1.
	 * 
	 * @param string
	 *            the string to be converted to integer
	 * @return the integer value of <tt>string</tt> or -1 if <tt>string</tt> is
	 *         not valid number
	 */
	public static int getInteger(String string)
	{
		int integer = -1;
		if (string != null && string.length() > 0)
		{
			try
			{
				integer = Integer.parseInt(string);
			}
			catch (final NumberFormatException e)
			{
				return -1;
			}
		}

		return integer;
	}

	/**
	 * Converts the first character to upper case and rest to lower case.
	 * 
	 * @param string
	 *            the string for which first character needs to be converted to
	 *            upper case
	 * @return the converted string
	 */
	public static String toUpperCaseOnlyFirstChar(String string)
	{
		if (string == null || string.length() == 0)
			return string;

		string = string.toLowerCase();
		string = (String.valueOf(string.charAt(0))).toUpperCase()
				+ string.substring(1, string.length());

		return string;
	}

	/**
	 * Converts the first character to upper case.
	 * 
	 * @param string
	 *            the string for which first character needs to be converted to
	 *            upper case
	 * @return the converted string
	 */
	public static String toUpperCaseFirstChar(String string)
	{
		if (string == null || string.length() == 0)
			return string;
		string = (String.valueOf(string.charAt(0))).toUpperCase()
				+ string.substring(1, string.length());

		return string;
	}

	/**
	 * Converts the string to camel case i.e. removes the underscore and
	 * converts next character to upper case and first character also.
	 * 
	 * @param string
	 *            the string which has to be converted to camel case
	 * @return the converted string
	 */
	public static String toCamelCase(String string)
	{
		if (string == null || string.length() == 0)
			return string;

		string = string.toLowerCase();
		StringBuilder stringBuilder = new StringBuilder(string.length());
		for (int i = 0; i < string.length(); i++)
		{
			char c = string.charAt(i);
			if (c == '_')
			{
				i++;
				if (i == string.length())
					break;

				c = string.charAt(i);
				c = Character.toUpperCase(c);
			}
			
			if (i == 0)
				c = Character.toUpperCase(c);

			stringBuilder.append(c);
		}

		return stringBuilder.toString();
	}

	/**
	 * Converts the unicode string to hex.
	 * 
	 * @param unicodeString
	 *            the unicode string which has to be converted to hex
	 * @return the converted hex string
	 */
	public static String convertUnicodeToHex(String unicodeString)
	{
		if (unicodeString == null)
			return null;

		StringBuilder hexString = new StringBuilder();

		int length = unicodeString.length();
		for (int i = 0; i < length; i++)
		{
			int thisChar = unicodeString.charAt(i);
			for (int digit = 0; digit < 4; digit++)
			{
				int thisDigit = thisChar & 0xf000;
				thisChar = thisChar << 4;
				thisDigit = (thisDigit >> 12);
				if (thisDigit >= 10)
					hexString.append((char) (thisDigit + 87));
				else
					hexString.append((char) (thisDigit + 48));
			}
			hexString.append("");
		}

		return hexString.toString();
	}
	
	public static String splitAndAppendSingleQuotes(String str) {
		if (null != str) {
			String[] arr = str.split(",");
			StringBuilder sb = new StringBuilder("'");
			for (int i = 0; i < arr.length; i++) {
				sb.append(arr[i].trim());
				sb.append("'");
				if (arr.length - 1 > i) {
					sb.append(",'");
				}
			}
			return sb.toString();
		}
		return str;
	}
}
