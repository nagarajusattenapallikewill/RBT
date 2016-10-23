/*
 * Created on Nov 30, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.Reporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;


/**
 * @author Mohsin
 *  
 */
public class MorpheusUpdater
{
	private static Logger logger = Logger.getLogger(MorpheusUpdater.class);
	
    private static class ParamHolder
    {
        public String strType = null;
        public Object oVal = null;
        public void setParam(String type,Object Val)
        {
            strType = type;
            oVal = Val;
        }
 
    }
    private static String m_strDBURL = "";
    private static String m_strCustDBURL = "";
    private String m_customer = "vodafone";

    private Connection m_conn = null;
    //	private Connection m_morpheusConn = null;

    private Statement m_stmt1 = null;
    private Statement m_stmt2 = null;
    //	private Statement m_morpheusStmt = null;

    private ResultSet m_rs = null;
    private ResultSet m_morpheusRs = null;

    private String m_db_root = "data";
    private String m_db_end = "db";
    private String m_cdr_dir = "cdr";
    private String m_sqlldr_uid = null;
    private long m_SUBS_PER_QUERY;//=50000;
    private int m_no_months_data_2delete;
    private boolean num_max = false;
    private static String REMARKS_TYPE_DB_ERROR = "DB ERROR";
    private boolean updateSelTataFlag = true;
    private SimpleDateFormat fullDateSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MorpheusUpdater(String customer)
    {
        m_customer = customer;
    }

    public boolean init(ResourceBundle bundle)
    {
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        }
        catch (SQLException e)
        {
            logger.error("", e);
            return false;
        }
        catch (ClassNotFoundException cnfe)
        {
            logger.error("", cnfe);
            return false;
        }
        logger.info("morpheus initialized");

        createResource(bundle);

