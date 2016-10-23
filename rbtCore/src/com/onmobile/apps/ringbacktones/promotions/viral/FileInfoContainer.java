package com.onmobile.apps.ringbacktones.promotions.viral;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author sridhar.sindiri
 *
 */
public class FileInfoContainer {
	private static Logger logger = Logger.getLogger(FileInfoContainer.class);
	
	private static HashMap<String, FileInfo> fileInfoMap = null;
	private static String dirLocation = null;

	public static void load() 
	{
		if (fileInfoMap != null)
			return;
		
		fileInfoMap = new HashMap<String, FileInfo>();
		dirLocation = RBTViralConfigManager.getInstance().getParameter("OBJECT_FILES_DIRECTORY");
		File dir = new File(dirLocation);
		File files[] = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				FileInfo fileInfo = readObject(file);
				String absolutePath = null;
				if (fileInfo != null)
					absolutePath = fileInfo.getFileName();
				if (absolutePath != null) {
					fileInfoMap.put(absolutePath, fileInfo);
				}
			}
		}

	}

	private static FileInfo readObject(File file) 
	{
		FileInputStream fis = null;
		FileInfo fi = null;
		try {
			fis = new FileInputStream(file);
			ObjectInputStream os = new ObjectInputStream(fis);
			fi = (FileInfo) os.readObject();

		} catch (Exception e) {
			logger.error("", e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
				// ignore
			}
		}
		File actualFile = new File(fi.getFileName());
		if (!actualFile.exists()) {
			file.delete();
			return null;
		}
		fi.setObjectPath(file);
		return fi;
	}

	public static void save(FileInfo fileInfo) 
	{
		File objFilePath = fileInfo.getObjectPath();
		if (objFilePath == null) {
			String objFile = fileInfo.getFileName();
			objFile = objFile.replace('/', '_').replace('\\', '_').replace(':',
					'_')
					+ ".store";

			objFilePath = new File(dirLocation, objFile);
			fileInfo.setObjectPath(objFilePath);
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(objFilePath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(fileInfo);

		} catch (Exception e) {
			logger.error("", e);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public static FileInfo getFileInfo(String absolutePath) 
	{
		FileInfo fileInfo = fileInfoMap.get(absolutePath);
		if(fileInfo == null)
		{
			fileInfo = new FileInfo();
			fileInfo.setFileName(absolutePath);
			fileInfoMap.put(absolutePath, fileInfo);
		}
		return fileInfo;
	}

}
