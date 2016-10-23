package com.onmobile.apps.ringbacktones.promotions.viral;
import java.io.File;
import java.io.Serializable;

/**
 * @author sridhar.sindiri
 *
 */
public class FileInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2090382383612346962L;
	private long fileSize = 0;
	private long location = 0;
	private String fileName = null;
	private transient File objectPath = null;
	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getLocation() {
		return location;
	}

	public void setLocation(long location) {
		this.location = location;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public File getObjectPath() {
		return objectPath;
	}

	public void setObjectPath(File objectPath) {
		this.objectPath = objectPath;
	}
	
	public void save() {
		FileInfoContainer.save(this);
	}

	public boolean hasToBeProcessed() {
		File cdrFile = new File(getFileName());
		boolean result = (cdrFile.length() > getFileSize());
		
//		Tools.logDetail("FileName:"+getFileName()+", previous length:"+getFileSize()+",actual:"+cdrFile.length()+", pointer:" + location);
		return result;
	}
}
