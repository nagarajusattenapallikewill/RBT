<%@page import="com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.Parameters"%>
<%@page import="com.onmobile.apps.ringbacktones.services.common.Utility"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page	import="com.onmobile.apps.ringbacktones.content.database.RBTDBManager"%>

<%@page import="com.onmobile.apps.ringbacktones.content.*,com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants"%>
<%@ page import="com.onmobile.apps.ringbacktones.subscriptions.*,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*,java.util.zip.GZIPOutputStream"%><%



//To prevent getOutputStream() has already been called error, as the generated servlet will have out . write statements for each free space or line.
%><%

String strSubID = null;
String strPrepaid = null;
String strRequestType = null;
String strRequestValue = null;
String callerID = null;
String categoryID= null;
String rbt = null;
String clipId = null;
int frmTime = 0;
int toTime = 23;
String strActivatedBy = null;
String strActInfo = null;
String strDeactivatedBy = null;
String strSelectedBy = null;
String strSelectionInfo = null;
String strSubClass = null;
String strChargeClass = null;
String strChannel = null;
String requestValue=null;
StringBuffer strBuffer = new StringBuffer();

boolean isprepaid = false;

RBTChannelHelper rbtChannel = null;
try
{
	rbtChannel = RBTChannelHelper.init();
}
catch(Throwable e)
{
	Tools.logException("RBT_Channel","RBT_Channel.jsp",e);
}
Tools.logDetail("rbt_gui.jsp","inside rbt_gui.jsp","RBT: Channel INCOMING REQUEST = " + request.getParameter("request_value") + " from SubscriberID = " + request.getParameter("SUB_ID"));

requestValue=request.getParameter("request_value");
strSubID = request.getParameter("SUB_ID");
strPrepaid = request.getParameter("SUB_TYPE");
strRequestType = request.getParameter("REQUEST_TYPE");
strRequestValue = request.getParameter("REQUEST_VALUE");
strActivatedBy = request.getParameter("ACTIVATED_BY");
strActInfo = request.getParameter("ACT_INFO");
strDeactivatedBy = request.getParameter("DEACTIVATED_BY");
strSelectedBy = request.getParameter("SELECTED_BY");
strSelectionInfo = request.getParameter("SELECTION_INFO");
strSubClass = request.getParameter("SUB_CLASS");
strChargeClass = request.getParameter("CHARGE_CLASS");
strChannel = request.getParameter("CHANNEL");

System.out.println("In rbt_gui...");
System.out.println("request_value="+requestValue);

	

