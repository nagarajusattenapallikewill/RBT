package com.onmobile.apps.ringbacktones.hunterFramework;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.onmobile.snmp.agentx.client.ManagedObjectCallback;
import com.onmobile.snmp.agentx.client.OID;

public class RBTVersionManagedObject extends ManagedObjectCallback
{
	Logger logger = Logger.getLogger(RBTVersionManagedObject.class);
	
	public RBTVersionManagedObject(String oid)
	{
		super(new OID(oid));
	}

	@Override
	public Object getValue()
	{
		String attrValue = "NA";
		try
		{
			String classPath = System.getProperty("java.class.path");
			logger.info("classpath is - " + classPath);
			
			StringTokenizer stk = new StringTokenizer(classPath, System.getProperty("path.separator"));
			logger.info("path.separator is - " + System.getProperty("path.separator"));
			while(stk.hasMoreTokens())
			{
				File file = new File(stk.nextToken());
				if(file.getName().equals("rbt.jar"))
				{
					logger.info("Found rbt.jar in classpath");
					
					ZipInputStream in = null;
					try {
						in = new ZipInputStream(new FileInputStream(file));
						ZipEntry zipEntry;
						
						while((zipEntry  = in.getNextEntry())!=null){
				            if (zipEntry.getName().equals("Manifest/RBTInstallManifest.txt")){
				            	logger.info("Found RBTInstallManifest.txt of rbt.jar");	
				            	Properties properties = new Properties();
				            	properties.load(in);
				            	attrValue = (String) properties.get("Product-Version");
				            	logger.info("Found Product-Version in manifets of rbt.jar");
						        logger.info("product-version in manifest "+ attrValue);
							    if(attrValue == null || attrValue.trim().length() == 0 || attrValue.trim().equals("${version}"))
						        	attrValue = "NA";
							    logger.info("final product-version is "+ attrValue);
						        return attrValue;
				            }
				        }
					}
					finally {
						try{
							if(in != null) { in.closeEntry(); in.close();}
						}
						catch(Exception e) {}
					}
					
				}	
			}
		}
		catch (Exception e)
		{
			logger.error("Exception caught while processing snmp get request, setting product version as NA", e);
			attrValue = "NA";
		}
		return attrValue;
	}
}
