package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

/*http://<IP:PORT>htconsentreturn?msisdn=<msisdn>&tid=<tid>&cptid=<cptid>
 & subid=<subid>&songpid=<songpid>&vcode=<vcode>&mgreceiver=<mgreceiver>
 &mdedicatee=<mdedicatee>&code=<responsecode>&desc=<desc>&Param1=<Param1>
 &Param2=<Param2>&Param3=< Param3>&Param4=< Param4>
 */

/* Consent CallBack Url::The above is redirected to the below Url
 *http://172.16.29.229:7171/rbt/consentCallback.do?
 * MSISDN=$MSISDN$&Result=$RESULT$&transID=$CPTID$&TPCGID=$CGID$*/

public class HTConsentReturnServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(HTConsentReturnServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("HT ConsentReturn Received  = " +request.getRequestURL());
		String subscriberID = request.getParameter("msisdn");
		String transID = request.getParameter("cptid");
		String tpcgID = request.getParameter("tid");
		String songProductID = request.getParameter("songpid");
		String vcode = request.getParameter("vcode");
		String mgReceiver = request.getParameter("mgreceiver");
		String mdedicatee = request.getParameter("mdedicatee");
		String responseCode = request.getParameter("code");

		String result = "FAILURE";
		if (responseCode != null
				&& (Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
						"RESPONSE_CODES_FOR_COMVIVA", "1000").split(",")).contains(responseCode))) {
			result = "SUCCESS";
		}
		String requestUrl = request.getRequestURL().toString();
		requestUrl = requestUrl.substring(0, requestUrl.lastIndexOf("/"));
        
		requestUrl = requestUrl + "/consentCallback.do?MSISDN=" + subscriberID + "&Result=" + result
				+ "&transID=" + transID + "&TPCGID=" + tpcgID;
		String additionalQueryString = getModifiedQueryString(request.getQueryString());
		if(additionalQueryString!=null){
			requestUrl = requestUrl + "&" + additionalQueryString;
		}
        logger.info("Redirecting HT ConsentReturn to DoubleConfirmationCallback with URL = " + requestUrl);
		response.sendRedirect(requestUrl);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	private String getModifiedQueryString(String queryString){
	  Map<String,String> queryMap = new HashMap<String,String>();
      if(queryString!=null){
    	  String str[] = queryString.split("&");
    	  for(String keyValuePair : str){
    		  String st[] = keyValuePair.split("=");
    		  if(st!=null && st.length==2){
    			  queryMap.put(st[0], st[1]);
    		  }
    	  }
      }
      
      queryMap.remove("msisdn");
      queryMap.remove("cptid");
      queryMap.remove("tid");
 //     queryMap.remove("code");
      
      StringBuilder strBuilder = new StringBuilder();
      Set<Entry<String, String>> entrySet = queryMap.entrySet();
      for(Entry<String, String> entry : entrySet){
    	  strBuilder = strBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
      }
      
      String query = strBuilder.toString();
      if(query!=null){
    	  query = query.substring(0,query.lastIndexOf("&"));
      }
      logger.info("HT ConsentReturn Additonal query String = "+query);
      return query;
	}

}
