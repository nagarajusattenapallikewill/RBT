package com.onmobile.apps.ringbacktones.rbt2.common;

public class DBUtils {

	public static String sqlString(String s) {
		if (s != null) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append('\'');
			int from = 0;
			int next;
			while ((next = s.indexOf('\'', from)) != -1) {
				stringBuilder.append(s.substring(from, next + 1));
				stringBuilder.append('\'');
				from = next + 1;

			}

			if (from < s.length())
				stringBuilder.append(s.substring(from));

			stringBuilder.append('\'');
			return stringBuilder.toString();
		}

		return "NULL";
	}

}
