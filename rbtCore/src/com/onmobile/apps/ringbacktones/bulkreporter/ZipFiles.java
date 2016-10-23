package com.onmobile.apps.ringbacktones.bulkreporter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * 
 */

/**
 * @author vinayasimha.patil
 *
 */
public class ZipFiles 
{
	private static Logger logger = Logger.getLogger(ZipFiles.class);
	
	static final int BUFFER = 2048;
	public static File zipFiles(String zipFileName, File[] files, String mode)
	{
		BufferedInputStream origin = null;
		FileOutputStream dest = null;
		ZipOutputStream out = null;
		try 
		{
			dest = new FileOutputStream(zipFileName);
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			for (int i = 0; i < files.length; i++) 
			{
				logger.info("Adding: "+files[i]);
				FileInputStream fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER);

				String entryName = files[i].getName();
				if(mode.equalsIgnoreCase("activation"))
					entryName = files[i].getParentFile().getParentFile().getName() +"\\"+ files[i].getName();
				else if(mode.equalsIgnoreCase("deactivation"))
					entryName = (i+1) +"_"+ files[i].getName();
				
				ZipEntry entry = new ZipEntry(entryName);
				out.putNextEntry(entry);

				int count;
				while((count = origin.read(data, 0, BUFFER)) != -1)
				{
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
			File zipFile = new File(zipFileName);
			return zipFile;
		}
		catch (FileNotFoundException e)
		{
			logger.error("", e);
			return null;
		}
		catch (IOException e)
		{
			logger.error("", e);
			return null;
		}
	}
}
