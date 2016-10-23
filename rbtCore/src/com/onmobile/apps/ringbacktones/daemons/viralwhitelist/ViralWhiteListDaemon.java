package com.onmobile.apps.ringbacktones.daemons.viralwhitelist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.apache.commons.vfs.FileName;
import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;

public class ViralWhiteListDaemon extends TimerTask {

	private static Logger LOG = Logger.getLogger(ViralWhiteListDaemon.class);
	private String addDirPath;
	private String removeDirPath;
	private String masterFilePath;
	private File masterFile;
	private Set<String> addNumbersSet = null;
	private Set<String> delNumbersSet = null;
	private List<File> addFiles = null;
	private List<File> deleteFiles = null;
	private static final String DONE = ".done";
	private MemCachedClient mc = null;
	private ParametersCacheManager parametersCacheManager = null;
	private int scheduleTime = 2;

	public ViralWhiteListDaemon()
	{
		try
		{
			parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
			addDirPath = getParamAsString("VIRAL_WHITELIST_ADD_DIR_PATH", null);
			removeDirPath = getParamAsString("VIRAL_WHITELIST_DEL_DIR_PATH", null);
			masterFilePath = getParamAsString("VIRAL_WHITELIST_MASTER_FILE_PATH",
					null);
			masterFile = new File(masterFilePath);
	
			checkDirsExists();
			scheduleTime = Integer.parseInt(getParamAsString(
						"VIRAL_WHITE_LIST_SCHEDULE_TIME", "2"));
			mc = RBTCache.getMemCachedClient();
		} catch (Exception e) {
			LOG.warn("Wrong configuration for "
					+ "VIRAL_WHITE_LIST_SCHEDULE_TIME message: "
					+ e.getMessage());
		}
		
	}

