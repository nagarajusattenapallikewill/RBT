package com.onmobile.apps.ringbacktones.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLXPathParser {
	private static Logger logger = Logger.getLogger(XMLXPathParser.class);
	
	private XPathExpression expr =null;
	private XPathExpression[] exprArray = null; 
	private Document doc = null;
	
	public XMLXPathParser(String strXML){
		
		logger.info("inside the constructor which takes only 1 xpath as argument");
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
		DocumentBuilder builder;
		try {
			builder = domFactory.newDocumentBuilder();
			ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(strXML.getBytes());
			this.doc = builder.parse(byteArrayInputStream);
			if(this.doc!=null){
				System.out.println("doc is not null");
			}
		} catch (SAXException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		}
		
	}
	public void populateExpr(String xPath){
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		try {
			this.expr = xpath.compile(xPath);
		} catch (XPathExpressionException e1) {
			e1.printStackTrace();
		}
	}
	public void populateExpr(String[] xPath){
		String method="populateExpr with arr";
		logger.info("entering");
		ArrayList<XPathExpression> xpressionArr=new ArrayList<XPathExpression>();
		if(xPath!=null && xPath.length>0){
			logger.info("xPath!=null && xPath.length>0");
		for(int i=0;i<xPath.length;i++){
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			logger.info("xpath=="+xpath.toString());
			try {
				xpressionArr.add( xpath.compile(xPath[i]));
				logger.info("this.exprArray["+"i"+"]=="+xpressionArr.get(xpressionArr.size()-1).toString());
				
			} catch (XPathExpressionException e1) {
				e1.printStackTrace();
			}
		}
		if(xpressionArr!=null && xpressionArr.size()>0){
			this.exprArray=(XPathExpression[])xpressionArr.toArray(new XPathExpression[0]);
		}
		}
	}
	//,String xPath
	//,String[] xPath
	
	public NodeList getXPathResultNodeList(String xPath){
		populateExpr(xPath);
		logger.info("inside getXPathResultNodeList");
		NodeList nodes = null;
		if(this==null){
			
			return nodes;
		}
		
		if(this.doc==null||this.expr==null){
			
			return nodes;
		}
		try {
			logger.info("inside try block evaluating the results of the xPath");
			
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;

		} catch (XPathExpressionException e) {
			
			logger.info("inside catch block : "+e.getStackTrace());
	
		}
		if(nodes==null){
			logger.info("No nodes are retrieved on evaluating the xPath");
			return null;
		}
		
		return nodes;
	}
	
	public Node getXPathResultNode(String xPath){
		
		logger.info("inside getXPathResultNode");
		populateExpr(xPath);
		Node node = null;
		if(this==null){
			
			return node;
		}
		
		if(this.doc==null||this.expr==null){
			
			return node;
		}
		try {
			logger.info("inside try block evaluating the results of the xPath");
			
			Object result = expr.evaluate(doc, XPathConstants.NODE);
			node = (Node) result;

		} catch (XPathExpressionException e) {
			
			logger.info("inside catch block : "+e.getStackTrace());
	
		}
		if(node==null){
			logger.info("No node is retrieved on evaluating the xPath");
			return null;
		}
		
		return node;

	}
	
	public boolean getXPathResultBoolean(String xPath){
		
		logger.info("inside getXPathResultBoolean");
		populateExpr(xPath);
		boolean returnBoolean = false;
		if(this==null){
			
			return returnBoolean;
		}
		
		if(this.doc==null||this.expr==null){
			
			return returnBoolean;
		}
		try {
			logger.info("inside try block evaluating the results of the xPath");
			
			Object result = expr.evaluate(doc, XPathConstants.BOOLEAN);
			returnBoolean = ((Boolean) result).booleanValue();

		} catch (XPathExpressionException e) {
			
			logger.info("inside catch block : "+e.getStackTrace());
	
		}
		
		logger.info("The boolean value retrieved on evaluating the xPath : "+returnBoolean);
		return returnBoolean;

	}
	
	public String getXPathResultString(String xPath){
		
		logger.info("inside getXPathResultString");
		populateExpr(xPath);
		String returnString = null;
		if(this==null){
			
			return returnString;
		}
		
		if(this.doc==null||this.expr==null){
			
			return returnString;
		}
		try {
			logger.info("inside try block evaluating the results of the xPath");
			
			Object result = expr.evaluate(doc, XPathConstants.STRING);
			returnString = (String) result;

		} catch (XPathExpressionException e) {
			
			logger.info("inside catch block : "+e.getStackTrace());
	
		}
		
		logger.info("The value of string retrieved on evaluating the xPath : "+returnString);
		return returnString;

	}
	
	public double getXPathResultDouble(String xPath){
		
		logger.info("inside getXPathResultDouble");
		populateExpr(xPath);
		double returnDouble=-1;
		if(this==null){
			
			return returnDouble;
		}
		
		if(this.doc==null||this.expr==null){
			
			return returnDouble;
		}
		try {
			logger.info("inside try block evaluating the results of the xPath");
			
			Object result = expr.evaluate(doc, XPathConstants.NUMBER);
			returnDouble = ((Double) result).doubleValue();

		} catch (XPathExpressionException e) {
			
			logger.info("inside catch block : "+e.getStackTrace());
	
		}
		
		logger.info("Returning double value on evaluating xpath :"+returnDouble);
		return returnDouble;

	}
	
	public String[] getXPathResultStringArray(String[] xPath){
		
		logger.info("inside getXPathResultStringArray");
		populateExpr(xPath);
		String[] returnStringArray = null;
		List returnStringList = new ArrayList();
		String tempString;
		if(this==null){
			logger.info("this==null");
			System.out.println("this==null");
			return returnStringArray;
		}
		
		if(this.doc==null||this.exprArray==null){
			if(this.doc==null){
				logger.info("this.doc==null");
				System.out.println("this==null");
			}else{
				logger.info("this.exprArray==null");
				System.out.println("this.exprArray==null");
			}
			return returnStringArray;
		}
		
		try {
			logger.info("inside try block evaluating the results of the xPath");
			for(int i=0;i<exprArray.length;i++){
				
				Object result = exprArray[i].evaluate(doc, XPathConstants.STRING);
				tempString = (String)result;
				returnStringList.add(tempString);
			}
		} catch (XPathExpressionException e) {
			
			logger.info("inside catch block : "+e.getStackTrace());
	
		}
		
		if(returnStringList!=null && returnStringList.size()>0){
			returnStringArray = (String[]) returnStringList.toArray(new String[0]);
			for(int cou=0;cou<returnStringArray.length;cou++){
				logger.info("value"+cou+"=="+returnStringArray[cou]);
			}
			logger.info("The value of string is retrieved on evaluating the xPaths");
			
			return returnStringArray;
			
		}else{
			logger.info("No string value is retrieved on evaluating the xpaths");
			return returnStringArray;
		}
	}
}
