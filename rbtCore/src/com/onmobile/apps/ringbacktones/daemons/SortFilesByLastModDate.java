package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;



public class SortFilesByLastModDate implements Comparable
{
	public File file;

	public  SortFilesByLastModDate(File file)
	{
		this.file=file;
	}
	public int compareTo(Object a)
	{																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											
		SortFilesByLastModDate b=(SortFilesByLastModDate)a;
		Long temp1=new Long(this.file.lastModified());
		Long temp2=new Long(b.file.lastModified());
		return(temp1.compareTo(temp2));
	}
}