	public void start() {
		LOG.info("Scheduling ViralWhiteListDaemon...");
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, scheduleTime);
		calendar.set(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 0);
		Date startDate = calendar.getTime();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, startDate, 1000 * 60 * 60 * 24);
		
		LOG.info("ViralWhiteListDaemon has been scheduled");
	}

	public void stop() {
		this.cancel();
	}

	@Override
	public void run() {

		LOG.info("Starting ViralWhiteList processing..");

		try {

			if (RBTCache.isCacheAlive()) {
				long stTime = System.currentTimeMillis();
				loadFiles();

				updateCache();

				updateMasterFile();

				renameFiles();

				cleanOldFiles();
				long enTime = System.currentTimeMillis();
				LOG.info("Processed ViralWhiteList in (ms): "
						+ (enTime - stTime));
			} else {
				LOG.warn("Couldnt process viral whitelist files."
						+ " Cache is not up");
			}

		} catch (IOException ioe) {
			LOG.error("IOException: " + ioe.getMessage(), ioe);
		} catch (Exception e) {
			LOG.error("Exception: " + e.getMessage(), e);
		}
	}

	private void cleanOldFiles() {
		File addFiles = new File(addDirPath);
		File delFiles = new File(removeDirPath);
		File masterFiles = new File(masterFilePath);
		deleteFiles(addFiles, Calendar.MONTH, DONE);
		deleteFiles(delFiles, Calendar.MONTH, DONE);
		deleteFiles(masterFiles.getParentFile(), Calendar.WEEK_OF_MONTH, "");
	}

	private void deleteFiles(File addFiles, int roll, String state) {
		Calendar c = Calendar.getInstance();
		// roll back a month/ week
		c.roll(roll, false);
		LOG.info("Back Time: " + c.getTime());
		File[] files = addFiles.listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(state)) {
				Date date = new Date(file.lastModified());
				// current file is more than a month old
				if (date.before(c.getTime())) {
					boolean isDeleted = file.delete();
					LOG.info("Deleting old file: " + file.getName()
							+ ", status: " + isDeleted);
				} else {
					LOG.debug("File: " + file.getName() + " is not older ");
				}
			}
		}
	}

	private void loadFiles() throws Exception {
		addFiles = getFiles(addDirPath);
		deleteFiles = getFiles(removeDirPath);
	}

	/**
	 * @param dirName
	 * @return
	 * @throws Exception
	 */
	private List<File> getFiles(String dirName) throws Exception {
		List<File> list = null;
		LOG.debug("Checking directory: " + dirName);
		File dir = new File(dirName);

		if (dir.isDirectory()) {
			File[] allFiles = dir.listFiles();
			list = new ArrayList<File>();
			for (File file : allFiles) {
				if (file.isFile() && !file.getName().endsWith(DONE)) {
					list.add(file);
				}
			}
			LOG.debug("No.of files under: " + list.size());
		}
		return list;
	}

	private void checkDirsExists() throws Exception {
		if (!new File(addDirPath).isDirectory()
				|| !new File(removeDirPath).isDirectory()) {
			throw new Exception("Directory is not found");
		}
	}

	private void updateCache() throws IOException {
		addNumbersSet = new TreeSet<String>();
		delNumbersSet = new TreeSet<String>();

		for (File file : addFiles) {

			Set<String> list = getSubscribersAsListFromFile(file);
			for (String subscriber : list) {
				mc.add(subscriber, 0);
			}
			addNumbersSet.addAll(list);
		}

		for (File file : deleteFiles) {

			Set<String> list = getSubscribersAsListFromFile(file);
			for (String subscriber : list) {
				mc.delete(subscriber);
			}
			delNumbersSet.addAll(list);
		}
	}

	private void updateMasterFile() throws FileNotFoundException, IOException {

		long stTime = System.currentTimeMillis();

		LOG.info("Add file size: " + addNumbersSet.size());
		LOG.info("Delete file size: " + delNumbersSet.size());

		FileReader fr = new FileReader(getLatestFile());
		BufferedReader br = new BufferedReader(fr);

		String masterFileWithExtn = getFileNameWithoutExtn(masterFile.getName());

		StringBuilder sb = new StringBuilder();
		sb.append(masterFile.getParent());
		sb.append(FileName.SEPARATOR);
		sb.append(masterFileWithExtn);
		sb.append("-");
		sb.append(getTimeStamp());

		File ffile = new File(sb.toString());
		FileWriter fw = new FileWriter(ffile);
		BufferedWriter bw = new BufferedWriter(fw);

		LOG.info("Updating master file.");
		try {

			String line = null;
			while ((line = br.readLine()) != null) {

				if (!delNumbersSet.contains(line)) {
					bw.write(line);
					bw.newLine();
					bw.flush();
				}

				if (addNumbersSet.contains(line)) {
					addNumbersSet.remove(line);
				}
			}

			for (String number : addNumbersSet) {
				bw.write(number);
				bw.newLine();
				bw.flush();
			}

		} catch (IOException ioe) {
			LOG.error("Filed to update master file");
			throw ioe;
		}

		long enTime = System.currentTimeMillis();

		LOG.info("Updated master file in time(ms): " + (enTime - stTime));
	}

	private void renameFiles() {

		for (File file : addFiles) {
			renameFile(addDirPath, file);
		}

		for (File file : deleteFiles) {
			renameFile(removeDirPath, file);
		}
	}

	private void renameFile(String dirName, File file) {
		String actualFilename = getFileNameWithoutExtn(file.getName());
		getTimeStamp();

		StringBuilder sb = new StringBuilder();
		sb.append(dirName).append(FileName.ROOT_PATH).append(actualFilename)
				.append(DONE);
		File processedFile = new File(sb.toString());
		boolean renamed = file.renameTo(processedFile);
		LOG.info("Renamed file: " + actualFilename
				+ " to .DONE extension. rename status: " + renamed);
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
			paramValue = parametersCacheManager.getParameter(
					iRBTConstant.DAEMON, param, defaultValue).getValue();
		} catch (Exception e) {
			LOG.error("Unable to get " + param);
			throw e;
		}
		return paramValue;
	}

	private static Set<String> getSubscribersAsListFromFile(File file)
			throws IOException {
		Set<String> set = new TreeSet<String>();
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			reader = new BufferedReader(isr);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String subscriberId = rbtdbManager.subID(line.trim());
				set.add(subscriberId);
			}

		} catch (FileNotFoundException fnfe) {
			LOG.error("Unable to locate file to convert subscriber list.");
			throw fnfe;
		} catch (IOException ioe) {
			LOG.error("Failed IO when converting file to subscriber list");
			throw ioe;
		} finally {
			try {
				fis.close();
				isr.close();
				reader.close();
			} catch (IOException ioe) {
				LOG.error("Failed IO when closing file");
				throw ioe;
			}
		}
		return set;
	}

	private File getLatestFile() {

		File file = new File(masterFilePath);
		File[] files = file.getParentFile().listFiles();

		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.lastModified()).compareTo(
						f2.lastModified());
			}
		});

		File latestFile = files[files.length - 1];
		LOG.info("Latest master file is: " + latestFile.getName());
		return latestFile;
	}

	public static void main(String[] args) throws Exception {
//		 ViralWhiteListDaemon vl = new ViralWhiteListDaemon();
//		 vl.start();
	}

}
