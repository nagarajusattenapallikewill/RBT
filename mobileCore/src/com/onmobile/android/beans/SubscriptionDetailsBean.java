package com.onmobile.android.beans;

public class SubscriptionDetailsBean {
	String amount;
	String subscriptionClass;
	String period;
	
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getSubscriptionClass() {
		return subscriptionClass;
	}
	public void setSubscriptionClass(String subscriptionClass) {
		this.subscriptionClass = subscriptionClass;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SelectionDetailsBean [amount=");
		builder.append(amount);
		builder.append(", subscriptionClass=");
		builder.append(subscriptionClass);
		builder.append(", period=");
		builder.append(period);
		builder.append("]");
		return builder.toString();
	}
}
