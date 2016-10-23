package com.onmobile.apps.ringbacktones.lucene.generic.msearch.utility;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringUtility {

	private static ApplicationContext context;

	public static ApplicationContext getApplicationContext() {
		if (context == null) {
			synchronized (SpringUtility.class) {
				if (context == null)
					context = new ClassPathXmlApplicationContext("genericMsearchSupport.xml");
			}
		}
		return context;
	}
}
