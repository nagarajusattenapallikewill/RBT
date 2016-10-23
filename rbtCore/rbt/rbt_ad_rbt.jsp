<%@page session="false"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,
com.onmobile.apps.ringbacktones.common.iRBTConstant,com.onmobile.apps.ringbacktones.common.Tools,com.onmobile.common.exception.OnMobileException,
com.onmobile.apps.ringbacktones.content.SubscriberStatus,com.onmobile.apps.ringbacktones.content.Clips,com.onmobile.apps.ringbacktones.content.Categories,
com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber,
com.onmobile.apps.ringbacktones.provisioning.AdminFacade,com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%><%@ page import = "java.util.*,java.text.*"%><%Subscriber subscriber;
String  strSubID, strClip, strSubType, strRequest, strTransID, strSubClass, actBy, selectedBy, strCategory, redirectNational;
int ad_rbt_category_id = 35;
String X_ONMOBILE_REASON, SUCCESS, FAILURE, ALREADY_ACTIVE_ON_RBT, ALREADY_ACTIVE_ON_ADRBT, NOT_ACTIVE_ON_ADRBT, NOT_ACTIVE_ON_RBT,DEACT_PENDING;
boolean bPrepaid, bActivate, bDeActivate;
subscriber = null;
actBy = "ADRBT";
selectedBy = "ADRBT";
String strIP  = request.getRemoteAddr();
String strName  = request.getRemoteHost();
String actInfo = strIP + ":ADRBT"; 
String circleID  = null;
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
RBTMOHelper rbtMO = RBTMOHelper.init();
System.out.println("RBT:REQUEST = " + request.getParameter("REQUEST") + ", MSISDN = " + request.getParameter("MSISDN") + " from " + strIP);
if (rbtLogin.isValidIP(strIP) || rbtLogin.isValidIP(strName))
{ 
	SUCCESS = "SUCCESS";
	FAILURE = "FAILURE";
	ALREADY_ACTIVE_ON_RBT = "Failure. This subscriber is active on RBT. Please deactivate the subscriber from RBT and try again."; 
    ALREADY_ACTIVE_ON_ADRBT = "Failure. This subscriber is already active on AD RBT.";
	NOT_ACTIVE_ON_ADRBT = "Failure. This subscriber is not active on AD RBT.";
	NOT_ACTIVE_ON_RBT = "Failure. This subscriber is not active on RBT.";
	DEACT_PENDING = "Failure. The deactivation request for this subscriber is still under processing. Try activating after some time.";
	bPrepaid = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").toUpperCase().startsWith("PRE");
	bActivate = false;
	bDeActivate = false;
	X_ONMOBILE_REASON = null;
	strSubID = request.getParameter("MSISDN");
	strRequest = request.getParameter("REQUEST");
	strSubType = request.getParameter("SUB_TYPE");
	if(strSubType != null )
	bPrepaid = strSubType.toUpperCase().startsWith("PRE");
	strTransID = request.getParameter("TRANSID");
	strCategory = request.getParameter("CATEGORY_ID");
	redirectNational = request.getParameter("REDIRECT_NATIONAL");
	if(strCategory != null)
	{
		try
		{
			 ad_rbt_category_id = Integer.parseInt(strCategory);
		}
		catch(Exception e)
		{
			ad_rbt_category_id = 35;
		}
	
	}
	if(strRequest != null && strRequest.toUpperCase().startsWith("ACT"))
		bActivate = true;
	else if(strRequest != null && strRequest.toUpperCase().startsWith("DEACT"))
		bDeActivate = true;
	String strRedirect = request.getParameter("REDIRECT_NATIONAL");
	System.out.println("RBT::SUB_TYPE ->" + strSubType  + " TRANSID -> " + strTransID +  " CATEGORY_ID -> " + strCategory );
	if(strSubID != null && (bActivate || bDeActivate))
	{
//		String valid = rbtLogin.isValidSub(strSubID.trim());
		subscriber = Processor.getSubscriber(strSubID,"CCC");
		strSubID = subscriber.getSubscriberID();
		circleID = 	subscriber.getCircleID();
		if(!subscriber.isValidPrefix() && strRedirect != null && (strRedirect.equalsIgnoreCase("TRUE") || strRedirect.equalsIgnoreCase("YES")))
		{
				X_ONMOBILE_REASON = rbtMO
				.connectToRemote(
								 strSubID,
								 "rbt_ad_rbt.jsp?MSISDN="
										 + strSubID
										 + "&REQUEST="
										 + strRequest
										 + "&SUB_TYPE="
										 + strSubType
										 + "&TRANSID="
										 + strTransID
										 + "&REDIRECT_NATIONAL="
										 + redirectNational
										 + "&CATEGORY_ID="
										 + strCategory,
								 true);
					
				if(X_ONMOBILE_REASON == null)
					X_ONMOBILE_REASON = "FAILURE";
		}
		else if(bActivate)
		{
			boolean isTransExist = false;
			if(strTransID != null)
			{
				isTransExist = rbtLogin.checkTransIDExist(strTransID,"ADRBT_ACT");
				if(!isTransExist)
				{
					rbtLogin.addTransData(strTransID, strSubID, "ADRBT_ACT");
				}
			}
			if (isTransExist)
				X_ONMOBILE_REASON = "Invalid Request. Already recieved a request with the same TransID : "
						+ strTransID
						+ " and subscriber ID : "
						+ strSubID;
			else
			{
				boolean isActive = Processor.isUserActive(subscriber.getStatus());
				boolean activeOnAdRbt = false;
				if(isActive)
				{
					if(subscriber.getActivatedBy().equalsIgnoreCase("ADRBT"))
						activeOnAdRbt = true;
				}
				if(isActive && activeOnAdRbt )
					X_ONMOBILE_REASON = ALREADY_ACTIVE_ON_ADRBT;
				else if(isActive && !activeOnAdRbt)
					X_ONMOBILE_REASON = ALREADY_ACTIVE_ON_RBT;
				else
				{
				if(strTransID != null)
					actInfo = actInfo + ":" + strTransID; 
				strSubClass = "DEFAULT";
				Clips[] allClips = rbtLogin.getActiveClips(ad_rbt_category_id);
				if(allClips != null && allClips[0] != null)
				{
					com.onmobile.apps.ringbacktones.content.Subscriber sub =  rbtLogin.activateSubscriber(strSubID, actBy, null, bPrepaid, 0, actInfo, strSubClass,circleID);
					if(sub != null)
					{
						Date endDate = null;
						String subYes = null;
						boolean OptIn = false;
						if(sub != null){
							subYes = sub.subYes();
							if(sub.activationInfo() != null && sub.activationInfo().indexOf(":optin:") != -1) 
                                OptIn = true; 
						}
						boolean success = rbtLogin.addPromoSelections(strSubID, null, bPrepaid, false, ad_rbt_category_id, allClips[0].wavFile(), endDate, 99, 0, selectedBy, actInfo, 0, 23, null, null, null , subYes, sub.maxSelections(), sub.subscriptionClass(),OptIn,sub);
						if(success)
						{
							rbtLogin.insertViralBlackList(strSubID,"TOTAL");
							X_ONMOBILE_REASON = SUCCESS;		
						}
						else 
                               X_ONMOBILE_REASON = "TECHNICAL_DIFFICULTY_ERROR";
					}
					else
						X_ONMOBILE_REASON = DEACT_PENDING;
				}
				else
					X_ONMOBILE_REASON = FAILURE;
			}
			}
		}
		else if(bDeActivate)
		{
			boolean isTransExist = false;
			if (strTransID != null)
			{
				isTransExist = rbtLogin
						.checkTransIDExist(strTransID, "ADRBT_DEACT");
				if (!isTransExist)
				{
					rbtLogin.addTransData(strTransID, strSubID, "ADRBT_DEACT");
				}
			}
			if (isTransExist)
				X_ONMOBILE_REASON = "Invalid Request. Already recieved a request with the same TransID : "
						+ strTransID
						+ " and subscriber ID : "
						+ strSubID;
			else
			{
			boolean isActive = Processor.isUserActive(subscriber.getStatus());
			boolean activeOnAdRbt = false;
			if(!isActive)
				X_ONMOBILE_REASON = NOT_ACTIVE_ON_RBT;
			else
			{
				String strResult = rbtLogin.deactSubscriber(strSubID, "ADRBT", true, actInfo, true);
				if(strResult.equalsIgnoreCase(SUCCESS))
				{
					rbtLogin.removeViralBlackList(strSubID, "TOTAL");
					X_ONMOBILE_REASON = SUCCESS;
				}
				else
					X_ONMOBILE_REASON = FAILURE;
			}
		}
	}
}
else
	X_ONMOBILE_REASON = "MISSING_PARAMETER";
}
else
	X_ONMOBILE_REASON = "Invalid IP Address";
System.out.println("X_ONMOBILE_REASON -"+ X_ONMOBILE_REASON);
out.println(X_ONMOBILE_REASON);
%>
