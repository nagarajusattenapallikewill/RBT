package com.onmobile.mobileapps.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.AESUtils;
import com.onmobile.android.utils.Utility;
import com.onmobile.apps.ringbacktones.utils.ListUtils;

public class WebFilter implements Filter {

	//private final static Logger log = Logger.getLogger(WebFilter.class);

	//private static ArrayList<String> allowedURIs = null;
	Boolean isEncryptionEnabled;
	String requestSubscriberIdEncryptionKey;
	String requestUniqueIdEncryptionKey;

	private Boolean isQueryStringEncryptionEnabled;
	private List<String> osTypesForEncryptionList = new ArrayList<String>();
	private String encryptionKey;

	private static final String OS_TYPE = "osType";
	private static final String QUERY_STRING = "queryString";
	private static final String QUERY_STRING_VALUES_MAP = "queryStringValuesMap";


	public static Logger logger = Logger.getLogger(WebFilter.class);

	@Override
	public void destroy() {
	}

	class FilteredRequest extends HttpServletRequestWrapper {

		public FilteredRequest(ServletRequest request) {
			super((HttpServletRequest)request);
		}

		@Override
		public String getParameter(String paramName) {
			String paramValue = super.getParameter(paramName);
			if (isQueryStringEncryptionEnabled) {
				if (!paramName.equals(OS_TYPE) && !paramName.equals(QUERY_STRING)) {
					ServletRequest request = getRequest();
					String osType = request.getParameter(OS_TYPE);
					if (osType != null) {
						if (osTypesForEncryptionList.contains(osType)) {
							Map<String, String[]> queryStringMap = getQueryStringMap(request);
							if (queryStringMap != null && queryStringMap.containsKey(paramName)) {
								String[] valueArray = queryStringMap.get(paramName);
								if (valueArray != null && valueArray.length>0) {
									return valueArray[0];
								}
							}
							return null;
						}
					}
				}
			} else if (isEncryptionEnabled && isValid(paramValue)) {
				if ("subscriberId".equals(paramName)) {
					paramValue = decryptAESENcodedParam(paramValue, requestSubscriberIdEncryptionKey);
				} else if ("uniqueId".equals(paramName)) {
					paramValue = decryptAESENcodedParam(paramValue, requestUniqueIdEncryptionKey);
				}
			}
			return paramValue;
		}

		@Override
		public String[] getParameterValues(String paramName) {
			String paramValues[] = super.getParameterValues(paramName);
			if (isQueryStringEncryptionEnabled) {
				if (!paramName.equals(OS_TYPE) && !paramName.equals(QUERY_STRING)) {
					ServletRequest request = getRequest();
					String osType = request.getParameter(OS_TYPE);
					if (osType != null) {
						if (osTypesForEncryptionList.contains(osType)) {
							Map<String, String[]> queryStringMap = getQueryStringMap(request);
							return queryStringMap.get(paramName);
						}
					}
				}
			} else if (isEncryptionEnabled) {
				if ("subscriberId".equals(paramName)) {
					for (int index = 0; index < paramValues.length; index++) {
						if (isValid(paramValues[index])) {
							paramValues[index] = decryptAESENcodedParam(paramValues[index], requestSubscriberIdEncryptionKey);
						}
					}
				} else if ("uniqueId".equals(paramName)) {
					for (int index = 0; index < paramValues.length; index++) {
						if (isValid(paramValues[index])) {
							paramValues[index] = decryptAESENcodedParam(paramValues[index], requestUniqueIdEncryptionKey);
						}
					}
				}
			}
			return paramValues;
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
	ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		filterChain.doFilter(new FilteredRequest(request), response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		isEncryptionEnabled = PropertyConfigurator.isEncryptionEnabled();
		if (isEncryptionEnabled) {
			requestSubscriberIdEncryptionKey = PropertyConfigurator.getRequestSubscriberIdEncryptionKey();
			requestUniqueIdEncryptionKey = PropertyConfigurator.getRequestUniqueIdEncryptionKey();	
		}

		isQueryStringEncryptionEnabled = PropertyConfigurator.isQueryStringEncryptionEnabled();
		logger.info("isQueryStringEncryptionEnabled: " + isQueryStringEncryptionEnabled);
		String osTypesForEncryption = PropertyConfigurator.getOstypesForEncryption();
		if (Utility.isStringValid(osTypesForEncryption)) {
			osTypesForEncryptionList = ListUtils.convertToList(osTypesForEncryption, ",");
			logger.info("osTypesForEncryptionList: " + osTypesForEncryptionList);
		}
		encryptionKey = PropertyConfigurator.getRequestEncryptionKey();
		logger.info("encryptionKey: " + encryptionKey);
	}

	private Boolean isValid(String str) {
		if (str == null || str.trim().equals("")) {
			return false;
		}
		return true;
	}
	private Map<String, String[]> getQueryStringMap(ServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String[]> queryStringMap = (Map<String, String[]>) request.getAttribute(QUERY_STRING_VALUES_MAP);
		if (queryStringMap == null) {
			String queryString = request.getParameter(QUERY_STRING);
			queryString = decryptAESENcodedParam(queryString, encryptionKey);
			queryStringMap = Utility.convertListMapToStringArrayMap(Utility.convertStringIntoURLDecodedStringListtMap(queryString, "&", "="));
			logger.debug("querStringMap: " + queryStringMap);
			request.setAttribute(QUERY_STRING_VALUES_MAP, queryStringMap);
		}
		return queryStringMap;
	}
	
	private String decryptAESENcodedParam(String paramValue, String key) {
		logger.debug("Key for Decryption: " + key);
		logger.debug("Before Decryption - paramValue :" + paramValue);
		String decodedVal = AESUtils.decrypt(paramValue, key);
		logger.debug("After Decryption - paramValue :" + decodedVal);
		return decodedVal;
	}
}
