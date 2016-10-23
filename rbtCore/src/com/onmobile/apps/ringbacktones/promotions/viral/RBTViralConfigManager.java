package com.onmobile.apps.ringbacktones.promotions.viral;

import java.util.ResourceBundle;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTViralConfigManager 
{
	private ResourceBundle rb = null;
	private static RBTViralConfigManager instance = new RBTViralConfigManager();

	private RBTViralConfigManager() {
		rb = ResourceBundle.getBundle("rbtviralconfig");
	}

	public static RBTViralConfigManager getInstance() {
		return instance;
	}

	public String getParameter(String key) {
		return rb.getString(key);
	}
}