//	 to use this method later when everyone else is done with the changes. 
    if(requestValue.equalsIgnoreCase("hlr_tick")) 
    { 
            try 
            { 
                  String SUB_ID=request.getParameter("SUB_ID");
                  String udid=request.getParameter("udid");
                  String opParam=request.getParameter("opParam");
                  udid = udid + ":"+opParam;
                  String mode="GUI";
//                  strBuffer = rbtChannel.getSubscriberSubYes(SUB_ID);
                  
                 Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(SUB_ID);
          		StringBuffer strXML = new StringBuffer();
          		if(subscriber != null)
				{
          			String subClass=subscriber.subscriptionClass();
	          		strBuffer.append(subscriber.subYes()+";"+subClass);
				}
          		
                  
                  
                  
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 
    if(requestValue.equalsIgnoreCase("gift_outbox")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID");
            	String mode="GUI";
            	System.out.println("**************IN GIFT OUTBOX ***********");
                    strBuffer = rbtChannel.getGiftOutbox(SUB_ID); 
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    }
    if(requestValue.equalsIgnoreCase("vcode")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID");
                    strBuffer = new StringBuffer(RBTDBManager.getInstance().getSubscriberDefaultVcode(SUB_ID, 0));
			} 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 
    }

	if(requestValue.equalsIgnoreCase("charge_details")) 
    { 
            try 
            { 
            	System.out.println("****IN CHARGE DETAILS*****");
            	String SUB_ID=request.getParameter("SUB_ID");
            	RBTSubUnsub rbtlogin = RBTSubUnsub.init();
                    strBuffer = new StringBuffer(rbtlogin.getChargingDetails(SUB_ID)); 
                    System.out.println("****IN CHARGE DETAILS*****");    
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 


    if(requestValue.equalsIgnoreCase("sub_detail")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID");
            	String mode="GUI";
                    strBuffer = rbtChannel.getSubscriberSelections(strSubID.trim(),mode); 
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 
     if(requestValue.equalsIgnoreCase("buying_history")){
        String method = request.getParameter("METHOD");
        String sub_id = request.getParameter("SUBSCRIBER_ID");
        String start  = request.getParameter("START_DATE");
        String end   =  request.getParameter("END_DATE");
        String clipName = request.getParameter("CLIP_NAME");
        String resp="";
        strBuffer=new StringBuffer();
		  if(method!=null)
        {
            if(method.length()>0){
                 if(method.equalsIgnoreCase("selections")){
                      RBTSubUnsub rbtLogin = RBTSubUnsub.init();
           String[] selections =rbtLogin.getallAirtelSubscriberSelections(sub_id,start,end,clipName);
				      if(selections!=null && selections.length>0){
				    	  Tools.logDetail("rbt_gui.jsp","buying_history","subscriber_id="+sub_id+"seelctions got is not null nw appending in response");
						for(int i=0;i<selections.length;i++){
							if(i==0)
								strBuffer.append(selections[i]);
                               else{
                            	   strBuffer.append(";;"+selections[i]);
                               }
						}	
					  }
				      if(strBuffer!=null){
				      Tools.logDetail("rbt_gui.jsp","buying_history","subscriber_id="+sub_id+"exiting buying history with string=="+strBuffer.toString());
				      }
                    }
              }
        }              
    }
     else if(requestValue.equalsIgnoreCase("sub_active_date")){
         String sub_id = request.getParameter("SUBSCRIBER_ID");
 		 System.out.println("in sub_active_date");
 		Subscriber sub = RBTDBManager.getInstance().getSubscriber(sub_id);
 		String circleName=Utility.getPrefix(sub_id).getSiteName();
    	if(circleName==null)circleName="na";
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd  h:mm a");
    	if(sub!=null){
    		String date ="";
    		if(sub.activationDate()!=null){
    			date=date+df.format(sub.activationDate());
    		}else if(!sub.subYes().equalsIgnoreCase("X")&& sub.startDate()!=null){
    			date=date+df.format(sub.startDate());
    		}
    		else {
    			date=date+"0-0-0 0:0";
    		}
    		date=date+"::";
    		 if(sub.subYes().equalsIgnoreCase("X")){
    			 if(sub.endDate()!=null){
    				 date=date+df.format(sub.endDate());
    			 }
    			 else {
    				 if(sub.lastDeactivationDate()!=null){
    			date=date+df.format(sub.lastDeactivationDate());
    				 }else{
    					 date=date+"0-0-0 0:0";
    				 }
    			}
    		}else if(sub.lastDeactivationDate()!=null){
    			date=date+df.format(sub.lastDeactivationDate());
    		}
    		else {
    			date=date+"0-0-0 0:0";
    		}
    		date=date+"::"+circleName;
    		if(sub.activatedBy()!=null)
    			date=date+"::"+sub.activatedBy();
    		else
    			date=date+"::CC";
    		if(sub.deactivatedBy()!=null){
    			if(sub.deactivatedBy().equalsIgnoreCase("NA")||sub.deactivatedBy().equalsIgnoreCase("NEF")||sub.deactivatedBy().equalsIgnoreCase("RA")||sub.deactivatedBy().equalsIgnoreCase("RF")){
    				date=date+"::CC(Insufficient Balance)";
    			}else{
    			date=date+"::"+sub.deactivatedBy();
    			}
    		}
    		else
    			date=date+"::CC";
    		strBuffer=new StringBuffer(date);
    	}
    	else strBuffer = new StringBuffer("-::-::"+circleName+"::CC::CC");
 				 
     }
    else if(requestValue.equalsIgnoreCase("copydetail")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID"); 
            	String mode="GUI";
                    strBuffer = rbtChannel.getSubscriberSelections(strSubID.trim(),mode,"copy"); 
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    }
    else if(requestValue.equalsIgnoreCase("selection")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID"); 
            	String chargeClass=request.getParameter("chargeClass"); 
            	String catId=request.getParameter("catId"); 
            	String callerno=request.getParameter("callerno");
            	String clipId2=request.getParameter("clipId");
            	String udid=request.getParameter("udid");
            	String opParam=request.getParameter("opParam");
                udid = udid + ":"+opParam;
            	if(callerno.equalsIgnoreCase("default"))
            		callerno=null;
            	String mode="GUI";
            	com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriberP = Processor.getSubscriber(SUB_ID);
				SUB_ID = subscriberP.getSubscriberID();
				String circleID = 	subscriberP.getCircleID();
				strBuffer = rbtChannel.addSelections(SUB_ID, callerno, Integer.parseInt(catId),Integer.parseInt(clipId2), 0, 23, "CCC", "CCC:"+udid, chargeClass,null, circleID);
    			     
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 
    else if(requestValue.equalsIgnoreCase("selectionEC")) 
    { 
            try 
            { 	
            	String SUB_ID=request.getParameter("SUB_ID"); 
            	String clipName2=request.getParameter("clipName"); 
            	String callerno=request.getParameter("callerno");
            	String sentTime=request.getParameter("sentTime");
            	String retailerId=request.getParameter("retailerId");
            	String udid=request.getParameter("udid");
            	String opParam=request.getParameter("opParam");
                udid = udid + ":"+opParam;
            	//String[] strArray=sentTime1.split("+");
            	//String sentTime=strArray[0]+" "+strArray[1];
            	//String clipName=request.getParameter("clipName")
            	String[] clipNaam=clipName2.split("#");
            	String clipName=null;
				for(int i=0;i<clipNaam.length;i++){
					if(clipName==null || clipName.equalsIgnoreCase("null")){
					clipName=clipNaam[i];	
					}else{
					clipName=clipName+" "+clipNaam[i];
					}
				}
            	Clips clip=null;
            	SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            	 RBTDBManager rbtDBManager=RBTDBManager.getInstance();
            	 Tools.logDetail("rbt_gui.jsp","requestValue is selectionEC","value of callerno  is "+callerno);
            	Subscriber subscriber=rbtDBManager.getSubscriber(SUB_ID);
            	String subscriberStatus=null;
            	if(subscriber!=null && (!subscriber.subYes().equalsIgnoreCase("null")))
            	{
	            	subscriberStatus=subscriber.subYes();
	            	if(!subscriberStatus.equalsIgnoreCase("Z"))
	            	{
	            		boolean a= rbtDBManager.isSubActive(subscriber);
		            	if(a){
	    				clip=rbtDBManager.getClip(clipName);
	    				Parameters parameter=null;
	    				parameter=CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "EC_CATEGORY");
	    				int catId = Integer.parseInt(parameter.getValue().trim());
		    			int clipId1=clip.id();
	    	        	if(callerno.equalsIgnoreCase("default"))
	        	    		callerno=null;
	            		strBuffer = rbtChannel.addSelections(SUB_ID, callerno,catId,clipId1, 0, 23, "EC", "CCC:"+retailerId+":"+udid,null,null,null);
	            		Tools.logDetail("rbt_gui.jsp","requestValue is selectionEC","value of strBuffer is "+strBuffer);
		            	RBTDBManager.getInstance()
	        	       .updateViralPromotion(retailerId,SUB_ID, dateFormat.parse(sentTime), "EC",
	            	                        "EC_PROCESSED", Integer.toString(clipId1), null);     
	
	            		}else{
	            			strBuffer = new StringBuffer(); 
		            		strBuffer.append("NotSubscribed");
	    	        	}
	                }else{
	            	    strBuffer = new StringBuffer();
	            	    strBuffer.append("Suspended");
	              	}
                }else{
            		strBuffer = new StringBuffer(); 
            		strBuffer.append("NotSubscribed");
            	}
            }
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    }
    else if(requestValue.equalsIgnoreCase("copy")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID"); 
            	String wavfile=request.getParameter("wavfile"); 
            	String catId=request.getParameter("catId"); 
            	String callerno=request.getParameter("callerno");
            	String copyno=request.getParameter("copymsisdn");
            	String udid=request.getParameter("udid");
            	String opParam=request.getParameter("opParam");
                udid = udid + ":"+opParam;
            	Tools.logDetail("RBT_WAR", "rbt_gui.jsp", "inside copy :: SUB_ID=="+SUB_ID + " wavfile=="+wavfile + " catId=="+catId + " callerno=="+callerno + " copymsisdn=="+copyno);
//				 OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp", "inside copy", "SUB_ID=="+SUB_ID);
  //          	OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp", "inside copy", "wavfile=="+wavfile);
    //        	OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp", "inside copy", "catId=="+catId);
   //         	OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp", "inside copy", "callerno=="+callerno);
   //         	OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp", "inside copy", "copymsisdn=="+copyno); 
            	if(callerno.equalsIgnoreCase("default"))
            		callerno=null;
            	
            	String mode="GUI";
                    strBuffer = rbtChannel.getcopy(strSubID.trim(),null,catId,wavfile,callerno,copyno); 
                    
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 
            

    } 
    else if(requestValue.equalsIgnoreCase("signUp")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID"); 
            	String sub_type=request.getParameter("sub_type");
            	String lang_type=request.getParameter("lang_type");
            	String udid=request.getParameter("udid");
            	String opParam=request.getParameter("opParam");
                udid = udid + ":"+opParam;
            	String mode="GUI";
            	boolean pre = true;
    			
    				pre = true;
    			
    			try
    			{
    				strBuffer = rbtChannel.activateSubscriber(SUB_ID.trim(), pre, "CCC", "CCC:"+udid, sub_type,lang_type);
    			}
    			catch(Throwable e)
    			{
    				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
    				strBuffer = new StringBuffer();
    				strBuffer.append("error");
    			}
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 
    else if(requestValue.equalsIgnoreCase("signUpEC")) 
    { 
            try 
            { 
            	String SUB_ID=request.getParameter("SUB_ID"); 
            	String retailerId=request.getParameter("retailerId");
            	//String lang_type=request.getParameter("lang_type");
            	String sentTime1=request.getParameter("sentTime");
            	String udid=request.getParameter("udid");
            	String opParam=request.getParameter("opParam");
                udid = udid + ":"+opParam;
            	Tools.logDetail("rbt_gui.jsp","signUpEC","value of sentTime1 is "+sentTime1);
            	//String[] strArray=sentTime1.split("+");
            	//Tools.logDetail("rbt_gui.jsp","signUpEC","value of sentTime is "+strArray[0]+" "+strArray[1]);
            	//String sentTime=strArray[0]+" "+strArray[1];
            	
            	boolean pre = true;
    			
    				pre = true;
    			
    			try
    			{	Parameters parameter= CacheManagerUtil.getParametersCacheManager().getParameter("COMMON","EC_SUB_CLASS");
    				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    				strBuffer = rbtChannel.activateSubscriber(SUB_ID.trim(), pre, "EC", "CCC:"+retailerId+":"+udid, parameter.getValue().trim(),null);
    				 RBTDBManager.getInstance()
                   .updateViralPromotion(retailerId,SUB_ID, dateFormat.parse(sentTime1), "EC",
                                         "EC_PROCESSED", "null", null);
    			}
    			catch(Throwable e)
    			{
    				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
    				strBuffer = new StringBuffer();
    				strBuffer.append("error");
    			}
            } 
            catch(Throwable e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 
    else if(requestValue.equalsIgnoreCase("unsubscribe"))
	{
		try
		{
        	String udid=request.getParameter("udid");
        	String opParam=request.getParameter("opParam");
            udid = udid + ":"+opParam;
			String SUB_ID=request.getParameter("SUB_ID"); 
			strBuffer = rbtChannel.deactivateSubscriber(SUB_ID.trim(), "CCC","CCC:"+udid );
		}
		catch(Throwable e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
    else if(requestValue.equalsIgnoreCase("update"))
	{
		try
		{
			String finaltype=(String)request.getParameter("finaltype");
			String inittype=(String)request.getParameter("inittype");
			String newlang=(String)request.getParameter("newlang");
        	String udid=request.getParameter("udid");
        	String opParam=request.getParameter("opParam");
            udid = udid + ":"+opParam;
			String SUB_ID=request.getParameter("SUB_ID"); 
			if(finaltype.equalsIgnoreCase("DEFAULT")){
				finaltype=inittype;
			}
           	Subscriber subscriber=RBTDBManager.getInstance().getSubscriber(SUB_ID);
       		boolean a= RBTDBManager.getInstance().isSubActive(subscriber);
			if(a && subscriber.rbtType() == 1)
				strBuffer.append("Error");
			else
				strBuffer = rbtChannel.updateSubscriber(SUB_ID.trim(),finaltype,inittype,newlang,"CCC:"+udid);
		}
		catch(Throwable e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
    else if(requestValue.equalsIgnoreCase("delete"))
	{
		try
		{
			String callerno=(String)request.getParameter("callerno");
			String SUB_ID=request.getParameter("SUB_ID"); 
        	String udid=request.getParameter("udid");
        	String opParam=request.getParameter("opParam");
            udid = udid + ":"+opParam;
			if(callerno.equalsIgnoreCase("DEFAULT"))
			{
				callerno = null;
			}

			
				strBuffer = rbtChannel.removeSelection(strSubID, callerno, "CCC:"+udid);
			}
			catch(Throwable e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		
	}
    else if(requestValue.equalsIgnoreCase("change_selection"))
	{
		try
		{
			String oldCaller=(String)request.getParameter("oldCaller");
			String SUB_ID=request.getParameter("SUB_ID"); 
			String newCaller = request.getParameter("newCaller");
			String chargeClass=request.getParameter("chargeClass"); 
        	String catId=request.getParameter("catId"); 
        	String clipId2=request.getParameter("clipId");
        	String udid=request.getParameter("udid");
        	String opParam=request.getParameter("opParam");
            udid = udid + ":"+opParam;
        	String resp=null;
        	
        	boolean shouldDelete=true;
        		if(oldCaller.equalsIgnoreCase("junk")){
        			shouldDelete=false;
        		}
        	com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriberP = Processor.getSubscriber(SUB_ID);
			SUB_ID = subscriberP.getSubscriberID();
			String circleID = 	subscriberP.getCircleID();
				
			if(shouldDelete){
        		  resp =rbtChannel.removeSelection(SUB_ID, oldCaller, "GUI").toString();
        	
        			strBuffer = rbtChannel.addSelections(SUB_ID, newCaller, Integer.parseInt(catId),Integer.parseInt(clipId2), 0, 23, "CCC", "CCC:"+udid, chargeClass,null,circleID);
        			
        		
        		strBuffer = new StringBuffer(resp);
        	}else{
        		strBuffer = rbtChannel.addSelections(SUB_ID, newCaller, Integer.parseInt(catId),Integer.parseInt(clipId2), 0, 23, "CCC", "CCC:"+udid, chargeClass,null,circleID);
        	}
        		
				
		}catch(Throwable e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		
	}

    else if(requestValue.equalsIgnoreCase("easyCharge"))
	{
 		try
 		{
 			String strCustomer=request.getParameter("msisdn");
 			Tools.logDetail("rbt_gui.jsp","inside easyCharge","customerID is "+strCustomer);
        	String udid=request.getParameter("udid");
        	String opParam=request.getParameter("opParam");
            udid = udid + ":"+opParam;
 			strBuffer=rbtChannel.getViralSmsDetail(strCustomer);
 			if(strBuffer==null){
 				strBuffer.append("Error");
 			}
 		}
 		catch(Throwable e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
    	
	}
    else if(requestValue.equalsIgnoreCase("gift"))
	{
		try
		{
			String callerno=(String)request.getParameter("callerno");
			String gifter=(String)request.getParameter("gifter");
			String SUB_ID=(String)request.getParameter("SUB_ID"); 
			String sentTime=(String)request.getParameter("sentTime"); 
        	String udid=request.getParameter("udid");
        	String opParam=request.getParameter("opParam");
            udid = udid + ":"+opParam;
			StringTokenizer st1=new StringTokenizer(sentTime,";;;");
			String temp1=st1.nextToken();
			String temp2=st1.nextToken();
			sentTime=temp1+" "+temp2;
			String clipno=(String)request.getParameter("clipId");
			Tools.logDetail("RBT_WAR", "rbt_gui.jsp/gifting", "displaying clipId :: SUB_ID=="+SUB_ID + " sentTime=="+sentTime + " clipno=="+clipno + " callerno=="+callerno + " gifter=="+gifter);
// 			OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp/gifting", "displaying clipId", "clipId (string value)>>=="+clipno);
//			OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp/gifting", "displaying callerno", "callerno (string value)>>=="+callerno);
//			OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp/gifting", "displaying subId", "SUB_ID (string value)>>=="+SUB_ID);
//			OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp/gifting", "displaying sent time", "sentTime (string value)>>=="+sentTime);

//			OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp/gifting", "displaying gifter", "gifter (string value)>>=="+gifter);

			int clipIdx=new Integer(clipno).intValue();	
			if(callerno.equalsIgnoreCase("100")){
				strBuffer = rbtChannel.giftSelection(SUB_ID, callerno, sentTime,clipIdx,"GUIDel",gifter);
			}else{
				if(callerno.equalsIgnoreCase("default"))
	        		callerno=null;
				strBuffer = rbtChannel.giftSelection(SUB_ID, callerno, sentTime,clipIdx,"CCC",gifter);
			}
			}
			catch(Throwable e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		
	}
    // to be removed later on and use 'status' instead with the underlying code. 
   

try
{
	if(requestValue != null && !requestValue.equalsIgnoreCase("vcode"))
	{
		response.setHeader("Content-Encoding", "gzip");
		String s =strBuffer.toString();
		Tools.logDetail("rbt_gui.jsp","sending response","response=="+s.toString());
//		OnvAppInteractor.getInstance().printLog(com.onmobile.common.debug.DebugLevel.DETAIL, "RingbackTones", "rbt_gui.jsp", "sending meassage back", "respose=="+s);
		ByteArrayOutputStream b = new ByteArrayOutputStream(s.length());
		GZIPOutputStream gzos = new GZIPOutputStream(b);
		gzos.write(s.getBytes());
		gzos.flush();
		gzos.close();

		response.getOutputStream().write(b.toByteArray(),0,b.toByteArray().length);
		response.getOutputStream().close();
	}
	else
		out.write(strBuffer.toString());
	//out.close();
}
catch(Throwable e)
{
	Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
}

%>
