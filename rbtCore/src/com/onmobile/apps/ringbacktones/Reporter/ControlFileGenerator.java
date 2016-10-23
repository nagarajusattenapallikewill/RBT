/*
 * Created on Dec 12, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.Reporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author Mohsin
 */
public class ControlFileGenerator
{
	private static Logger logger = Logger.getLogger(ControlFileGenerator.class);
	
    String _class = "MorpheusUpdater$ControlFileGenerator";
    private MorpheusUpdater obj = null;
    private String load_data = "load data ";
    private String in_file = "infile ";
    private String append_into_table = " append into table ";
    private String truncate_into_table = " truncate into table ";
    //  private String when=" when (01) <> ";
    private String fields_terminated = " fields terminated by \",\" optionally enclosed by '\"' \n TRAILING NULLCOLS \n";
    private String m_site_id = null;

    //c'tor
    public ControlFileGenerator(String site_id,
            MorpheusUpdater MorpheusUpdateObj)
    {
        this.m_site_id = site_id;
        obj = MorpheusUpdateObj;
    }

    public String[] createControlFile(File dataFile, String site_id)
    {
        String dataFileName = dataFile.getAbsolutePath();
        logger.info("full data file path:" + dataFileName);

        String tmp_dataFileName = null;
        int dotindex = dataFileName.lastIndexOf(".");
        int slashindex = dataFileName.lastIndexOf(File.separator);
        if (slashindex == -1)
            slashindex = 0;
        if (dotindex == -1)
            dotindex = dataFileName.length() - 1;

        tmp_dataFileName = dataFileName.substring(slashindex + 1, dotindex);
        logger.info("data file name:" + tmp_dataFileName);

        if (tmp_dataFileName.equalsIgnoreCase("RBT_SUBSCRIBER"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_subscriber_report");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS"))
        {
            logger.info("puja : entered at req:"
                    + tmp_dataFileName);
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_subs_selections_report");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_ACCESS_TABLE"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_access_table_report");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_CATEGORIES"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "RBT_CATEGORIES_REPORT");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_CATEGORY_TYPE"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "RBT_CATEGORY_TYPE_REPORT");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_CLIPS"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_clips_report");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_CATEGORY_CLIP_MAP"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_category_clip_map_report");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("CDR_Report"))
        {
            //			logger.info("puja : entered CDR :");
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_cdr_entries");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_CONTENT_RELEASE"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_content_release_report");
        }
        else if (tmp_dataFileName
                .equalsIgnoreCase("RBT_SUBSCRIBER_CHARGING_CLASS"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_subs_charging_class_report");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_CHARGE_CLASS"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_charge_class_report");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_MUSICBOXES"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_musicboxes");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_MUSICBOX_CLIP_MAP"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "rbt_musicbox_clip_map");
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_VIRAL_SMS_TABLE"))
        {
            return createControlFile_Common(dataFileName, site_id,
                                            "RBT_VIRAL_SMS_TABLE");
        }
        return null;
    }

    public String[] createStagingControlFile(File dataFile, String site_id,
            int check)
    {
        String dataFileName = dataFile.getAbsolutePath();
        logger.info("full data file path:" + dataFileName);

        String tmp_dataFileName = null;
        int dotindex = dataFileName.lastIndexOf(".");
        int slashindex = dataFileName.lastIndexOf(File.separator);
        if (slashindex == -1)
            slashindex = 0;
        if (dotindex == -1)
            dotindex = dataFileName.length() - 1;

        tmp_dataFileName = dataFileName.substring(slashindex + 1, dotindex);
        logger.info("data file name:" + tmp_dataFileName);

        if (tmp_dataFileName.equalsIgnoreCase("RBT_SUBSCRIBER")
                || tmp_dataFileName.equalsIgnoreCase("RBT_SUBSCRIBER_TEMP"))
        {
            return createStagingControlFile_Common(dataFileName, site_id,
                                                   check,
                                                   "rbt_subscriber_staging");
            // CHECK : FOR tata:TEMP = 0,1 . FOR others=2
        }
        else if (tmp_dataFileName.equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS")
                || tmp_dataFileName
                        .equalsIgnoreCase("RBT_SUBSCRIBER_SELECTIONS_TEMP"))
        {
            return createStagingControlFile_Common(dataFileName, site_id,
                                                   check,
                                                   "rbt_selections_staging");
            //CHECK : FOR tata:TEMP = 0 :TXT = 1 . FOR others = 2
        }
        return null;
    }

    private String[] createControlFile_Common(String infileFullPath,
            String site_id, String table)
    {
        ArrayList alHeader = new ArrayList();
        HashMap colDetails = obj.getColNameTypeWithNullable(table, site_id);
        File tempFile = new File(infileFullPath);

        int slashindex = infileFullPath.lastIndexOf(File.separator);
        String check_file = infileFullPath.substring(0, slashindex);
        File fFile = new File(check_file + File.separator
                + "RBT_SUBSCRIBER_CHARGING_CLASS.txt");
        if (!fFile.exists())
        {
            fields_terminated = " fields terminated by \",\" optionally enclosed by \"'\" \n TRAILING NULLCOLS \n";
            //		logger.info("fFile does not exist");
        }
        else
        {
            fields_terminated = " fields terminated by \",\" optionally enclosed by '\"' \n TRAILING NULLCOLS \n";
            //		logger.info("fFile exist");
        }
        StringBuffer sb = new StringBuffer();
        String str = null;
        String firstLetter = null;
        try
        {
            LineNumberReader fin = new LineNumberReader(
                    new FileReader(tempFile));
            BufferedReader br = new BufferedReader(fin);
            str = br.readLine();
            firstLetter = str.substring(0, 1);
            //			logger.info("puja : table :"+table+"
            // firstLetter:"+firstLetter);
            alHeader = obj.getColHeader(str);
        }
        catch (Exception e)
        {
        }

        String[] arTemp = new String[2];
        arTemp[0] = "load_" + table + "_" + site_id + ".ctl";
        arTemp[1] = table;
        String table_name = table;
        //     String when_char=firstLetter;
        String field_0 = "site_id CONSTANT \'" + m_site_id + "\', \n";
        String field[] = new String[alHeader.size()];
        for (int i = 0; i < alHeader.size(); i++)
        {
            if (colDetails.containsKey((String) alHeader.get(i)))
            {
                String type = (String) colDetails.get(alHeader.get(i));
                field[i] = getFieldWithNullable(i, type, ((String) alHeader
                        .get(i)));
                //				logger.info("puja : table :"+table+" and
                // field["+i+"] :"+field[i]);
            }
        }
        //		String file_content =load_data+in_file+"'"+infileFullPath+"'
        // \n"+append_into_table+table_name+"\n"+when+"'"+when_char+"'
        // \n"+fields_terminated+"("+field_0;
        String file_content = load_data + in_file + "'" + infileFullPath
                + "' \n" + append_into_table + table_name + "\n"
                + fields_terminated + "(" + field_0;

        for (int j = 0; j < alHeader.size(); j++)
        {
            if (colDetails.containsKey((String) alHeader.get(j)))
            {
                file_content += field[j];
            }
        }
        file_content += "\n" + ")" + "\n";
        //	logger.info("puja: file_content :"+file_content);
        File ctlFile = new File(arTemp[0]);
        FileOutputStream fileout = null;
        try
        {
            ctlFile.createNewFile();
            fileout = new FileOutputStream(ctlFile);
            fileout.write(file_content.getBytes());
        }
        catch (IOException ioe)
        {
            logger.error("", ioe);
            return null;
        }
        try
        {
            fileout.close();
        }
        catch (IOException e)
        {
            logger.error("", e);
        }

        logger.info("control file name:" + arTemp[0]);
        return arTemp;
    }

    private String[] createStagingControlFile_Common(String infileFullPath,
            String site_id, int check, String table)
    {
        ArrayList alHeader = new ArrayList();
        HashMap colDetails = obj.getColNameTypeWithNullable(table, site_id);
        File tempFile = new File(infileFullPath);
        StringBuffer sb = new StringBuffer();
        String str = null;
        String firstLetter = null;
        try
        {
            LineNumberReader fin = new LineNumberReader(
                    new FileReader(tempFile));
            BufferedReader br = new BufferedReader(fin);
            str = br.readLine();
            firstLetter = str.substring(0, 1);
            alHeader = obj.getColHeader(str);
        }
        catch (Exception e)
        {
        }

        String[] arTemp = new String[2];
        arTemp[0] = "load_" + table + "_" + site_id + ".ctl";
        arTemp[1] = table;
        String table_name = table;
        //       String when_char="S";
        String field_0 = "site_id CONSTANT \'" + m_site_id + "\', \n";
        String field[] = new String[alHeader.size()];
        for (int i = 0; i < alHeader.size(); i++)
        {
            if (colDetails.containsKey((String) alHeader.get(i)))
            {
                String type = (String) colDetails.get(alHeader.get(i));
                field[i] = getFieldWithNullable(i, type, ((String) alHeader
                        .get(i)));
                //		logger.info("puja : table :"+table+" and
                // check :"+check+" and type :"+type+" and field["+i+"]
                // :"+field[i]);
            }
        }
        String file_content = null;
        if (check == 1)
            file_content = load_data + in_file + "'" + infileFullPath + "' \n"
                    + append_into_table + table_name + "\n" + fields_terminated
                    + "(" + field_0;
        else
            file_content = load_data + in_file + "'" + infileFullPath + "' \n"
                    + truncate_into_table + table_name + "\n"
                    + fields_terminated + "(" + field_0;

        for (int j = 0; j < alHeader.size(); j++)
        {
            if (colDetails.containsKey((String) alHeader.get(j)))
            {
                file_content += field[j];
            }
        }
        file_content += "\n" + ")" + "\n";
        // 		logger.info("puja
        // :staging:file_content:"+file_content+ new
        // SimpleDateFormat("hh24mmss").format(Calendar.getInstance().getTime()));
        File ctlFile = new File(arTemp[0]);
        FileOutputStream fileout = null;
        try
        {
            ctlFile.createNewFile();
            fileout = new FileOutputStream(ctlFile);
            fileout.write(file_content.getBytes());
        }
        catch (IOException ioe)
        {
            logger.error("", ioe);
            return null;
        }
        try
        {
            fileout.close();
        }
        catch (IOException e)
        {
            logger.error("", e);
        }

        logger.info("control file name:" + arTemp[0]);
        return arTemp;
    }

    private String getFieldWithNullable(int fieldNo, String type, String value)
    {
        String field = null;
        if (type.equalsIgnoreCase("Number_1"))
        {
            if (fieldNo == 0)
                field = value + " INTEGER EXTERNAL \n NULLIF " + value
                        + " =\"null\"";
            else
                field = ", \n " + value + " INTEGER EXTERNAL \n NULLIF "
                        + value + " =\"null\"";
        }
        if (type.equalsIgnoreCase("Number_0"))
        {
            if (fieldNo == 0)
                field = value + " INTEGER EXTERNAL";
            else
                field = ", \n " + value + " INTEGER EXTERNAL ";
        }
        else if (type.equalsIgnoreCase("VARCHAR2_1")
                || type.equalsIgnoreCase("CHAR_1"))
        {
            if (fieldNo == 0)
                field = value + " CHAR \n NULLIF " + value + " =\"null\" ";
            else
                field = ", \n " + value + " CHAR \n NULLIF " + value
                        + " =\"null\"";
        }
        else if (type.equalsIgnoreCase("VARCHAR2_0")
                || type.equalsIgnoreCase("CHAR_0"))
        {
            if (fieldNo == 0)
                field = value + " CHAR ";
            else
                field = ", \n " + value + " CHAR ";
        }
        else if (type.equalsIgnoreCase("DATE_1"))
        {
            if (fieldNo == 0)
                field = value + " date 'YYYY-MM-dd hh24:mi:ss' NULLIF " + value
                        + " =\"null\" ";
            else
                field = " , \n " + value
                        + " date 'YYYY-MM-dd hh24:mi:ss' NULLIF " + value
                        + " =\"null\" ";
        }
        else if (type.equalsIgnoreCase("DATE_0"))
        {
            if (fieldNo == 0)
                field = value + " date 'YYYY-MM-dd hh24:mi:ss' ";
            else
                field = ", \n " + value + " date 'YYYY-MM-dd hh24:mi:ss' ";
        }
        return field;
    }

}

/*
 * private String getField(int fieldNo,int colNo,String type,String value) {
 * String _method = "getField"; String field = null;
 * if(type.equalsIgnoreCase("Number")) { if(fieldNo==colNo) field = value+"
 * INTEGER EXTERNAL \n NULLIF "+value+" =\"null\" \n "; else field = value+"
 * INTEGER EXTERNAL \n NULLIF "+value+" =\"null\" , \n "; }else
 * if(type.equalsIgnoreCase("VARCHAR2") || type.equalsIgnoreCase("CHAR")) {
 * if(fieldNo==colNo) field = value+" CHAR \n NULLIF "+value+" =\"null\" \n ";
 * else field = value+" CHAR \n NULLIF "+value+" =\"null\" , \n "; }else
 * if(type.equalsIgnoreCase("DATE")) { if(fieldNo==colNo) field = value+" date
 * 'YYYY-MM-dd hh24:mi:ss' NULLIF "+value+" =\"null\" \n "; else field = value+"
 * date 'YYYY-MM-dd hh24:mi:ss' NULLIF "+value+" =\"null\" , \n "; } return
 * field; }
 *  
 */
