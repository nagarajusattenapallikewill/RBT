package com.onmobile.android.utils.comparator;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.ExtendedGroupBean;

public class ExtendedGroupBeanNameComparator implements Comparator<ExtendedGroupBean> {

	private static Collator collator = Collator.getInstance(ComparatorUtility.getDefaultLocale());
	private static Logger logger = Logger.getLogger(ExtendedGroupBeanNameComparator.class);

	static {
		Locale locale = ComparatorUtility.getLocaleFromConfig();
		if (ComparatorUtility.isLocaleValid(locale)) {
			collator = Collator.getInstance(locale);
		} else {
			logger.error("Invalid locale. Default locale will be set.");
		}
	}

	@Override
	public int compare(ExtendedGroupBean first, ExtendedGroupBean second) {
		return collator.compare(first.getGroupName(), second.getGroupName());
	}
}
