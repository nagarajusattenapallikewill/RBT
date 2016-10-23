package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.jspsmart.upload.SRequest;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * Implemented for BSNL. 
 * Redirects requests to a different server based on the circle of the subscriber as well as the value of a boolean request parameter, {@link WebServiceConstants#param_redirectionRequired}.
 * 
 * <br>
 * <a href=https://jira.onmobile.com/browse/RBT-10331>https://jira.onmobile.com/browse/RBT-10331</a>
 * <br>
 * <a href=https://athene.onmobile.com/display/RBT4/RBT+Handset+client+for+BSNL>https://athene.onmobile.com/display/RBT4/RBT+Handset+client+for+BSNL</a>
 * @author rony.gregory
 *
 * */
public class RedirectionFilter implements Filter, iRBTConstant,WebServiceConstants {

	static Logger logger = Logger.getLogger(RedirectionFilter.class);
	private Configurations configurations = null;
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		this.configurations = new Configurations();
		this.configurations.getLogger().info(
				"configurations: " + this.configurations);
	}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		String responseText = null;
		String redirectionURL = null;
		HashMap<String, File> fileParamMap = null;
		// Start: JiraID -RBT-11693 - TEF Spain - Enhanced security for 3rd
		// party APIs.

		String ipAddress = ((HttpServletRequest) request)
				.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		// Check for method type.
		HttpServletRequest req = (HttpServletRequest) request;
		String methodType = req.getMethod();// Method type to be validated
		String userId = req.getParameter(param_userId);
		String passWord = req.getParameter(param_password);
		String mode = req.getParameter(param_mode);
		String action = null;

		String reqProtocol = request.getScheme();
		String requestUrl = ((HttpServletRequest) request).getRequestURL()
				.toString();
		String apiNameFromUrl = requestUrl.substring(requestUrl
				.lastIndexOf("/") + 1);
		if (apiNameFromUrl.equalsIgnoreCase("rbt_promotion.jsp")) {
			action = req.getParameter(param_REQUEST);
		} else if (apiNameFromUrl.equalsIgnoreCase("Rbt.do")) {
			action = req.getParameter(param_info);
		} else {
			action = req.getParameter(param_action);
		}
		MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest((HttpServletRequest)request);
		HashMap<String, String> requestParams = getRequestParamsMap(multiReadRequest, (HttpServletResponse)response);
		
		if (Utility.isMultipartContent((HttpServletRequest)multiReadRequest)) {	
			fileParamMap = getRequestParamsMapAndAddFormParamsToReqParamsMap((HttpServletRequest)multiReadRequest, (HttpServletResponse)response, requestParams);
		}

		WebServiceContext task = Utility.getTask(requestParams);
		String redirectionRequired = requestParams
				.get(WebServiceConstants.param_redirectionRequired);
		
		boolean userAuthentication = userAuthentication(userId, passWord,
				apiNameFromUrl, mode, action, ipAddress, methodType,
				reqProtocol);
		IUserDetailsService operatorUserDetailsService = null;
		// Processor (ServiceProcessor)
		String subscriberID = (String) task
				.get(WebServiceConstants.param_subscriberID);
		if (subscriberID == null) {
			subscriberID = (String) task.get("MSISDN");
		}
		String ip = null ;
		String port = null ; 
		
		try {
			//Added for redirection logic for callback
			if(subscriberID == null && task.containsKey("MSISDN")){
				subscriberID = (String)task.get("MSISDN");
			}else if(subscriberID == null && task.containsKey("msisdn")){
				subscriberID = (String)task.get("msisdn");
			}
			
			if (subscriberID == null) {
				throw new UserException("MSISDN is null");
			}

			operatorUserDetailsService = (IUserDetailsService) ConfigUtil
					.getBean(BeanConstant.USER_DETAIL_BEAN);
			OperatorUserDetails operatorDetails = (OperatorUserDetails) operatorUserDetailsService
					.getUserDetails(subscriberID);
			if ((operatorUserDetailsService != null && operatorDetails != null) 
					&& ((operatorDetails.serviceKey().equalsIgnoreCase(OperatorUserTypes.LEGACY.getDefaultValue()) ||
					operatorDetails.serviceKey().equalsIgnoreCase(OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue())))) {
				redirectionRequired = "TRUE" ;
				String ipPort = configurations.getValueFromResourceBundle(operatorDetails.operatorName() + "_"+ operatorDetails.circleId());
				
				List<String> ipAddressNPort = new ArrayList<String>();
				if(ipPort != null && !ipPort.isEmpty()){
					ipAddressNPort = Arrays.asList(ipPort.split(":"));
				}		
						
				if (ipAddressNPort != null && ipAddressNPort.size() == 2) {
					ip = ipAddressNPort.get(0);
					port = ipAddressNPort.get(1);
				}
			}
		} catch (UserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("B2B INFORMATION  IP :" + ip  + " PORT :" + port );
		if (!userAuthentication) { //End: JiraID -RBT-11693 - TEF Spain - Enhanced security for 3rd party APIs.
			response.getWriter().write(user_access_information);
		} else {
			logger.info("Checking for redirection required or not. "
					+ "redirectionRequired: " + redirectionRequired
					+ ", requestUrl: " + requestUrl);

			if (redirectionRequired != null
					&& redirectionRequired.toUpperCase().equalsIgnoreCase(
							"TRUE")) {
				logger.info("Redirection required. task: " + task);

				Task provTask = new Task();
				String paramUrl = requestUrl.substring(requestUrl.lastIndexOf("/") + 1);

				provTask.setObject(Constants.param_URL, paramUrl);

				provTask.setObject(WebServiceConstants.param_subscriberID, task.get(WebServiceConstants.param_subscriberID));
				AdminFacade.getProcessorObject(""); 					//Done to initialize Processor data members. api set to empty string so as to use default
				redirectionURL = Processor.getRedirectionURL(provTask); //Returns null if redirection is not to be done
				//This redirection is for B2B user and the ip & portt are replaced based on the configurations 
				if( redirectionURL == null && ip !=null && port != null){
					redirectionURL = replacedIpPort(requestUrl , ip , port);
				}
				
				if (redirectionURL != null) {
					requestParams.remove(WebServiceConstants.param_redirectionRequired);
					logger.info("Redirecting to URL: " + redirectionURL + ", requestParams: " + requestParams);
					HttpParameters httpParameters = new HttpParameters(configurations.isUseProxy(), configurations.getProxyHost(),
							configurations.getProxyPort(), configurations.getHttpConnectionTimeout(), configurations.getHttpSoTimeout(),
							configurations.getMaxTotalHttpConnections(), configurations.getMaxHostHttpConnections());
					httpParameters.setUrl(redirectionURL);
					responseText = makeRequest(requestParams,
							httpParameters, fileParamMap);
				} else {
					logger.info("Not redirecting since redirectionURL is null");
				}

			} else {
				logger.info("Not redirecting, parameter redirectionRequired is false or not passed");
			}
			if(redirectionURL == null) { 					//Redirection not done
				filterChain.doFilter(multiReadRequest, response);
			} else {
				response.getWriter().write(responseText);	//Redirection already done
			}
		}
	}
	
	private String replacedIpPort(String requestUrl, String ip, String port) {
		List<String> ipPort = Arrays.asList((requestUrl.substring(
				requestUrl.indexOf("//") + 2,
				requestUrl.indexOf("/", requestUrl.indexOf("//") + 2)))
				.split(":"));
		String passedIp = ipPort.get(0);
		String passedPort = ipPort.get(1);
		requestUrl = requestUrl.replace(passedIp, ip);
		requestUrl = requestUrl.replace(":" + passedPort, ":" + port);

		return requestUrl;
	}

	/**
	 * This Method is used to verify the user authentication on each request.
	 * JiraID -RBT-11693 - TEF Spain - Enhanced security for 3rd party APIs.
	 * 
	 * @param userId
	 * @param passWord
	 * @param apiName
	 * @param mode
	 * @param ipAddress
	 * @return boolean based on user access details.
	 */
	private boolean userAuthentication(String userId, String passWord,
			String apiName, String mode, String action, String ipAddress,
			String methodType, String reqProtocol) {
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String configuredIPFromDB = RBTParametersUtils.getParamAsString(COMMON,
				"CONFIGURED_IP", null);
		logger.info("Checking for userAuthentication required or not. "
				+ "userId: " + userId + " apiName: " + apiName + " mode: " + mode
				+ " ipAddress: " + ipAddress+"passWord: "+passWord);
		if (null == configuredIPFromDB) {
			logger.info("CONFIGURED_IP Param is null so it won't do any validation");
			return true;
		}
		boolean returnValue = false;
		boolean isDecryptionReq = RBTParametersUtils.getParamAsBoolean(COMMON,
				"DECRYPT", null);
		String decryptKey = RBTParametersUtils.getParamAsString(COMMON,
				"AESKEY", null);

		if (configuredIPFromDB != null) {
			String[] allConfiguredIP = configuredIPFromDB.split(";");
			logger.info("configuredIPFromDB:--> " + allConfiguredIP.toString()
					+ " ipAddressFromURL --->" + ipAddress);
			for (String configuredIP : allConfiguredIP) {				
				if (configuredIP.equalsIgnoreCase(ipAddress)) {
					logger.info(ipAddress+" is configured in whitelisted IP it won't do any validation");
					return true;
				}
			}
			boolean isCheckRequestType = RBTParametersUtils.getParamAsBoolean(
					COMMON, "CHECK_REQUEST_TYPE", "TRUE");
			if (isCheckRequestType && methodType.equalsIgnoreCase("GET")) {
				logger.info("userAuthentication is failed for the user "
						+ userId + " methodType " + methodType
						+ " is not acceptable.");
				return false;
			} else if (isCheckRequestType
					&& methodType.equalsIgnoreCase("POST")
					&& reqProtocol.equalsIgnoreCase("http")) {
				logger.info("userAuthentication is failed for the user "
						+ userId + " methodType " + methodType
						+ " is not acceptable.");
				return false;
			}
		}

		if (!returnValue && userId != null && passWord != null) {
			if (isDecryptionReq) {
				passWord = decrypt(passWord, decryptKey);
			}
			HashMap<String, HashMap<String, List<String>>> urlAccessHashMap = rbtDBManager
					.getAccessDetails(userId, passWord);
			if (null == urlAccessHashMap) {
				logger.info("userAuthentication is failed for the user "
						+ userId + " check the userid and password " + passWord);
				return false;
			}
			HashMap<String, List<String>> modeAccessDetailsMap = urlAccessHashMap
					.get(apiName);
			logger.debug("urlAccessHashMapFromDB:--> " + urlAccessHashMap
					+ "  modeAccessDetailsMapFromDB:--> "
					+ modeAccessDetailsMap);
			if (modeAccessDetailsMap != null) {
				// If Db value is set the do the validation
				List<String> lstAccessDetails = new ArrayList<String>();
				if (modeAccessDetailsMap.containsKey(mode))
					lstAccessDetails = modeAccessDetailsMap.get(mode);
				if (modeAccessDetailsMap.containsKey("ALL"))
					lstAccessDetails.addAll(modeAccessDetailsMap.get("ALL"));
				if (lstAccessDetails != null && lstAccessDetails.size() > 0) {
					for (String accessRights : lstAccessDetails) {
						if (accessRights.equalsIgnoreCase(action)) {
							return true;
						}
					}
					logger.info("userAuthentication is failed for the user "
							+ userId + " for the api " + apiName+" and mode "+mode+" action "+action);
				} else {		
					logger.info("userAuthentication is failed for the user "
							+ userId + " for the api " + apiName+" and mode "+mode);
					returnValue = false;
				}
			} else{
				logger.info("userAuthentication is failed for the user "
						+ userId + " for the api " + apiName);
				returnValue = false;
			}
		} else{
			logger.info("Either userId or password is null");
			returnValue = false;
		}

		return returnValue;
	}
	
	
	/**
	 * THis function will decrypt the encrypted password which is send by the user.
	 * JiraID -RBT-11693 - TEF Spain - Enhanced security for 3rd party APIs. 
	 * @param encryptedText
	 * @param key
	 * @return
	 */
	public String decrypt(String encryptedText, String key) {
		String decryptedString = null;
		try {
			if (key == null || key.trim().length() == 0) {
				throw new InvalidKeyException("Invalid key: '" + key + "'. Decryption failed!");
			}
			if (encryptedText == null) {
				logger.error("Invalid encryptedText: '" + encryptedText + "'. Decryption failed!");
				return null;
			}
			byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec keySpec;

			keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

			byte[] encryptedTextBytes = Base64.decodeBase64(encryptedText.getBytes());
			byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
			decryptedString = new String(decryptedTextBytes);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			logger.error(e.getMessage(), e);
		} catch (BadPaddingException e) {
			logger.error(e.getMessage(), e);
		}
		return decryptedString;
	}

	/**
	 * Method to make the redirection request. 
	 * @param request
	 * @param response
	 * @param requestParams
	 * @param httpParameters
	 * @return
	 */
	private String makeRequest(HashMap<String, String> requestParams,
			HttpParameters httpParameters, HashMap<String, File> fileParamMap) {
		String responseText;
		logger.info("Making http request. requestParams: " + requestParams
				+ ", httpParameters: " + httpParameters + ", fileParamMap: "
				+ fileParamMap);
		if (null != fileParamMap) {
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(
						httpParameters, requestParams, fileParamMap);
				logger.info("Redirected multi-part request, using post"
						+ " method. " + "httpResponse: " + httpResponse
						+ ", httpParameters: " + httpParameters
						+ ", fileParamMap: " + fileParamMap);
				responseText = httpResponse.getResponse();
			} catch (Exception e) {
				logger.error("Unable to redirect using post. Exception: " + e.getMessage(), e);
				responseText = Utility.getErrorXML();
			}

		} else {
			try {
				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
						httpParameters, requestParams);
				logger.info("Redirected using get method. httpResponse: "
						+ httpResponse + ", httpParameters: " + httpParameters);
				responseText = httpResponse.getResponse();
			} catch (Exception e) {
				logger.error("Unable to redirect using get. Exception: " + e.getMessage(), e);
				responseText = Utility.getErrorXML();
			}
		}
		logger.info("responseText: " + responseText);
		return responseText;
	}

	/**
	 * Returns a map of requestParam and corresponding values.
	 * @param request
	 * @param response
	 * @return
	 */
	private HashMap<String, String> getRequestParamsMap(HttpServletRequest request,
			HttpServletResponse response) {
		HashMap<String, String> requestParams = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String key = params.nextElement();
			String value = request.getParameter(key).trim();
			requestParams.put(key, value);
		}
		logger.info("requestParams: " + requestParams);
		return requestParams;
	}

	/**
	 * The files in the request would be saved in the filesystem and the corresponding {@link java.io.File} objects would be put into a map.
	 * @param request
	 * @param response
	 * @return
	 */
	private HashMap<String, File> getRequestParamsMapAndAddFormParamsToReqParamsMap(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> requestParams) {
		HashMap<String, File> fileParams = new HashMap<String, File>();
		try {
			SmartUpload smartUpload = new SmartUpload();
			smartUpload.initialize(request.getSession().getServletContext(), request.getSession(), request, response, null);
			smartUpload.setTotalMaxFileSize(20000000);
			smartUpload.upload();

			for (int i = 0; i < smartUpload.getFiles().getCount(); i++) {
				com.jspsmart.upload.File file = smartUpload.getFiles().getFile(i);
				if (file.getSize() > 0) {			
					String fieldName = file.getFieldName();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					String fileName = "BulkTask-" + dateFormat.format(new Date()) + ".txt";
					String tmpDir = System.getProperty("java.io.tmpdir");
					File savedFile = new File(tmpDir, fileName);
					file.saveAs(savedFile.getAbsolutePath());
					fileParams.put(fieldName, savedFile);
					logger.info("Saved file to: " + savedFile.getAbsolutePath());
				} else {
					logger.info("File empty. Not saved!");
				}
			}

			SRequest smartUploadRequest = smartUpload.getRequest();

			@SuppressWarnings("unchecked")
			Enumeration<String> params = smartUploadRequest.getParameterNames();
			while (params.hasMoreElements()) {
				String key = params.nextElement();
				String value = smartUploadRequest.getParameter(key).trim();
				requestParams.put(key, value);
			}
		} catch (SmartUploadException e) {
			logger.error(e, e);
		} catch (IOException e) {
			logger.error(e, e);
		} catch (ServletException e) {
			logger.error(e, e);
		}
		logger.info("Returning fileParams: " + fileParams + ", requestParams: "
				+ requestParams);
		return fileParams;
	}

	class MultiReadHttpServletRequest extends HttpServletRequestWrapper {

		private byte[] body;

		public MultiReadHttpServletRequest(HttpServletRequest httpServletRequest) {
			super(httpServletRequest);
			// Read the request body and save it as a byte array
			InputStream is;
			try {
				is = super.getInputStream();
				body = IOUtils.toByteArray(is);
			} catch (IOException e) {
				logger.error(e, e);
			}

		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return new ServletInputStreamImpl(new ByteArrayInputStream(body));
		}

		@Override
		public BufferedReader getReader() throws IOException {
			String enc = getCharacterEncoding();
			if(enc == null) enc = "UTF-8";
			return new BufferedReader(new InputStreamReader(getInputStream(), enc));
		}

		private class ServletInputStreamImpl extends ServletInputStream {

			private InputStream is;

			public ServletInputStreamImpl(InputStream is) {
				this.is = is;
			}

			public int read() throws IOException {
				return is.read();
			}

			public boolean markSupported() {
				return false;
			}

			public synchronized void mark(int i) {
				throw new RuntimeException(new IOException("mark/reset not supported"));
			}

			public synchronized void reset() throws IOException {
				throw new IOException("mark/reset not supported");
			}
		}
	}
}