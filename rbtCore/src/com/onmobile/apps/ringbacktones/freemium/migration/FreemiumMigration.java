package com.onmobile.apps.ringbacktones.freemium.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.freemium.ConfigurationParameter;
import com.onmobile.apps.ringbacktones.freemium.FreemiumMemcacheClient;

public class FreemiumMigration {

	private static Logger logger = Logger.getLogger(FreemiumMigration.class);
	private static String migrationFilePath = null;
	private static FreemiumMemcacheClient mc = null;
	
	public static void main(String[] args) throws Exception {
		if(FreemiumMemcacheClient.getInstance().isCacheInitialized()){
			logger.info("Freemium cache Initialized and Starting Process");
			getMigrationFilePathAndMemcache();
			startProcess();
		} else {
			logger.info("Freemium cache is not initialized");
		}
	}
	
	public static void startProcess() {
		File folder = new File(migrationFilePath);
		File[] listOfFiles = folder.listFiles();
		try {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					FileReader fileReader = new FileReader(file);
					logger.info("File is in Process: "+file.getAbsolutePath());
					BufferedReader bfrReader = new BufferedReader(fileReader);
					String line = null;
					while ((line = bfrReader.readLine()) != null) {
						String split[] = line.split(",");
						String subscriberId = split[0];
						String dateStr = split[1];
						mc.addSubscriberBlacklistTime(subscriberId, dateStr);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getMigrationFilePathAndMemcache() throws Exception {
		migrationFilePath = ConfigurationParameter.getParameterValue("FILE_PATH");
		mc = FreemiumMemcacheClient.getInstance();
		if(migrationFilePath == null){
			throw new Exception("FREEMIUM FILE PATH NOT CONFIGURED.....");
		}
		logger.info("MigrationFilePath = "+migrationFilePath);
	}
	
}
