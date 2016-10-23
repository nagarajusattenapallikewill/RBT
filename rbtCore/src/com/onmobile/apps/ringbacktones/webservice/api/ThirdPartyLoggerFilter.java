package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.io.PrintWriter;
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

public class ThirdPartyLoggerFilter implements Filter {

	Logger thirdPartyRequestLogger = Logger
			.getLogger("ThirdPartyRequestLogger");

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		final CustomPrintWriter writer = new CustomPrintWriter(
				response.getWriter());

		long initTime = System.currentTimeMillis();
		filterChain.doFilter(request, new HttpServletResponseWrapper(
				(HttpServletResponse) response) {
			@Override
			public PrintWriter getWriter() {
				return writer;
			}
		});
		long finalTime = System.currentTimeMillis();
		long timeTaken = finalTime - initTime;
		String reposnse = (writer.getCopy() != null ? writer.getCopy().trim()
				: writer.getCopy());
		thirdPartyRequestLogger.info(getIpAddress(req) + ", " + getFullURL(req)
				+ ", " + reposnse + ", " + timeTaken + ThirdPartyLoggerUtil.getRefIds(req, reposnse));
	}

	@SuppressWarnings("unchecked")
	private String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
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
	}

	public String getIpAddress(HttpServletRequest request) {
		String[] HEADERS_TO_TRY = { "X-Forwarded-For", "Proxy-Client-IP",
				"WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR",
				"HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
				"HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED",
				"HTTP_VIA", "REMOTE_ADDR" };
		for (String header : HEADERS_TO_TRY) {
			String ip = request.getHeader(header);
			if (ip != null && ip.length() != 0
					&& !"unknown".equalsIgnoreCase(ip)) {
				return ip;
			}
		}
		return request.getRemoteAddr();
	}
}