package com.onmobile.android.utils.comparator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.ExtendedSelectionBean;

/**
 * @author sridhar.sindiri
 *
 */
public class SelectionEndTimeComparator implements Comparator<ExtendedSelectionBean>
{
	private static DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ExtendedSelectionBean sel1,
			ExtendedSelectionBean sel2)
	{
		try {
			Date sel1EndTime = formatter.parse(sel1.getSelectionEndTime());
			Date sel2EndTime = formatter.parse(sel2.getSelectionEndTime());

			if (sel1EndTime.before(sel2EndTime))
				return 1;
			else if (sel1EndTime.after(sel2EndTime))
				return -1;

		} catch (ParseException e) {
			Logger.getLogger(SelectionEndTimeComparator.class).error(e.getMessage(), e);
		}
		return 0;
	}

}
