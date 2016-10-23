package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Log4jErrorConnector;
import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IWavFileMappingDAO;
import com.onmobile.apps.ringbacktones.v2.daemons.LoadWavFileMappingToMapping;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.RBTLoginUserMemcacheLoad;

/**
 * @author vinayasimha.patil
 *
 */
/**
 * Servlet implementation class for Servlet: StartUp
 *
 */
public class StartUp extends HttpServlet {
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(StartUp.class);

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public StartUp() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		try {
			super.init(servletConfig);
			Log4jErrorConnector r = new Log4jErrorConnector();
			System.out.println("Done");
			Log4jSysOutConnector l = new Log4jSysOutConnector();
			RBTAdminFacade.initialize();
			try {// Changes are done for handling the voldemort issues.
				MemcacheClientForCurrentPlayingSong.getInstance()
						.checkCacheInitialized();
				boolean isCallLogMemCacheIsUp = MemcacheClientForCurrentPlayingSong
						.getInstance().isCacheAlive();
				if (isCallLogMemCacheIsUp) {
					RBTLoginUserMemcacheLoad loadLoginUserToMemCache = new RBTLoginUserMemcacheLoad();
					loadLoginUserToMemCache.start();
				}
				try {
					LoadWavFileMappingToMapping loadWavFileToMapping = (LoadWavFileMappingToMapping) ConfigUtil
							.getBean(BeanConstant.LOAD_WAVE_FILE_MAPPING_FOR_2_0);
					loadWavFileToMapping.start();
				} catch (Exception e) {
					logger.error("Bean is not configured:"
							+ BeanConstant.LOAD_WAVE_FILE_MAPPING_FOR_2_0);
				}
			} catch (Exception e) {
				logger.error("Exception occured while putting the data into the memcache for Login user data");
			}
		} catch (Throwable e) {
			logger.error("", e);
		}
	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 * HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 * HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}
}