        return true;
    }

    private boolean createResource(ResourceBundle bundle)
    {
        try
        {
            m_strDBURL = bundle.getString("M0RPHEUS_ORACLE_DB_URL");
		//  m_strDBURL = "jdbc:oracle:thin:mmptest/mmptest@172.16.2.21:1525:mmptest";

        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        try
        {
		//	m_strCustDBURL ="jdbc:oracle:thin:mmptest/mmptest@172.16.2.21:1525:mmptest";
             m_strCustDBURL = bundle.getString("M0RPHEUS_ORACLE_"
                     + m_customer.toUpperCase() + "_DB_URL");
        }
        catch (Exception E)
        {
            logger.info("Customer DB URL Not in properties file exiting");
            return false;
        }

        try
        {
            long tmp = Long.parseLong(bundle.getString("SUBS_PER_QUERY"));
//          m_SUBS_PER_QUERY = tmp;
//			long tmp = Long.parseLong(""+50000);
            m_SUBS_PER_QUERY = tmp;
        }
        catch (Exception e)
        { /* Do Nothing */
        }

        try
        {
            int tmp = Integer.parseInt(bundle
                    .getString("NO_MONTHS_OLD_DATA_2DELETE"));
//		     int tmp = Integer.parseInt(""+6);
            m_no_months_data_2delete = tmp;
        }
        catch (Exception E)
        {
            m_no_months_data_2delete = 6;
        }

        try
        {
//			long tmp = Long.parseLong(""+50000);
            long tmp = Long.parseLong(bundle.getString("SUBS_PER_QUERY"));
            m_SUBS_PER_QUERY = tmp;
        }
        catch (Exception e)
        { /* Do Nothing */
        }

        try
        {
//		    String tmp ="mmptest/mmptest@mmptest";
            String tmp = bundle.getString("SQLLDR_" + m_customer.toUpperCase()
                    + "_UID");
            m_sqlldr_uid = tmp;
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

        try
        {
            m_conn = getConnection();
            //	m_morpheusConn = getMorpheusConnection();
            //	m_morpheusStmt = m_morpheusConn.createStatement();
            m_stmt1 = m_conn.createStatement();
            m_stmt2 = m_conn.createStatement();
        }
        catch (Exception e)
        {
            logger.error("", e);
            return false;
        }
        return true;
    }
	public int startUpdate_ConnInAutoCommitFale(String zipfile, int db_collection_days, String customer)
	{
		boolean bSuccessInsertion = false;
		int iReturnValue = 0;
		try
		{
			//m_conn.setAutoCommit(false);
			iReturnValue = startUpdate(zipfile, db_collection_days, customer);
			if (iReturnValue == -1)
			{
				bSuccessInsertion = true;
			}
		}
		catch (Throwable t)
		{
            logger.error("", t);
			bSuccessInsertion = false;
		}
		finally
		{
			try{
				if (bSuccessInsertion == false)
				{
					logger.info("DATAWARE HOUSE PROCESS HAS FAILED ::");
					m_conn.rollback();
				}
				else
				{
					logger.info("DATAWARE HOUSE PROCESS WAS SUCCESSFULL ::");
					m_conn.commit();
				}
			}catch(Exception e)
			{
				logger.info("DATAWARE HOUSE PROCESS HAS FAILED SOME EXCEPTION :: ");
				logger.error("", e);
			}
		}
		return iReturnValue;
	}
    public int startUpdate(String zipfile, int db_collection_days, String custInDb)	
    {
        boolean flagTata = false;
        Connection morp_conn1 = null;
        try
        {
            morp_conn1 = getMorpheusConnection();
        }
        catch (Exception e)
        {
        }
        StringTokenizer underscores = new StringTokenizer(zipfile, "_");
        // eliminate extra tokens if the base file name itself has _s in it
        StringTokenizer prefixToks = new StringTokenizer(RBTReporter.ftpFilePrefix, "_");
        prefixToks.nextToken(); // eliminate the 1 token that is already considered
        while (prefixToks.hasMoreTokens()) {
        	underscores.nextToken();
        	prefixToks.nextToken();
        }

        underscores.nextToken();
        String cust = underscores.nextToken().toLowerCase();
        logger.info("cust" + cust);
        String site = underscores.nextToken().toLowerCase();
        String site_fordb = site;
        if(site.equalsIgnoreCase("gujarat"))
        {
        	site_fordb = "gujrat";
        }
        if(site.equalsIgnoreCase("westbengal"))
        {
            site_fordb = "wb";
        }
        if(site.equalsIgnoreCase("ap"))
        {
            site_fordb = "hyderabad";
        }
        //modified by yuva..
        
        if(custInDb.equalsIgnoreCase("idea"))
        {
	        if(site.equalsIgnoreCase("rajasthan"))
	        {
	            site_fordb = "jaipur";
	        }
        }
        if(site.equalsIgnoreCase("kerela"))
        {
            site_fordb = "Kerala";
        }
        String day = underscores.nextToken();
        day = day.substring(0, day.lastIndexOf("."));//eliminate extn
        //create control file generator.
        File upload_file = new File("./" + m_db_root + File.separator + cust
                + "-" + site + File.separator + day);
        //String subs_mode =null;
        String siteid = getSiteCode(custInDb, site_fordb);
        /*
         * int db_collection_days = 1; try { subs_mode
         * =getCfgValue(upload_file,"SUBS_MODEL"); db_collection_days =
         * Integer.parseInt(getCfgValue(upload_file,"DB_COLLECTION_DAYS")); }
         * catch(Exception e) { db_collection_days = 1; }
         */
        //to be removed after
        //System.out.println("CAHNGED>>>>>>>>>>>>>>>");
        //db_collection_days = 3;
        logger.info("DB_COLLECTION_DAYS for " + custInDb + "-"
                + site + " is " + db_collection_days);
        int no_days_data_to_load = 0;

        if(RBTReporter.m_no_of_days_toload == null)
        {
            no_days_data_to_load = getNoDaysData2Load(siteid);
        }
        else
        {
            no_days_data_to_load = Integer.parseInt(RBTReporter.m_no_of_days_toload);
            
        }
        
        if (no_days_data_to_load == 0
                || no_days_data_to_load > db_collection_days)
        {
            logger.info("Not loading for " + cust + "-"
                    + site + " as " + no_days_data_to_load
                    + " days data missing and only " + db_collection_days
                    + " data in flat file");
            return 0;
        }

        logger.info("Data to be loaded for "
                + no_days_data_to_load + " for " + cust + "-" + site);
        String min_date = null;

        //for cdr folder:-
        File cTemp = new File("./" + m_db_root + File.separator + cust + "-"
                + site + File.separator + day + File.separator + m_cdr_dir);
        if (!cTemp.exists())
        {
            logger.info("CDR file not present in extracted zip files");
        }
        else
        {
            File[] clist = cTemp.listFiles();
            String cdr_Str = clist[0].getName();
            //System.out.println("The value of the clist.length() is "+clist.length);
            if(cust.equalsIgnoreCase("esia"))
            {
            	 //System.out.println("The value of the customer is "+cust);
            	 createCDRFile(clist, cTemp,no_days_data_to_load);
            }
            
            else
            {
            	//System.out.println("Entered else");
	            if (!cdr_Str.equalsIgnoreCase("CDR_Report.csv"))
	            {
	                File incr_sub_file = new File(cTemp + "\\" + "CDR_Report_"
	                        + db_collection_days + ".csv");
	                if (incr_sub_file.exists())
	                {
	                    logger.info("Creating CDR_Report.csv for " + cust + "-"
	                                            + site);
	                    createCDRFile(incr_sub_file, no_days_data_to_load);
	                }
	            }
            }
        }

        if (siteid == null)
        {
            logger.info("COULD NOT GET SITE ID FROM DB for:" + custInDb
                                        + "-" + site);
            return 1;
        }

        String sTemp = "./" + m_db_root + File.separator + cust + "-" + site
                + File.separator + day + File.separator + m_db_end;
        File fTemp = new File(sTemp);

        if (!fTemp.exists())
        {
            logger.info("DB file not present in extracted zip file:"
                                    + fTemp);
            return 1;
        }

        logger.info("uploading to morpheus only one file"
                + zipfile);

        if (siteid == null)
        {
            logger.info("COULD NOT GET SITE ID FROM DB for:" + custInDb
                                        + "-" + site);
            return 1;
        }
        ControlFileGenerator control_files = new ControlFileGenerator(siteid,
                this);

        if (!m_customer.equalsIgnoreCase("tata"))
        {
        	
            loadCsv(siteid, upload_file, control_files);
            
        }

        //For tata in cremental mode
        if (cust.equalsIgnoreCase("tata"))
        {
        	
            File incr_sub_file = new File(sTemp + "\\" + "RBT_DEACTIVATED_SUBSCRIBERS_" + db_collection_days + ".txt");
            if (incr_sub_file.exists())
            {
                logger.info("Creating RBT_SUBSCRIBERS_Temp.txt with RBT_DEACTIVATED_SUBSCRIBERS.txt FOR "+ cust + "-" + site);
                File incr_sub = createDeactivatedSubFile(incr_sub_file, no_days_data_to_load);
                File Temp = updateDeactivatedSubscriber(incr_sub, siteid);
                logger.info("puja : Temp For Deactivated Subsc" + Temp);
                String[] ctl_inf = control_files.createStagingControlFile(Temp, siteid, 0);
                loadDataUsingSqlldr(ctl_inf[0], "indirect");
            }

            incr_sub_file = new File(sTemp + "\\" + "RBT_DELETED_SELECTIONS_"
                    + db_collection_days + ".txt");
            if (incr_sub_file.exists())
            {
                logger.info("Creating RBT_SUBSCRIBER_SELECTIONS_TEMP.txt with RBT_DELETED_SELECTIONS.txt for "
                                           + cust + "-" + site);
                File incr_sub = createDeactivatedSelFile(incr_sub_file,
                                                         no_days_data_to_load);
                File Temp = updateDeactivatedSelections(incr_sub, siteid);
                String[] ctl_inf = control_files
                        .createStagingControlFile(Temp, siteid, 0);
                loadDataUsingSqlldr(ctl_inf[0], "indirect");
            }

            incr_sub_file = new File(sTemp + "\\" + "RBT_SUBSCRIBER_"
                    + db_collection_days + ".TXT");
            if (incr_sub_file.exists())
            {
                logger.info("Creating RBT_SUBSCRIBER_TEMP.txt for " + cust
                                        + "-" + site);
                File Temp = createSubscriberFile(incr_sub_file,
                                                 no_days_data_to_load, siteid);
                logger.info("Creating RBT_SUBSCRIBER.txt for " + cust + "-"
                                        + site);
                updateSubscriber(Temp, siteid);
            }

            incr_sub_file = new File(sTemp + "\\"
                    + "RBT_SUBSCRIBER_SELECTIONS_" + db_collection_days
                    + ".TXT");
            if (incr_sub_file.exists())
            {
                updateSelTataFlag = checkActiveSelections(no_days_data_to_load,
                                                          siteid);
                if (updateSelTataFlag)
                {
                    logger.info("Creating RBT_SUBSCRIBER_SELECTIONS.txt for "
                                            + cust + "-" + site);
                    File Temp = createSelectionsFile(incr_sub_file,
                                                     no_days_data_to_load,
                                                     siteid);
                    logger.info("Creating RBT_SUBSCRIBER.txt for " + cust
                                            + "-" + site);
                    updateSelection(Temp, siteid);
                }
                else
                {
                    logger.info("Creating RBT_SUBSCRIBER_SELECTIONS.txt for "
                                            + cust + "-" + site);
                    File Temp = createSelectionsFile(incr_sub_file,
                                                     no_days_data_to_load,
                                                     siteid);
                }
            }
            
        }
        else
        {
            File incr_sub_file = new File(sTemp + "\\" + "RBT_SUBSCRIBER_"+ db_collection_days + ".TXT");
            
        	 if (incr_sub_file.exists())
            {
                logger.info("Creating RBT_SUBSCRIBER_TEMP.txt for " + cust+ "-" + site);
                File Temp = createSubscriberFile(incr_sub_file,no_days_data_to_load, siteid);
				if(Temp.length()<=0){
					logger.info(" FILE IS EMPTY FOR "+cust+"-"+site+" and Temp is :"+Temp.getAbsolutePath());
					return 0;
				}
                logger.info("Creating RBT_SUBSCRIBER.txt for " + cust + "-"+ site);
                updateSubscriber(Temp, siteid);
            }
           

            incr_sub_file = new File(sTemp + "\\" + "RBT_SUBSCRIBER_SELECTIONS_" + db_collection_days+ ".TXT");
            if (incr_sub_file.exists())
            {
                logger.info("Creating RBT_SUBSCRIBER_SELECTIONS_TEMP.txt for "+ cust + "-" + site);
                File Temp = createSelectionsFile(incr_sub_file,no_days_data_to_load, siteid);
				if(Temp.length()<=0){
					logger.info(" FILE IS EMPTY FOR "+cust+"-"+site+" and Temp is :"+Temp.getAbsolutePath());
					return 0;
				}
                logger.info("Creating RBT_SUBSCRIBER_SELECTIONS.txt for "+ cust + "-" + site);
                updateSelection(Temp, siteid);
            }
            
            File incr_viral_file = new File(sTemp + "\\"
                    + "RBT_VIRAL_SMS_TABLE_" + db_collection_days + ".TXT");
            if (incr_viral_file.exists())
            {
                logger.info("Creating RBT_VIRAL_SMS_TABLE.txt for " + cust
                                        + "-" + site);
                createViralFile(incr_viral_file, no_days_data_to_load, siteid);
            }
            
        }

        File[] file_list = fTemp.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                if (name.startsWith("RBT") && name.endsWith(".txt"))
                {
                    logger.info("file:" + name
                            + " accepted");
                    return true;
                }
                else
                {
                    logger.info("file:" + name
                            + " NOT accepted");
                    return false;
                }
            }
        });
        
        System.out.println("The final check for teh db_collection_days ---->> "+db_collection_days);
        if (file_list == null || file_list.length == 0)
        {
            logger.info("No flat data files in:"
                    + upload_file);
            return 1;
        }
        else
        {
            for (int k = 0; k < file_list.length; k++)
            {
                if (file_list[k].getName().equalsIgnoreCase("RBT_DEACTIVATED_SUBSCRIBERS_"+ db_collection_days + ".txt"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_DELETED_SELECTIONS_"+ db_collection_days + ".txt"))
                    continue;
                //VIRAL SMS CHANGE ONLY IF -CONDITION : -
                if (file_list[k].getName().equalsIgnoreCase("RBT_VIRAL_SMS_TABLE_"+ db_collection_days + ".txt"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_DELETED_SELECTIONS.txt"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_DEACTIVATED_SUBSCRIBERS.txt"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("ALL_RBT_DEACTIVATED_SUBSCRIBERS.txt"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("ALL_RBT_DELETED_SELECTIONS.txt"))
                    continue;


                if (file_list[k].getName().equalsIgnoreCase("ALL_RBT_CATEGORIES.txt"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_SITE_PREFIX.txt"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_CHARGING_CLASS.TXT"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIPTION_CLASS.TXT"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_"+ db_collection_days + ".TXT"))
                    continue;

                if (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS_"+ db_collection_days + ".TXT"))
                    continue;

                // PRONEEL Temp execute only RBT_SUBS_SELECTIONS
               /* if (!file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS.TXT"))
                {
                	continue;
                }*/

                if (min_date == null)
                {
                    if (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS.TXT"))
                    {
                        min_date = getMinDate(file_list[k]);
                    }
                }
                /*
                 * if(!m_customer.equalsIgnoreCase("tata")) {
                 * 
                 * if(file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS.TXT")) {
                 * deleteSelections(file_list[k],siteid); }
                 * if(file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER.TXT")) {
                 * deleteSubscriber(file_list[k],siteid); } }
                 */
                // 				if(file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_TEMP.txt")
                // ||
                // (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS_TEMP.txt")
                // && !m_customer.equalsIgnoreCase("tata")))
                if (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_TEMP.txt") || (file_list[k].getName().equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS_TEMP.txt") && (updateSelTataFlag == true)))
                {
                    int check = 0;
                    if (m_customer.equalsIgnoreCase("tata"))
                        check = 1;
                    String[] ctl_inf = control_files
                            .createStagingControlFile(file_list[k], siteid,
                                                      check);
                    loadDataUsingSqlldr(ctl_inf[0], "indirect");
                    continue;
                }
                if (m_customer.equalsIgnoreCase("tata")
                        && file_list[k]
                                .getName()
                                .equalsIgnoreCase(
                                                  "RBT_SUBSCRIBER_SELECTIONS_TEMP.txt"))
                    continue;
             
                //create control file for this flat file.
                String[] ctl_info = control_files
                        .createControlFile(file_list[k], siteid);
                String fileName = file_list[k].getName();
                if (ctl_info == null)
                {
                    logger.info("ERROR IN GENERATING CONTROL FILE FOR:"
                                                + file_list[k]);
                    continue;
                }
            //    logger.info("puja : ctl_info: check : "+ctl_info[1]);

                // 	       		if((subs_mode.equalsIgnoreCase("full") &&
                // (!m_customer.equalsIgnoreCase("tata")))
                // ||(!ctl_info[1].equals("rbt_subscriber_report")&&!ctl_info[1].equals("rbt_subs_selections_report")))
                //VIRAL SMS CHANGE ONLY IF -CONDITION : -
                
                if (!ctl_info[1].equals("rbt_subscriber_report")
                        && !ctl_info[1].equals("rbt_subs_selections_report")
                        && !ctl_info[1].equals("RBT_VIRAL_SMS_TABLE"))
                {
                    logger.info("table_name from which to be deleted"
                                            + ctl_info[1]);
                    int deleted = deleteOldRecords(ctl_info[1], siteid,
                                                   min_date);
                    if (deleted == -1)
                    {
                        logger.info("COULD NOT DELETE OLD RECORDS from table "
                                                    + ctl_info[1]
                                                    + " for site id:" + siteid);
                        Tools.addToAuditTable(morp_conn1, siteid,
                                              "ISSUE_WARNING",
                                              "Deletion Error " + ctl_info[1],
                                              REMARKS_TYPE_DB_ERROR);
                        continue;
                    }else
					{
						try{
							m_conn.commit();
							m_conn.setAutoCommit(false);
						}catch(Exception e)
						{
							 logger.error("", e);
							System.out.println("ERROR E ::: POOJA POOJA :::: "+e);
						}
					}

                }
               
                String mode = "direct";
                if (ctl_info[1].equals("rbt_subscriber_report")
                        || ctl_info[1].equals("rbt_subs_selections_report")
                        || ctl_info[1].equals("RBT_VIRAL_SMS_TABLE"))
                {
                    mode = "indirect";
                }

                /*
                 * if((!m_customer.equalsIgnoreCase("tata")) &&
                 * (ctl_info[1].equals("rbt_subscriber_report") ||
                 * ctl_info[1].equals("rbt_subs_selections_report"))) { String[]
                 * ctl_inf =
                 * control_files.createStagingControlFile(file_list[k], siteid,
                 * 2); loadDataUsingSqlldr(ctl_inf[0], mode); }
                 */
                //		if(m_customer.equalsIgnoreCase("tata") &&
                // ctl_info[1].equals("rbt_subs_selections_report"))
                if (m_customer.equalsIgnoreCase("tata")
                        && ctl_info[1].equals("rbt_subs_selections_report")
                        && (updateSelTataFlag == false))
                {
                    String[] ctl_inf = control_files
                            .createStagingControlFile(file_list[k], siteid, 1);
                    loadDataUsingSqlldr(ctl_inf[0], mode);
                }

                loadDataUsingSqlldr(ctl_info[0], mode);

            }//end for (file list)

            //updateCDR_Data(siteid);
        }

        return -1;
    }

    //For CDR
    private void createCDRFile(File file, int days)
    {
        Calendar cal = Tools.getCalendarInstance();
       
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
        //    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Date end_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -days);

        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        logger.info("Collecting data from "
                + start_data_collection + " to " + end_data_collection);

        StringBuffer sb = new StringBuffer();

        try
        {
            LineNumberReader fin = new LineNumberReader(new FileReader(file));
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");
            //			logger.info("Line Number
            // "+fin.getLineNumber());

            String str;

            while ((str = fin.readLine()) != null)
            {
                if (str.startsWith("H"))
                {
                    sb.append(str + "\n");
                    continue;
                }
                Date report_date = new Date();
                String dateStr = str.substring(0, str.indexOf(","));
                try
                {
                    report_date = sdf1.parse(dateStr);
                    //					logger.info("report_date in
                    // cdr_report "+report_date);
                }
                catch (Exception pe)
                {
                    logger.error("", pe);
                }

                if (report_date != null
                        && (report_date.equals(start_data_collection) || (report_date
                                .after(start_data_collection) && report_date
                                .before(end_data_collection))))
                {
                    sb.append(str + "\n");
                    continue;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

        //write to CDR_Report.csv
        try
        {
            File cdr = new File(file.getAbsolutePath()
                    .substring(0, file.getAbsolutePath().lastIndexOf("CDR_"))
                    + "CDR_Report.csv");
            logger.info("cdr: " + cdr);
            if (!cdr.exists())
                cdr.createNewFile();

            FileOutputStream fout = new FileOutputStream(cdr);
            fout.write(sb.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

    }
    
    private void createCDRFile(File [] files,File filepath, int days)
    {
        Calendar cal = Tools.getCalendarInstance();
       
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
        //    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Date end_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -days);

        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }
        logger.info("Collecting data from "
                + start_data_collection + " to " + end_data_collection);

        StringBuffer sb = new StringBuffer();
        File file = null;
        try
        {
        	for (int fileno = 0;fileno<files.length;fileno++)
        	{
        		//System.out.println("The file name from the array is  "+files[fileno]);
        		file = files[fileno];
        		//System.out.println("The file name after adding the path is : "+file);
	            LineNumberReader fin = new LineNumberReader(new FileReader(file));
	            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");
	            String strFileName = file.getName();
	            if(strFileName.length() <= 14)
	            {
	            	//System.out.println("The value fo the length :::"+strFileName.length());
	            	continue;
	            }
	            //			logger.info("Line Number
	            // "+fin.getLineNumber());
	
	            String str;
	
	            while ((str = fin.readLine()) != null)
	            {
	                if (str.startsWith("H"))
	                {
	                	if(sb.length() >0)
	                	{
	                		//System.out.println("Already there ...");
	                		continue;
	                	}
	                	else
	                	{
		                    sb.append(str +",Point"+ "\n");
		                    continue;
	                	}
	                }
	                Date report_date = new Date();
	                String dateStr = str.substring(0, str.indexOf(","));
	                try
	                {
	                    report_date = sdf1.parse(dateStr);
	                    //					logger.info("report_date in
	                    // cdr_report "+report_date);
	                }
	                catch (Exception pe)
	                {
	                    logger.error("", pe);
	                }
	
	                if (report_date != null
	                        && (report_date.equals(start_data_collection) || (report_date
	                                .after(start_data_collection) && report_date
	                                .before(end_data_collection))))
	                {
	                	String strPoint = strFileName.substring(11,strFileName.lastIndexOf("_"));
	                	sb.append(str +","+strPoint+ "\n");
	                    continue;
	                }
	            }
        	}
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

        //write to CDR_Report.csv
        try
        {
            File cdr = new File(filepath + "\\CDR_Report.csv");
            logger.info("cdr: " + cdr);
            if (!cdr.exists())
                cdr.createNewFile();

            FileOutputStream fout = new FileOutputStream(cdr);
            fout.write(sb.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

    }


    //createViralFile
    private void createViralFile(File file, int days, String siteid)
    {
        String table = "RBT_VIRAL_SMS_TABLE";
        HashMap hm_colDetails = new HashMap();
        ArrayList alHeader = new ArrayList();
        ArrayList alLine = null;
        ArrayList alDate = null;
        Calendar cal = Tools.getCalendarInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //	   	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date end_data_collection = new Date();
        ArrayList alSubId = null;
        if ((!m_customer.equalsIgnoreCase("tata"))
                && (!m_customer.equalsIgnoreCase("esia")))
        {
            alSubId = prefixValidation(siteid);
        }
        try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -days);

        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        logger.info("Collecting data from "
                + start_data_collection + " to " + end_data_collection);
        try
        {
            File sub = new File(file.getAbsolutePath()
                    .substring(0, file.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_VIRAL_SMS_TABLE.txt");
            if (!sub.exists())
                sub.createNewFile();
            FileOutputStream fout = new FileOutputStream(sub);

            hm_colDetails = getColNameType(table, siteid);

            LineNumberReader fin = new LineNumberReader(new FileReader(file));
            String str;
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //    		SimpleDateFormat sdf1 = new SimpleDateFormat ("MM/dd/yyyy
            // HH:mm:ss a");

            while ((str = fin.readLine()) != null)
            {
                int i = 0;
                if ((str = str.trim()).length() <= 0)
                    continue;
                alLine = new ArrayList();
                alDate = new ArrayList();
                if (str.startsWith("S"))
                {
                    alHeader = getColHeader(str);
                    str = str + "\r\n";
                    fout.write(str.getBytes());
                    continue;
                }
                str = str.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(str, ",");
                while (tokens.hasMoreTokens())
                {
                    String tokenVal = tokens.nextToken();
                    alDate.add(tokenVal);
                    if (hm_colDetails.containsKey((String) alHeader.get(i)))
                    {
                        String tokenType = (String) hm_colDetails.get(alHeader
                                .get(i));
                        if (((String) alHeader.get(i))
                                .equalsIgnoreCase("SUBSCRIBER_ID"))
                        {
                            try
                            {
                                String subId = (String) tokenVal
                                        .substring(0, 4);
                                int subInt = Integer.parseInt(subId);
                                if ((!m_customer.equalsIgnoreCase("tata"))
                                        && (!m_customer
                                                .equalsIgnoreCase("esia")))
                                {
                                    if (alSubId.contains(subId))
                                        break;
                                }
                            }
                            catch (Exception e)
                            {
                                logger.error("", e);
                                break;
                            }
                        }

                        if (tokenType.equalsIgnoreCase("NUMBER"))
                        {
                            if (tokenVal.equalsIgnoreCase("null"))
                                tokenVal = "0";
                            int tokenValInt = Integer.parseInt(tokenVal);
                            alLine.add(new Integer(tokenValInt));
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("VARCHAR2"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("CHAR"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("DATE"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        }
                    }
                }
                if (alDate.size() != alHeader.size())
                    continue;

                int sendTimeNo = alHeader.indexOf("SMS_SENT_TIME");
                Date send_time = RBTReporter.maxProcessDate!=null?RBTReporter.maxProcessDate:new Date();
                String SendTime = (String) alLine.get(sendTimeNo);
                if (SendTime != null && !(SendTime.equalsIgnoreCase("null")))
                {
                    send_time = sdf1.parse(SendTime);
                }
                else
                    send_time = null;
                if (send_time != null
                        && (send_time.compareTo(start_data_collection) >= 0)
                        && send_time.compareTo(end_data_collection) < 0)
                {
                    str = str + "\r\n";
                    fout.write(str.getBytes());
                    continue;
                }
            }
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }

    private File createSubscriberFile(File file, int days, String siteid)
    {
        String table = "RBT_SUBSCRIBER_REPORT";
		Object obj = null;
        HashMap hm_colDetails = new HashMap();
        ArrayList alHeader = new ArrayList();
        ArrayList alLine = null;
		ArrayList alData = null;
        ArrayList alDate = new ArrayList();
//        Calendar cal = Calendar.getInstance();
//        if(RBTReporter.maxProcessDate != null)
//        {
//            cal.setTime(RBTReporter.maxProcessDate);
//            
//        }
		File sub =null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //	   	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date end_data_collection = new Date();
		Date start_data_collection = new Date();
        ArrayList alSubId = null;
        if ((!m_customer.equalsIgnoreCase("tata")) && (!m_customer.equalsIgnoreCase("esia")))
        {
            alSubId = prefixValidation(siteid);
        }
		alDate = getDateRange(days);
		end_data_collection =(Date)alDate.get(0);
		start_data_collection =(Date)alDate.get(1);

 /*       try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -days);

        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }
*/
        logger.info("Collecting data from " + start_data_collection + " to " + end_data_collection);

        StringBuffer sb = new StringBuffer();

        try
        {
	        sub = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("RBT_"))+ "RBT_SUBSCRIBER_TEMP.txt");;
            if (!sub.exists())
                sub.createNewFile();
            FileOutputStream fout = new FileOutputStream(sub);

            hm_colDetails = getColNameType(table, siteid);

            LineNumberReader fin = new LineNumberReader(new FileReader(file));
            String str;
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //    		SimpleDateFormat sdf1 = new SimpleDateFormat ("MM/dd/yyyy HH:mm:ss a");
			logger.info("POOJA POOJA ::: STARTED CREATION OF RBT_SUBSCRIBER_TEMP.txt File ");

			str=fin.readLine();
			if((str=str.trim()).length()<=0)
			{
				logger.info("FILE IS EMPTY :: "+file.getAbsolutePath());
				return sub;
			}
			if(str.startsWith("S"))
			{
                    alHeader = getColHeader(str);
					str = str + "\r\n";
					fout.write(str.toString().getBytes());
			}
            while ((str = fin.readLine()) != null)
            {
                int i = 0;
                if ((str = str.trim()).length() <= 0)
                    continue;
                alLine = new ArrayList();
                alData = new ArrayList();
      /*          if (str.startsWith("S"))
                {
					str = str + "\r\n";
					fout.write(str.toString().getBytes());
//                    sb.append(str + "\n");
                    alHeader = getColHeader(str);
                    continue;
                }*/
                str = str.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(str, ",");
                while (tokens.hasMoreTokens())
                {
                    String tokenVal = tokens.nextToken();
                    alData.add(tokenVal);
                    if (hm_colDetails.containsKey((String) alHeader.get(i)))
                    {
                        String tokenType = (String) hm_colDetails.get(alHeader
                                .get(i));
                        if (((String) alHeader.get(i))
                                .equalsIgnoreCase("SUBSCRIBER_ID"))
                        {
                            try
                            {
                                String subId = (String) tokenVal
                                        .substring(0, 4);
                                int subInt = Integer.parseInt(subId);
                                if ((!m_customer.equalsIgnoreCase("tata"))
                                        && (!m_customer
                                                .equalsIgnoreCase("esia")))
                                {
                                    if (alSubId.contains(subId))
                                        break;
                                }
                            }
                            catch (Exception e)
                            {
                                logger.error("", e);
                                break;
                            }
                        }
						obj = getInsertDataByType(tokenVal,tokenType);
						alLine.add(obj);
						i=i+1;

/*                       if (tokenType.equalsIgnoreCase("NUMBER"))
                        {
                            if (tokenVal.equalsIgnoreCase("null"))
                                tokenVal = "0";
                            int tokenValInt = Integer.parseInt(tokenVal);
                            alLine.add(new Integer(tokenValInt));
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("VARCHAR2"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("CHAR"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("DATE"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        } */
                    }
                }
                if (alData.size() != alHeader.size())
                    continue;

                /*
                 * if((!m_customer.equalsIgnoreCase("tata")) &&
                 * (!m_customer.equalsIgnoreCase("esia"))) { String subId =
                 * (String)alLine.get(0); subId = subId.substring(0,4);
                 * if(alSubId.contains(subId)) continue; }
                 */
                int endNo = alHeader.indexOf("END_DATE");
                int nextChargingNo = alHeader.indexOf("NEXT_CHARGING_DATE");

                /*
                 * For TATA : act_date = START_DATE For OTHERS : act_date =
                 * ACTIVATION_DATE For Esia : act_date = start_date and
                 * comparing with next_charging_date
                 */
                Date act_date = new Date();
                String ActDate = null;
                if (!m_customer.equalsIgnoreCase("tata"))
                {
                    int activationNo = alHeader.indexOf("ACTIVATION_DATE");
                    ActDate = (String) alLine.get(activationNo);
                    if (ActDate != null && !(ActDate.equalsIgnoreCase("null")))
                    {
                        act_date = sdf1.parse(ActDate);
                    }
                    else
                        act_date = null;
                }//for Tata
                else
                {
                    int startNo = alHeader.indexOf("START_DATE");
                    ActDate = (String) alLine.get(startNo);
                    if (ActDate != null && !(ActDate.equalsIgnoreCase("null")))
                        act_date = sdf1.parse(ActDate);
                    else
                        act_date = null;
                }

                Date end_date = new Date();
                try
                {
                    String endDate = (String) alLine.get(endNo);
                    if (endDate != null && !(endDate.equalsIgnoreCase("null")))
                        end_date = sdf1.parse(endDate);
                    else
                        end_date = null;
                }
                catch (Exception e)
                {
                    end_date = null;
                }

                Date next_charging_date = new Date();
                try
                {
                    String nextChargingDate = (String) alLine
                            .get(nextChargingNo);
                    if (nextChargingDate != null
                            && !(nextChargingDate.equalsIgnoreCase("null")))
                        next_charging_date = sdf1.parse(nextChargingDate);
                    else
                        next_charging_date = null;
                }
                catch (Exception e)
                {
                    next_charging_date = null;
                }

                if (end_date != null
                        && (end_date.compareTo(start_data_collection) >= 0)
                        && end_date.before(end_data_collection))
                {
					str = str + "\r\n";
					fout.write(str.toString().getBytes());
					continue;
                }
                if (m_customer.equalsIgnoreCase("esia"))
                {
                    if (next_charging_date != null
                            && (next_charging_date
                                    .compareTo(start_data_collection) >= 0)
                            && next_charging_date.before(end_data_collection))
                    {
						str = str + "\r\n";
						fout.write(str.toString().getBytes());
						continue;
                    }
                }

                //	logger.info("act_date "+act_date);

                if (act_date != null
                        && (act_date.compareTo(start_data_collection) >= 0)
                        && act_date.before(end_data_collection))
                {
						str = str + "\r\n";
						fout.write(str.toString().getBytes());
						continue;
                }
            }
            fout.close();
			logger.info("POOJA POOJA ::: ENDED CREATION OF RBT_SUBSCRIBER_TEMP.txt File ");

        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        //write to rbt_subscriber.txt
/*        try
        {
            sub = new File(file.getAbsolutePath()
                    .substring(0, file.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_SUBSCRIBER_TEMP.txt");

            if (!sub.exists())
                sub.createNewFile();
            FileOutputStream fout = new FileOutputStream(sub);
            fout.write(sb.toString().getBytes());
            fout.close();
            //			logger.info("puja : sb for temp file
            // :"+sb.toString());
        }
        catch (Exception e)
        {
            logger.error("", e);
        } */
        return sub;
    }

    private File createSelectionsFile(File file, int days, String siteid)
    {
        String table = "RBT_SUBS_SELECTIONS_REPORT";
		Object obj = null;
        HashMap hm_colDetails = new HashMap();
        ArrayList alHeader = new ArrayList();
        ArrayList alLine = null;
        ArrayList alData = null;
		ArrayList alDate = new ArrayList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        File selections = null;
        Date end_data_collection = new Date();
		Date start_data_collection = new Date();
        ArrayList alSubId = null;
        if ((!m_customer.equalsIgnoreCase("tata")) && (!m_customer.equalsIgnoreCase("esia")))
        {
            alSubId = prefixValidation(siteid);
        }
		alDate = getDateRange(days);
		end_data_collection =(Date)alDate.get(0);
		start_data_collection =(Date)alDate.get(1);
/*
        try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -days);

        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }
*/
        logger.info("Collecting data from "+ start_data_collection + " to " + end_data_collection);

        StringBuffer sb = new StringBuffer();

        int startNo = 0;
        int endNo = 0;
        int nextChargingNo = 0;
        
        try
        {
            if (updateSelTataFlag == false)
                selections = new File(file.getAbsolutePath().substring(0,
                                   file.getAbsolutePath().lastIndexOf("RBT_"))
                        + "RBT_SUBSCRIBER_SELECTIONS.txt");
            else
                selections = new File(file.getAbsolutePath()
                        .substring(0,
                                   file.getAbsolutePath().lastIndexOf("RBT_"))
                        + "RBT_SUBSCRIBER_SELECTIONS_TEMP.txt");

            if (!selections.exists())
                selections.createNewFile();

            FileOutputStream fout = new FileOutputStream(selections);

            hm_colDetails = getColNameType(table, siteid);
            LineNumberReader fin = new LineNumberReader(new FileReader(file));
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //    		SimpleDateFormat sdf1 = new SimpleDateFormat ("MM/dd/yyyy
            // HH:mm:ss a");
			logger.info("POOJA POOJA ::: STARTED CREATION OF RBT_SUBSCRIBER_SELECTIONS_TEMP.txt File ");

            String str=null;
			str=fin.readLine();
			if((str=str.trim()).length()<=0)
			{
				logger.info("FILE IS EMPTY :: "+file.getAbsolutePath());
				return selections;
			}
			if(str.startsWith("S"))
			{
				alHeader = getColHeader(str);
				str = str + "\r\n";
				fout.write(str.toString().getBytes());
                startNo = alHeader.indexOf("START_TIME");
                endNo = alHeader.indexOf("END_TIME");
                nextChargingNo = alHeader.indexOf("NEXT_CHARGING_DATE");
			}

            while ((str = fin.readLine()) != null)
            {
                if ((str = str.trim()).length() <= 0)
                    continue;
                alLine = new ArrayList();
                alData = new ArrayList();
                int i = 0;
         /*       if (str.startsWith("S"))
                {
					str =str+"\r\n";
					fout.write(str.toString().getBytes());
          //          sb.append(str + "\n");
                    alHeader = getColHeader(str);
                    continue;
                }*/
                str = str.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(str, ",");
                while (tokens.hasMoreTokens())
                {
                    String tokenVal = tokens.nextToken();
                    alData.add(tokenVal);
                    if (hm_colDetails.containsKey(alHeader.get(i)))
                    {
                        String tokenType = (String) hm_colDetails.get(alHeader
                                .get(i));
                        if (((String) alHeader.get(i))
                                .equalsIgnoreCase("SUBSCRIBER_ID"))
                        {
                            try
                            {
                                String subId = (String) tokenVal
                                        .substring(0, 4);
                                int subInt = Integer.parseInt(subId);
                                if ((!m_customer.equalsIgnoreCase("tata"))
                                        && (!m_customer
                                                .equalsIgnoreCase("esia")))
                                {
                                    if (alSubId.contains(subId))
                                        break;
                                }
                            }
                            catch (Exception e)
                            {
                                logger.error("", e);
                                break;
                            }
                        }
						obj = getInsertDataByType(tokenVal,tokenType);
						alLine.add(obj);
						i=i+1;

                /*       if (tokenType.equalsIgnoreCase("NUMBER"))
                        {
                            if (tokenVal.equalsIgnoreCase("null"))
                                tokenVal = "0";
                            int tokenValInt = Integer.parseInt(tokenVal);
                            alLine.add(new Integer(tokenValInt));
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("VARCHAR2"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("CHAR"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        }
                        if (tokenType.equalsIgnoreCase("DATE"))
                        {
                            alLine.add(tokenVal);
                            i = i + 1;
                        } */
                    }
                }
                if (alData.size() != alHeader.size()) {
    				logger.info("number of elements in line " + i + " does not match count in header\n");
                    continue;
                }


                /*
                 * if((!m_customer.equalsIgnoreCase("tata")) &&
                 * (!m_customer.equalsIgnoreCase("esia"))) { String subId =
                 * (String)alLine.get(0); subId = subId.substring(0,4);
                 * if(alSubId.contains(subId)) continue; }
                 */

                String startDate = (String) alLine.get(startNo);
                String endDate = (String) alLine.get(endNo);
                String nextChargingDate = (String) alLine.get(nextChargingNo);
                Date start_date = null;
                Date end_date = null;
                Date next_charging_date = null;

                try
                {
                    if (startDate != null
                            && !(startDate.equalsIgnoreCase("null")))
                        start_date = sdf1.parse(startDate);
                    else
                        start_date = null;
                }
                catch (Throwable e)
                {
                    start_date = null;
                }
                if (start_date != null
                        && (start_date.compareTo(start_data_collection) >= 0)
                        && start_date.before(end_data_collection))
                {
					str =str+"\r\n";
					fout.write(str.toString().getBytes());
                    continue;
                }
                //For Next_CHARGING_DATE
                try
                {
                    if (nextChargingDate != null
                            && !(nextChargingDate.equalsIgnoreCase("null")))
                        next_charging_date = sdf1.parse(nextChargingDate);
                    else
                        next_charging_date = null;
                }
                catch (Exception e)
                {
                    next_charging_date = null;
                }
                if (m_customer.equalsIgnoreCase("esia"))
                {
                    if (next_charging_date != null
                            && (next_charging_date
                                    .compareTo(start_data_collection) >= 0)
                            && next_charging_date.before(end_data_collection))
                    {
						str =str+"\r\n";
						fout.write(str.toString().getBytes());
                        continue;
                    }
                }

                try
                {
                    if (endDate != null && !(endDate.equalsIgnoreCase("null")))
                        end_date = sdf1.parse(endDate);
                    else
                        end_date = null;
                }
                catch (Throwable e)
                {
                    end_date = null;
                }
                // 			logger.info("end_date "+end_date);
                if (end_date != null
                        && (end_date.compareTo(start_data_collection) >= 0)
                        && end_date.before(end_data_collection))
                {
					str =str+"\r\n";
					fout.write(str.toString().getBytes());
                    continue;
                }

            }
			fout.close();
			logger.info("POOJA POOJA ::: ENDED CREATION OF RBT_SUBSCRIBER_SELECTIONS_TEMP.txt File ");

        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

        //write to rbt_subscriber_selections.txt
 /*        try
        {
            //			if(m_customer.equalsIgnoreCase("tata"))
            if (updateSelTataFlag == false)
                selections = new File(file.getAbsolutePath()
                        .substring(0,
                                   file.getAbsolutePath().lastIndexOf("RBT_"))
                        + "RBT_SUBSCRIBER_SELECTIONS.txt");
            else
                selections = new File(file.getAbsolutePath()
                        .substring(0,
                                   file.getAbsolutePath().lastIndexOf("RBT_"))
                        + "RBT_SUBSCRIBER_SELECTIONS_TEMP.txt");

            if (!selections.exists())
                selections.createNewFile();

            FileOutputStream fout = new FileOutputStream(selections);
            fout.write(sb.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }*/
        return selections;
    }
	//Getting Data on the basis of its Data Type (For Insert )
	private Object getInsertDataByType(String tokenVal,String tokenType)
	{
		Object result = null;
		tokenType = tokenType.toUpperCase();
		if(tokenType.equals("NUMBER"))
		{
			if(tokenVal.equals("null"))
				tokenVal = "0";
				int tokenValInt = Integer.parseInt(tokenVal);
				result = new Integer(tokenValInt);
		}
		else if(tokenType.equals("VARCHAR2"))
		{
			result = tokenVal;
		}
		else if(tokenType.equals("CHAR"))
		{
			result = tokenVal;
		}
		else if(tokenType.equals("DATE"))
		{
			result = tokenVal;
		}
		return result;
	}

    private void updateCDR_Data(String site_id)
    {

        Calendar cal = Tools.getCalendarInstance();
        
        cal.add(Calendar.DATE, -1);

        String date = new SimpleDateFormat("yyyy-MMM-dd").format(cal.getTime());

        String query = "select count(SUBSCRIBER_ID) from RBT_SUBSCRIBER_REPORT where START_DATE<=to_date('"
                + date
                + "115959PM','yyyy-mon-ddHHMISSPM') and END_DATE >=to_date('"
                + date
                + "115959PM','yyyy-mon-ddHHMISSPM') and site_id = '"
                + site_id + "'";
		
        try
        {
				long lPreTime = System.currentTimeMillis();
				 m_rs = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
          
            while (m_rs.next())
            {
                int subs = m_rs.getInt(1);
                query = "update rbt_cdr_entries set active_subscribers ='"
                        + subs + "' where cdr_date like '" + date
                        + "%' and site_id ='" + site_id + "'";
				lPreTime = System.currentTimeMillis();
                m_stmt2.executeUpdate(query);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            }

        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
    }

    private int getNoDaysData2Load(String site_id)
    {

        Date max_date = null;
        String query = "SELECT MAX(REPORT_DATE) FROM RBT_ACTIVITY_REPORT WHERE SITE_ID = '"+ site_id + "' and REPORT_DATE<to_Date('"+Tools.getChangedFormatDate(RBTReporter.maxProcessDate)+"','yyyy-mm-dd')";
        try
        {
				long lPreTime = System.currentTimeMillis();
                m_rs = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
          
            if (m_rs.next())
            {
                max_date = m_rs.getDate(1);
                logger.info("max_date " + max_date);
            }

        }
        catch (SQLException sqle)
        {
            logger.error("", sqle);
        }
        catch (Exception E)
        {
            logger.error("", E);
        }

        if (max_date == null)
        {
            return 0;
        }
        else
        {
            Calendar cal = Tools.getCalendarInstance();
            int val = new Long(
                    (cal.getTime().getTime() - max_date
                            .getTime())
                            / (1000 * 3600 * 24) - 1).intValue();
            logger.info("puja: no_date:" + val);

            return val;
        }
    }

    //For createDeactivatedSubFile
    private File createDeactivatedSubFile(File file, int days)
    {
        Calendar cal =Tools.getCalendarInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date end_data_collection = new Date();
        File sub = null;
        try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -days);

        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        logger.info("Collecting data from "
                + start_data_collection + " to " + end_data_collection);

        StringBuffer sb = new StringBuffer();

        try
        {
            LineNumberReader fin = new LineNumberReader(new FileReader(file));
            String str;
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //    		SimpleDateFormat sdf1 = new SimpleDateFormat ("MM/dd/yyyy
            // HH:mm:ss a");

            while ((str = fin.readLine()) != null)
            {
                if (str.startsWith("S"))
                {
                    sb.append(str + "\n");
                }
                str = str.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(str, ",");
                if (tokens.hasMoreTokens())
                    tokens.nextToken();
                Date end_date = new Date();
                try
                {
                    if (tokens.hasMoreTokens())
                        end_date = sdf1.parse(tokens.nextToken());
                    else
                        end_date = null;
                }
                catch (Exception e)
                {
                    end_date = null;
                }

                if (end_date != null && end_date.after(start_data_collection)
                        && end_date.before(end_data_collection))
                {
                    sb.append(str + "\n");
                    continue;
                }

            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        //write to rbt_subscriber.txt
        try
        {
            sub = new File(file.getAbsolutePath()
                    .substring(0, file.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_DEACTIVATED_SUBSCRIBERS.txt");
            if (!sub.exists())
                sub.createNewFile();
            FileOutputStream fout = new FileOutputStream(sub);
            fout.write(sb.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return sub;
    }

    //createDeactivatedSelFile
    private File createDeactivatedSelFile(File file, int days)
    {
        Calendar cal = Tools.getCalendarInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        File selections = null;
        Date end_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            end_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        cal.add(Calendar.DATE, -days);

        Date start_data_collection = new Date();
        try
        {
            String date = sdf.format(cal.getTime());
            start_data_collection = sdf.parse(date);
        }
        catch (Exception e)
        {

        }

        logger.info("Collecting data from "
                + start_data_collection + " to " + end_data_collection);

        StringBuffer sb = new StringBuffer();

        try
        {
            LineNumberReader fin = new LineNumberReader(new FileReader(file));
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String str;

            while ((str = fin.readLine()) != null)
            {
                if (str.startsWith("S"))
                {
                    sb.append(str + "\n");
                    continue;
                }
                str = str.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(str, ",");
                if (tokens.hasMoreTokens())
                    tokens.nextToken();
                if (tokens.hasMoreTokens())
                    tokens.nextToken();
                Date end_date = new Date();

                try
                {
                    if (tokens.hasMoreTokens())
                        end_date = sdf1.parse(tokens.nextToken());
                    else
                        end_date = null;
                }
                catch (Throwable e)
                {
                    end_date = null;
                }
                if (end_date != null && end_date.after(start_data_collection)
                        && end_date.before(end_data_collection))
                {
                    sb.append(str + "\n");
                    continue;
                }

            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }

        //write to rbt_subscriber_selections.txt
        try
        {
            selections = new File(file.getAbsolutePath()
                    .substring(0, file.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_DELETED_SELECTIONS.txt");
            if (!selections.exists())
                selections.createNewFile();

            FileOutputStream fout = new FileOutputStream(selections);
            fout.write(sb.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }
        return selections;
    }

    //For updateDeactivatedSubscriber
    private File updateDeactivatedSubscriber(File fFile, String site_id)
    {
        String SUBSCRIBER_ID = null;
        String DEACTIVATED_TIME = null;
        String DEACTIVATED_TIME_STR = null;
        String DEACTIVATED_BY = null;
        String DEACTIVATED_BY_STR = null;
        String PREPAID_YES = null;
        String strLine = null;
        String strQuery = null;
        ArrayList al = null;
        int i = 0;
        File subscribers = null;
        StringBuffer sbSubscriber = new StringBuffer();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            FileReader objFile = new FileReader(fFile);
            BufferedReader br = new BufferedReader(objFile);
            while ((strLine = br.readLine()) != null)
            {
                String line = null;
                SUBSCRIBER_ID = null;

                String token = null;
                strLine = strLine.replaceAll(",,", ",null,");
                StringTokenizer strToken = new StringTokenizer(strLine, ",");
                if (strToken.hasMoreTokens())
                {
                    token = strToken.nextToken();
                    if (token != null && !token.equalsIgnoreCase("null"))
                        SUBSCRIBER_ID = token;
                    else
                        SUBSCRIBER_ID = null;
                    if (SUBSCRIBER_ID != null && SUBSCRIBER_ID.startsWith("S"))
                    {
                        sbSubscriber
                                .append("SUBSCRIBER_ID,ACTIVATED_BY,DEACTIVATED_BY,START_DATE,END_DATE,PREPAID_YES,LAST_ACCESS_DATE,NEXT_CHARGING_DATE,NUM_MAX_SELECTIONS,NUM_VOICE_ACCESS,ACTIVATION_INFO,SUBSCRIPTION_YES,LAST_DEACTIVATION_INFO,LAST_DEACTIVATION_DATE,SUBSCRIPTION_CLASS"
                                        + "\n");
                        continue;
                    }
                }
                if (strToken.hasMoreTokens())
                {
                    DEACTIVATED_TIME = strToken.nextToken();
                    DEACTIVATED_TIME_STR = DEACTIVATED_TIME;
                    if (!DEACTIVATED_TIME.equalsIgnoreCase("null"))
                        DEACTIVATED_TIME = "'" + DEACTIVATED_TIME + "'";
                }
                if (strToken.hasMoreTokens())
                {
                    DEACTIVATED_BY = strToken.nextToken();
                    DEACTIVATED_BY_STR = DEACTIVATED_BY;
                    if (!DEACTIVATED_BY.equalsIgnoreCase("null"))
                        DEACTIVATED_BY = "'" + DEACTIVATED_BY + "'";
                }

                String query = null;
                if (DEACTIVATED_BY_STR.equalsIgnoreCase("VP-PROMO")
                        || DEACTIVATED_BY_STR.equalsIgnoreCase("SMS-PROMO"))
                {
                    query = "update rbt_subscriber_report set PREPAID_YES = 'y', DEACTIVATED_BY = "
                            + DEACTIVATED_BY
                            + " , END_DATE = to_date("
                            + DEACTIVATED_TIME
                            + ",'yyyy-MM-dd hh24:mi:ss') where site_id ='"
                            + site_id
                            + "' and subscriber_id = '"
                            + SUBSCRIBER_ID + "'";
                }
                else
                {
                    query = "update rbt_subscriber_report set DEACTIVATED_BY = "
                            + DEACTIVATED_BY
                            + " , END_DATE = to_date("
                            + DEACTIVATED_TIME
                            + ",'yyyy-MM-dd hh24:mi:ss') where site_id ='"
                            + site_id
                            + "' and subscriber_id = '"
                            + SUBSCRIBER_ID + "'";
                }

                //	logger.info("puja: update strQuery
                // :"+query);

                long lPreTime = System.currentTimeMillis();
                 i = m_stmt1.executeUpdate(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime))); 
               
                PREPAID_YES = getPrepaidYes(SUBSCRIBER_ID, site_id);

                if (i != 0)
                {
                    //subscriber_id,activated_by,deactivated_by,start_date,end_date,prepaid_yes,last_access_date,next_charging_date
                    //num_max_selections,num_voice_access,activation_info,subscription_yes,last_deactivation_info,last_deactivation_date,subscription_class
                    if (DEACTIVATED_BY_STR.equalsIgnoreCase("VP-PROMO")
                            || DEACTIVATED_BY_STR.equalsIgnoreCase("SMS-PROMO"))
                    {
                        line = SUBSCRIBER_ID + ",null," + DEACTIVATED_BY
                                + ",2006-01-01 00:00:00," + DEACTIVATED_TIME
                                + ",y,null,null,0,0,null,n,VP,null,DEFAULT ";
                    }
                    else
                    {
                        line = SUBSCRIBER_ID + ",null," + DEACTIVATED_BY
                                + ",2006-01-01 00:00:00," + DEACTIVATED_TIME
                                + "," + PREPAID_YES
                                + ",null,null,0,0,null,n,VP,null,DEFAULT ";
                    }
                    line = line.replaceAll("'", "");
                    sbSubscriber.append(line + "\n");
                    i = 0;
                }
                else
                {
                    query = "insert into rbt_subscriber_report(site_id,subscriber_id,activated_by,deactivated_by,start_date,end_date,prepaid_yes,last_access_date,next_charging_date,num_max_selections,num_voice_access,activation_info,subscription_yes,subscription_class,last_deactivation_info,last_deactivation_date) values ("
                            + site_id
                            + ","
                            + SUBSCRIBER_ID
                            + ","
                            + DEACTIVATED_BY
                            + ","
                            + DEACTIVATED_BY
                            + ",to_date ('"
                            + DEACTIVATED_TIME_STR
                            + "','yyyy-MM-dd hh24:mi:ss'),to_date('"
                            + DEACTIVATED_TIME_STR
                            + "', 'yyyy-MM-dd hh24:mi:ss'),'"
                            + PREPAID_YES
                            + "',null,null,0,0,'VP','n','DEFAULT',null,null)";
                    //				logger.info("puja : deactivated Subs
                    // :"+query);
					lPreTime = System.currentTimeMillis();
					m_stmt1.executeUpdate(query);
					lPostTime = System.currentTimeMillis();
					logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime))); 
					   

                    line = SUBSCRIBER_ID + "," + DEACTIVATED_BY + ","
                            + DEACTIVATED_BY + "," + DEACTIVATED_TIME + ","
                            + DEACTIVATED_TIME + "," + PREPAID_YES
                            + ",null,null,0,0,VP,n,null,null,DEFAULT ";
                    line = line.replaceAll("'", "");
                    sbSubscriber.append(line + "\n");

                }
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
            logger.info(" Exception : " + e.getMessage());
        }
        //write to rbt_subscriber_temp.txt
        try
        {
            subscribers = new File(fFile.getAbsolutePath()
                    .substring(0, fFile.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_SUBSCRIBER_TEMP.txt");
            if (!subscribers.exists())
                subscribers.createNewFile();

            FileOutputStream fout = new FileOutputStream(subscribers);
            fout.write(sbSubscriber.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }
        return subscribers;
    }

    //puja : For updateDeactivatedSelections
    private File updateDeactivatedSelections(File fFile, String site_id)
    {

        String SUBSCRIBER_ID = null;
        String PROMO_ID = null;
        String PROMO_ID_STR = null;
        String DELETED_TIME = null;
        String DELETED_TIME_STR = null;
        String strLine = null;
        String strQuery = null;
        File selections = null;
        StringBuffer sbSelection = new StringBuffer();
        int i = 0;
        try
        {
            FileReader objFile = new FileReader(fFile);
            BufferedReader br = new BufferedReader(objFile);
            String token = null;
            while ((strLine = br.readLine()) != null)
            {
                String line = null;
                if (strLine.startsWith("S"))
                {
                    sbSelection
                            .append("SUBSCRIBER_ID,CALLER_ID,CATEGORY_ID,SUBSCRIBER_WAV_FILE,SET_TIME,START_TIME,END_TIME,STATUS,CLASS_TYPE,SELECTED_BY,SELECTION_INFO,NEXT_CHARGING_DATE,PREPAID_YES,FROM_TIME,TO_TIME,LOOP_SETTING"
                                    + "\n");
                    continue;
                }
                strLine = strLine.replaceAll(",,", ",null,");
                StringTokenizer strToken = new StringTokenizer(strLine, ",");
                if (strToken.hasMoreTokens())
                {
                    token = strToken.nextToken();
                    if (token != null && !token.equalsIgnoreCase("null"))
                        SUBSCRIBER_ID = token;
                    else
                        SUBSCRIBER_ID = null;
                }

                if (strToken.hasMoreTokens())
                {
                    PROMO_ID = strToken.nextToken();
                    PROMO_ID_STR = PROMO_ID;
                    if (!PROMO_ID.equalsIgnoreCase("null"))
                    {
                        PROMO_ID = "'" + PROMO_ID + "'";
                    }
                }

                if (strToken.hasMoreTokens())
                {
                    DELETED_TIME = strToken.nextToken();
                    DELETED_TIME_STR = DELETED_TIME;
                    if (!DELETED_TIME.equalsIgnoreCase("null"))
                        DELETED_TIME = "'" + DELETED_TIME + "'";
                }

                //subscriber_id,caller_id,category_id,subscriber_wav_file,set_time,start_time,end_time,status,class_type,selected_by,selection_info,next_charging_date,prepaid_yes,from_time,to_time,loop_setting

                line = SUBSCRIBER_ID + ",null,1," + PROMO_ID
                        + ",2006-01-01 00:00:00,2006-01-01 00:00:00,"
                        + DELETED_TIME + ",1,DEFAULT,VP,VP,null,n,0,0,1";
                line = line.replaceAll("'", "");
                sbSelection.append(line + "\n");

                strQuery = "update rbt_subs_selections_report set end_time=to_date("
                        + DELETED_TIME
                        + ",'yyyy-MM-dd hh24:mi:ss') where site_id ='"
                        + site_id
                        + "' and subscriber_id = '"
                        + SUBSCRIBER_ID
                        + "' and subscriber_wav_file = " + PROMO_ID + " ";

                //       			logger.info("puja: query
                // check:"+strQuery);

				 long lPreTime = System.currentTimeMillis();
					 i = m_stmt1.executeUpdate(strQuery);
					long lPostTime = System.currentTimeMillis();
					logger.info("PERFMON: Query=>"+ strQuery+"&&&TimeTaken="+((lPostTime - lPreTime))); 

               
                if (i != 0)
                {
                    i = 0;
                }
                else
                {
                    strQuery = "insert into rbt_subs_selections_report values ("
                            + site_id
                            + ",'"
                            + SUBSCRIBER_ID
                            + "',null,1,'"
                            + PROMO_ID_STR
                            + "',to_date('2006-01-01 00:00:00','yyyy-MM-dd hh24:mi:ss'),to_date('2006-01-01 00:00:00','yyyy-MM-dd hh24:mi:ss'),to_date("
                            + DELETED_TIME
                            + ",'yyyy-MM-dd hh24:mi:ss'),'1','DEFAULT','VP','VP',null,'n',0,0,1)";
                   
				lPreTime = System.currentTimeMillis();
				m_stmt1.executeUpdate(strQuery);
				lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ strQuery+"&&&TimeTaken="+((lPostTime - lPreTime)));

                }
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
            logger.info(" Exception : " + e.getMessage()
                    + " while exceuting query " + strQuery);
        }
        //write to rbt_subs_selections_temp.txt
        try
        {
            selections = new File(fFile.getAbsolutePath()
                    .substring(0, fFile.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_SUBSCRIBER_SELECTIONS_TEMP.txt");
            if (!selections.exists())
                selections.createNewFile();
            FileOutputStream fout = new FileOutputStream(selections);
            fout.write(sbSelection.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        catch (Throwable t)
        {
            logger.info("Throwable " + t);
        }
        return selections;
    }

   /* private void updateSubscriber(File fFile, String site_id)
    {
        String _method = "updateSubscriber";
        String table = "RBT_SUBSCRIBER_REPORT";
		Object obj = null;
        HashMap hm_colDetails = new HashMap();
        ArrayList alHeader = new ArrayList();
        ArrayList alLine = null;
        String strLine = null;
        String strQuery = null;
        StringBuffer query = null;
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
			m_conn.setAutoCommit(false);
            File subscribers = new File(fFile.getAbsolutePath()
                    .substring(0, fFile.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_SUBSCRIBER.txt");
            if (!subscribers.exists())
                subscribers.createNewFile();
            FileOutputStream fout = new FileOutputStream(subscribers);

            hm_colDetails = getColNameType(table, site_id);
            FileReader objFile = new FileReader(fFile);
            BufferedReader br = new BufferedReader(objFile);
			String tokenVal =null;
			String tokenType =null;
			PrepStatementHolder psHolder = new PrepStatementHolder(m_conn);
			logger.info("POOJA POOJA ::: STARTED CREATION OF RBT_SUBSCRIBER.txt File ");

            while ((strLine = br.readLine()) != null)
            {
                if ((strLine = strLine.trim()).length() <= 0)
                    continue;

                alLine = new ArrayList();
                int i = 0;
                if (strLine.startsWith("S"))
                {
 //                   sbSubscriber.append(strLine + "\n");
					alHeader = getColHeader(strLine);
					strLine =strLine+"\r\n";
					fout.write(strLine.toString().getBytes());
                    continue;
                }
                strLine = strLine.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(strLine, ",");
                int tokencount = tokens.countTokens();

                while (tokens.hasMoreTokens())
                {
                    tokenVal = tokens.nextToken();
                    if (hm_colDetails.containsKey((String) alHeader.get(i)))
                    {
                        tokenType = (String) hm_colDetails.get(alHeader.get(i));
						obj = getUpdateDataByType(tokenType,tokenVal);
						alLine.add(obj);
						i=i+1;

                    }
                }
                List paramList = new ArrayList();
                
				query = new StringBuffer();
                query = query.append("update rbt_subscriber_report set ");
               
                for (int k = 1; k < alHeader.size(); k++)
                {
                    ParamHolder pHolder = new ParamHolder();
                    if (hm_colDetails.containsKey((String) alHeader.get(k)))
                    {
                        
                        if (k == 1)
                        {
                            query.append(alHeader.get(k) + "=" + "?");
                            //paramList.add(alLine.get(k));
                            pHolder.setParam((String) hm_colDetails.get(alHeader.get(k)), alLine.get(k));
                            paramList.add(pHolder);
                            
                        }
                        else
                        {
                            query.append("," + alHeader.get(k) + "="+ "?");
                            pHolder.setParam((String) hm_colDetails.get(alHeader.get(k)), alLine.get(k));
                            paramList.add(pHolder);
                        //    paramList.add(alLine.get(k));
                        
                        }
                        
                    }
                }
                query.append(" where site_id=? and "
                        + alHeader.get(0) + "=? ");
                ParamHolder pHolder = new ParamHolder();
                pHolder.setParam((String)hm_colDetails.get("SITE_ID"), new Integer(site_id));
                paramList.add(pHolder);
                ParamHolder pHolder1 = new ParamHolder();
                pHolder1.setParam((String)hm_colDetails.get(alHeader.get(0)), alLine.get(0));
                paramList.add(pHolder1);
                //paramList.add(site_id);
                //paramList.add(alLine.get(0));
                
                //DebugLevel debug = DebugLevel.ON;
               // PreparedStatement ps = StatementFactory.getStatement(m_conn,query.toString());
                PreparedStatement ps = psHolder.getPreparedStatement(query.toString(), paramList);    
            //    logger.info("puja: updateSubscriber query executed :"+query.toString());
				long lPreTime = System.currentTimeMillis();
				int  count = ps.executeUpdate(query.toString());
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query.toString()+"&&&TimeTaken="+((lPostTime - lPreTime)));
              
	            if (count != 0)
                {
	                count = 0;
                }
                else
                {
					strLine =strLine+"\r\n";
					fout.write(strLine.toString().getBytes());
                }
				
            }
			fout.close();
			logger.info("POOJA POOJA ::: ENDED CREATION OF RBT_SUBSCRIBER.txt File ");

        }
        catch (Exception e)
        {
            logger.error("", e);
            logger.info(" Exception : " + e.getMessage());
        }
		finally{
			try{
				m_conn.commit();
			}
			catch(Exception e){
			}
		}
 
    }*/
    
    private void updateSubscriber(File fFile, String site_id)
    {
        String table = "RBT_SUBSCRIBER_REPORT";
        Object obj = null;
        HashMap hm_colDetails = new HashMap();
        ArrayList alHeader = new ArrayList();
        ArrayList alLine = null;
        String strLine = null;
        String strQuery = null;
        StringBuffer query = null;
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            File subscribers = new File(fFile.getAbsolutePath()
                    .substring(0, fFile.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_SUBSCRIBER.txt");
            if (!subscribers.exists())
                subscribers.createNewFile();
            FileOutputStream fout = new FileOutputStream(subscribers);

            hm_colDetails = getColNameType(table, site_id);
            FileReader objFile = new FileReader(fFile);
            BufferedReader br = new BufferedReader(objFile);
            String tokenVal =null;
            String tokenType =null;
            logger.info("POOJA POOJA ::: STARTED CREATION OF RBT_SUBSCRIBER.txt File ");

            while ((strLine = br.readLine()) != null)
            {
                if ((strLine = strLine.trim()).length() <= 0)
                    continue;

                alLine = new ArrayList();
                int i = 0;
                if (strLine.startsWith("S"))
                {
 //                   sbSubscriber.append(strLine + "\n");
                    alHeader = getColHeader(strLine);
                    strLine =strLine+"\r\n";
                    fout.write(strLine.toString().getBytes());
                    continue;
                }
                strLine = strLine.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(strLine, ",");
                int tokencount = tokens.countTokens();

                while (tokens.hasMoreTokens())
                {
                    tokenVal = tokens.nextToken();
                    if (hm_colDetails.containsKey((String) alHeader.get(i)))
                    {
                        tokenType = (String) hm_colDetails.get(alHeader.get(i));
                        obj = getUpdateDataByTypeForSubs(tokenType,tokenVal);
                        alLine.add(obj);
                        i=i+1;

                      }
                }
                query=new StringBuffer();
                query = query.append("update rbt_subscriber_report set ");
                for (int k = 1; k < alHeader.size(); k++)
                {
                    if (hm_colDetails.containsKey((String) alHeader.get(k)))
                    {
                        if (k == 1)
                            query.append(alHeader.get(k) + "=" + alLine.get(k));
                        else
                            query.append("," + alHeader.get(k) + "="
                                    + alLine.get(k));
                    }
                }
                query.append(" where site_id =" + site_id + " and "
                        + alHeader.get(0) + "=" + alLine.get(0) );

            //    logger.info("puja: updateSubscriber query executed :"+query.toString());
                i = m_stmt1.executeUpdate(query.toString());
                if (i != 0)
                {
                    i = 0;
                }
                else
                {
                    strLine =strLine+"\r\n";
                    fout.write(strLine.toString().getBytes());
                }
                
            }
            fout.close();
            logger.info("POOJA POOJA ::: ENDED CREATION OF RBT_SUBSCRIBER.txt File ");

        }
        catch (Exception e)
        {
            logger.error("", e);
            logger.info(" Exception : " + e.getMessage());
        }
   
    }


    //puja : For updateSelection
    private void updateSelection(File fFile, String site_id)
    {
		Object obj = null;
        String table = "RBT_SUBS_SELECTIONS_REPORT";
        HashMap hm_colDetails = new HashMap();
        ArrayList alHeader = new ArrayList();
        ArrayList alLine = null;
        String CALLER_ID = null;
        int CATEGORY_ID = 0;
        String SUBSCRIBER_WAV_FILE = null;
        java.sql.Timestamp SET_TIME = null;
        String CLASS_TYPE = null;
        String strLine = null;
        String strQuery = null;
        boolean flag = false;
        StringBuffer query = null;
		PrepStatementHolder psHolder = new PrepStatementHolder(m_conn);
        try
        {
			m_conn.setAutoCommit(false);
            File selections = new File(fFile.getAbsolutePath()
                    .substring(0, fFile.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_SUBSCRIBER_SELECTIONS.txt");
            if (!selections.exists())
                selections.createNewFile();
            FileOutputStream fout = new FileOutputStream(selections);
            
            // TEMP: create an output file for logging bad data
           // String dateStr = new SimpleDateFormat("yyyyMMdd").format(RBTReporter.maxProcessDate);
           // String folder = "./RBTErrors/" + m_customer + "/" + site_id + "/" + dateStr;
            //File dir = new File(folder);
            //dir.mkdirs();
            //FileOutputStream ferr = new FileOutputStream(new File(dir, "errors.txt"));

            hm_colDetails = getColNameType(table, site_id);
            FileReader objFile = new FileReader(fFile);
            BufferedReader br = new BufferedReader(objFile);
			logger.info("POOJA POOJA ::: STARTED CREATION OF RBT_SUBSCRIBER_SELECTIONS.txt File ");

			String tokenType =null;
			String tokenVal=null;
            while ((strLine = br.readLine()) != null)
            {
            	//System.out.println("The value of hte line being read is "+strLine);
                if ((strLine = strLine.trim()).length() <= 0)
                    continue;

                alLine = new ArrayList();
                int i = 0;

                if (strLine.startsWith("S"))
                {
                    alHeader = getColHeader(strLine);
					strLine=strLine+ "\r\n";
					fout.write(strLine.toString().getBytes());
//                    sbSelection.append(strLine + "\n");
                    continue;
                }
                strLine = strLine.replaceAll(",,", ",null,");
                StringTokenizer tokens = new StringTokenizer(strLine, ",");
                int tokencount = tokens.countTokens();

                while (tokens.hasMoreTokens())
                {
                    tokenVal = tokens.nextToken();
                    if (hm_colDetails.containsKey((String)alHeader.get(i)))
                    {
						tokenType = (String) hm_colDetails.get(alHeader.get(i));
						obj = getUpdateDataByType(tokenType,tokenVal);
						//System.out.println("The value being added is "+obj);
						alLine.add(obj);
						i=i+1;
        
                    }
                    //added by yuva for adding other columns in rbt_subs_selections table
                    else
                    {
                    	//tokenType = (String) hm_colDetails.get(alHeader.get(i));
                    	//Here the token type is hard coded because the from time and to time are integers in the db.
                    	//ANy how these two are not required.Can be changed later...
						obj = getUpdateDataByType("NUMBER",tokenVal);
						//System.out.println("The value being added is "+obj);
						alLine.add(obj);
                    	i=i+1;
                    }
                }
                CLASS_TYPE = (String) alLine.get(alHeader.indexOf("CLASS_TYPE"));
                CALLER_ID = (String) alLine.get(alHeader.indexOf("CALLER_ID"));
                CATEGORY_ID = ((Integer) alLine.get(alHeader
                        .indexOf("CATEGORY_ID"))).intValue();
                SUBSCRIBER_WAV_FILE = (String) alLine.get(alHeader
                        .indexOf("SUBSCRIBER_WAV_FILE"));
                SET_TIME = (java.sql.Timestamp) alLine.get(alHeader.indexOf("SET_TIME"));

                int first = 1;
				query = new StringBuffer();
				List paramList = new ArrayList();
                query = query.append("update rbt_subs_selections_report set ");
                if (-1 == CLASS_TYPE.indexOf("_SHUFFLE"))
                {
                    for (int k = 1; k < alHeader.size(); k++)
                    {
                        if (k == (alHeader.indexOf("CALLER_ID")) || k == (alHeader.indexOf("CATEGORY_ID")) || k == (alHeader.indexOf("SUBSCRIBER_WAV_FILE")) || k == (alHeader.indexOf("SET_TIME")))
                            continue;
						//change by eshwar@onmobile.com to increase the performance
						
						if (hm_colDetails.containsKey((String) alHeader.get(k)))
                        {
                            if (first == 1)
                            {
                                query.append(alHeader.get(k) + "=?");
								paramList.add(alLine.get(k));
								//System.out.println("getting "+alHeader.get(k)+" and " +
										//"adding "+alLine.get(k));
                                first = 2;
                            }
                            else {
                                query.append(" , " + alHeader.get(k) + "=?");
								paramList.add(alLine.get(k));
								//System.out.println("getting "+alHeader.get(k)+" and " +"adding "+alLine.get(k));
							}
                        }
                    }
                    query.append(" where site_id =? and "
                            + alHeader.get(0) + "=?"
                            + " and SET_TIME = ?" 
                            + " and CATEGORY_ID =?" 
                            + " and SUBSCRIBER_WAV_FILE =?");
					paramList.add(new Integer(site_id));
					paramList.add(alLine.get(0));
					paramList.add(SET_TIME);
					paramList.add(new Integer(CATEGORY_ID));
					paramList.add(SUBSCRIBER_WAV_FILE);

                    if (CALLER_ID != null && !(CALLER_ID.equalsIgnoreCase("null"))) {
                        query.append(" and CALLER_ID=? ");
						paramList.add(CALLER_ID);
                    }
                    else
                    {
                        query.append(" and CALLER_ID is null");
                    }
                    //	logger.info("puja without Shuffle: and
                    // caller_id :"+CALLER_ID+" and query:"+query);
                }
                else
                {
                    for (int k = 1; k < alHeader.size(); k++)
                    {
                        if (k == (alHeader.indexOf("CALLER_ID")) || k == (alHeader.indexOf("CATEGORY_ID")) || k == (alHeader.indexOf("SET_TIME")))
                            continue;
                        if (hm_colDetails.containsKey((String) alHeader.get(k)))
                        {
                            if (first == 1)
                            {
                                query.append(alHeader.get(k) + "=?");
								paramList.add(alLine.get(k));
                                first = 2;
                            }
                            else {
                                query.append(" , " + alHeader.get(k) + "=?");
								paramList.add(alLine.get(k));
							}
                        }
                    }
                    query.append(" where site_id =? and "
                            + alHeader.get(0) + "=?"
                            + " and SET_TIME =?"
                            + " and CATEGORY_ID =?");
					paramList.add(new Integer(site_id));
					paramList.add(alLine.get(0));
					paramList.add(SET_TIME);
					paramList.add(new Integer(CATEGORY_ID));

					if (CALLER_ID != null && !(CALLER_ID.equalsIgnoreCase("null"))) {
                        query.append(" and CALLER_ID=?");
						paramList.add(CALLER_ID);
					}
                    else
                    {
                        query.append(" and CALLER_ID is null");
                    }
                  //   		logger.info("puja with Shuffle: and caller_id :"+CALLER_ID+" and query:"+query);
                }
                
				// get the prepared statement from the cache and apply parameters
				long lPreTime = System.currentTimeMillis();
				// get the prepared statement from the cache and apply parameters
				PreparedStatement ps = psHolder.getPreparedStatement(query.toString(), paramList);
				int count = ps.executeUpdate();
				long lPostTime = System.currentTimeMillis();
				//logger.info("PERFMON: Query=>"+ query.toString()+"&&&TimeTaken="+((lPostTime - lPreTime)));
				
				if (count == 0) {
					// no record, insert so it will get loaded by sqlloader
					strLine=strLine+ "\r\n";
					fout.write(strLine.toString().getBytes());
				} else if (count > 1) {
					logger.info("Got count > 1 for update subscriber id = " + alLine.get(0) + " and SET_TIME = " + fullDateSdf.format(SET_TIME));
					//ferr.write(("Got count > 1 for " + strLine+ "\n").getBytes());
				}
            }
			fout.close();
			//ferr.close();
			logger.info("POOJA POOJA ::: ENDED CREATION OF RBT_SUBSCRIBER_SELECTIONS.txt File ");
        }
        catch (Exception e)
        {
            logger.error("", e);
            logger.info(" Exception : " + e.getMessage()
                    + " while exceuting query " + query.toString());
        }
		finally{
			try{
				m_conn.commit();
				psHolder.closeAll();
			}
			catch(Exception e){
			}
		}

        //write to RBT_SUBSCRIBERS_SELECTION.TXT
   /*     try
        {
            File selections = new File(fFile.getAbsolutePath()
                    .substring(0, fFile.getAbsolutePath().lastIndexOf("RBT_"))
                    + "RBT_SUBSCRIBER_SELECTIONS.txt");
            if (!selections.exists())
                selections.createNewFile();
            FileOutputStream fout = new FileOutputStream(selections);

            fout.write(sbSelection.toString().getBytes());
            fout.close();
        }
        catch (Exception e)
        {
            logger.error("", e);
        }*/
    }
    
	private Object getUpdateDataByType(String tokenType,String tokenVal)
	{
		String _method = "getUpdateDataByType";
		Object result = null;
		tokenType = tokenType.toUpperCase();
		if(tokenType.equals("NUMBER"))
		{
			if(tokenVal.equals("null"))
				tokenVal = "0";
			else if(tokenVal.equals("?"))
				tokenVal = "0";
			int tokenValInt = Integer.parseInt(tokenVal);
			result =new Integer(tokenValInt);
 		}
		if(tokenType.equals("VARCHAR2"))
		{
			result = tokenVal;
		}
		if(tokenType.equals("CHAR"))
		{
			result = tokenVal;		
 		}
		if(tokenType.equals("DATE"))
		{
			if(tokenVal.equals("null")) {
				result = "null";
			} else {
				try {
					result = new java.sql.Timestamp(fullDateSdf.parse(tokenVal).getTime());
				} catch (ParseException e) {
					logger.error("", e);
					result = null;
				}
			}
		}
		return result;
	}

    //For TATA -rbt_subs_selections_report - checkActiveSelections:-
    private boolean checkActiveSelections(int days, String site_id)
    {
        String _method = "checkActiveSelections";
        Calendar cal = Tools.getCalendarInstance();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        File selections = null;
        cal.add(Calendar.DATE, -days);
//        Date start_date = new Date();
        String date = sdf.format(cal.getTime());
        logger.info("Checking data for date: "
                + date);

        try
        {
            String query = "select * from RBT_SUBS_SELECTIONS_REPORT where site_id = '"
                    + site_id
                    + "' and start_time between to_date('"
                    + date
                    + " 12:00:00AM','yyyy-MM-dd hh:mi:ssAM') and to_date('"
                    + date + " 11:59:59PM','yyyy-MM-dd hh:mi:ssPM') ";
            logger.info("puja :query :" + query
                    + " and stmt :" + m_stmt1);
			long lPreTime = System.currentTimeMillis();
				  ResultSet rs = m_stmt1.executeQuery(query);
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            updateSelTataFlag = rs.next();
            logger.info("puja : updateSelTataFlag :"
                    + updateSelTataFlag);
        }
        catch (SQLException se)
        {
            logger.error("", se);
            logger.info(" Exception :" + se.getMessage());
        }
        //			rs.close();
        return updateSelTataFlag;

    }

    private void loadDataUsingSqlldr(String controlFile, String mode)
    {
        String _method = "loadDataUsingSqlldr";
        //run the sqlldr command.
        Runtime runtime = Runtime.getRuntime();
        String fileName = controlFile;

        int index = controlFile.lastIndexOf(".ctl");
        if (index != -1)
            fileName = controlFile.substring(0, index);

        Calendar calendar = Tools.getCalendarInstance();
        calendar.add(Calendar.DATE, 0);
        String s1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(calendar
                .getTime());

        String badfilename = "./"
                + fileName
                + new SimpleDateFormat("hh24mmss").format(Calendar
                        .getInstance().getTime()) + ".bad";
        String logfilename = "./log/"
                + fileName
                + new SimpleDateFormat("hh24mmss").format(Calendar
                        .getInstance().getTime()) + ".log";
        String command = "sqlldr direct=true skip=1 userid=" + m_sqlldr_uid
                + " control=" + controlFile + ",log=" + logfilename + ",bad="
                + badfilename + "\n";
        if (mode.equalsIgnoreCase("indirect"))
        {
            command = "sqlldr skip=1 userid=" + m_sqlldr_uid + " control="
                    + controlFile + ",log=" + logfilename + ",bad="
                    + badfilename + "\n";
        }

        int exitvalue = 0;
        try
        {
            Process process = runtime.exec(command);
            InputStream in = process.getInputStream();
            InputStream err = process.getErrorStream();
            ErrorReader er1 = new ErrorReader(in);
            ErrorReader er2 = new ErrorReader(err);
            er1.start();
            er2.start();

            exitvalue = process.waitFor();//wait for it to complete
            if (exitvalue != 0)
            {
                logger.info("command:" + command
                        + " exited with value:" + exitvalue);
            }
            else
            {
                logger.info("command:" + command
                        + " exited NORMALLY");
            }
        }
        catch (IOException ioe)
        {
            logger.info("could not execute command: "
                    + command);
            logger.error("", ioe);
        }
        catch (InterruptedException ie)
        {
            logger.info("current thread was waiting, but was interrupted!");
            logger.error("", ie);
        }
         new File(controlFile).delete();

    }

    private String getCfgValue(File dir, String property)
    {
        File cfg_file = null;
        File[] list = dir.listFiles();
        if (list == null)
            return null;
        for (int i = 0; i < list.length; i++)
        {
            if (list[i].getName().endsWith(".cfg"))
                cfg_file = list[i];
        }
        if (cfg_file == null)
        {
            logger.info("no cfg file in " + dir);
            return null;
        }
        try
        {
            LineNumberReader fin = new LineNumberReader(
                    new FileReader(cfg_file));
            String str;
            while ((str = fin.readLine()) != null)
            {
                StringTokenizer tokens = new StringTokenizer(str, "=");
                if (tokens.nextToken().trim().equalsIgnoreCase(property))
                    return (tokens.nextToken().trim());
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return null;
    }

    //puja : getPrepaidYes
    public String getPrepaidYes(String subscriber_id, String site_id)
    {
		String _method = "getPrepaidYes";
        String prepaid_yes = null;
        String prepaidQuery = "select PREPAID_YES from rbt_subscriber_report where site_id ="
                + site_id + " and subscriber_id =" + subscriber_id + "";
        try
        {
			long lPreTime = System.currentTimeMillis();
				  ResultSet rset = m_stmt2.executeQuery(prepaidQuery);;
				long lPostTime = System.currentTimeMillis();
				logger.info("PERFMON: Query=>"+ prepaidQuery+"&&&TimeTaken="+((lPostTime - lPreTime)));
          
            if (rset.next() == true)
                prepaid_yes = rset.getString("PREPAID_YES");
            else
                prepaid_yes = "y";
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return prepaid_yes;
    }

    public String getMinDate(File selections)
    {

        Date min_date = new Date();
        if(RBTReporter.maxProcessDate != null)
        {
            min_date=RBTReporter.maxProcessDate;
            
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            LineNumberReader fin = new LineNumberReader(new FileReader(
                    selections));
            String str;

            while ((str = fin.readLine()) != null)
            {
                StringTokenizer tokens = new StringTokenizer(str, ",");

                for (int i = 0; i < 6; i++)
                {
                    if (tokens.hasMoreTokens())
                        tokens.nextToken();
                }

                Date date = new Date();

                try
                {
                    if (tokens.hasMoreTokens())
                        date = sdf.parse(tokens.nextToken());
                    else
                        date = null;
                }
                catch (Exception e)
                {
                    date = null;
                }

                if (date != null)
                {
                    if (date.before(min_date))
                        min_date = date;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        return sdf.format(min_date);
    }

    private void loadCsv(String siteid, File dir,
            ControlFileGenerator control_files)
    {
        String _method = "loadCsv";
        File cdr_file = new File(dir + File.separator + m_cdr_dir
                + File.separator + "CDR_Report.csv");
        if (cdr_file == null)
        {
            logger.info("No Csv Data to upload in dir : "
                    + dir);
            return;
        }

        String[] ctl_info = control_files.createControlFile(cdr_file, siteid);
        if (ctl_info == null)
        {
            logger.info("ERROR IN GENERATING CONTROL FILE FOR:"
                                        + cdr_file);
            return;
        }
        Runtime runtime = Runtime.getRuntime();

        String fileName = ctl_info[0];

        int index = ctl_info[0].lastIndexOf(".ctl");
        if (index != -1)
            fileName = ctl_info[0].substring(0, index);

        String badfilename = "./" + fileName + ".bad";
        String logfilename = "./log/" + fileName + ".log";
        String command = "sqlldr skip=1 userid=" + m_sqlldr_uid + " control="
                + ctl_info[0] + ",log=" + logfilename + ",bad=" + badfilename
                + "\n";

        int exitvalue = 0;
        try
        {
            Process process = runtime.exec(command);
            InputStream in = process.getInputStream();
            InputStream err = process.getErrorStream();
            ErrorReader er1 = new ErrorReader(in);
            ErrorReader er2 = new ErrorReader(err);
            er1.start();
            er2.start();

            exitvalue = process.waitFor();//wait for it to complete
            if (exitvalue != 0)
            {
                logger.info("command:" + command
                        + " exited with value:" + exitvalue);
            }
            else
            {
                logger.info("command:" + command
                        + " exited NORMALLY");
            }
        }
        catch (IOException ioe)
        {
            logger.info("could not execute command: "
                    + command);
            logger.error("", ioe);
        }
        catch (InterruptedException ie)
        {
            logger.info("current thread was waiting, but was interrupted!");
            logger.error("", ie);
        }
        logger.info("loaded csv file and deleting control file");
         new File(ctl_info[0]).delete();

    }

    private int deleteOldRecords(String tablename, String site_id,
            String min_date)
    {
        try
        {
/*            if (tablename.equalsIgnoreCase("rbt_subscriber_report"))
            {
                return deleteOldRecordsSubscriber(tablename, site_id);
            }
            else if (tablename.equalsIgnoreCase("rbt_subs_selections_report"))
            {
                return deleteOldRecordsSelections(tablename, site_id, min_date);
            }
            else
            { */
                return deleteOldRecordsWithOneQuery(tablename, site_id);
        //    }
        }
        catch (Exception e)
        {
        }
        return 0;
    }

    /*
     * private void deleteSubscriber(File fFile,String site_id) { String _method =
     * "deleteSubscriber"; String SUBSCRIBER_ID = null;
     * 
     * String strLine = null; String strQuery = null; try { FileReader objFile =
     * new FileReader(fFile); BufferedReader br = new BufferedReader(objFile);
     * strLine = br.readLine(); while ((strLine = br.readLine())!=null) {
     * SUBSCRIBER_ID = null;
     * 
     * String token = null;
     * 
     * StringTokenizer strToken = new StringTokenizer(strLine,",");
     * if(strToken.hasMoreTokens()) { token = strToken.nextToken(); if(token !=
     * null && !token.equalsIgnoreCase("null") ) SUBSCRIBER_ID = token; else
     * SUBSCRIBER_ID = null; if(SUBSCRIBER_ID != null
     * &&SUBSCRIBER_ID.startsWith("S")) continue; } strQuery = "delete from
     * rbt_subscriber_report where site_id ='"+site_id+"' and subscriber_id =
     * '"+SUBSCRIBER_ID+"'"; m_stmt1.executeUpdate(strQuery); } } catch
     * (Exception e) { logger.error("", e);
     * logger.info(" Exception : "+e.getMessage()); } }
     * 
     * private void deleteSelections(File fFile,String site_id) { String _method =
     * "deleteSelections"; String SUBSCRIBER_ID = null; String CALLER_ID = null;
     * String CATEGORY_ID = null; String SUBSCRIBER_WAV_FILE = null; String
     * SET_TIME = null; String CLASS_TYPE = null; String strLine = null; String
     * strQuery = null; try { FileReader objFile = new FileReader(fFile);
     * BufferedReader br = new BufferedReader(objFile); strLine = br.readLine();
     * String token = null; while ((strLine = br.readLine())!=null) {
     * CATEGORY_ID = null; SUBSCRIBER_WAV_FILE= null; SET_TIME = null;
     * StringTokenizer strToken = new StringTokenizer(strLine,",");
     * if(strToken.hasMoreTokens()) { token = strToken.nextToken(); if(token !=
     * null && !token.equalsIgnoreCase("null") ) SUBSCRIBER_ID = token; else
     * SUBSCRIBER_ID = null; if(SUBSCRIBER_ID != null
     * &&SUBSCRIBER_ID.startsWith("S")) continue; } if(strToken.hasMoreTokens()) {
     * token = strToken.nextToken(); if(token != null &&
     * !token.equalsIgnoreCase("null") ) CALLER_ID = token; else CALLER_ID =
     * null; }
     * 
     * if(strToken.hasMoreTokens()) { CATEGORY_ID = strToken.nextToken(); }
     * if(strToken.hasMoreTokens()) { SUBSCRIBER_WAV_FILE =
     * strToken.nextToken(); } if(strToken.hasMoreTokens()) { SET_TIME =
     * strToken.nextToken(); } if(SET_TIME == null) continue;
     * 
     * for(int i=0;i <3;i++) { if(strToken.hasMoreTokens())
     * strToken.nextToken(); } if(strToken.hasMoreTokens()) { CLASS_TYPE =
     * strToken.nextToken(); if(-1 == CLASS_TYPE.indexOf("_SHUFFLE")) {
     * if(CALLER_ID == null) { strQuery = "delete from
     * rbt_subs_selections_report where site_id ='"+site_id+"' and subscriber_id =
     * '"+SUBSCRIBER_ID+"' and caller_id is null and category_id =
     * "+CATEGORY_ID+" and subscriber_wav_file = '"+SUBSCRIBER_WAV_FILE+"' and
     * set_time = " + "to_date('"+SET_TIME+"','yyyy-mm-dd hh24:mi:ss')"; } else {
     * strQuery = "delete from rbt_subs_selections_report where site_id
     * ='"+site_id+"' and subscriber_id = '"+SUBSCRIBER_ID+"' and caller_id
     * ='"+CALLER_ID+"' and category_id = "+CATEGORY_ID+" and
     * subscriber_wav_file = '"+SUBSCRIBER_WAV_FILE+"' and set_time = " +
     * "to_date('"+SET_TIME+"','yyyy-mm-dd hh24:mi:ss')"; } }else { if(CALLER_ID ==
     * null) { strQuery = "delete from rbt_subs_selections_report where site_id
     * ='"+site_id+"' and subscriber_id = '"+SUBSCRIBER_ID+"' and caller_id is
     * null and category_id = "+CATEGORY_ID+" and set_time = " +
     * "to_date('"+SET_TIME+"','yyyy-mm-dd hh24:mi:ss')"; } else { strQuery =
     * "delete from rbt_subs_selections_report where site_id ='"+site_id+"' and
     * subscriber_id = '"+SUBSCRIBER_ID+"' and caller_id ='"+CALLER_ID+"' and
     * category_id = "+CATEGORY_ID+" and set_time = " +
     * "to_date('"+SET_TIME+"','yyyy-mm-dd hh24:mi:ss')"; } } //
     * logger.info("query check:"+strQuery);
     * 
     * m_stmt1.executeUpdate(strQuery); } } } catch (Exception e) {
     * logger.error("", e); logger.info("
     * Exception : "+e.getMessage() + " while exceuting query "+strQuery); } }
     */
    /*
     * private void addSelections(String[] controlFile,String site_id,String
     * cust_site){
     * 
     * String _method = "addSelections"; Connection conn = getConnection();
     * Statement stmt=null; ResultSet rs = null;
     * 
     * int old_selections_cleanup = 7; // it is a daemon config parameter
     * oldselection cleanup in days
     * 
     * String query = "select * from rbt_subs_selections_report where end_time <
     * sysdate and end_time > sysdate-"+(old_selections_cleanup+1)+" and site_id
     * ='"+site_id+"'"; logger.info("going to execute query
     * "+query);
     * 
     * StringBuffer sb = new StringBuffer(); try{ stmt = conn.createStatement();
     * rs = stmt.executeQuery(query);
     * deleteOldRecordsSelections(controlFile[1],site_id);
     * 
     * logger.info("Loading data from site into
     * rbt_subs_selections_report");
     * 
     * loadDataUsingSqlldr(controlFile[0],site_id);
     * 
     * while(rs.next()){ String subscriber=rs.getString("SUBSCRIBER_ID"); String
     * caller_id=rs.getString("CALLER_ID"); int cat_id=rs.getInt("CATEGORY_ID");
     * String sub_wav_file=rs.getString("SUBSCRIBER_WAV_FILE"); String
     * dsettime=rs.getString("SET_TIME").substring(0,rs.getString("SET_TIME").lastIndexOf("."));
     * String
     * dstarttime=rs.getString("START_TIME").substring(0,rs.getString("START_TIME").lastIndexOf("."));
     * String
     * dendtime=rs.getString("END_TIME").substring(0,rs.getString("END_TIME").lastIndexOf("."));
     * int status=rs.getInt("STATUS");
     * 
     * if(caller_id == null || caller_id.equals("null")) query = "select
     * subscriber_id from rbt_subs_selections_report where site_id =
     * '"+site_id+"' and subscriber_id ='"+subscriber+"' and caller_id is null
     * and category_id = '"+cat_id+"' and subscriber_wav_file
     * ='"+sub_wav_file+"' and set_time = to_date('"+dsettime+"','yyyy-MM-dd
     * hh24:mi:ss') and start_time = to_date('"+dstarttime+"','yyyy-MM-dd
     * hh24:mi:ss') and end_time = to_date('"+dendtime+"','yyyy-MM-dd
     * hh24:mi:ss') and status = '"+status+"'"; else query = "select
     * subscriber_id from rbt_subs_selections_report where site_id =
     * '"+site_id+"' and subscriber_id ='"+subscriber+"' and caller_id
     * ='"+caller_id+"' and category_id = '"+cat_id+"' and subscriber_wav_file
     * ='"+sub_wav_file+"' and set_time = to_date('"+dsettime+"','yyyy-MM-dd
     * hh24:mi:ss') and start_time = to_date('"+dstarttime+"','yyyy-MM-dd
     * hh24:mi:ss') and end_time = to_date('"+dendtime+"','yyyy-MM-dd
     * hh24:mi:ss') and status = '"+status+"'"; stmt = conn.createStatement();
     * if(stmt.executeQuery(query).next()){ stmt.close(); }else { stmt.close();
     * sb.append(subscriber+","+caller_id+","+cat_id+","+sub_wav_file+","+dsettime+","+dstarttime+","+dendtime+","+status+"\n"); } }
     * File reportfile=new File("./RBT_SUBSCRIBER_SELECTIONS.TXT");
     * FileOutputStream fout=null; if(!reportfile.exists())
     * reportfile.createNewFile(); fout = new FileOutputStream(reportfile);
     * fout.write(sb.toString().getBytes()); fout.close(); ControlFileGenerator
     * cfg = new ControlFileGenerator(site_id);
     * logger.info("Report File "+reportfile); String[]
     * ctl_info = cfg.createControlFile(reportfile);
     * loadDataUsingSqlldr(ctl_info[0],site_id); reportfile.delete();
     * 
     * }catch (Exception e){ System.out.println(e); }
     * 
     * finally{ try{ stmt.close(); rs.close(); conn.commit();conn.close();
     * }catch(Exception e){ System.out.println(e); } } }
     */
//Return ArrayList with "End Date " and "Start Date "
	private ArrayList getDateRange(int days)
	{
		ArrayList alDate = new ArrayList();
	  	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			Calendar cal = Tools.getCalendarInstance();
			Date end_data_collection = new Date();
			Date start_data_collection = new Date();

			String date = sdf.format(cal.getTime());
			end_data_collection = sdf.parse(date);
			alDate.add(end_data_collection);

			cal.add(Calendar.DATE, -days);
			date = sdf.format(cal.getTime());
			start_data_collection = sdf.parse(date);
			alDate.add(start_data_collection);
		}
		catch (Exception e)
		{
    		logger.error("", e);
		}
		return alDate;
	}

    //prefixValidation
    private ArrayList prefixValidation(String site_id)
    {
		String _method = "prefixValidation";
        String query = "select distinct(substr(subscriber_id,1,4)) from rbt_subscriber_report where site_id!="
                + site_id;//make query
        ArrayList prefixes = new ArrayList();
        try
        {
			 long lPreTime = System.currentTimeMillis();
            m_rs = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));

            while (m_rs.next())
            {
                prefixes.add(m_rs.getString(1));
            }
        }
        catch (Exception E)
        {
        }

        return prefixes;
    }

    private String[] getprefixes(String site_id)
    {
		String _method = "getprefixes";
        String query = "select unique substr(subscriber_id,1,4)from rbt_subscriber_report where site_id="
                + site_id;//make query
        ArrayList prefixes = new ArrayList();
        try
        {
			 long lPreTime = System.currentTimeMillis();
         m_rs = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            while (m_rs.next())
            {
                prefixes.add(m_rs.getString(1));
            }
        }
        catch (Exception E)
        {
        }

        return (String[]) prefixes.toArray(new String[0]);
    }
/*
    private int deleteOldRecordsSubscriber(String tablename, String site_id)
    {
        String _method = "deleteOldRecordsSubscriber";
        int recordsdeleted = -1;
        Connection conn = getConnection();
        Statement stmt = null;
        //get prefixes for this site code
        String[] subs_prefixes = getprefixes(site_id);//(String[])hmAllPrefixes.get(cust_site);

        if (subs_prefixes == null || subs_prefixes.length <= 0)
            return -1;

        for (int i = 0; i < subs_prefixes.length; i++)
        {
            logger.info("prefix[" + i + "] = "
                    + subs_prefixes[i]);
        }
        for (int index = 0; index < subs_prefixes.length; index++)
        {
            long subsStartRange = Long.parseLong(subs_prefixes[index]
                    + "000000");
            subsStartRange--;//subtract one, because of the query.
            long subsEndRange = Long.parseLong(subs_prefixes[index] + "999999");
            long subsStartBlock = subsStartRange;
            long subsEndBlock = 0;
            boolean end_of_querying = false;
            while (!end_of_querying)
            {
                subsEndBlock = subsStartBlock + m_SUBS_PER_QUERY;
                if (subsEndBlock > subsEndRange)
                    subsEndBlock = subsEndRange;
                if (subsStartBlock == subsEndBlock)
                {
                    end_of_querying = true;
                    continue;
                }
                String query = "delete from " + tablename + " where site_id="
                        + site_id;
                query += " and subscriber_id > '"
                        + String.valueOf(subsStartBlock)
                        + "' and subscriber_id <= '"
                        + String.valueOf(subsEndBlock) + "'";

                logger.info("going to execute query: "
                        + query);
                try
                {
                    stmt = conn.createStatement();
                    recordsdeleted += stmt.executeUpdate(query);

                    logger.info("executed the query. getting the results");
                    stmt.close();
                }
                catch (SQLException sqle)
                {
                    logger.info("database problem!");
                    logger.error("", sqle);
                    try
                    {
                        conn.commit();
                        conn.close();
                    }
                    catch (Exception e)
                    {
                    }
                    return -1;
                }

                subsStartBlock = subsEndBlock;
            }//end while
        }//end for

        try
        {
            conn.commit();
            conn.close();
        }
        catch (Exception e)
        {
        }
        logger.info("rows deleted:" + recordsdeleted);
        return recordsdeleted;
    }

     private int deleteOldRecordsSelections(String tablename, String site_id,
            String min_date)
    {
        String _method = "deleteOldRecordsSelections";
        int recordsdeleted = -1;
        Connection conn = getConnection();

        Statement stmt = null;
        //get prefixes for this site code
        String[] subs_prefixes = getprefixes(site_id);//(String[])hmAllPrefixes.get(cust_site);
        if (subs_prefixes == null || subs_prefixes.length <= 0)
            return -1;
        for (int i = 0; i < subs_prefixes.length; i++)
        {
            logger.info("prefix[" + i + "] = "
                    + subs_prefixes[i]);
        }
        for (int index = 0; index < subs_prefixes.length; index++)
        {
            long subsStartRange = Long.parseLong(subs_prefixes[index]
                    + "000000");
            subsStartRange--;//subtract one, because of the query.
            long subsEndRange = Long.parseLong(subs_prefixes[index] + "999999");
            long subsStartBlock = subsStartRange;
            long subsEndBlock = 0;
            boolean end_of_querying = false;
            while (!end_of_querying)
            {
                subsEndBlock = subsStartBlock + m_SUBS_PER_QUERY;
                if (subsEndBlock > subsEndRange)
                    subsEndBlock = subsEndRange;
                if (subsStartBlock == subsEndBlock)
                {
                    end_of_querying = true;
                    continue;
                }
                String query = "delete from " + tablename + " where site_id="
                        + site_id;
                query += " and END_TIME >= to_date('" + min_date
                        + "','yyyy-MM-dd hh24:mi:ss') and subscriber_id > '"
                        + String.valueOf(subsStartBlock)
                        + "' and subscriber_id <= '"
                        + String.valueOf(subsEndBlock) + "'";

                logger.info("going to execute query: "
                        + query);
                try
                {
                    stmt = conn.createStatement();
                    recordsdeleted += stmt.executeUpdate(query);

                    logger.info("executed the query. getting the results");
                    stmt.close();
                }
                catch (SQLException sqle)
                {
                    logger.info("database problem!");
                    logger.error("", sqle);
                    try
                    {
                        conn.commit();
                        conn.close();
                    }
                    catch (Exception e)
                    {
                    }
                    return -1;
                }
                subsStartBlock = subsEndBlock;
            }//end while
        }//end for

        logger.info("rows deleted:" + recordsdeleted);
        return recordsdeleted;
    }

    private void deleteOldData(String site_id, int months)
    {

        String _method = "deleteOldData";
        Connection conn = getConnection();
        Statement _stmt = null;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -(months * 30));
        String chkDate = Tools.getChangedFormatDate(cal.getTime());
        String query = "delete from rbt_subscriber_report where site_id="
                + site_id;//make query
        query += " and end_date <= to_date('" + chkDate + "','yyyy-mm-dd')";

        /*
         * logger.info("Going to Execute Query:"+query);
         * 
         * try{ _stmt=conn.createStatement(); _stmt.executeUpdate(query);
         * }catch(SQLException e){ logger.error("", e); }
         */
/*        query = "delete from rbt_subs_selections_report where site_id="
                + site_id;//make query
        query += " and end_time <= to_date('" + chkDate + "','yyyy-mm-dd')";

        logger.info("Going to Execute Query:" + query);

        try
        {
            _stmt = conn.createStatement();
            _stmt.executeUpdate(query);
        }
        catch (SQLException e)
        {
            logger.error("", e);
        }

        try
        {
            if (_stmt != null)
                _stmt.close();
            if (conn != null)
                conn.commit();
            conn.close();
        }
        catch (SQLException e1)
        {
        }

    }
*/
    private int deleteOldRecordsWithOneQuery(String table_name, String site_id)
    {
        String _method = "deleteOldRecordsWithOneQuery";
        int rows = -1;

        String query = "delete from " + table_name + " where site_id="
                + site_id;//make query
        logger.info("Going to Execute Query:" + query);
        try
        {
			long lPreTime = System.currentTimeMillis();
            rows = m_stmt1.executeUpdate(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
        }
        catch (SQLException e)
        {
            logger.error("", e);
            return -1;
        }

        logger.info("rows deleted:" + rows);

        return rows;
    }

    /**
     * @param cust
     * @param site
     * @return site id
     */
    private String getSiteCode(String cust, String site)
    {
        String _method = "getSiteCode";
        String cust_id = null;
        Connection morp_conn2 = null;
        Statement morp_stmt2 = null;
        ResultSet morp_rs2 = null;
        String query = "select site_id from rbt_sites where LOWER(cust_name)='"
                + cust + "' and LOWER(site_name)='" + site + "'";//make query
        logger.info("Query:" + query);
        try
        {
            morp_conn2 = getMorpheusConnection();
            morp_stmt2 = morp_conn2.createStatement();
			 long lPreTime = System.currentTimeMillis();
           morp_rs2 = morp_stmt2.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
           
            while (morp_rs2.next())
            {
                int custid = morp_rs2.getInt(1);
                cust_id = String.valueOf(custid);
            }
        }
        catch (SQLException e)
        {
            logger.error("", e);
        }
        logger.info("returning site id:" + cust_id);
        try
        {
            if (morp_rs2 != null)
                morp_rs2.close();
            if (morp_stmt2 != null)
                morp_stmt2.close();
            if (morp_rs2 != null)
                morp_rs2.close();
        }
        catch (Exception e)
        {
        }
        return cust_id;
    }

    /*
     * private String[] getSiteList() { String _method="getSiteList"; File
     * db_dir=new File(m_db_root); String[] dir_list=db_dir.list(); return
     * dir_list; }
     */

    /**
     * @return list of directories
     */
    /*
     * private File[] getDirList(String site_dir) { String _method="getDirList";
     * String dir_name=m_db_root+File.separator+site_dir; File[] dir=new
     * File(dir_name).listFiles(); return dir; }
     */
    public HashMap getColNameType(String table, String site_id)
    {
        String _method = "getColNameType";
        HashMap hm = new HashMap();
        try
        {
            String delimiter = ",";
      /*      String query = "select * from '" + table + "' where site_id = '"
                    + site_id + "' ";
            query = query.replaceAll("'", "");
            logger.info("puja :query :" + query
                    + " and stmt :" + m_stmt1);
            ResultSet rs = m_stmt1.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int colNum = rsmd.getColumnCount();
            for (int i = 1; i <= colNum; i++)
            {
                String colName = rsmd.getColumnName(i);
                String colType = rsmd.getColumnTypeName(i);
                hm.put(colName.toUpperCase(), colType.toUpperCase());
                //	logger.info("puja : colName : "+colName+ "
                // and colType :"+colType);
            } */
			String query = "select COLUMN_NAME, DATA_TYPE from USER_TAB_COLUMNS where TABLE_NAME='"+table+"' ";
			logger.info("Query for geting column table and type :"+query);
			long lPreTime = System.currentTimeMillis();
            	ResultSet rs= m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
		
			while(rs.next())
			{
				String colName = rs.getString("COLUMN_NAME");
				String colType = rs.getString("DATA_TYPE");
				 hm.put(colName.toUpperCase(), colType.toUpperCase());
			}
			logger.info("POOJA POOJA ::: hm :"+hm.size());
        }
        catch (SQLException se)
        {
            logger.error("", se);
            logger.info(" Exception :" + se.getMessage());
        }
        //			rs.close();
        return hm;
    }

    public HashMap getColNameTypeWithNullable(String table, String site_id)
    {
        String _method = "getColNameTypeWithNullable";
        HashMap hm = new HashMap();
        try
        {
            String delimiter = ",";
			//change by eshwar@onmobile.com to improve the performance
			site_id = "-1";
            String query = "select * from '" + table + "' where site_id = '"
                    + site_id + "' ";
            query = query.replaceAll("'", "");
            logger.info("puja :query :" + query
                    + " and stmt :" + m_stmt1);
			long lPreTime = System.currentTimeMillis();
			ResultSet rs = m_stmt1.executeQuery(query);
			long lPostTime = System.currentTimeMillis();
			logger.info("PERFMON: Query=>"+ query+"&&&TimeTaken="+((lPostTime - lPreTime)));
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int colNum = rsmd.getColumnCount();
            for (int i = 1; i <= colNum; i++)
            {
                String colName = rsmd.getColumnName(i);
                String colType = rsmd.getColumnTypeName(i);
                hm.put(colName.toUpperCase(), colType.toUpperCase() + "_"
                        + rsmd.isNullable(i));
                // 				logger.info("puja : colName : "+colName+ "
                // and colType :"+colType.toUpperCase()+"_"+rsmd.isNullable(i));
            }
        }
        catch (SQLException se)
        {
            logger.error("", se);
            logger.info(" Exception :" + se.getMessage());
        }
        //			rs.close();
        return hm;
    }

    public ArrayList getColHeader(String str)
    {
    	//System.out.println("The column header got is "+str);
        String _method = "getColHeader";
        ArrayList al = new ArrayList();
        StringTokenizer st = new StringTokenizer(str, ",");
        while (st.hasMoreTokens())
        {
            String name = st.nextToken();
            name = name.trim();
            name = name.replaceAll(" ", "_");
            if (name.equalsIgnoreCase("Hour"))
                name = "CDR_DATE";
            //	logger.info("puja : name value :"+name);
           // System.out.println("adding : "+name.toUpperCase());
            al.add(name.toUpperCase());
        }
        return al;
    }

    private Connection getConnection()
    {
        String _method = "getConnection";
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(m_strCustDBURL);
			conn.setAutoCommit(false);
        }
        catch (Exception e)
        {
            logger.info("Exception	Caught	while getting Connection Exception=>"
                                           + e.getMessage());
            return null;
        }
        return conn;
    }

    public Connection getMorpheusConnection()
    {
        String _method = "getMorpheusConnection";
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(m_strDBURL);
			conn.setAutoCommit(false);
        }
        catch (Exception e)
        {
            logger.info("Exception	Caught	while getting Connection Exception=>"
                                           + e.getMessage());
            return null;
        }
        return conn;
    }

    public boolean clean()
    {
        String _method = "clean";
        try
        {
        	if(m_rs != null)
            m_rs.close();
        	if(m_stmt1 != null)
            m_stmt1.close();
        	if(m_stmt2 != null)
            m_stmt2.close();
        	if(m_conn != null)
            m_conn.close();
            //	m_morpheusStmt.close();
            //		m_morpheusConn.close();
        }
        catch (Exception e)
        {
            logger.info("Exception Caught while MoprheusUpdater Cleanup. Exception=>"
                                        + e.getMessage());
            return false;
        }

        return true;
    }

    //testing only
    /*
     * public static void main(String[] args) { String _method="main";
     * ResourceBundle bundle=null; String bundlename="resources/rbtreporter";
     * try{ bundle = ResourceBundle.getBundle(bundlename); }catch(Exception e){
     * System.out.println("bundle not found:"+bundlename); e.printStackTrace(); }
     * Tools.init(bundle); MorpheusUpdater morpheus=new MorpheusUpdater(null);
     * boolean initialized=morpheus.init(bundle); if(initialized)
     * morpheus.startUpdate(); System.exit(0); }
     */

    
    private class ErrorReader extends Thread
    {
        InputStream m_in = null;
        String _class = "MorpheusUpdater$ErrorReader";

        public ErrorReader(InputStream in)
        {
            m_in = in;
        }

        public void run()
        {
            String _method = "run";
            if (m_in == null)
                return;
            int data = 0;
			byte[] bdata = new byte[512];
            while (data != -1)
            {
                try
                {
                    data = m_in.read(bdata);
                }
                catch (IOException e)
                {
                    logger.error("", e);
                }
            }//end while
            return;
        }
    }

	private static class PrepStatementHolder
	{
		private Map prepStmtMap = new HashMap();
		private Connection conn = null;

		public PrepStatementHolder(Connection conn) {
			this.conn = conn;
		}

		public PreparedStatement getPreparedStatement(String query, List paramList) throws SQLException {
	        String _method = "getPreparedStatement";
	        
			PreparedStatement ps = (PreparedStatement)prepStmtMap.get(query);
			if (ps == null)
			{
				// this version of the query has not been prepared and cached, prepare it
				logger.info("Statement prepared for " + query);
				ps = conn.prepareStatement(query);
				prepStmtMap.put(query, ps); // cache it
			}

			ps.clearParameters();
			// if a parameter list has been passed in, apply those parameters to the prepared statement
			if (paramList != null)
			{
				for (int ii = 0; ii < paramList.size(); ++ii)
				{
					//ParamHolder value = new ParamHolder();
					//value.setParam(((ParamHolder)paramList.get(ii)).strType,((ParamHolder)paramList.get(ii)).oVal);
				    Object value = paramList.get(ii);
					// PRONEEL: Can be improved below to use setObject(), but no time to change :)
					if (value == null || (value instanceof String && ((String)value).equalsIgnoreCase("null"))) {
						ps.setNull(ii+1, java.sql.Types.VARCHAR);
					} else if (value instanceof String){
						ps.setString(ii+1, (String) value);
					} else if (value instanceof Integer){
						ps.setInt(ii+1, ((Integer) value).intValue());
					} else if (value instanceof Timestamp){
						ps.setTimestamp(ii+1, (Timestamp) value);
					} else {
						ps.setObject(ii+1, value);
					}
/*					if((value.oVal == null) || value.oVal.toString().equalsIgnoreCase("null") )
					{
					    if(value.strType.equalsIgnoreCase("VARCHAR2"))
					    {
					        ps.setNull(ii+1, java.sql.Types.VARCHAR);
					    }
					    else if(value.strType.equalsIgnoreCase("DATE") )
					    {
					        ps.setNull(ii+1, java.sql.Types.DATE);
					    }
					    else if(value.strType.equalsIgnoreCase("NUMBER"))
					    {
					        ps.setNull(ii+1, java.sql.Types.NUMERIC);
					    }
					    else if(value.strType.equalsIgnoreCase("CHAR"))
					    {
					        ps.setNull(ii+1, java.sql.Types.CHAR);
					    }
					    else
					    {
					        ps.setNull(ii+1, java.sql.Types.VARCHAR);
					    }
					    
					}
					else if((value.strType.equalsIgnoreCase("VARCHAR2"))||(value.strType.equalsIgnoreCase("CHAR")))
					{
					    ps.setString(ii+1, (String)value.oVal);
					}
					else if(value.strType.equalsIgnoreCase("DATE"))
					{
					    ps.setTimestamp(ii+1, (Timestamp)value.oVal);   
					}
					else if(value.strType.equalsIgnoreCase("NUMBER"))
					{
					    ps.setInt(ii+1, (Integer)value.oVal);
					}
					else 
					{
					    ps.setObject(ii+1, value.oVal);
					}*/
										
				}
			}

			return ps;
		}

		public void closeAll ()  throws SQLException {
			for (Iterator iter = prepStmtMap.values().iterator(); iter.hasNext();)
			{
				PreparedStatement ps = (PreparedStatement) iter.next();
				ps.close();
			}
		}
	}



private Object getUpdateDataByTypeForSubs(String tokenType,String tokenVal)
{
    String _method = "getUpdateDataByType";
    Object result = null;
    tokenType = tokenType.toUpperCase();
    if(tokenType.equals("NUMBER"))
    {
        if(tokenVal.equals("null"))
            tokenVal = "0";
        int tokenValInt = Integer.parseInt(tokenVal);
        result =new Integer(tokenValInt);
    }
    if(tokenType.equals("VARCHAR2"))
    {
        if(!tokenVal.equals("null"))
            tokenVal = "'"+tokenVal+"'";
        result = tokenVal;
    }
    if(tokenType.equals("CHAR"))
    {
        if(!tokenVal.equals("null"))
            tokenVal = "'"+tokenVal+"'";
        result = tokenVal;      
    }
    if(tokenType.equals("DATE"))
    {
        if(!tokenVal.equals("null"))
            tokenVal = "'"+tokenVal+"'";
        tokenVal = "to_date("+tokenVal+",'yyyy-MM-dd hh24:mi:ss')";
        result = tokenVal;
    }
    return result;
}
}