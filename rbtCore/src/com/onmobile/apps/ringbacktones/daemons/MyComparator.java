package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.util.Comparator;

public class MyComparator implements Comparator{

	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		File f1=(File)o1;
		File f2=(File)o2;
		Long temp1=new Long(f2.lastModified());
		Long temp2=new Long(f1.lastModified());
		return(temp1.compareTo(temp2));
	}

}
