package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.v2.dto.RbtNameTuneLoggerDTO;

public class NameTuneFileUtils extends NameTuneConstants {

	private static Logger logger = Logger.getLogger(NameTuneFileUtils.class);

	public static void writeNewRequestFile(List<RbtNameTuneLoggerDTO> nameTuneLoggerDTOs) {
		if (nameTuneLoggerDTOs != null && nameTuneLoggerDTOs.size() > 0) {
			File file = null;
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			String path = LOCAL_BASE_DIRECTORY + File.separator + NEW_REQ_DIR;
			try {
				file = new File(path);
				if (!file.exists()) {
					file.mkdir();
				}
				file = new File(path + File.separator + NEW_REQ_FILE_NAME + "_" + System.currentTimeMillis());
				file.createNewFile();
				// Assume default encoding.
				fileWriter = new FileWriter(file);

				// Always wrap FileWriter in BufferedWriter.
				bufferedWriter = new BufferedWriter(fileWriter);

				// Note that write() does not automatically
				// append a newline character.
				boolean isFirstLine = true;
				for (RbtNameTuneLoggerDTO nameTuneLoggerDTO : nameTuneLoggerDTOs) {
					if (isFirstLine) {
						isFirstLine = false;
						bufferedWriter.write(NEW_REQ_FILE_HEADER);
					}
					bufferedWriter.newLine();
					bufferedWriter.write(nameTuneLoggerDTO.getNewRequestLogFormat());
				}

			} catch (IOException ex) {
				System.out.println("Error writing to file '" + NEW_REQ_FILE_NAME + "'");

			} finally {
				try {
					if (bufferedWriter != null)
						bufferedWriter.close();
					if (fileWriter != null)
						fileWriter.close();
				} catch (IOException e) {
					logger.error("Error Trace " + ExceptionUtils.getFullStackTrace(e));
				}
			}
		} else {
			logger.fatal("NO DATA AVAILABLE TO CREATE FILE AT TIME:" + date);
		}
	}

	public static void logIntoFile(List<RbtNameTuneLoggerDTO> nameTuneLoggerDTOs, boolean isNewReqLog,
			boolean isComplLog, boolean isFailureLog) {
		if (nameTuneLoggerDTOs != null && nameTuneLoggerDTOs.size() > 0) {
			File file = null;
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			String basePath = LOCAL_BASE_DIRECTORY;
			String fileName = null;
			try {
				if (isNewReqLog) {
					basePath = basePath + File.separator + NEW_REQ_DIR;
					fileName = NEW_REQ_FILE_NAME;
				} else if (isComplLog) {
					basePath = basePath + File.separator + COMPLETED_REPORT_LOG_DIR;
					fileName = COMPLETED_REPORT_FILE_NAME;
				} else if (isFailureLog) {
					basePath = basePath + File.separator + FAILURE_REPORT_LOG_DIR;
					fileName = FAILURE_REPORT_FILE_NAME;
				}
				file = new File(basePath);
				if (!file.exists()) {
					file.mkdir();
				}
				file = new File(basePath + File.separator + fileName + "_" + System.currentTimeMillis() + ".log");
				file.createNewFile();
				fileWriter = new FileWriter(file);
				bufferedWriter = new BufferedWriter(fileWriter);

				boolean isFirstLine = true;
				for (RbtNameTuneLoggerDTO nameTuneLoggerDTO : nameTuneLoggerDTOs) {
					// First Line should be the Column names
					if (isFirstLine) {
						isFirstLine = false;
						if (isNewReqLog)
							bufferedWriter.write(NEW_REQ_FILE_HEADER);
						else
							bufferedWriter.write(NAME_TUNE_COMPLTED_FAILURE_HEADER);
					}
					bufferedWriter.newLine();

					if (isNewReqLog)
						bufferedWriter.write(nameTuneLoggerDTO.getNewRequestLogFormat());
					else
						bufferedWriter.write(nameTuneLoggerDTO.getStatusLogFormat());
				}

			} catch (IOException ex) {
				logger.error("Error writing to basepath: '" + basePath + " File:" + fileName + "'");

			} finally {
				try {
					if (bufferedWriter != null)
						bufferedWriter.close();
					if (fileWriter != null)
						fileWriter.close();
				} catch (IOException e) {
					logger.error("Error Trace " + ExceptionUtils.getFullStackTrace(e));
				}
			}
		} else {
			logger.fatal("NO DATA AVAILABLE TO CREATE FILE AT TIME:" + date);
		}
	}

	public static void deleteLocalFiles(boolean delReqLog, boolean delReportLog) {
		File folder = null;
		String newReqLogDir = LOCAL_BASE_DIRECTORY + File.separator + NEW_REQ_DIR;
		String completedLogDir = LOCAL_BASE_DIRECTORY + File.separator + COMPLETED_REPORT_LOG_DIR;
		String failLogDir = LOCAL_BASE_DIRECTORY + File.separator + FAILURE_REPORT_LOG_DIR;
		String[] listOfDirectories = null;
		if (delReqLog)
			listOfDirectories = new String[] { newReqLogDir };
		if (delReportLog)
			listOfDirectories = new String[] { completedLogDir, failLogDir };

		try {
			for (String dirName : listOfDirectories) {
				folder = new File(dirName);
				if (folder.isDirectory()) {
					File[] files = folder.listFiles();
					for (File file : files) {
						if (!file.delete()) {
							logger.error("EORROR : FAILED TO DELETE FILE AFTER UPLOADING , FILE NAME"
									+ file.getCanonicalPath());
						} else {
							logger.debug("SUCCESSFULLY DELETED" + file.getCanonicalPath());
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("EXCEPTION :" + e.getMessage(), e);
		}
	}
}