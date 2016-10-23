/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import org.apache.log4j.PatternLayout;

/**
 * @author vinayasimha.patil
 * 
 */
public class PatternLayoutWithHeader extends PatternLayout
{
	private String header = null;

	/**
	 * @return the header
	 */
	
	//Added constructors for TTG-14814 to get the layout along with header
	public PatternLayoutWithHeader(String patternLayout) {
		super(patternLayout);
	}

	public PatternLayoutWithHeader() {
		super();
	}
	//End of TTG-14814
	
	@Override
	public String getHeader()
	{
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(String header)
	{
		this.header = header + LINE_SEP;
	}
}
