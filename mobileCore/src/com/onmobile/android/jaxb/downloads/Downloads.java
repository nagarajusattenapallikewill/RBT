//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.01 at 12:55:44 PM IST 
//

package com.onmobile.android.jaxb.downloads;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}clips"/>
 *       &lt;/sequence>
 *       &lt;attribute name="noOfActiveDownloads" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="noOfDownloads" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "clips" })
@XmlRootElement(name = "downloads")
public class Downloads {

	@XmlElement(required = true)
	protected Clips clips;
	@XmlAttribute(required = true)
	protected BigInteger noOfActiveDownloads;
	@XmlAttribute(required = true)
	protected BigInteger noOfDownloads;

	/**
	 * Gets the value of the clips property.
	 * 
	 * @return possible object is {@link Clips }
	 * 
	 */
	public Clips getClips() {
		return clips;
	}

	/**
	 * Sets the value of the clips property.
	 * 
	 * @param value
	 *            allowed object is {@link Clips }
	 * 
	 */
	public void setClips(Clips value) {
		this.clips = value;
	}

	/**
	 * Gets the value of the noOfActiveDownloads property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getNoOfActiveDownloads() {
		return noOfActiveDownloads;
	}

	/**
	 * Sets the value of the noOfActiveDownloads property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setNoOfActiveDownloads(BigInteger value) {
		this.noOfActiveDownloads = value;
	}

	/**
	 * Gets the value of the noOfDownloads property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getNoOfDownloads() {
		return noOfDownloads;
	}

	/**
	 * Sets the value of the noOfDownloads property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setNoOfDownloads(BigInteger value) {
		this.noOfDownloads = value;
	}

}