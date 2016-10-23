<%@page session="false"%>
<%@ page
	import="com.onmobile.apps.ringbacktones.common.iRBTConstant,com.onmobile.apps.ringbacktones.common.Tools,
	com.onmobile.apps.ringbacktones.content.database.RBTDBManager,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,
	java.net.URL,java.net.HttpURLConnection,java.io.InputStream,java.io.InputStreamReader,java.io.BufferedReader,java.io.BufferedInputStream,
	java.io.BufferedOutputStream,com.onmobile.apps.ringbacktones.provisioning.AdminFacade,com.onmobile.apps.ringbacktones.provisioning.Processor,
	com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber,com.onmobile.apps.ringbacktones.provisioning.common.Task,
	com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%><%
%><%
String strSubID = null;
String strRequestType = null;
String siteUrl = null;
String strChannel = null;
boolean m_usePoolDefault = true;
boolean m_usePool = m_usePoolDefault;
String m_port = "8080";
String m_channel_url = "rbt/rbt_channel.jsp";
String local_server_ip = "localhost";

local_server_ip = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "JBOSS_IP", "localhost").trim();
if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "WEB_SERVER_PORT", null) != null)
	m_port = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "WEB_SERVER_PORT", null).trim();
strSubID = request.getParameter("SUB_ID");
strRequestType = request.getParameter("REQUEST_TYPE");
siteUrl = request.getParameter("SITE_URL");
strChannel = request.getParameter("CHANNEL");

Tools.logStatus("interceptor.jsp", "interceptor.jsp", "INCOMING REQUEST = " + strRequestType + " from SubscriberID = " + strSubID);
            
if(strRequestType == null)
{
    Tools.logStatus("RBT_CHANNEL_"+strChannel,"interceptor.jsp","strRequestType is " + strRequestType);
	Tools.logStatus("interceptor.jsp", "interceptor.jsp", "REQUEST_TYPE is NULL. Hence Response from central server :: Error");
	out.write("Error");
}
else
{
		String newURL = null;
		String remoteURL = null;
		String localURL = null;
		//if strSubID is null then it means that the request_type is for 'search' or 'cat'
		if(strSubID != null){
			Subscriber subscriber = Processor.getSubscriber(strSubID);
			strSubID = subscriber.getSubscriberID();
			Task task = new Task();
			task.setObject("subscriber",subscriber);
			if(subscriber.isValidPrefix()){
//				means the prefix is that of central site 
                Tools.logStatus("interceptor.jsp", "interceptor.jsp", "Prefix was that of central site");
                localURL = Processor.getRedirectionURL(task);
                Tools.logStatus("interceptor.jsp", "interceptor.jsp", "localURL :: "+localURL); 
                if (localURL == null) 
                { 
					if(local_server_ip!=null && local_server_ip.indexOf(":")!=-1)
					{
						newURL = "http://" + local_server_ip + "/" + m_channel_url + "?"; 
					}else{
						newURL = "http://" + local_server_ip + ":" + m_port + "/" + m_channel_url + "?"; 
					}
                } 
                else 
                { 
                    //here '?' in rbt_sms.jsp? is put as a regex, and does not represent localURL as obtained 
                    //from RBTMOHelper.init().getURL(strSubID.substring(0, 4)) 
                    newURL = localURL.replaceAll("rbt_sms.jsp?", "rbt_channel.jsp"); 
                } 
            }else { 
                //means the prefix is that of regional site 
                Tools.logStatus("interceptor.jsp", "interceptor.jsp", "Prefix was that of regional site"); 
                remoteURL =  Processor.getRedirectionURL(task);
                Tools.logStatus("interceptor.jsp", "interceptor.jsp", "remoteURL :: "+remoteURL); 
                if (remoteURL != null) 
		        {	
                	//here '?' in rbt_sms.jsp? is put as a regex, and does not represent remoteURL as obtained 
                    //from RBTMOHelper.init().getURL(strSubID.substring(0, 4)) 
					newURL = remoteURL.replaceAll("rbt_sms.jsp?","rbt_channel.jsp");
				}
            }
	    }
        if (strRequestType.equalsIgnoreCase("cat") && siteUrl != null && siteUrl.length() != 0)
        {
            newURL = "http://" + siteUrl + "/" + m_channel_url + "?";
            Tools.logStatus("interceptor.jsp", "interceptor.jsp", "Poller Job being redirected to site " + newURL);
            InputStream isStream = null;
			ServletOutputStream sosStream = null;
			response.setHeader("Content-Encoding", "gzip");
            try
            {
                isStream = getStreamResponseFromURL(newURL + request.getQueryString());
                BufferedInputStream bis = new BufferedInputStream(isStream,1024);
				sosStream = response.getOutputStream();
				//ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				BufferedOutputStream baos = new BufferedOutputStream(sosStream,1024);
				byte[] buffer = new byte[1024];
				int ibit = 1;//any value greater than 0
				while ((ibit = bis.read(buffer,0,buffer.length)) >= 0) {
				    baos.write(buffer,0,ibit);
				}
				baos.close();
				bis.close();
				isStream.close();

				//baos.writeTo(sosStream);
				//sosStream.flush();
				//sosStream.close();
				//baos.close();						
				//bis.close();
				//isStream.close();
            }
            catch (Exception e)
            {
                Tools.logException("interceptor.jsp", "Exception in getting Poller response from other site", e);
            }
			//out.clear();
            //out = pageContext.pushBody(); 
        }
        else
        {
            Tools.logStatus("interceptor.jsp", "interceptor.jsp", "newURL :: " + newURL); 
	        String responseStr = null; 
    	    if(newURL == null) 
            { 
				Tools.logStatus("interceptor.jsp", "interceptor.jsp", "Not going to connect to any server as newURL is null,Hence Response :: Error"); 
                out.write("Error"); 
            } 
            else 
            { 
            	responseStr = getResponseFromURL(newURL + request.getQueryString());
                out.write(responseStr);
            }
        }
}
%><%
%><%!private String getResponseFromURL(String value)
    {
    	String response = "";
        try
        {
        	Tools.logStatus("interceptor.jsp", "getResponseFromURL(String value)", "Requesting URL " + value);
            URL url = new URL(value);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = buffer.readLine())!= null)
                response += line;
	            is.close();
        }
        catch(Exception e)
        {
        	Tools.logException("interceptor.jsp", "getResponseFromURL(String value)", e);
        	Tools.logStatus("interceptor.jsp","getResponseFromURL(String value)","Exception in connecting to regional server, Hence Response :: Error");
	        return "Error";
        }
        Tools.logStatus("interceptor.jsp", "getResponseFromURL(String value)", "Response from other server :: " + response);
        return response;
    }

	private InputStream getStreamResponseFromURL(String value)
	 {
	     InputStream inputStream = null;
	     try
	     {
	    	 Tools.logStatus("interceptor.jsp", "getStreamResponseFromURL(String value)", "Requesting URL " + value);
	         URL url = new URL(value);
	         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	         connection.setRequestMethod("GET");
	         inputStream = connection.getInputStream();
	     }
	     catch (Exception e)
	     {
	    	 Tools.logException("interceptor.jsp", "getStreamResponseFromURL(String value)", e);
	     }
	     Tools.logStatus("interceptor.jsp", "getStreamResponseFromURL(String value)", "Response from other server :: " + inputStream);
	     return inputStream;
	 }

%>
