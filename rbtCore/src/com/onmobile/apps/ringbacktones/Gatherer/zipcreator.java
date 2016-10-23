/*
 * 
 *  
 */
package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;

/**
 * @version 1.0, 01/10/05
 * @author mohsin
 *  
 */
public class zipcreator
{
	private static Logger logger = Logger.getLogger(zipcreator.class);
	
    private String m_cust;
    private String m_site;
    private String m_datename;
    private String hostname=null;
    
    RbtGenericCacheWrapper rbtGenericCacheWrapper = null;

    public static boolean dir2zip(ZipOutputStream out, String dname)
    {
        File dir = new File(dname);
        String[] filesToZip = dir.list();
        if (filesToZip == null || filesToZip.length <= 0)
        {
           logger.info(dname
                    + " does not have any files");
            return false;
        }
        filesToZip = dir.list();
        File tmp_file;
        /*
         * try { out.putNextEntry(new ZipEntry (dname + "/")); } catch
         * (IOException ioe) { ioe.printStackTrace(); }
         */for (int i = 0; i < filesToZip.length; i++)
        {
            String fname = dname + File.separator + filesToZip[i];
            logger.info("RBTGatherer zip file trying to be zipped "
                                       + fname);
            tmp_file = new File(fname);
            if (tmp_file.isDirectory())
            {
                /*
                 * File[] f = tmp_file.listFiles(); for(int j=0; j
                 * <f.length;j++) { Tools.logDetail("_class", "_class",
                 * "RBTGatherer zip file CHECKING is directory
                 * "+f[j].getName()); if(f[j].isDirectory()) return false; }
                 */dir2zip(out, fname);
            }
            else
            {
                add2zip(out, fname, dname);
            }
        }

        return true;
    }

