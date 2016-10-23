/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.smcallback;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.StringUtil;

/**
 * @author vinayasimha.patil
 */
public class SMCallbackContext
{
	private static Logger logger = Logger.getLogger(SMCallbackContext.class);

	private String action = null;
	private String amountCharged = null;
	private String chargeKeyword = null;
	private String cosid = null;
	private String curStatus = null;
	private String cycleResponse = null;
	private String date = null;
	private String eventkey = null;
	private String info = null;
	private String mode = null;
	private String msisdn = null;
	private String offerid = null;
	private String probeResult = null;
	private String reason = null;
	private String reasonCode = null;
	private String refid = null;
	private String reqtype = null;
	private String rtkey = null;
	private String sbnID = null;
	private String srvkey = null;
	private String status = null;
	private String time = null;
	private String transId = null;
	private String type = null;
	private String siteId = null;

	private boolean isActivationCallback = false;
	private boolean isSelectionCallback = false;
	private boolean isPackSelectionCallback = false;


	/**
	 * 
	 */
	public SMCallbackContext()
	{

	}

	/**
	 * @return the action
	 */
	public String getAction()
	{
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action)
	{
		this.action = action;
	}

	/**
	 * @return the amountCharged
	 */
	public String getAmountCharged()
	{
		return amountCharged;
	}

	/**
	 * @param amountCharged
	 *            the amountCharged to set
	 */
	public void setAmountCharged(String amountCharged)
	{
		this.amountCharged = amountCharged;
	}

	/**
	 * @return the chargeKeyword
	 */
	public String getChargeKeyword()
	{
		return chargeKeyword;
	}

	/**
	 * @param chargeKeyword
	 *            the chargeKeyword to set
	 */
	public void setChargeKeyword(String chargeKeyword)
	{
		if (chargeKeyword != null)
		{
			chargeKeyword = chargeKeyword.replace("RBT_ACT_", "");
			chargeKeyword = chargeKeyword.replace("RBT_SEL_", "");
			chargeKeyword = chargeKeyword.replace("RBT_SET_", "");
			chargeKeyword = chargeKeyword.replace("RBT_PACK_", "");

			if (RBTDeploymentFinder.isRRBTSystem())
				chargeKeyword = chargeKeyword.replace("_RRBT", "");
			else if (RBTDeploymentFinder.isPRECALLSystem())
				chargeKeyword = chargeKeyword.replace("_PRECALL", "");
			else if (RBTDeploymentFinder.isBGMSystem())
				chargeKeyword = chargeKeyword.replace("_BGM", "");
		}

		this.chargeKeyword = chargeKeyword;
	}

	/**
	 * @return the cosid
	 */
	public String getCosid()
	{
		return cosid;
	}

	/**
	 * @param cosid
	 *            the cosid to set
	 */
	public void setCosid(String cosid)
	{
		this.cosid = cosid;
	}

	/**
	 * @return the curStatus
	 */
	public String getCurStatus()
	{
		return curStatus;
	}

	/**
	 * @param curStatus
	 *            the curStatus to set
	 */
	public void setCurStatus(String curStatus)
	{
		this.curStatus = curStatus;
	}

	/**
	 * @return the cycleResponse
	 */
	public String getCycleResponse()
	{
		return cycleResponse;
	}

	/**
	 * @param cycleResponse
	 *            the cycleResponse to set
	 */
	public void setCycleResponse(String cycleResponse)
	{
		this.cycleResponse = cycleResponse;
	}

	/**
	 * @return the date
	 */
	public String getDate()
	{
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date)
	{
		this.date = date;
	}

	/**
	 * @return the eventkey
	 */
	public String getEventkey()
	{
		return eventkey;
	}

	/**
	 * @param eventkey
	 *            the eventkey to set
	 */
	public void setEventkey(String eventkey)
	{
		this.eventkey = eventkey;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	/**
	 * @return the mode
	 */
	public String getMode()
	{
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode)
	{
		this.mode = mode;
	}

	/**
	 * @return the msisdn
	 */
	public String getMsisdn()
	{
		return msisdn;
	}

	/**
	 * @param msisdn
	 *            the msisdn to set
	 */
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	/**
	 * @return the offerid
	 */
	public String getOfferid()
	{
		return offerid;
	}

	/**
	 * @param offerid
	 *            the offerid to set
	 */
	public void setOfferid(String offerid)
	{
		this.offerid = offerid;
	}

	/**
	 * @return the probeResult
	 */
	public String getProbeResult()
	{
		return probeResult;
	}

	/**
	 * @param probeResult
	 *            the probeResult to set
	 */
	public void setProbeResult(String probeResult)
	{
		this.probeResult = probeResult;
	}

	/**
	 * @return the reason
	 */
	public String getReason()
	{
		return reason;
	}

	/**
	 * @param reason
	 *            the reason to set
	 */
	public void setReason(String reason)
	{
		this.reason = reason;
	}

	/**
	 * @return the reasonCode
	 */
	public String getReasonCode()
	{
		return reasonCode;
	}

	/**
	 * @param reasonCode
	 *            the reasonCode to set
	 */
	public void setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
	}

