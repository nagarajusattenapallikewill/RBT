package com.onmobile.apps.ringbacktones.tools;

import com.onmobile.apps.ringbacktones.common.ResourceReader;

public class XMLConfigTools
{
	 public static String getDBSelectionString()
	 {
		 return ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
	 }
}
