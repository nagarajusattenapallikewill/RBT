package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.axis.AxisFault;
import org.apache.log4j.Logger;
import com.nokia.ntms.tip.api.SubscriberInfo;
import com.nokia.ntms.tip.api.TipServiceSoapBindingStub;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;

/* @author roshan.david
 Gets the MSISDN from the url hit , retrieves the imei no by hitting the corresponding DM url based on 
 the prefix of the MSISDN , stores imei no in RbtDetailsRequest and gets offer , if offer exists Yes is 
 returned else no is returned

 */

public class AirtelTAC extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Map<String, String> prefixUrlMap = null;
	private static long lastInitTime = 0L;
	Logger m_logger=Logger.getLogger(AirtelTAC.class);
	static Object syncObject=new Object();

	public AirtelTAC() {
		super();

	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String MSISDN=request.getParameter("subscriberID");
		String prefix=MSISDN.substring(0,4);
		m_logger.info("Request made for MSISDN : "+ MSISDN);
		String url=null;
		String responseText="NO";


		if(prefixUrlMap == null || (System.currentTimeMillis()-lastInitTime > 300000L)) {
			synchronized(syncObject){		
				prefixUrlMap = getPrefixUrlMap();
				m_logger.info("got the prefix url map");
				m_logger.debug("prefix Url map : "+prefixUrlMap);
				lastInitTime = System.currentTimeMillis();
			}
		}

		url = prefixUrlMap.get(prefix);
		m_logger.info("Dm Url : "+url);
		
		
		if(url!=null) 
		{ 	
			StringBuffer urlresponse =new StringBuffer();
			Integer statusCode=null;
			String imei=null;

			try {

				if(url.equals("WEBSERVICE"))
				{
					//URL serviceurl=new URL("http://10.49.19.41:7080/tipws/services/TipService");
					URL serviceurl=new URL(prefixUrlMap.get("WEBSERVICE_BASED_DM_URL"));
					TipServiceSoapBindingStub tip=new TipServiceSoapBindingStub(serviceurl,null);
					SubscriberInfo si=tip.getPreviousImeiOfSubscriber("+91"+MSISDN);
					if(null != si) {
						imei=si.getImei();
					}
					else
						responseText="NO";

				}
				else 
				{

					url = url.replace("%MSISDN%", MSISDN);
					m_logger.info("URL : "+url);
					Tools.callURL(url, statusCode, urlresponse);
					m_logger.info("url response : "+urlresponse);
					String responseString=urlresponse.toString();
					m_logger.info("response string : "+responseString);
					//DM server response format : device:Nokia 1600 imei:3545340170780025 
					String responseSplit[] = responseString.split(":");
					imei = responseSplit[responseSplit.length-1].trim();
				}
				m_logger.info("imei no: "+imei);
				HashMap<String, String> extraInfoMap = new HashMap<String, String>();
				extraInfoMap.put("IMEI_NO",imei);
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(MSISDN);
				rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
				Offer offer[] = RBTClient.getInstance().getOffers(rbtDetailsRequest);
				if(offer!=null&&offer.length>0)
				{	responseText="YES";
					m_logger.debug("Response is "+responseText);
				}	

			} catch (AxisFault e) {
				responseText="ERROR";
				m_logger.error("RBT ERROR ::"+e.getMessage(),e);
			} catch (RemoteException e) {
				responseText="ERROR";
				m_logger.error("RBT ERROR ::"+e.getMessage(),e);
			} catch (MalformedURLException e) {
				responseText="ERROR";
				m_logger.error("RBT ERROR ::"+e.getMessage(),e);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		else{	
			responseText="ERROR";	
		}

		response.getWriter().write(responseText);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

	private Map<String, String> getPrefixUrlMap() {
		ParametersCacheManager parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		Parameters param = parametersCacheManager.getParameter("TAC", "PREFIX_BASED_DM_URL");
		String prefixUrlComb = param.getValue();
		String multiplePrefixexAndOneUrl[] = prefixUrlComb.split(";");
		Map<String,String> map = new HashMap<String,String>();

		for(int i=0;i<multiplePrefixexAndOneUrl.length;i++) {	
			String multiplePrefixexAndOneUrlArray[] = multiplePrefixexAndOneUrl[i].split("#");
			String multiplePrefixes = multiplePrefixexAndOneUrlArray[0].trim();
			String url = multiplePrefixexAndOneUrlArray[1].trim();
			String prefixes[] = multiplePrefixes.split(",");
			for(int j=0;j<prefixes.length;j++) {
				map.put(prefixes[j].trim(), url);
			}
		}
		Parameters webserviceparam=parametersCacheManager.getParameter("TAC", "WEBSERVICE_BASED_DM_URL");
		map.put("WEBSERVICE_BASED_DM_URL", webserviceparam.getValue());
		return map;

	}
}
