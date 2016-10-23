package com.onmobile.android.beans;

public class SelectionDetailsBean {
	String amount;
	String chargeClass;
	String period;
	String periodDescription;
	
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getChargeClass() {
		return chargeClass;
	}
	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getPeriodDescription() {
		return periodDescription;
	}
	public void setPeriodDescription(String periodDescription) {
		this.periodDescription = periodDescription;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SelectionDetailsBean [amount=");
		builder.append(amount);
		builder.append(", chargeClass=");
		builder.append(chargeClass);
		builder.append(", period=");
		builder.append(period);
		builder.append(", periodDescription=");
		builder.append(periodDescription);
		builder.append("]");
		return builder.toString();
	}
}
