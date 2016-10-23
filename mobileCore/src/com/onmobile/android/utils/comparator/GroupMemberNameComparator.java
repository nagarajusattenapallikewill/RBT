package com.onmobile.android.utils.comparator;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;

public class GroupMemberNameComparator implements Comparator<GroupMember> {

	private static Collator collator = Collator.getInstance(ComparatorUtility.getDefaultLocale());
	private static Logger logger = Logger.getLogger(GroupMemberNameComparator.class);

	static {
		Locale locale = ComparatorUtility.getLocaleFromConfig();
		if (ComparatorUtility.isLocaleValid(locale)) {
			collator = Collator.getInstance(locale);
		} else {
			logger.error("Invalid locale. Default locale will be set.");
		}
	}

	@Override
	public int compare(GroupMember first, GroupMember second) {
		return collator.compare(first.getMemberName(), second.getMemberName());
	}
}
