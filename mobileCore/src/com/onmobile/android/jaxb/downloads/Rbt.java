//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.01 at 12:55:44 PM IST 
//

package com.onmobile.android.jaxb.downloads;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}downloads"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "downloads" })
@XmlRootElement(name = "rbt")
public class Rbt {

	@XmlElement(required = true)
	protected Downloads downloads;

	/**
	 * Gets the value of the downloads property.
	 * 
	 * @return possible object is {@link Downloads }
	 * 
	 */
	public Downloads getDownloads() {
		return downloads;
	}

	/**
	 * Sets the value of the downloads property.
	 * 
	 * @param value
	 *            allowed object is {@link Downloads }
	 * 
	 */
	public void setDownloads(Downloads value) {
		this.downloads = value;
	}

}
