package com.onmobile.apps.ringbacktones.genericcache;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;

public class ComparatorUtil implements Comparator<BulkPromoSMS>
{
	private int sortBy = 0;
	private boolean isAscending = false;

	public ComparatorUtil()
	{

	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(BulkPromoSMS obj1, BulkPromoSMS obj2)
	{
		int compareValue = 0;
		switch (sortBy)
		{
			case 0:
				compareValue = obj1.getBulkpromoID().compareToIgnoreCase(obj2.getBulkpromoID());
				break;
			case 1:
				// compareValue = tb1.getId() - tb2.getId();
				break;
			case 2:
				return 2;
			case 3:
				return 2;
			case 4:
				return 2;
		}

		if (!isAscending)
			compareValue *= -1;

		return compareValue;
	}

	public List<BulkPromoSMS> sortByPromoID(List<BulkPromoSMS> bulkPromoSMSList, boolean isAscending)
	{
		this.isAscending = isAscending;
		sortBy = 0;
		Collections.sort(bulkPromoSMSList, this);

		return bulkPromoSMSList;
	}
}