    public static boolean add2zip(ZipOutputStream out, String fname,
            String dname)
    {
        try
        {
            String name = null;
            if (dname != null)
            {
                name = new File(dname).getName() + "/"
                        + new File(fname).getName();
            }
            else
            {
                name = new File(fname).getName();
            }

            out.putNextEntry(new ZipEntry(name));
            FileInputStream in = new FileInputStream(new File(fname));
            int len;
            byte[] buffer = new byte[18024];
            while ((len = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            in.close();
            return true;
        }
        catch (IllegalArgumentException iae)
        {
            iae.printStackTrace();
        }
        catch (FileNotFoundException fnfe)
        {
            fnfe.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return false;
    }

    public String createzip(String fileNamePrefix)
    {

        if (RBTGatherer.folders_to_zip.size() == 0)
            return null;

        getparams();
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);//yesterday
        String m_datename = Tools.getDateAsName(cal.getTime());

        boolean zipped = false;
        String zipFileName = "";
        if (fileNamePrefix != null)
            zipFileName = zipFileName.trim() + fileNamePrefix + "-";

        zipFileName =  getParamAsString("GATHERER", "GATHERER_PATH", null) + "/" + zipFileName.trim() + "RBTGatherer_" +
        getParamAsString("COMMON", "CUST_NAME", null) + "_" + getParamAsString("COMMON", "SITE_NAME", null) + "_" + m_datename+ ".zip";

        try
        {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                    zipFileName));
            out.setLevel(Deflater.BEST_COMPRESSION);

            if (fileNamePrefix == null)
            {
                File[] files = new File( getParamAsString("GATHERER", "GATHERER_PATH", null)).listFiles();
                if (files != null && files.length > 0)
                {
                    for (int j = 0; j < files.length; j++)
                    {
                        if (files[j].getName().endsWith(".htm")
                                || files[j].getName().endsWith(".cfg")
                                || files[j].getName().endsWith(".log")
                                || files[j].getName().endsWith(".xml"))
                        {
                            add2zip(out, getParamAsString("GATHERER", "GATHERER_PATH", null) + "/"
                                    + files[j].getName(), null);
                            files[j].delete();
                        }
                    }
                }
                for (int i = 0; i < RBTGatherer.folders_to_zip.size(); i++)
                {
                    String zip_folder = String
                            .valueOf(RBTGatherer.folders_to_zip.get(i));
                    logger.info("Adding to zip folder : " + zip_folder);
                    zipped = dir2zip(out, zip_folder);
                    if (zipped)
                        delete(zip_folder);
                    zipped = false;
                }

            }
            else
            {
                zipped = dir2zip(out,  getParamAsString("GATHERER", "GATHERER_PATH", null) + "/" + "db-Full");
                if (zipped)
                    delete(getParamAsString("GATHERER", "GATHERER_PATH", null) + "/" + "db-Full");
            }
            out.close();
        }
        catch (FileNotFoundException fnfe)
        {
            fnfe.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        return zipFileName;
    }
    public String createzipForTonePlayers(String fileNamePrefix)
    {

        if (CDRReprterForTonePlayers.folders_to_zip.size() == 0)
            return null;

        getparamsForTonePlayers();

        boolean zipped = false;
        String zipFileName = "";
        if (fileNamePrefix != null)
            zipFileName = zipFileName.trim() + fileNamePrefix + "-";
        
        try {
			InetAddress localHost=InetAddress.getLocalHost();
			hostname=localHost.getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        zipFileName = CDRReprterForTonePlayers.m_gathererPath + "\\" + zipFileName.trim()
                + "RBTGatherer_" + m_cust + "_" + m_site + "_" +hostname+"_"+ m_datename
                + ".zip";

        try
        {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                    zipFileName));
            out.setLevel(Deflater.BEST_COMPRESSION);

            if (fileNamePrefix == null)
            {
                File[] files = new File(CDRReprterForTonePlayers.m_gathererPath).listFiles();
                if (files != null && files.length > 0)
                {
                    for (int j = 0; j < files.length; j++)
                    {
                        if (files[j].getName().endsWith(".htm")
                                || files[j].getName().endsWith(".cfg")
                                || files[j].getName().endsWith(".log")
                                || files[j].getName().endsWith(".xml"))
                        {
                            add2zip(out, CDRReprterForTonePlayers.m_gathererPath + "/"
                                    + files[j].getName(), null);
                            files[j].delete();
                        }
                    }
                }
                for (int i = 0; i < CDRReprterForTonePlayers.folders_to_zip.size(); i++)
                {
                    String zip_folder = String
                            .valueOf(CDRReprterForTonePlayers.folders_to_zip.get(i));
                    logger.info("Adding to zip folder : " + zip_folder);
                    zipped = dir2zip(out, zip_folder);
                    if (zipped)
                        delete(zip_folder);
                    zipped = false;
                }

            }
            else
            {
                zipped = dir2zip(out,  getParamAsString("GATHERER", "GATHERER_PATH", null) + "/"
                        + "db-Full");
                if (zipped)
                    delete(getParamAsString("GATHERER", "GATHERER_PATH", null) + "/" + "db-Full");
            }
            out.close();
        }
        catch (FileNotFoundException fnfe)
        {
            fnfe.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        return zipFileName;
    }

    private void delete(String dir)
    {
        File delete_folder = new File(dir);
        String[] files_to_delete = delete_folder.list();
        for (int i = 0; i < files_to_delete.length; i++)
        {
            new File(dir + File.separator + files_to_delete[i]).delete();
        }
        delete_folder.delete();
    }

    private void getparams()
    {
    	rbtGenericCacheWrapper = RbtGenericCacheWrapper.getInstance();
       
    }
    private void getparamsForTonePlayers()
    {
    	rbtGenericCacheWrapper = RbtGenericCacheWrapper.getInstance();
    	String tmp=null;String cust=null;
    	String site=null;
    	HashMap m_params=CDRReprterForTonePlayers.m_params;
    	try {
			if (m_params.containsKey("CUST_NAME"))
			{
				tmp = (String) m_params.get("CUST_NAME");
				if (tmp != null && tmp.length() > 0)
				{
					m_cust=tmp;
					}
			}
			if (m_params.containsKey("SITE_NAME"))
			{
				tmp = (String) m_params.get("SITE_NAME");
				if (tmp != null && tmp.length() > 0)
				{
					m_site=tmp;
					}
			}
		} catch (Throwable E)
		{
			logger.error("", E);
			E.printStackTrace();
		}
       
        if (m_cust == null)
        {
            logger.info(" cust_name not present");
        }

        
        if (m_site == null)
        {
            logger.info(" site_name not present");
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);//yesterday
        m_datename = Tools.getDateAsName(cal.getTime());
    }
    
    public String getParamAsString(String type, String param, String defualtVal)
    {
    	try{
    		return rbtGenericCacheWrapper.getParameter(type, param, defualtVal);
    	}catch(Exception e){
    		logger.info("Unable to get param ->"+param +"  type ->"+type);
    		return defualtVal;
    	}
    }
}