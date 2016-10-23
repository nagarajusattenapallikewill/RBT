/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.io.Serializable;

/**
 * RBTText bean used by hibernate to persist the RBT_TEXT table.
 * 
 * @author vinayasimha.patil
 *
 */
public class RBTText implements Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7293668588678593634L;

	private String type;
	private String subType;
	private String language;
	private String text;

	/**
	 * 
	 */
	public RBTText()
	{

	}

	/**
	 * @param type
	 * @param subType
	 * @param language
	 * @param text
	 */
	public RBTText(String type, String subType, String language, String text)
	{
		this.type = type;
		this.subType = subType;
		this.language = language;
		this.text = text;
	}

	/**
	 * @return the serialVersionUID
	 */
	public static long getSerialversionUID()
	{
		return serialVersionUID;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the subType
	 */
	public String getSubType()
	{
		return subType;
	}

	/**
	 * @param subType the subType to set
	 */
	public void setSubType(String subType)
	{
		this.subType = subType;
	}

	/**
	 * @return the language
	 */
	public String getLanguage()
	{
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * @return the text
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public RBTText clone() throws CloneNotSupportedException
	{
		return (RBTText) super.clone();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("RBTText[language = ");
		builder.append(language);
		builder.append(", subType = ");
		builder.append(subType);
		builder.append(", text = ");
		builder.append(text);
		builder.append(", type = ");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
}
