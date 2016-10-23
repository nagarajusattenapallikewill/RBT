package com.onmobile.apps.ringbacktones.daemons.genericftp.beans;

/**
 * @author sridhar.sindiri
 *
 */
public class SelectionConfig
{
	private String chargeClass;
	private String selectedBy;
	private String contentId;
	private String contentIdType;
	private String categoryId;
	private String inLoop;
	private String accept;

	/**
	 * @return the chargeClass
	 */
	public String getChargeClass() {
		return chargeClass;
	}

	/**
	 * @param chargeClass the chargeClass to set
	 */
	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}

	/**
	 * @return the selectedBy
	 */
	public String getSelectedBy() {
		return selectedBy;
	}

	/**
	 * @param selectedBy the selectedBy to set
	 */
	public void setSelectedBy(String selectedBy) {
		this.selectedBy = selectedBy;
	}

	/**
	 * @return the contentId
	 */
	public String getContentId() {
		return contentId;
	}

	/**
	 * @param contentId the contentId to set
	 */
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	/**
	 * @return the contentIdType
	 */
	public String getContentIdType() {
		return contentIdType;
	}

	/**
	 * @param contentIdType the contentIdType to set
	 */
	public void setContentIdType(String contentIdType) {
		this.contentIdType = contentIdType;
	}

	/**
	 * @return the categoryId
	 */
	public String getCategoryId() {
		return categoryId;
	}

	/**
	 * @param categoryId the categoryId to set
	 */
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * @return the inLoop
	 */
	public String getInLoop() {
		return inLoop;
	}

	/**
	 * @param inLoop the inLoop to set
	 */
	public void setInLoop(String inLoop) {
		this.inLoop = inLoop;
	}

	/**
	 * @return the accept
	 */
	public String getAccept() {
		return accept;
	}

	/**
	 * @param accept the accept to set
	 */
	public void setAccept(String accept) {
		this.accept = accept;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SelectionConfig [chargeClass = ");
		builder.append(chargeClass);
		builder.append(", selectedBy = ");
		builder.append(selectedBy);
		builder.append(", contentId = ");
		builder.append(contentId);
		builder.append(", contentIdType = ");
		builder.append(contentIdType);
		builder.append(", categoryId = ");
		builder.append(categoryId);
		builder.append(", inLoop = ");
		builder.append(inLoop);
		builder.append(", accept = ");
		builder.append(accept);
		builder.append("] ");

		return builder.toString();
	}
}
