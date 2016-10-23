package com.onmobile.apps.ringbacktones.dncto.rules;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.onmobile.apps.ringbacktones.dncto.DNCTOContext;
import com.onmobile.dnctoservice.exception.DNCTOException;
import com.onmobile.dnctoservice.plugin.util.DNCTOChannel;
import com.onmobile.dnctoservice.plugin.util.DNCTOPluginUtil;

public class VFSRBTRule implements Rule {

	@RuleAttribute
	String period = null;
	
	@RuleAttribute
	String repeatCount = null;
	
	@RuleAttribute
	String reason = null;
	
	/**
	 * Holds the period start time in milliseconds.
	 */
	private long periodStartTime = 0;

	@Override
	public boolean applyRule(DNCTOContext dnctoContext) throws DNCTOException {


		String line = null;
		if (dnctoContext.getLine() != null) {
			line = dnctoContext.getLine();
		}

		List<DNCTOChannel> dnctoChannels =DNCTOPluginUtil
				.listOfDeliverableChannel();

		for (DNCTOChannel dnctoChannel : dnctoChannels) {
			List<Long> failureDateList = DNCTOPluginUtil
					.getFailureContactListOfMDN(line, dnctoChannel);
			List<Long> successDateList = DNCTOPluginUtil
					.getSuccessContactListOfMDN(line, dnctoChannel);

			if (successDateList != null || failureDateList != null) {
				Long latestSuccessDate = null ;
				Long latestFailureDate =null;
				if (successDateList != null && successDateList.size() > 0) {
					Collections.sort(successDateList);
					latestSuccessDate = successDateList.get(successDateList.size() - 1);
				}
				if(failureDateList!=null && failureDateList.size()>0) {	
					Collections.sort(failureDateList);
					latestFailureDate = failureDateList.get(failureDateList.size() - 1);
				}
				
				if(successDateList!=null && successDateList.size()>0 && (failureDateList==null || failureDateList.size()==0)){
					
					if(latestSuccessDate >= periodStartTime) {
						dnctoContext.setReason(getReason());
						return false;
					}
					
					
					
				}else if(successDateList!=null && successDateList.size()>0 && failureDateList!=null && failureDateList.size()>0) {
					
					if (latestSuccessDate > latestFailureDate) {

						
						if(latestSuccessDate >= periodStartTime) {
							dnctoContext.setReason(getReason());
							return false;
						}

					} else {

							
						if(latestFailureDate >= periodStartTime) {	

							if (failureDateList.size() >= Integer
									.parseInt(repeatCount)) {
								
								
								int count = 0;
								for (int i = 1; i <= Integer.parseInt(repeatCount); i++) {

									if(failureDateList.get(failureDateList.size() - i) >= periodStartTime) {
										count++;
									}

								}
								if (count == Integer.parseInt(repeatCount)) {
									dnctoContext.setReason(getReason());
									return false;
								}
							}
						}
					}
				}
				else if(failureDateList!=null && failureDateList.size()>0 && (successDateList==null || successDateList.size()==0)){
					
					if(latestFailureDate >= periodStartTime) {
							
						if (failureDateList.size() >= Integer.parseInt(repeatCount)) {

							int count = 0;
							for (int i = 1; i <= Integer.parseInt(repeatCount); i++) {

								
								if(failureDateList.get(failureDateList.size() - i) >= periodStartTime) {
									count++;
								}

							}
							if (count == Integer.parseInt(repeatCount)) {
								dnctoContext.setReason(getReason());
								return false;
							}
						}
					}
				}

			}
		}
		return true;
	}

	/**
	 * @return the monthlyDuration
	 */
	public String getPeriod() {
		return period;
	}

	/**
	 * @param monthlyDuration the monthlyDuration to set
	 */
	public void setPeriod(String period) {
		this.period = period;
		periodStartTime = getPeriodStartTimestamp();
	}

	/**
	 * @return the repeatCount
	 */
	public String getRepeatCount() {
		return repeatCount;
	}

	/**
	 * @param repeatCount the repeatCount to set
	 */
	public void setRepeatCount(String repeatCount) {
		this.repeatCount = repeatCount;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	private long getPeriodStartTimestamp()
	{
		if (period == null)
			return 0;

		Calendar calendar = Calendar.getInstance();

		char ch = period.charAt(0);
		if (ch == 'D' || ch == 'd')
		{
			int noOfDays = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.DAY_OF_YEAR, -(noOfDays - 1));
		}
		else if (ch == 'M' || ch == 'm')
		{
			int noOfMonths = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.MONTH, -(noOfMonths - 1));
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		}
		else if (ch == 'Y' || ch == 'y')
		{
			int noOfYears = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.YEAR, -(noOfYears - 1));
			calendar.set(Calendar.DAY_OF_YEAR, 1);
		}
		else if (Character.isLetter(ch))
		{
			// If the first character is invalid, then it will be trimmed and
			// considered as Number Of Days.
			int noOfDays = Integer.parseInt(period.substring(1));
			calendar.add(Calendar.DAY_OF_YEAR, -(noOfDays - 1));
		}
		else
		{
			int noOfDays = Integer.parseInt(period);
			calendar.add(Calendar.DAY_OF_YEAR, -(noOfDays - 1));
		}

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis();
	}
	
}
