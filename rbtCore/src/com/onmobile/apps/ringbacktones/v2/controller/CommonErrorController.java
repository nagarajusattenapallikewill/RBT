package com.onmobile.apps.ringbacktones.v2.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

@ControllerAdvice()
@RestController
public class CommonErrorController {

	@Autowired
	private ApplicationContext context;
	private Logger logger = Logger.getLogger(CommonErrorController.class);
	@ExceptionHandler(Exception.class)
	public Map<String,String> handleError(Exception e, HttpServletResponse response){
		Map<String,String> map = new HashMap<String, String>();
		map.put("code", "GENERAL_ERROR");
		map.put("sub_code","GENERAL_ERROR");
		map.put("description", e.getMessage());
		logger.error("Exception Occured:" +e, e);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		
		return map;
	}
	
	@ExceptionHandler(UserException.class)
	public Map<String,String> handleError(UserException e, HttpServletResponse response){
		logger.error("Exception Occured:" +e, e);
		Locale locale = LocaleContextHolder.getLocale();
		
		String message = null;
		try {
			message = context.getMessage(e.getResponse(), null, locale);
		}
		catch(Throwable t) {
			String[] objects = new String[1];
			objects[0] = e.getResponse();
			message = context.getMessage(MessageResource.GENERAL_MESSAGE, objects, locale);
		}
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("code", "RBT_ERROR");
		map.put("sub_code",e.getCode());
		map.put("description", message);
		
		response.setStatus(e.getStatusCode());
		
		return map;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}
}
