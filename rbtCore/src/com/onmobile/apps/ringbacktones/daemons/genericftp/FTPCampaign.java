package com.onmobile.apps.ringbacktones.daemons.genericftp;

import java.util.List;

import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.BaseConfig;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.FTPConfig;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.SelectionConfig;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.SubClassMap;

/**
 * @author sridhar.sindiri
 *
 */
public class FTPCampaign
{
	private FTPConfig ftpConfig;
	private String delimiter;
	private String format;
	private String outputRequired;
	private String moveToFolder;

	private BaseConfig baseConfig;
	private SelectionConfig selConfig;
	private List<SubClassMap> subClassMappingList;
	private List<ChargeClassMap> chargeClassMappingList;

	private long sleepTime;
	private String retailerSmsText;
	private String senderID;
	private String retailerID;
	private String retailerSmsTextPerMsisdn;

	/**
	 * @return the ftpConfig
	 */
	public FTPConfig getFtpConfig() {
		return ftpConfig;
	}

	/**
	 * @param ftpConfig the ftpConfig to set
	 */
	public void setFtpConfig(FTPConfig config) {
		this.ftpConfig = config;
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the outputRequired
	 */
	public String getOutputRequired() {
		return outputRequired;
	}

	/**
	 * @param outputRequired the outputRequired to set
	 */
	public void setOutputRequired(String outputRequired) {
		this.outputRequired = outputRequired;
	}

	/**
	 * @return the moveToFolder
	 */
	public String getMoveToFolder() {
		return moveToFolder;
	}

	/**
	 * @param moveToFolder the moveToFolder to set
	 */
	public void setMoveToFolder(String moveToFolder) {
		this.moveToFolder = moveToFolder;
	}

	/**
	 * @return the baseConfig
	 */
	public BaseConfig getBaseConfig() {
		return baseConfig;
	}

	/**
	 * @param baseConfig the baseConfig to set
	 */
	public void setBaseConfig(BaseConfig baseConfig) {
		this.baseConfig = baseConfig;
	}

	/**
	 * @return the selConfig
	 */
	public SelectionConfig getSelConfig() {
		return selConfig;
	}

	/**
	 * @param selConfig the selConfig to set
	 */
	public void setSelConfig(SelectionConfig selConfig) {
		this.selConfig = selConfig;
	}

	/**
	 * @return the subClassMappingList
	 */
	public List<SubClassMap> getSubClassMappingList() {
		return subClassMappingList;
	}

	/**
	 * @param subClassMappingList the subClassMappingList to set
	 */
	public void setSubClassMappingList(List<SubClassMap> subClassMappingList) {
		this.subClassMappingList = subClassMappingList;
	}

	/**
	 * @return the chargeClassMappingList
	 */
	public List<ChargeClassMap> getChargeClassMappingList() {
		return chargeClassMappingList;
	}

	/**
	 * @param chargeClassMappingList the chargeClassMappingList to set
	 */
	public void setChargeClassMappingList(
			List<ChargeClassMap> chargeClassMappingList) {
		this.chargeClassMappingList = chargeClassMappingList;
	}

	/**
	 * @return the sleepTime
	 */
	public long getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param sleepTime the sleepTime to set
	 */
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * @return the retailerSmsText
	 */
	public String getRetailerSmsText() {
		return retailerSmsText;
	}

	/**
	 * @param retailerSmsText the retailerSmsText to set
	 */
	public void setRetailerSmsText(String retailerSmsText) {
		this.retailerSmsText = retailerSmsText;
	}

	/**
	 * @return the senderID
	 */
	public String getSenderID() {
		return senderID;
	}

	/**
	 * @param senderID the senderID to set
	 */
	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}

	/**
	 * @return the retailerID
	 */
	public String getRetailerID() {
		return retailerID;
	}

	/**
	 * @param retailerID the retailerID to set
	 */
	public void setRetailerID(String retailerID) {
		this.retailerID = retailerID;
	}

	/**
	 * @return the retailerSmsTextPerMsisdn
	 */
	public String getRetailerSmsTextPerMsisdn() {
		return retailerSmsTextPerMsisdn;
	}

	/**
	 * @param retailerSmsTextPerMsisdn the retailerSmsTextPerMsisdn to set
	 */
	public void setRetailerSmsTextPerMsisdn(String retailerSmsTextPerMsisdn) {
		this.retailerSmsTextPerMsisdn = retailerSmsTextPerMsisdn;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("FTPCampaign [ftpConfig = ");
		builder.append(ftpConfig);
		builder.append(", delimiter = ");
		builder.append(delimiter);
		builder.append(", format = ");
		builder.append(format);
		builder.append(", outputRequired = ");
		builder.append(outputRequired);
		builder.append(", moveToFolder = ");
		builder.append(moveToFolder);
		builder.append(", baseConfig = ");
		builder.append(baseConfig);
		builder.append(", selConfig = ");
		builder.append(selConfig);
		builder.append(", subClassMappingList = ");
		builder.append(subClassMappingList);
		builder.append(", chargeClassMappingList = ");
		builder.append(chargeClassMappingList);
		builder.append(", sleepTime = ");
		builder.append(sleepTime);
		builder.append(", retailerSmsText = ");
		builder.append(retailerSmsText);
		builder.append(", senderID = ");
		builder.append(senderID);
		builder.append(", retailerID = ");
		builder.append(retailerID);
		builder.append(", retailerSmsTextPerMsisdn = ");
		builder.append(retailerSmsTextPerMsisdn);
		builder.append("] ");

		return builder.toString();
	}
}
