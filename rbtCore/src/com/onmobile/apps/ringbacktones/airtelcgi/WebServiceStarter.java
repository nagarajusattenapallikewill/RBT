/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.onmobile.common.debug.DebugManager;
import com.onmobile.ds.web.http.sc.EmbeddedTC;

/**
 * @author vinayasimha.patil
 *
 */
class WebServiceStarter 
{
	private static Logger logger = Logger.getLogger(WebServiceStarter.class);

	private String hostName = "localhost";
	private int httpPort = -1;
	private int httpMaxConnections = -1;
	private int httpsMaxConnections = -1;
	private int httpsPort = -1;
	private String appBase = null;


	/**
	 * @param hostName
	 * @param httpPort
	 * @param httpMaxConnections
	 * @param httpsMaxConnections
	 * @param httpsPort
	 * @param appBase
	 */
	public WebServiceStarter(String hostName, int httpPort, int httpMaxConnections, int httpsMaxConnections, int httpsPort, String appBase)
	{
		this.hostName = hostName;
		this.httpPort = httpPort;
		this.httpMaxConnections = httpMaxConnections;
		this.httpsMaxConnections = httpsMaxConnections;
		this.httpsPort = httpsPort;
		this.appBase = appBase;
	}

	public void startWebServer() throws Exception
	{

		logger.info("RBT:: Before parseing xml");

		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory oDbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder oDb = oDbf.newDocumentBuilder();
		Document document = null;

		logger.info("RBT:: Before creating config xml");

		String config = "<xml><config><GUI host=\"" + hostName + "\" appBase=\"" + appBase + "\">";

		if(httpMaxConnections>0 && httpPort>0)
			config += "<HTTP enabled=\"y\" port=\""+httpPort+"\" maxConnections=\""+httpMaxConnections+"\" minConnections=\""+(httpMaxConnections/4+1)+"\"/>" ;
		else
			config += "<HTTP enabled=\"n\" port=\""+httpPort+"\" maxConnections=\""+httpMaxConnections+"\" minConnections=\""+(httpMaxConnections/4+1)+"\"/>" ;

		if(httpsMaxConnections>0 && httpsPort>0)
			config += "<HTTPS enabled=\"y\" port=\""+httpsPort+"\" maxConnections=\""+httpsMaxConnections+"\" minConnections=\""+(httpsMaxConnections/4+1)+"\"/>" ;
		else
			config += "<HTTPS enabled=\"n\" port=\""+httpsPort+"\" maxConnections=\""+httpsMaxConnections+"\" minConnections=\""+(httpsMaxConnections/4+1)+"\"/>" ;

		config += "<AccessLog enabled=\"y\"/></GUI></config></xml>";

		DebugManager.trace("SMSGateway", "WebServiceStarter", "startWebServer", config, Thread.currentThread().getName(), null);
		logger.info("RBT:: Config data: "+ config);

		document = oDb.parse(new ByteArrayInputStream(config.getBytes()));

		Element e = document.getDocumentElement();
		NodeList oAllProjectsNode = e.getElementsByTagName("config");
		int iNoOfProjects = oAllProjectsNode.getLength();
		System.out.println();
		Node configNode = null;
		for (int iIndex=0;iIndex < iNoOfProjects;iIndex++)
		{
			Node oProjNode = oAllProjectsNode.item(iIndex);
			configNode = oProjNode;
		}

		startTomcatServer(configNode);
	}

	private void startTomcatServer(Node configNode) throws Exception
	{
		String name = "Tomcat";
		EmbeddedTC embeddedTC = new EmbeddedTC(name, false);
		embeddedTC.configure(configNode);
		if(!embeddedTC.startServletContainer())
			throw new Exception("Web Server not started");
	}
}