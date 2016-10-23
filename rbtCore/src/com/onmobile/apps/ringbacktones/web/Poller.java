package com.onmobile.apps.ringbacktones.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsUUID;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Poller
{

    public static Logger m_logger = Logger.getLogger(Poller.class.getName());

    private HashMap hmRbtSiteURLs = new HashMap();

    private CmsObject cms = null;

    private ResourceBundle m_bundle = null;
    private String m_resource = "resources/RBTWap";
    //  private String m_port = "8080";
    private String m_url = "rbt/rbt_channel.jsp";
    Hashtable m_categoriesTable = null;
    Hashtable m_sub1CategoriesTable = null;
    Hashtable m_sub2CategoriesTable = null;
    Hashtable m_clipsTable = null;

    public Poller()
    {

        try
        {
            m_bundle = ResourceBundle
                    .getBundle(m_resource, Locale.getDefault());
        }
        catch (Exception e)
        {
            m_logger
                    .error("RBT::Bundle could not be created and hence returning");
            return;
        }

        if (m_bundle != null)
        {
            //          try {
            //              m_port = m_bundle.getString("PORT_NO");
            //              if (m_port.length() == 0) {
            //                  m_port = "8080";
            //              }
            //          } catch (MissingResourceException e) {
            //              m_logger.info("RBT::PORT_NO not available in the resource file.
            // So using default");
            //              m_port = "8080";
            //          }

            try
            {
                m_url = m_bundle.getString("CHANNEL_URL");
                if (m_url.length() == 0)
                {
                    m_url = "rbt/rbt_channel.jsp";
                }
            }
            catch (MissingResourceException e)
            {
                m_logger
                        .error("RBT::CHANNEL_URL not available in the resource file. So using default");
                m_url = "rbt/rbt_channel.jsp";
            }
        }
    }

    //  private String getResponse(String strURL, String strType)
    //  {
    //        String value = null;
    //        try{
    //// value = "http://" + strURL + ":" + m_port + "/" + m_url + "?" ;
    //          if(!strURL.endsWith("/")) {
    //              strURL = strURL.trim() + "/" ;
    //          }
    //          value = strURL.trim() + m_url + "?" ;
    //          value = value + "REQUEST_TYPE=" + URLEncoder.encode(strType);
    //            
    //          m_logger.info("URL going to hit : " + value );
    //          
    //          URL url = new URL(value);
    //          HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    //          connection.setRequestMethod("GET");
    //          InputStream is = connection.getInputStream();
    //          BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
    //          String line = null;
    //            String response = "";
    //            while ((line = buffer.readLine())!= null){
    //              response += line;
    //            }
    //            if(response != null && !response.trim().equals("") &&
    // !response.trim().equalsIgnoreCase("Error")){
    //              return response;
    //          }
    //      }
    //      catch(Exception e)
    //      {
    //          m_logger.error("RBT::Exception in getResponse", e);
    //          return null;
    //      }
    //      
    //      return null;
    //    }

    private Document getTransDocFromServer(String xmlGetURL, String strType,
            String siteUrl)
    {
        boolean isReqThruInterceptor = false;
        if (siteUrl != null && siteUrl.length() != 0)
            isReqThruInterceptor = true;
        HttpClient client = null;
        GetMethod getMethod = null;
        int statusCode;
        if (!xmlGetURL.endsWith("/"))
        {
            xmlGetURL = xmlGetURL.trim() + "/";
        }
        xmlGetURL = xmlGetURL.trim() + m_url + "?";
        try
        {
            if (isReqThruInterceptor)

                xmlGetURL = xmlGetURL + "REQUEST_TYPE="
                        + URLEncoder.encode(strType, "UTF-8") + "&SITE_URL="
                        + siteUrl;
            else
                xmlGetURL = xmlGetURL + "REQUEST_TYPE="
                        + URLEncoder.encode(strType, "UTF-8");
        }
        catch (UnsupportedEncodingException e1)
        {
            m_logger.error("Exception unsupported encoding ");
            return null;
        }

        try
        {
            client = new HttpClient();
            client.setConnectionTimeout(1 * 60 * 1000);
            //            xmlGetURL = xmlGetURL.replaceAll(" ", "+");
            m_logger.info("URL going to hit : " + xmlGetURL);
            getMethod = new GetMethod(xmlGetURL);
            getMethod.addRequestHeader("Accept-Encoding", "gzip");
            statusCode = client.executeMethod(getMethod);
            m_logger.info("Status code: " + statusCode + " Text : "
                    + HttpStatus.getStatusText(statusCode));
        }
        catch (Exception e)
        {
            m_logger.error("Exception connecting URL ", e);
            getMethod.releaseConnection();
            return null;
        }
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        Document doc = null;
        try
        {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            Header h = getMethod.getResponseHeader("Content-Encoding");

            if (null != h && h.getValue().toLowerCase().indexOf("gzip") > -1)
            {
                m_logger.info("Content sent as gzip from the server");
                GZIPInputStream gIn = new GZIPInputStream(getMethod
                        .getResponseBodyAsStream());
                doc = db.parse(gIn);
            }
            else
            {
                m_logger.info("Content sent as plain text from the server");
                doc = db.parse(getMethod.getResponseBodyAsStream());
            }
        }
        catch (Exception e)
        {
            m_logger.error("Got exception while creating the document object",
                           e);
            return null;
        }
        finally
        {
            if (getMethod != null)
                getMethod.releaseConnection();
        }
        return doc;
    }

    public void init(CmsObject cms, HashMap urls) throws Exception
    {
        this.cms = cms;
        hmRbtSiteURLs = urls;
    }

    public void populateSites()
    {

        List resourceList = new CmsJspNavBuilder(cms)
                .getNavigationForFolder(cms.getRequestContext().getUri());
        Iterator it = resourceList.iterator();
        //get all sites for the customer
        while (it.hasNext())
        {
            CmsJspNavElement navElement = (CmsJspNavElement) it.next();
            String resource = navElement.getResourceName();
            /**
             * For Hutch RBT sites this variable is added.For each folder, the
             * site url of respective hutch servers will be indicated as a
             * property.This property will be used in interceptor.jsp in getting
             * the response of that server.In PollerScheduler property,each site
             * will have the same url i.e the Hutch Mumbai servers ip.
             */
            String siteUrl = navElement.getProperty("site_url");
            String url = (String) hmRbtSiteURLs.get(resource);
            if (url == null)
            {
                m_logger.info("RBT::URL is null for the site " + resource);
                continue;
            }
            m_logger.info("RBT::Processing URL " + url + " for the site "
                    + resource);

            m_categoriesTable = new Hashtable();
            m_sub1CategoriesTable = new Hashtable();
            m_clipsTable = new Hashtable();
            m_sub2CategoriesTable = new Hashtable();
            Document doc = getTransDocFromServer(url, "cat", siteUrl);
            if (doc != null)
            {
                try
                {
                    parseResponse(doc);
                    handleResources(cms, resource, m_categoriesTable, false);
                }
                catch (Exception e)
                {
                    m_logger
                            .error(
                                   "RBT::Exception in run while handling categories",
                                   e);
                    continue;
                }

                try
                {
                    m_logger
                            .info("*** Calling publishResources of " + resource);
                    cms.publishResource(resource, false, new CmsLogReport(
                            Locale.getDefault(), Poller.class));
                    //                  /**/cms.publishResource(resource, false, new
                    // CmsLogReport(Poller.class));
                    m_logger.info("*** Publishing of resource " + resource
                            + " is done.");
                }
                catch (Exception e)
                {
                    m_logger
                            .error(
                                   "RBT::Exception in run while publishing resources",
                                   e);
                }
            }
            else
            {
                m_logger.info("RBT::No response for the site " + resource);
            }
        }
    }

    //  private void handleResources(CmsJspActionElement actionElement, CmsObject
    // cmsobj, String resource, Hashtable resourceTable, boolean isClip) throws
    // CmsException
    private void handleResources(CmsObject cmsobj, String resource,
            Hashtable resourceTable, boolean isClip) throws CmsException
    {

        m_logger.debug("*** In  handleResources ");

        if (resource == null)
            return;

        CmsJspNavElement navElement = null;

        List resourceList = CmsJspNavBuilder.getNavigationForFolder(cmsobj,
                                                                    resource);
        CmsLock lock = null;
        String resourceNameAbsolute = null;
        String resourceID = null;
        String navText = null;
        String navPos = null;
        String promoID = null;
        String classType = null;
        String wavFile = null;
        String resourceName = null;
        String resourceNameModified = null;
        String album = null;

        String valueInTable = null;
        StringTokenizer st = null;
        //          String resourceIDInTable = null;
        String resourceNameInTable = null;
        String resourceNameInTableModified = null;
        String resourceWavFileNameInTable = null;
        String resourceIndexInTable = null;
        String resourcePromoIDInTable = null;
        String resourceClassTypeInTable = null;
        String resourceParent = null;
        String identifier = null;
        String albumInTable = null;

        if (resourceTable == null)
        {
            m_logger.info("RBT::No categories available for the site "
                    + resource);
            return;
        }

        if (resourceList != null)
        {
            for (int i = 0; i < resourceList.size(); i++)
            {
                try
                {
                    navElement = (CmsJspNavElement) resourceList.get(i);
                    resourceNameAbsolute = navElement.getResourceName();
                    m_logger.debug("Checking the resource "
                            + resourceNameAbsolute + " for changes.");
                    resourceID = cmsobj
                            .readPropertyObject(resourceNameAbsolute,
                                                "category_id", false)
                            .getValue();
                    navText = cmsobj.readPropertyObject(resourceNameAbsolute,
                                                        "NavText", false)
                            .getValue();
                    navPos = cmsobj.readPropertyObject(resourceNameAbsolute,
                                                       "NavPos", false)
                            .getValue();
                    promoID = cmsobj.readPropertyObject(resourceNameAbsolute,
                                                        "promoID", false)
                            .getValue();
                    classType = cmsobj.readPropertyObject(resourceNameAbsolute,
                                                          "classType", false)
                            .getValue();
                    album = cmsobj.readPropertyObject(resourceNameAbsolute,
                                                      "album", false)
                            .getValue();
                    wavFile = cmsobj.readPropertyObject(resourceNameAbsolute,
                                                        "wav_file", false)
                            .getValue();
                    resourceName = navElement.getTitle();
                    identifier = resourceID;
                    if (isClip)
                        identifier = resourceName;
                    if (identifier == null || identifier.length() == 0)
                        continue;

                    if (resourceName == null
                            || resourceName.trim().length() == 0)
                        continue;

                    resourceNameModified = modifyName(resourceName.replace(' ',
                                                                           '_'));

                    if (resourceTable.containsKey(identifier))
                    {
                        lock = cmsobj.getLock(resourceNameAbsolute);
                        if (lock.getType() == 0)
                        {
                            cmsobj.lockResource(resourceNameAbsolute);
                        }

                        valueInTable = (String) resourceTable.get(identifier);
                        st = new StringTokenizer(valueInTable, ",");
                        if (isClip)
                        {
                            resourceNameInTable = identifier;
                            if (st.hasMoreTokens())
                            {
                                resourceWavFileNameInTable = st.nextToken();
                            }
                            if (st.hasMoreTokens())
                            {
                                resourcePromoIDInTable = st.nextToken();
                            }
                            if (st.hasMoreTokens())
                            {
                                resourceIndexInTable = st.nextToken();
                            }
                            if (st.hasMoreTokens())
                            {
                                albumInTable = st.nextToken();
                            }
                        }
                        else
                        {
                            if (st.hasMoreTokens())
                            {
                                resourceNameInTable = st.nextToken();
                            }
                            if (st.hasMoreTokens())
                            {
                                resourceIndexInTable = st.nextToken();
                            }
                            if (st.hasMoreTokens())
                            {
                                resourcePromoIDInTable = st.nextToken();
                            }
                            if (st.hasMoreTokens())
                            {
                                resourceClassTypeInTable = st.nextToken();
                            }
                        }

                        if (resourceNameInTable != null)
                            resourceNameInTableModified = modifyName(resourceNameInTable
                                    .replace(' ', '_'));

                        resourceParent = resourceNameAbsolute
                                .substring(0, (resourceNameAbsolute
                                        .lastIndexOf(resourceNameModified)));

                        String resourceInTable = resourceParent
                                + resourceNameInTableModified
                                + (isClip ? "" : "/");

                        if (!resourceNameAbsolute
                                .equalsIgnoreCase(resourceInTable))
                        {
                            cmsobj.renameResource(resourceNameAbsolute,
                                                  resourceInTable);
                            m_logger.info("RBT::Category name changed from  "
                                    + resourceNameAbsolute + " to "
                                    + resourceInTable);
                        }
                        if (!navText.equalsIgnoreCase(resourceNameInTable))
                        {
                            cmsobj
                                    .writePropertyObject(
                                                         resourceInTable,
                                                         new CmsProperty(
                                                                 "NavText",
                                                                 resourceNameInTable,
                                                                 null));
                        }
                        if (isClip
                                && resourceWavFileNameInTable != null
                                && !resourceWavFileNameInTable
                                        .equalsIgnoreCase("null")
                                && (wavFile == null || !wavFile
                                        .equalsIgnoreCase(resourceWavFileNameInTable)))
                        {
                            cmsobj
                                    .writePropertyObject(
                                                         resourceInTable,
                                                         new CmsProperty(
                                                                 "wav_file",
                                                                 resourceWavFileNameInTable,
                                                                 null));
                        }
                        if (resourceIndexInTable != null
                                && !resourceIndexInTable
                                        .equalsIgnoreCase("null")
                                && (navPos == null || !navPos
                                        .equalsIgnoreCase(resourceIndexInTable)))
                        {
                            cmsobj
                                    .writePropertyObject(
                                                         resourceInTable,
                                                         new CmsProperty(
                                                                 "NavPos",
                                                                 resourceIndexInTable,
                                                                 null));
                        }
                        if (resourcePromoIDInTable != null
                                && !resourcePromoIDInTable
                                        .equalsIgnoreCase("null")
                                && (promoID == null || !promoID
                                        .equalsIgnoreCase(resourcePromoIDInTable)))
                        {
                            cmsobj
                                    .writePropertyObject(
                                                         resourceInTable,
                                                         new CmsProperty(
                                                                 "promoID",
                                                                 resourcePromoIDInTable,
                                                                 null));
                        }
                        if (!isClip
                                && resourceClassTypeInTable != null
                                && !resourceClassTypeInTable
                                        .equalsIgnoreCase("null")
                                && (classType == null || !classType
                                        .equalsIgnoreCase(resourceClassTypeInTable)))
                        {
                            cmsobj
                                    .writePropertyObject(
                                                         resourceInTable,
                                                         new CmsProperty(
                                                                 "classType",
                                                                 resourceClassTypeInTable,
                                                                 null));
                        }
                        if (!resourceName.equalsIgnoreCase(resourceNameInTable))
                        {
                            cmsobj
                                    .writePropertyObject(
                                                         resourceInTable,
                                                         new CmsProperty(
                                                                 "Title",
                                                                 resourceNameInTable,
                                                                 null));
                        }

                        lock = cmsobj.getLock(resourceInTable);

                        if (!resourceNameAbsolute
                                .equalsIgnoreCase(resourceInTable)
                                && lock.getType() != 0)
                        {
                            cmsobj.unlockResource(resourceInTable);
                        }
                        if (isClip
                                && albumInTable != null
                                && !albumInTable.equalsIgnoreCase("null")
                                && (album == null || !album
                                        .equalsIgnoreCase(albumInTable)))
                        {
                            cmsobj.writePropertyObject(resourceInTable,
                                                       new CmsProperty("album",
                                                               albumInTable,
                                                               null));
                        }
                        lock = cmsobj.getLock(resourceNameAbsolute);
                        if (lock.getType() != 0)
                        {
                            cmsobj.unlockResource(resourceNameAbsolute);
                        }

                        if (!isClip
                                && m_sub1CategoriesTable
                                        .containsKey(resourceID))
                        {
                            handleResources(cmsobj, resourceInTable,
                                            (Hashtable) m_sub1CategoriesTable
                                                    .get(resourceID), false);
                            //                          handleResources(cmsobj,resourceNameAbsolute,
                            //                                  (Hashtable)m_sub1CategoriesTable.get(resourceID),
                            // false);
                        }
                        if (!isClip
                                && m_sub2CategoriesTable
                                        .containsKey(resourceID))
                        {
                            handleResources(cmsobj, resourceInTable,
                                            (Hashtable) m_sub2CategoriesTable
                                                    .get(resourceID), false);
                            //                          handleResources(cmsobj,resourceNameAbsolute,
                            //                                  (Hashtable)
                            // m_sub2CategoriesTable.get(resourceID), false);
                        }
                        if (!isClip && m_clipsTable.containsKey(resourceID))
                        {
                            handleResources(cmsobj, resourceInTable,
                                            (Hashtable) m_clipsTable
                                                    .get(resourceID), true);
                            //                          handleResources(cmsobj,resourceNameAbsolute,
                            //                                  (Hashtable) m_clipsTable.get(resourceID),true);
                        }

                        if (cmsobj.readResource(resourceInTable).getState() == CmsResource.STATE_CHANGED)
                        {
                            m_logger.info("RBT::Resource  " + resourceInTable
                                    + " has been modified.");
                        }

                        resourceTable.remove(identifier);
                    }
                    else
                    {

                        lock = cmsobj.getLock(resourceNameAbsolute);

                        if (lock.isNullLock())
                        {
                            // resource is not locked, lock it automatically
                            cms.lockResource(resourceNameAbsolute);
                            m_logger.info("RBT::Deleting "
                                    + resourceNameAbsolute
                                    + " as it is no longer available");
                            cms
                                    .deleteResource(
                                                    resourceNameAbsolute,
                                                    CmsResource.DELETE_REMOVE_SIBLINGS);
                            cms.unlockResource(resourceNameAbsolute);
                        }
                        else
                        {
                            CmsUUID userUUID = lock.getUserId();
                            m_logger.error("Can't delete resource "
                                    + resourceNameAbsolute
                                    + ", is locked by user "
                                    + cms.readUser(userUUID).getName());
                        }

                    }
                }
                catch (Exception e)
                {
                    m_logger.error("RBT::Exception in handle resources", e);
                }
            }
        }

        if (!resourceTable.isEmpty())
        {
            createResources(cmsobj, resource, resourceTable, isClip);
        }

    }

    //  private void createResources(CmsJspActionElement actionElement, CmsObject
    // cmsobj, String resource, Hashtable resourceTable, boolean isClip) throws
    // CmsException
    private void createResources(CmsObject cmsobj, String resource,
            Hashtable resourceTable, boolean isClip) throws CmsException
    {
        if (resourceTable.isEmpty())
            return;

        CmsLock lock = null;
        String resourceName = null;
        String resourceId = null;
        String valueInTable = null;
        String resourceNameInTable = null;
        String resourceWavFileNameInTable = null;
        String resourcePromoIDInTable = null;
        String resourceClassTypeInTable = null;
        String resourceIndexInTable = null;
        String albumInTable = null;
        StringTokenizer st = null;
        String resourceNameInTableModified = null;
        Iterator it = (Iterator) resourceTable.keys();
        while (it.hasNext())
        {
            try
            {
                if (isClip)
                {
                    resourceNameInTable = (String) it.next();
                    valueInTable = (String) resourceTable
                            .get(resourceNameInTable);
                }
                else
                {
                    resourceId = (String) it.next();
                    valueInTable = (String) resourceTable.get(resourceId);
                }
                st = new StringTokenizer(valueInTable, ",");
                if (isClip)
                {
                    if (st.hasMoreTokens())
                    {
                        resourceWavFileNameInTable = st.nextToken();
                    }
                    if (st.hasMoreTokens())
                    {
                        resourcePromoIDInTable = st.nextToken();
                    }
                    if (st.hasMoreTokens())
                    {
                        resourceIndexInTable = st.nextToken();
                    }
                    if (st.hasMoreTokens())
                    {
                        albumInTable = st.nextToken();
                    }
                }
                else
                {
                    if (st.hasMoreTokens())
                    {
                        resourceNameInTable = st.nextToken();
                    }
                    if (st.hasMoreTokens())
                    {
                        resourceIndexInTable = st.nextToken();
                    }
                    if (st.hasMoreTokens())
                    {
                        resourcePromoIDInTable = st.nextToken();
                    }
                    if (st.hasMoreTokens())
                    {
                        resourceClassTypeInTable = st.nextToken();
                    }
                }
                resourceNameInTableModified = modifyName(resourceNameInTable
                        .replace(' ', '_'));
                resourceName = resource + resourceNameInTableModified
                        + (isClip ? "" : "/");

                m_logger.info(isClip + "  RBT::Adding new resource "
                        + resourceName);
                try
                {
                    cmsobj.createResource(resourceName, (isClip ? 32 : 0)); // 0 -
                    // Folder
                    // , 32
                    // -
                    // OnspireLink
                    // xml
                }
                catch (CmsLockException le)
                {
                    m_logger.error("Exception while updating resource "
                            + resourceName);
                    m_logger.error(le.toString());
                    cms.publishResource(resource, false, new CmsLogReport(
                            Locale.getDefault(), Poller.class));
                    //                  /**/cmsobj.publishResource(resourceName, false, new
                    // CmsLogReport(Poller.class));
                    cmsobj.createResource(resourceName, (isClip ? 32 : 0)); // 0 -
                    // Folder
                    // , 32
                    // -
                    // OnspireLink
                    // xml
                }

                lock = cmsobj.getLock(resourceName);

                if (lock.getType() == 0)
                {
                    cmsobj.lockResource(resourceName);
                }
                cmsobj.writePropertyObject(resourceName, new CmsProperty(
                        "NavText", resourceNameInTable, null));
                cmsobj.writePropertyObject(resourceName, new CmsProperty(
                        "NavPos", resourceIndexInTable, null));
                cmsobj.writePropertyObject(resourceName, new CmsProperty(
                        "Title", resourceNameInTable, null));
                if (!isClip && resourceId != null
                        && !resourceId.equalsIgnoreCase("null"))
                    cmsobj.writePropertyObject(resourceName, new CmsProperty(
                            "category_id", resourceId, null));
                if (!isClip && resourceClassTypeInTable != null
                        && !resourceClassTypeInTable.equalsIgnoreCase("null"))
                    cmsobj.writePropertyObject(resourceName, new CmsProperty(
                            "classType", resourceClassTypeInTable, null));
                if (resourcePromoIDInTable != null
                        && !resourcePromoIDInTable.equalsIgnoreCase("null"))
                    cmsobj.writePropertyObject(resourceName, new CmsProperty(
                            "promoID", resourcePromoIDInTable, null));
                if (isClip && resourceWavFileNameInTable != null
                        && !resourceWavFileNameInTable.equalsIgnoreCase("null"))
                    cmsobj.writePropertyObject(resourceName, new CmsProperty(
                            "wav_file", resourceWavFileNameInTable, null));
                if (isClip && albumInTable != null
                        && !albumInTable.equalsIgnoreCase("null"))
                    cmsobj.writePropertyObject(resourceName, new CmsProperty(
                            "album", albumInTable, null));

                lock = cmsobj.getLock(resourceName);

                if (lock.getType() != 0)
                {
                    cmsobj.unlockResource(resourceName);
                }
                if (!isClip && m_sub1CategoriesTable.containsKey(resourceId))
                {
                    createResources(cmsobj, resourceName,
                                    (Hashtable) m_sub1CategoriesTable
                                            .get(resourceId), false);
                }
                if (!isClip && m_sub2CategoriesTable.containsKey(resourceId))
                {
                    createResources(cmsobj, resourceName,
                                    (Hashtable) m_sub2CategoriesTable
                                            .get(resourceId), false);
                }
                if (!isClip && m_clipsTable.containsKey(resourceId))
                {
                    createResources(cmsobj, resourceName,
                                    (Hashtable) m_clipsTable.get(resourceId),
                                    true);
                }
            }
            catch (Exception e)
            {
                m_logger.error("RBT::Exception in handle resources", e);
            }

        }

    }

    private void parseResponse(Document document)
    {

        try
        {
            NodeList nodeList = document.getElementsByTagName("rbt");
            Node node = nodeList.item(0);
            nodeList = ((Element) node).getElementsByTagName("categories");
            if (nodeList.getLength() == 0)
            {
                return;
            }

            node = nodeList.item(0);
            nodeList = ((Element) node).getElementsByTagName("category");

            String id = null;
            String name = null;
            String index = null;
            String promoID = null;
            String classType = null;
            String value = null;

            for (int i = 0; i < nodeList.getLength(); i++)
            {
                node = nodeList.item(i);
                id = getAttribute(node, "id");
                name = getAttribute(node, "name");
                index = "" + (i + 1);
                promoID = getAttribute(node, "promoID");
                classType = getAttribute(node, "classType");
                value = name + "," + index + "," + promoID + "," + classType;
                m_categoriesTable.put(id, value);
                Hashtable sub1CategoriesTable = checkForSubCategory(node);
                if (sub1CategoriesTable != null)
                {
                    m_sub1CategoriesTable.put(id, sub1CategoriesTable);
                }
                else
                {
                    Hashtable clipsTable = checkForClip(node);
                    if (clipsTable != null)
                    {
                        m_clipsTable.put(id, clipsTable);
                    }
                }
            }
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in parseResponse", e);
        }
        return;
    }

    private Hashtable checkForSubCategory(Node node)
    {

        String id = null;
        String name = null;
        String index = null;
        String promoID = null;
        String classType = null;
        String value = null;
        Hashtable sub2CategoriesTable = null;
        Hashtable sub1CategoriesTable = null;
        try
        {
            NodeList nodeList = ((Element) node)
                    .getElementsByTagName("sub1Category");
            if (nodeList.getLength() == 0)
            {
                return sub1CategoriesTable;
            }

            sub1CategoriesTable = new Hashtable();

            for (int i = 0; i < nodeList.getLength(); i++)
            {
                node = nodeList.item(i);
                id = getAttribute(node, "id");
                name = getAttribute(node, "name");
                index = "" + (i + 1);
                promoID = getAttribute(node, "promoID");
                classType = getAttribute(node, "classType");
                value = name + "," + index + "," + promoID + "," + classType;
                sub1CategoriesTable.put(id, value);
                sub2CategoriesTable = checkForBouquet(node);

                if (sub2CategoriesTable != null)
                {
                    m_sub2CategoriesTable.put(id, sub2CategoriesTable);
                }
                else
                {
                    Hashtable clipsTable = checkForClip(node);
                    if (clipsTable != null)
                    {
                        m_clipsTable.put(id, clipsTable);
                    }
                }
            }
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in checkForSubCategory", e);
        }

        return sub1CategoriesTable;
    }

    private Hashtable checkForBouquet(Node node)
    {
        String id = null;
        String name = null;
        String index = null;
        String promoID = null;
        String classType = null;
        String value = null;
        Hashtable clipsTable = null;
        Hashtable sub2CategoriesTable = null;
        try
        {
            NodeList nodeList = ((Element) node)
                    .getElementsByTagName("sub2Category");
            if (nodeList.getLength() == 0)
            {
                return sub2CategoriesTable;
            }

            sub2CategoriesTable = new Hashtable();

            for (int i = 0; i < nodeList.getLength(); i++)
            {
                node = nodeList.item(i);
                id = getAttribute(node, "id");
                name = getAttribute(node, "name");
                index = "" + (i + 1);
                promoID = getAttribute(node, "promoID");
                classType = getAttribute(node, "classType");
                value = name + "," + index + "," + promoID + "," + classType;
                sub2CategoriesTable.put(id, value);
                clipsTable = checkForClip(node);
                if (clipsTable != null)
                {
                    m_clipsTable.put(id, clipsTable);
                }
            }
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in checkForBouquet", e);
        }

        return sub2CategoriesTable;
    }

    private Hashtable checkForClip(Node node)
    {
        String name = null;
        String index = null;
        String wavFile = null;
        String promoID = null;
        String album = null;
        String value = null;
        Hashtable clipsTable = null;
        try
        {
            NodeList nodeList = ((Element) node).getElementsByTagName("clip");
            if (nodeList.getLength() == 0)
            {
                return clipsTable;
            }

            clipsTable = new Hashtable();

            for (int i = 0; i < nodeList.getLength(); i++)
            {
                node = nodeList.item(i);
                name = getAttribute(node, "name");
                index = "" + (i + 1);
                wavFile = getAttribute(node, "wavFile");
                promoID = getAttribute(node, "promoID");
                album = getAttribute(node, "album");
                value = wavFile + "," + promoID + "," + index + "," + album;
                clipsTable.put(name, value);
            }
        }
        catch (Exception e)
        {
            m_logger.error("RBT::Exception in checkForClip", e);
        }
        return clipsTable;
    }

    private String getAttribute(Node node, String strAttribute)
    {
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Attr attr = (Attr) attributes.item(i);
            if ((attr.getName()).equalsIgnoreCase(strAttribute))
                return attr.getValue();
        }
        return null;
    }

    public String modifyName(String str)
    {
        String result = "";
        for (int i = 0; i < str.length(); i++)
        {
            if ((str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')
                    || (str.charAt(i) >= 'a' && str.charAt(i) <= 'z')
                    || (str.charAt(i) >= '0' && str.charAt(i) <= '9')
                    || str.charAt(i) == '_')
            {
                result = result + str.charAt(i);
            }

        }
        return result;
    }

    public static String getStackTrace(Throwable ex)
    {
        StringWriter stringWriter = new StringWriter();
        String trace = "";
        if (ex instanceof Exception)
        {
            Exception exception = (Exception) ex;
            exception.printStackTrace(new PrintWriter(stringWriter));
            trace = stringWriter.toString();
            trace = trace.substring(0, trace.length() - 2);
            trace = System.getProperty("line.separator") + " \t" + trace;
        }
        return trace;
    }
}