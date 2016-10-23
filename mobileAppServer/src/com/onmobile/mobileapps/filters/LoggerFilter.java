package com.onmobile.mobileapps.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;


public class LoggerFilter implements javax.servlet.Filter {

	Logger masRequestLogger = Logger.getLogger("MASLogger");

	@Override
	public void destroy() {}

	@Override
	public void init(FilterConfig arg0) throws ServletException {}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		final CustomPrintWriter writer ;
		String uri = req.getRequestURI();
		if (uri != null && uri.indexOf("FileStreaming") != -1) {
			writer = new CustomPrintWriter(response.getOutputStream());
		}else{
			writer = new CustomPrintWriter(response.getWriter());
		}
		
		
		

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
		if(reposnse !=null && !reposnse.isEmpty()){
			reposnse= reposnse.trim();
		}
		
		masRequestLogger.info(getFullURL(req)
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
}

class CustomPrintWriter extends PrintWriter {

	private StringBuilder copy = new StringBuilder();

	public CustomPrintWriter(ServletOutputStream servletOutputStream) {
		super(servletOutputStream);
	}

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