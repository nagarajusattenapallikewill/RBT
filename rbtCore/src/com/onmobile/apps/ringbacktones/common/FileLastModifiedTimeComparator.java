package com.onmobile.apps.ringbacktones.common;

import java.io.File;
import java.util.Comparator;

public class FileLastModifiedTimeComparator implements Comparator<File>
{

	public int compare(File o1, File o2)
	{
		if(o1.lastModified() > o2.lastModified())
			return -1;
		else if(o1.lastModified() < o2.lastModified())
			return 1;
		return 0;
	}

	
}