	/**
	 * @return the refid
	 */
	public String getRefid()
	{
		return refid;
	}

	/**
	 * @param refid
	 *            the refid to set
	 */
	public void setRefid(String refid)
	{
		this.refid = refid;
	}

	/**
	 * @return the reqtype
	 */
	public String getReqtype()
	{
		return reqtype;
	}

	/**
	 * @param reqtype
	 *            the reqtype to set
	 */
	public void setReqtype(String reqtype)
	{
		this.reqtype = reqtype;
	}

	/**
	 * @return the rtkey
	 */
	public String getRtkey()
	{
		return rtkey;
	}

	/**
	 * @param rtkey
	 *            the rtkey to set
	 */
	public void setRtkey(String rtkey)
	{
		this.rtkey = rtkey;
	}

	/**
	 * @return the sbnID
	 */
	public String getSbnID()
	{
		return sbnID;
	}

	/**
	 * @param sbnID
	 *            the sbnID to set
	 */
	public void setSbnID(String sbnID)
	{
		this.sbnID = sbnID;
	}

	/**
	 * @return the srvkey
	 */
	public String getSrvkey()
	{
		return srvkey;
	}

	public void setSiteId(String siteId) {
		if (siteId != null) {
			this.siteId = siteId;
		}
	}
	
	/**
	 * @param srvkey
	 *            the srvkey to set
	 */
	public void setSrvkey(String srvkey)
	{
		if (srvkey != null)
		{
			if (srvkey.startsWith("RBT_ACT_"))
			{
				isActivationCallback = true;
				srvkey = srvkey.replace("RBT_ACT_", "");
			}
			else if (srvkey.startsWith("RBT_SEL_") || srvkey.startsWith("RBT_SET_"))
			{
				isSelectionCallback = true;
				srvkey = srvkey.replace("RBT_SEL_", "");
				srvkey = srvkey.replace("RBT_SET_", "");
			}
			else if (srvkey.startsWith("RBT_PACK_"))
			{
				isPackSelectionCallback = true;
				srvkey = srvkey.replace("RBT_PACK_", "");
			}

			if (RBTDeploymentFinder.isRRBTSystem())
				srvkey = srvkey.replace("_RRBT", "");
			else if (RBTDeploymentFinder.isPRECALLSystem())
				srvkey = srvkey.replace("_PRECALL", "");
			else if (RBTDeploymentFinder.isBGMSystem())
				srvkey = srvkey.replace("_BGM", "");
		}

		this.srvkey = srvkey;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @return the time
	 */
	public String getTime()
	{
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time)
	{
		this.time = time;
	}

	/**
	 * @return the transId
	 */
	public String getTransId()
	{
		return transId;
	}

	/**
	 * @param transId
	 *            the transId to set
	 */
	public void setTransId(String transId)
	{
		this.transId = transId;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the isActivationCallback
	 */
	public boolean isActivationCallback()
	{
		return isActivationCallback;
	}

	/**
	 * @return the isSelectionCallback
	 */
	public boolean isSelectionCallback()
	{
		return isSelectionCallback;
	}

	public static SMCallbackContext buildSMCallbackContext(
			Map<String, String[]> parametersMap)
	{
		SMCallbackContext smCallbackContext = new SMCallbackContext();

		Set<Entry<String, String[]>> entries = parametersMap.entrySet();
		for (Entry<String, String[]> entry : entries)
		{
			String paramName = entry.getKey();
			String paramValue = entry.getValue()[0];

			try
			{
				Method method = SMCallbackContext.class.getDeclaredMethod("set"
						+ StringUtil.toCamelCase(paramName), String.class);
				method.invoke(smCallbackContext, paramValue);
			}
			catch (Exception e)
			{
				logger.error("Error while populating the '" + paramName
						+ "' field. " + e.getMessage());
			}
		}

		logger.info("smCallbackContext: " + smCallbackContext);
		return smCallbackContext;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SMCallbackContext [action=");
		builder.append(action);
		builder.append(", amountCharged=");
		builder.append(amountCharged);
		builder.append(", chargeKeyword=");
		builder.append(chargeKeyword);
		builder.append(", cosid=");
		builder.append(cosid);
		builder.append(", curStatus=");
		builder.append(curStatus);
		builder.append(", cycleResponse=");
		builder.append(cycleResponse);
		builder.append(", date=");
		builder.append(date);
		builder.append(", eventkey=");
		builder.append(eventkey);
		builder.append(", info=");
		builder.append(info);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", msisdn=");
		builder.append(msisdn);
		builder.append(", offerid=");
		builder.append(offerid);
		builder.append(", probeResult=");
		builder.append(probeResult);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", reasonCode=");
		builder.append(reasonCode);
		builder.append(", refid=");
		builder.append(refid);
		builder.append(", reqtype=");
		builder.append(reqtype);
		builder.append(", rtkey=");
		builder.append(rtkey);
		builder.append(", sbnID=");
		builder.append(sbnID);
		builder.append(", srvkey=");
		builder.append(srvkey);
		builder.append(", status=");
		builder.append(status);
		builder.append(", time=");
		builder.append(time);
		builder.append(", transId=");
		builder.append(transId);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}
}
