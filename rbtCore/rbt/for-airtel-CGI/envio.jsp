<%@page import="com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil"%>
<%@ page import = "com.onmobile.apps.ringbacktones.thirdparty.AirtelThirdPartyRequestHandler"%>
<%@ page import = "java.util.HashMap"%>
<%@ page import = "com.onmobile.apps.ringbacktones.common.WriteSDR"%> 
<%@ page import = "com.onmobile.apps.ringbacktones.common.RBTCommonConfig"%> 
<%@ page import = "com.onmobile.apps.ringbacktones.common.RBTSMSConfig"%> 
<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager"%> 
<%@ page import = "com.onmobile.apps.ringbacktones.common.WriteSDR"%> 
<%@ page import = "java.util.Date"%> 
<%@ page import = "java.util.Iterator"%>
<%@ page import = "java.util.Map"%>
<%
	Date startTime = new Date();
	Map<String, String[]> paramMap = request.getParameterMap();
	String strIP = request.getRemoteAddr();
	String subscriberID = request.getParameter("srcmsisdn");

	Map<String, String> responseMap = AirtelThirdPartyRequestHandler.processThirdPartyRequest(subscriberID, paramMap, strIP, "envio");

	int status = 200;
	try {
		status = Integer.parseInt(responseMap.get("status"));
	}
	catch(Exception e) {

	}
	String responseStr = responseMap.get("response");
	response.setStatus(status);
	out.println(responseStr);
	Date endTime = new Date();     
    String path = "./"; 
    int size = 2000; 
    try { 
		   RBTSMSConfig rbtSMSConfig = RBTSMSConfig.getInstance(); 
		   RBTCommonConfig rbtCommonConfig = RBTCommonConfig.getInstance(); 
		   int m_nConn = 4;
		   String s = rbtSMSConfig.getParameter("NUM_CONN");
    		if(s != null)
    		{
    			try
    			{
    				m_nConn = Integer.parseInt(s);
    			}
    			catch(Exception e)
    			{
    				m_nConn = 4;
    			}
    		}
		   RBTDBManager dbManager = RBTDBManager.init(rbtCommonConfig.dbURL(), m_nConn); 
		   path = CacheManagerUtil.getParametersCacheManager().getParameter("WAR", "THIRD_PARTY_SDR_DIR").getValue(); 
    } 
    catch(Exception e) { 

    } 

    StringBuffer sb = new StringBuffer("/envio/envio.jsp?"); 
    Iterator itr = paramMap.keySet().iterator(); 
    while(itr.hasNext()) { 
		   String key = (String)itr.next(); 
		   String[] valueArr = (String[])paramMap.get(key); 
		   String value = null; 
		   if(valueArr != null) 
				   value = valueArr[0]; 
		   sb.append(key + "=" + value + "&"); 
    } 
    WriteSDR.addToAccounting(path, size, "ENVIO_REQUEST", "NA", "NA", "NA", "OK", startTime.toString(), endTime.getTime() - startTime.getTime()+"", "NA", sb.toString(), responseStr); 

%>