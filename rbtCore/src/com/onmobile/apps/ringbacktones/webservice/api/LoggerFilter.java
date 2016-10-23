package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;


/**
 * Implemented for Telefonica Czech. Generates CDRs for a set of URLs in a separate log file (Logger name = ThirdPartyRequestLogger).
 * The required URL patterns are mapped in the web.xml file.
 * Writes request url, response text and the request processed time.
 * <br>
 * <a href=https://jira.onmobile.com/browse/RBT-10214>https://jira.onmobile.com/browse/RBT-10214</a>
 * <br>
 * <a href=https://athene.onmobile.com/display/RBT4/SDR+for+API%27s>https://athene.onmobile.com/display/RBT4/SDR+for+API%27s</a>
 * @author rony.gregory
 *
 * */

public class LoggerFilter implements Filter {

	Logger thirdPartyRequestLogger = Logger.getLogger("ThirdPartyRequestLogger");

	@Override
	public void destroy() {}

	@Override
	public void init(FilterConfig arg0) throws ServletException {}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		final CustomPrintWriter writer = new CustomPrintWriter(response.getWriter());

		long initTime = System.currentTimeMillis();
		filterChain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {
			@Override public PrintWriter getWriter() {
				return writer;
			}
		});
		long finalTime = System.currentTimeMillis();
		long timeTaken = finalTime - initTime;
		String reposnse = (writer.getCopy() != null ? writer.getCopy().trim()
				: writer.getCopy());
		thirdPartyRequestLogger.info(getIpAddress(req) + ", " + getFullURL(req)
				+ ", " + reposnse + ", " + timeTaken);
	}

	@SuppressWarnings("unchecked")
	private String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		// Implementation for SDR Logs for JiraID -RBT-11693 - TEF Spain -
		// Enhanced security for 3rd party APIs.
		String requestType = request.getMethod();
		if (requestType.equalsIgnoreCase("POST")) {
			Enumeration<String> params;
			requestURL.append('?');
			params = request.getParameterNames();
			while (params.hasMoreElements()) {
				String key = params.nextElement();
				String value = request.getParameter(key).trim();
				requestURL.append(key).append("=").append(value).append("&");
			}
			requestURL.deleteCharAt((requestURL.length() - 1));
		} else {
			String queryString = request.getQueryString();
			if (queryString == null) {
				requestURL.toString();
			} else {
				requestURL.append('?').append(queryString).toString();
			}
		}
		return requestURL.toString();
		// Implementation for SDR Logs for JiraID -RBT-11693 - TEF Spain -
		// Enhanced security for 3rd party APIs.
	}
	
	public  String getIpAddress(HttpServletRequest request) {
		String[] HEADERS_TO_TRY = { 
			    "X-Forwarded-For",
			    "Proxy-Client-IP",
			    "WL-Proxy-Client-IP",
			    "HTTP_X_FORWARDED_FOR",
			    "HTTP_X_FORWARDED",
			    "HTTP_X_CLUSTER_CLIENT_IP",
			    "HTTP_CLIENT_IP",
			    "HTTP_FORWARDED_FOR",
			    "HTTP_FORWARDED",
			    "HTTP_VIA",
			    "REMOTE_ADDR" } ;
	    for (String header : HEADERS_TO_TRY) {
	        String ip = request.getHeader(header);
	        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
	            return ip;
	        }
	    }
	    return request.getRemoteAddr();
	}
	
	
}
class CustomPrintWriter extends PrintWriter {

	private StringBuilder copy = new StringBuilder();

	public CustomPrintWriter(Writer writer) {
		super(writer);
	}

	@Override
	public void write(int c) {
		copy.append((char) c);
		super.write(c);
	}

	@Override
	public void write(char[] chars, int offset, int length) {
		copy.append(chars, offset, length);
		super.write(chars, offset, length);
	}

	@Override
	public void write(String string, int offset, int length) {
		copy.append(string, offset, length);
		super.write(string, offset, length);
	}

	public String getCopy() {
		return copy.toString();
	}

}