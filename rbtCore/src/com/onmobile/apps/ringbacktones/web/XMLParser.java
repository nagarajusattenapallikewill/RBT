package com.onmobile.apps.ringbacktones.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLParser
{
    public static Logger m_logger = null;
    private ResourceBundle m_bundle = null;
    private String m_resource = "resources/RBTWap";
    private String m_port = "8080";
    private String m_channel_url = "rbt/rbt_channel.jsp";
    private String m_activated_by_WAP = "WAP";
    private String m_act_info_WAP = "WAP";
    private String m_deactivated_by_WAP = "WAP";
    private String m_selected_by_WAP = "WAP";
    private String m_selection_info_WAP = "WAP";
    private String m_sub_class_WAP = "WAP";
    private String m_charge_class_WAP = "WAP";
    private String m_activated_by_WEB = "WEB";
    private String m_act_info_WEB = "WEB";
    private String m_deactivated_by_WEB = "WEB";
    private String m_selected_by_WEB = "WEB";
    private String m_selection_info_WEB = "WEB";
    private String m_sub_class_WEB = "WEB";
    private String m_charge_class_WEB = null;
    private int m_search_category_id = 11;

    public XMLParser()
    {

        m_logger = Logger.getLogger("RBT_CHANNEL_WEB_WAP");

        try
        {
            m_bundle = ResourceBundle
                    .getBundle(m_resource, Locale.getDefault());
        }
        catch (Exception e)
        {
            m_logger
                    .info("RBT::Bundle couldnot be created and hence returning");
            return;
        }
        if (m_bundle != null)
        {
            try
            {
                m_port = m_bundle.getString("PORT_NO");
                if (m_port.length() == 0)
                {
                    m_port = "8080";
                }
            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::PORT_NO not available in the resource file. So using default");
                m_port = "8080";
            }

            try
            {
                m_channel_url = m_bundle.getString("CHANNEL_URL");
                if (m_channel_url.length() == 0)
                {
                    m_channel_url = "rbt/rbt_channel.jsp";
                }
            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::CHANNEL_URL not available in the resource file. So using default");
                m_channel_url = "rbt/rbt_channel.jsp";
            }

            try
            {
                String categoryID = m_bundle.getString("SEARCH_CATEGORY_ID");
                if (categoryID == null || categoryID.length() == 0)
                {
                    m_search_category_id = 11;
                }
                else
                {
                    m_search_category_id = Integer.parseInt(categoryID);
                }
            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::SEARCH_CATEGORY_ID not available in the resource file. So using default");
                m_search_category_id = 11;
            }

            try
            {
                m_activated_by_WAP = m_bundle.getString("ACTIVATED_BY_WAP");
                if (m_activated_by_WAP == null
                        || m_activated_by_WAP.length() == 0)
                {

                    m_activated_by_WAP = "WAP";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::ACTIVATED_BY_WAP not available in the resource file. So using default");
                m_activated_by_WAP = "WAP";
            }

            try
            {
                m_act_info_WAP = m_bundle.getString("ACT_INFO_WAP");
                if (m_act_info_WAP == null || m_act_info_WAP.length() == 0)
                {

                    m_act_info_WAP = "WAP";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::ACT_INFO_WAP not available in the resource file. So using default");
                m_act_info_WAP = "WAP";
            }
            try
            {
                m_deactivated_by_WAP = m_bundle.getString("DEACTIVATED_BY_WAP");
                if (m_deactivated_by_WAP == null
                        || m_deactivated_by_WAP.length() == 0)
                {

                    m_deactivated_by_WAP = "WAP";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::DEACTIVATED_BY_WAP not available in the resource file. So using default");
                m_deactivated_by_WAP = "WAP";
            }

            try
            {
                m_selected_by_WAP = m_bundle.getString("SELECTED_BY_WAP");
                if (m_selected_by_WAP == null
                        || m_selected_by_WAP.length() == 0)
                {

                    m_selected_by_WAP = "WAP";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::SELECTED_BY_WAP not available in the resource file. So using default");
                m_selected_by_WAP = "WAP";
            }

            try
            {
                m_selection_info_WAP = m_bundle.getString("SELECTION_INFO_WAP");
                if (m_selection_info_WAP == null
                        || m_selection_info_WAP.length() == 0)
                {

                    m_selection_info_WAP = "WAP";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::SELECTION_INFO_WAP not available in the resource file. So using default");
                m_selection_info_WAP = "WAP";
            }

            try
            {
                m_sub_class_WAP = m_bundle.getString("SUB_CLASS_WAP");
                if (m_sub_class_WAP == null || m_sub_class_WAP.length() == 0)
                {

                    m_sub_class_WAP = "WAP";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::SUB_CLASS_WAP not available in the resource file. So using default");
                m_sub_class_WAP = "WAP";
            }

            try
            {
                m_charge_class_WAP = m_bundle.getString("CHARGE_CLASS_WAP");
                if (m_charge_class_WAP == null
                        || m_charge_class_WAP.length() == 0)
                {

                    m_charge_class_WAP = "WAP";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::CHARGE_CLASS_WAP not available in the resource file. So using default");
                m_charge_class_WAP = "WAP";
            }

            try
            {
                m_activated_by_WEB = m_bundle.getString("ACTIVATED_BY_WEB");
                if (m_activated_by_WEB == null
                        || m_activated_by_WEB.length() == 0)
                {

                    m_activated_by_WEB = "WEB";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::ACTIVATED_BY_WEB not available in the resource file. So using default");
                m_activated_by_WEB = "WEB";
            }

            try
            {
                m_act_info_WEB = m_bundle.getString("ACT_INFO_WEB");
                if (m_act_info_WEB == null || m_act_info_WEB.length() == 0)
                {

                    m_act_info_WEB = "WEB";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::ACT_INFO_WEB not available in the resource file. So using default");
                m_act_info_WEB = "WEB";
            }
            try
            {
                m_deactivated_by_WEB = m_bundle.getString("DEACTIVATED_BY_WEB");
                if (m_deactivated_by_WEB == null
                        || m_deactivated_by_WEB.length() == 0)
                {

                    m_deactivated_by_WEB = "WEB";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::DEACTIVATED_BY_WEB not available in the resource file. So using default");
                m_deactivated_by_WEB = "WEB";
            }

            try
            {
                m_selected_by_WEB = m_bundle.getString("SELECTED_BY_WEB");
                if (m_selected_by_WEB == null
                        || m_selected_by_WEB.length() == 0)
                {

                    m_selected_by_WEB = "WEB";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::SELECTED_BY_WEB not available in the resource file. So using default");
                m_selected_by_WEB = "WEB";
            }

            try
            {
                m_selection_info_WEB = m_bundle.getString("SELECTION_INFO_WEB");
                if (m_selection_info_WEB == null
                        || m_selection_info_WEB.length() == 0)
                {

                    m_selection_info_WEB = "WEB";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::SELECTION_INFO_WEB not available in the resource file. So using default");
                m_selection_info_WEB = "WEB";
            }

            try
            {
                m_sub_class_WEB = m_bundle.getString("SUB_CLASS_WEB");
                if (m_sub_class_WEB == null || m_sub_class_WEB.length() == 0)
                {

                    m_sub_class_WEB = "WEB";
                }

            }
            catch (MissingResourceException e)
            {
                m_logger
                        .info("RBT::SUB_CLASS_WEB not available in the resource file. So using default");
                m_sub_class_WEB = "WEB";
            }

        }
    }

    public int searchCategoryID()
    {
        return m_search_category_id;
    }

    public String getResponse(String strURL, String strSubID, String strType,
            String strValue, boolean isWap)
    {
        String value = null;

        try
        {
            if (isWap)
                value = "http://" + strURL + ":" + m_port + "/" + m_channel_url
                        + "?ACTIVATED_BY="
                        + URLEncoder.encode(m_activated_by_WAP, "UTF-8")
                        + "&ACT_INFO="
                        + URLEncoder.encode(m_act_info_WAP, "UTF-8")
                        + "&DEACTIVATED_BY="
                        + URLEncoder.encode(m_deactivated_by_WAP, "UTF-8")
                        + "&SELECTED_BY="
                        + URLEncoder.encode(m_selected_by_WAP, "UTF-8")
                        + "&SELECTION_INFO="
                        + URLEncoder.encode(m_selection_info_WAP, "UTF-8")
                        + "&SUB_CLASS="
                        + URLEncoder.encode(m_sub_class_WAP, "UTF-8")
                        + "&CHARGE_CLASS="
                        + URLEncoder.encode(m_charge_class_WAP, "UTF-8")
                        + "&CHANNEL=WAP";
            else
                value = "http://" + strURL + ":" + m_port + "/" + m_channel_url
                        + "?ACTIVATED_BY="
                        + URLEncoder.encode(m_activated_by_WEB, "UTF-8")
                        + "&ACT_INFO="
                        + URLEncoder.encode(m_act_info_WEB, "UTF-8")
                        + "&DEACTIVATED_BY="
                        + URLEncoder.encode(m_deactivated_by_WEB, "UTF-8")
                        + "&SELECTED_BY="
                        + URLEncoder.encode(m_selected_by_WEB, "UTF-8")
                        + "&SELECTION_INFO="
                        + URLEncoder.encode(m_selection_info_WEB, "UTF-8")
                        + "&SUB_CLASS="
                        + URLEncoder.encode(m_sub_class_WEB, "UTF-8")
                        + "&CHANNEL=WEB";
            if (strValue == null)
                value = value + "&SUB_ID="
                        + URLEncoder.encode(strSubID, "UTF-8")
                        + "&REQUEST_TYPE="
                        + URLEncoder.encode(strType, "UTF-8");
            else
                value = value + "&SUB_ID="
                        + URLEncoder.encode(strSubID, "UTF-8")
                        + "&REQUEST_TYPE="
                        + URLEncoder.encode(strType, "UTF-8")
                        + "&REQUEST_VALUE="
                        + URLEncoder.encode(strValue, "UTF-8");

            URL url = new URL(value);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(is));
            String line = null;
            String response = "";
            while ((line = buffer.readLine()) != null)
                response += line;

            if (response != null && !response.trim().equals("")
                    && !response.trim().equalsIgnoreCase("Error"))
            {
                return response;
            }

        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in getResponse", e);
            return null;
        }

        return null;
    }

    public Hashtable parseStatus(String strResponse) throws Exception
    {
        Hashtable table = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(strResponse));
            Document document = builder.parse(input);

            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);

            nodeList = ((Element) node).getElementsByTagName("selections");
            if (nodeList.getLength() == 0)
            {
                return null;
            }
            node = nodeList.item(0);

            table = new Hashtable();
            nodeList = ((Element) node).getElementsByTagName("selection");
            if (nodeList.getLength() == 0)
            {
                return table;
            }

            for (int i = 0; i < nodeList.getLength(); i++)
            {
                node = nodeList.item(i);
                String caller = getAttribute(node, "caller");
                String song = getAttribute(node, "song");
                String fromTime = getAttribute(node, "fromTime");
                String toTime = getAttribute(node, "toTime");
                if (caller != null && song != null && fromTime != null
                        && toTime != null)
                    table.put(caller, song + "," + fromTime + "," + toTime);
            }
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseStatus", e);
            throw new Exception("Exception in parseStatus");
        }

        return table;
    }

    public String parseActivate(String strResponse) throws Exception
    {
        String activate = "false";
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(strResponse));
            Document document = builder.parse(input);

            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);

            nodeList = ((Element) node).getElementsByTagName("subscriber");
            node = nodeList.item(0);
            activate = getAttribute(node, "activate");
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseActivate", e);
            throw new Exception("Exception in parseActivate");
        }

        return activate;
    }

    public boolean parseDeactivate(String strResponse) throws Exception
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(strResponse));
            Document document = builder.parse(input);

            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);

            nodeList = ((Element) node).getElementsByTagName("subscriber");
            node = nodeList.item(0);
            String activate = getAttribute(node, "deactivate");
            if (activate.equalsIgnoreCase("true"))
                return true;

        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseDeactivate", e);
            throw new Exception("Exception in parseDeactivate");
        }

        return false;
    }

    public boolean parseSelection(String strResponse) throws Exception
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(strResponse));
            Document document = builder.parse(input);

            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);

            nodeList = ((Element) node).getElementsByTagName("setSelection");
            node = nodeList.item(0);
            String activate = getAttribute(node, "success");
            if (activate.equalsIgnoreCase("true"))
                return true;

        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseSelection", e);
            throw new Exception("Exception in parseSelection");
        }

        return false;
    }

    public boolean parseRemove(String strResponse) throws Exception
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(strResponse));
            Document document = builder.parse(input);

            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);

            nodeList = ((Element) node).getElementsByTagName("remove");
            node = nodeList.item(0);
            String activate = getAttribute(node, "success");
            if (activate.equalsIgnoreCase("true"))
                return true;

        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseRemove", e);
            throw new Exception("Exception in parseRemove");
        }

        return false;
    }

    public Hashtable parseSearch(String strResponse) throws Exception
    {
        Hashtable table = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(strResponse));
            Document document = builder.parse(input);

            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);

            nodeList = ((Element) node).getElementsByTagName("searchSongs");
            if (nodeList.getLength() == 0)
            {
                return null;
            }
            node = nodeList.item(0);

            table = new Hashtable();
            nodeList = ((Element) node).getElementsByTagName("searchSong");
            if (nodeList.getLength() == 0)
            {
                return table;
            }

            for (int i = 0; i < nodeList.getLength(); i++)
            {
                node = nodeList.item(i);
                String name = getAttribute(node, "name");
                String wavFile = getAttribute(node, "wavFile");
                String promoID = getAttribute(node, "promotionID");
                String album = getAttribute(node, "album");
                String lang = getAttribute(node, "lang");
                if (name != null && wavFile != null && promoID != null)
                    table.put(name, wavFile + "-" + promoID + "-" + album + "-"
                            + lang);
            }
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseSearch", e);
            throw new Exception("Exception in parseSearch");
        }

        return table;
    }

    public boolean parseSMS(String strResponse) throws Exception
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource input = new InputSource(new StringReader(strResponse));
            Document document = builder.parse(input);

            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);

            nodeList = ((Element) node).getElementsByTagName("sms");
            node = nodeList.item(0);
            String success = getAttribute(node, "success");
            if (success.equalsIgnoreCase("true"))
                return true;
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseSMS", e);
            throw new Exception("Exception in parseSMS");
        }
        return false;
    }

    private Node getNode(Node node, String strTag)
    {
        NodeList nodeList = ((Element) node).getElementsByTagName(strTag);
        if (nodeList.getLength() != 0)
        {
            return nodeList.item(0);
        }
        return null;
    }

    private String getAttribute(Node node, String strAttribute)
    {
        Element element = (Element) node;
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Attr attr = (Attr) attributes.item(i);
            if ((attr.getName()).equalsIgnoreCase(strAttribute))
                return attr.getValue();
        }
        return null;
    }

    public static void main(String[] args)
    {

    }
}