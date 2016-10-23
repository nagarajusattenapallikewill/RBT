package com.onmobile.apps.ringbacktones.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public final class ListUtils {
	private static Logger logger = Logger.getLogger(ListUtils.class);

	public static List<String> convertToList(String str, String delimiter) {
		List<String> list = new ArrayList<String>();
		String[] arr = StringUtils.toStringArray(str, delimiter);
		if (arr != null) {
			list = Arrays.asList(arr);
		}
		logger.debug("Converted str: " + str + ", to list: " + list);
		return list;
	}

	public static String convertToString(List<String> list, String delimiter) {
		StringBuilder sb = new StringBuilder();
		if (null == list || list.size() <= 0) {
			logger.debug("Failed to convert list: " + list + ", to string: "
					+ sb.toString());
			return sb.toString();
		}
		for (String cos : list) {
			sb.append(cos).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		logger.debug("Converted list: " + list + ", to string: "
				+ sb.toString());
		return sb.toString();
	}

	public static Set<String> intersection(List<String> list1,
			List<String> list2) {
		Set<String> set = new HashSet<String>();
		if (list1 == null || list2 == null) {
			logger.debug("Failed return intersection of list1: " + list1
					+ ", list2: " + list2 + ". returns empty set");
			return set;
		}
		set.addAll(list1);
		set.retainAll(list2);
		logger.debug("Return intersection of list1: " + list1 + ", list2: "
				+ list2 + ", set: " + set);
		return set;
	}

	public static List<String> removeCommonInLHS(List<String> list1,
			List<String> list2) {
		List<String> list = new ArrayList<String>();
		if (list1 == null || list2 == null) {
			logger.debug("Failed removeCommonInLHS of list1: " + list1
					+ ", list2: " + list2 + ". returns empty list");
			return list;
		}
		list.addAll(list1);
		list.removeAll(list2);
		logger.debug("Return removeCommonInLHS of list1: " + list1
				+ ", list2: " + list2 + ", resulting list: " + list);

		return list;
	}

	public static void main(String[] args) {
		List<String> list = null;
		new ArrayList<String>();
		List<String> list1 = new ArrayList<String>();
		list1.add("1");
		list1.add("2");
		list1.add("2");
		List<String> list2 = new ArrayList<String>();
		list2.add("1");
		list2.add("3");
		list2.add("1");

		// removeCommonInLHS(list1, list2);

		// System.out.println("string: " + convertToString(list2, ","));
		// System.out.println("intersection: " + intersection(list1, list2));
		System.out.println("removeCommonInLHS: "
				+ removeCommonInLHS(list1, list2));
	}

}
