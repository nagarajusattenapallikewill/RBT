package com.onmobile.apps.ringbacktones.callLog.beans;

import java.util.List;

public class CallLogHistoryBean {

	private int count;
	private List<CallLog> callLog;

	public CallLogHistoryBean() {

	}

	public CallLogHistoryBean(int count, List<CallLog> callLog) {
		this.count = count;
		this.callLog = callLog;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<CallLog> getCallLog() {
		return callLog;
	}

	public void setCallLog(List<CallLog> callLog) {
		this.callLog = callLog;
	}

	@Override
	public String toString() {
		return "CallLogHistoryBean [count=" + count + ", callLog=" + callLog
				+ "]";
	}

}
