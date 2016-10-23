<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager,com.onmobile.apps.ringbacktones.content.*,com.onmobile.apps.ringbacktones.common.Tools,com.onmobile.apps.ringbacktones.subscriptions.*,java.util.*"%>
<%
String sResponse = "ERROR";
try
{
	RBTMOHelper rbtMO = RBTMOHelper.init();
	RBTDBManager rbtDbManager = RBTDBManager.getInstance();
	String subscriberId = request.getParameter("subscriber_id");
	if( subscriberId != null && subscriberId.equalsIgnoreCase("null"))
		subscriberId = null;
	String callerID = request.getParameter("caller_id");
	String clipId = request.getParameter("clip_id");
	String smsType = request.getParameter("sms_type");
	String selBy = request.getParameter("sel_by");
	if(selBy == null || selBy.length() <= 0 || selBy.equalsIgnoreCase("null"))
		selBy = null;
	if(smsType == null || smsType.equalsIgnoreCase("null"))
		smsType = "COPY";

	 //String finalClipID = clipId;
	if(rbtDbManager == null)
		sResponse = "TECHNICAL DIFFICULTY ERROR";
	else if(subscriberId == null || callerID == null)
		sResponse = "INSUFFICIENT PARAMETERS";
	else
	{
		if(clipId != null && clipId.equalsIgnoreCase("null"))
			clipId = null;

		String finalWavFile = clipId; 
		if(clipId != null && clipId.toUpperCase().indexOf("MISSING") == -1) 

		{		finalWavFile = "ERROR";
				String clipIDFromRdc = new StringTokenizer(clipId,":").nextToken().trim();
    			//ClipMinimal clipMin = null;
					try
					{
						int clipIDInt = Integer.parseInt(clipIDFromRdc); 
						Clips clip =  rbtDbManager.getClip(clipIDInt); 
						if(clip != null) 
						   finalWavFile = clip.wavFile(); 

					}
					catch(Exception e)
				{
				}
    			//String finalWavFile = "MISSING";
			//if(clipMin != null && clipMin.getWavFile() != null)
				//finalWavFile = clipMin.getWavFile();
			//finalClipID = Tools.findNReplace(finalClipID, wavFile,finalWavFile);
    		}
		rbtDbManager.insertViralSMSTableMap(subscriberId, null, smsType, callerID, finalWavFile, 0, selBy, null, null);
		sResponse = "0";
	}
}
catch(Throwable e)
{
	Tools.logException("rbt_copy.jsp", "RBT::Exception caught ", e);
	sResponse = "-1";
}
%><%=sResponse%>