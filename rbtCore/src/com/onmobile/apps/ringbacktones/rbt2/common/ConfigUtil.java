package com.onmobile.apps.ringbacktones.rbt2.common;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ConfigUtil implements ApplicationContextAware {
	
	
	private static ApplicationContext applicationContext;
	private static Logger logger = Logger.getLogger(ConfigUtil.class);
	
	public static Object getBean(String beanName) {
		logger.info("Getting bean object for bean name: "+beanName);
		return applicationContext.getBean(beanName);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		ConfigUtil.applicationContext = applicationContext;
		
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

}
