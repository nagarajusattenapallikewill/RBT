package com.onmobile.apps.ringbacktones.daemons.viralwhitelist;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.vfs.FileName;
import org.apache.log4j.Logger;

public class UploadViralWhiteList extends Thread {
	private static Logger LOG = Logger.getLogger(ViralWhiteListDaemon.class);
	private MemCachedClient mc = null;
	private ParametersCacheManager parametersCacheManager = null;
	private String masterFilePath;
	private File masterFile;
	private String state = null;


	private void init() throws Exception {
		this.mc = RBTCache.getMemCachedClient();
		this.parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();
		this.masterFilePath = getParamAsString(
				"VIRAL_WHITELIST_MASTER_FILE_PATH", null);

		this.masterFile = new File(this.masterFilePath);
	}

	public UploadViralWhiteList() throws Exception {
		init();
	}
	
	public UploadViralWhiteList(String state) throws Exception {
		this.state = state;
		init();
	}

	public void run() {
		if (RBTCache.isCacheAlive())
			loadAllSubscribersToCache();
		else
			LOG.error("Cache is not alive.");
	}

	private void loadAllSubscribersToCache() {
		LOG.info("Started loading file data into Cache");
		try {
			String masterFileWithExtn = getFileNameWithoutExtn(this.masterFile
					.getName());

			StringBuilder sb = new StringBuilder();
			sb.append(this.masterFile.getParent());
			sb.append(FileName.SEPARATOR);
			sb.append(masterFileWithExtn);
			sb.append("-");
			sb.append(getTimeStamp());

			File ffile = new File(sb.toString());
			FileWriter fw = new FileWriter(ffile);
			BufferedWriter bw = new BufferedWriter(fw);

			File file = new File(this.masterFilePath);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr);
			String line = null;
			LOG.info("Writing file data into Cache");
			long stTime = System.currentTimeMillis();
			RBTDBManager rbtdbManager = RBTDBManager.getInstance();

			while ((line = reader.readLine()) != null) {
				String subscriberId = rbtdbManager.subID(line.trim());
				if(null != state) {
					subscriberId = subscriberId.concat("_").concat(state);
				} 
				mc.add(subscriberId, Integer.valueOf(0));
				
				bw.write(subscriberId);
				bw.newLine();
				bw.flush();
			}

			long endTime = System.currentTimeMillis();

			LOG.info(new StringBuilder().append(
					"Successfully loaded master into Cache in time (ms): ")
					.append(endTime - stTime).toString());
		} catch (FileNotFoundException fnfe) {
			LOG.error(new StringBuilder().append(
					"Unable to find the file. Message:").append(
					fnfe.getMessage()).toString(), fnfe);
		} catch (IOException ioe) {
			LOG.error(new StringBuilder().append(
					"Failed IO when reading master file. Message:").append(
					ioe.getMessage()).toString(), ioe);
		}
	}

	private static String getFileNameWithoutExtn(String filename) {
		int dotIndex = filename.lastIndexOf(".");
		String actualFilename = filename.substring(0, dotIndex);
		return actualFilename;
	}

	private String getTimeStamp() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
		return sdf.format(c.getTime());
	}

	private String getParamAsString(String param, String defaultValue)
			throws Exception {
		String paramValue = null;
		try {
			paramValue = this.parametersCacheManager.getParameter("DAEMON",
					param, defaultValue).getValue();
		} catch (Exception e) {
			LOG.error(new StringBuilder().append("Unable to get ")
					.append(param).toString());
			throw e;
		}
		return paramValue;
	}

	public static void main(String[] args) {
		UploadViralWhiteList vl;
		try {
			if(args.length > 0) {
				String state = args[0];
				LOG.debug("State passed as: " + state);
				vl = new UploadViralWhiteList(state);
				vl.start();
			} else {
				vl = new UploadViralWhiteList();
				vl.start();
			}
		} catch (Exception e) {
			LOG.error("Unable to start UploadViralWhiteList." + " Exception: "
					+ e.getMessage(), e);
		}
	}
}
