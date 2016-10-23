package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

import java.io.File;

import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;

public class InterOperatorCopyThread implements Runnable {
	
	File file=null;
	
	public InterOperatorCopyThread(File xmlFile)
	{
		file=xmlFile;
	}
	
	
	public void run()
	{
		InterOperatorUtility.processXmlFile(file,"HTTP");
	}

}
