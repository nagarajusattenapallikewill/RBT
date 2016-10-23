package com.onmobile.apps.ringbacktones.subscriptions;

import com.onmobile.apps.ringbacktones.content.*;
import java.util.Comparator;

public class CategoriesComparator implements Comparator
{

	public int compare(Object obj1, Object obj2)
	{
		if(obj1 == null || obj2 == null)
			return -1;

		Categories cat1 = (Categories)obj1;
		Categories cat2 = (Categories)obj2;

		String catName1 = cat1.name();
		String catName2 = cat2.name();

		if(catName1 == null || catName2 == null)
			return -1;

		return catName1.compareToIgnoreCase(catName2);
		
	}

}