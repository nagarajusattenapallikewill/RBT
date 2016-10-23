package com.onmobile.apps.ringbacktones.rbtcontents.utils;

import java.util.Comparator;

public class CaseInsensitiveStringComparator implements Comparator<String> {
	public int compare(String o1, String o2) {
		return o1.compareToIgnoreCase(o2);
	}
}