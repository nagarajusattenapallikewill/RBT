package com.onmobile.apps.ringbacktones.rbtcontents.re.impl;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfoAction;
import com.onmobile.apps.ringbacktones.rbtcontents.bi.BIInterface;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;


public class REOnmobileRecommendation implements BIInterface {
	
	private static Logger logger = Logger.getLogger(REOnmobileRecommendation.class);

	public REOnmobileRecommendation() {
      logger.info("Instantiating ReRecommendation class");		
	}

	public Clip[] process(Category category, String subscriberId, String circleId, boolean doReturnActiveClips, String language, String appName,  boolean isFromCategory, ClipInfoAction clipInfoAction) {

		String strUrl = RBTContentJarParameters.getInstance().getParameter(
				"BI_URL_" + category.getCategoryTpe());
		logger.info("RE recommendation url from rbtContentJar.properties file : " + strUrl);
		if(subscriberId != null) {
			strUrl = strUrl.replaceAll("%msisdn%", subscriberId);
		}
		if(category != null) {
			strUrl = strUrl.replaceAll("%catId%", category.getCategoryId() + "");
		}
		if (circleId != null) {
			strUrl = strUrl.replaceAll("%circleId%", circleId);
		}
		
		logger.info("RE recommendation url after replacing the parameters : " + strUrl);
		
//		HttpParameters httpParam = new HttpParameters();
//		httpParam.setUrl(strUrl);
//		httpParam.setConnectionTimeout(6000);
//		Map<String, String> requestParams = null;
//		String responsStr = "ERROR";
//		try {
//			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(
//					httpParam, requestParams, null);
//			logger.info("Response from RE recommedations " + httpResponse);
//			if (null != httpResponse && httpResponse.getResponseCode() == 200) {
//				responsStr = httpResponse.getResponse();
//			}
//		} catch (Exception ex) {
//			logger.error("Error : " + ex.getMessage()); 
//		}
//		logger.info("Response : " + responsStr);
//		
//		if (responsStr == null || responsStr.equalsIgnoreCase("ERROR")) {
//			 return null;
//		 }
		
		StringBuilder builder = null;
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {

					return null;

				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {

				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {

				}

			}

			};

			// Install the all-trusting trust manager

			SSLContext sc = SSLContext.getInstance("SSL");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier

			HostnameVerifier allHostsValid = new HostnameVerifier() {

				public boolean verify(String hostname, SSLSession session) {

					return true;

				}

			};

			// Install the all-trusting host verifier

			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

			URL url = new URL(strUrl);

			URLConnection con = url.openConnection();

			Reader reader = new InputStreamReader(con.getInputStream());
			BufferedReader buffReader = new BufferedReader(reader);
			builder = new StringBuilder();
			while (true) {
				
				String line = buffReader.readLine();
				if(line == null) {
					break;
				}
				
				builder.append(line);
			}
			logger.info("Response: " + builder.toString());
		} catch (Exception e) {
			logger.error("Exception:",e);
		}
		
		if(builder != null) {
			return getClipsFromRE(builder.toString(), language, appName);
		}
		return null;
	}
	
	@Override
	public boolean processHitBIForPurchase(String subscriberId, String refId,
			String mode, String toneId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Clip[] getClipsFromRE(String clipREResponseXml, String language, String appName) {
		Clip clips[] = null;

		ArrayList<Clip> clipList = new ArrayList<Clip>();
		Document document = XMLUtils.getDocumentFromString(clipREResponseXml.trim());
		if (document != null) {
			NodeList nodelist = document.getElementsByTagName("file_name");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node =  nodelist.item(i);
	            String wavFile = node.getTextContent();
	            Clip tempClip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFile, language, appName);
	            if(tempClip != null) {
	            	clipList.add(tempClip);
	            }
	            logger.info("ClipList size : " + clipList.size());
			}
        }
		clips = clipList.toArray(new Clip[0]);
		return clips;
	}

}
