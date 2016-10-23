package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.type.StringType;

public class UTF8String extends StringType {

	private static final long serialVersionUID = 1L;
	private static Logger basicLogger = Logger.getLogger(UTF8String.class);

	public Object get(ResultSet rs, String name) throws SQLException {
		byte[] utf8bytes = null;
		try {
			String value = rs.getString(name);
			if(value != null)
				utf8bytes = rs.getString(name).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
		}
		if(utf8bytes == null) {
			return null;
		}
		String output = null;
		try {
			output = new String(utf8bytes, "UTF-8");
		} catch(UnsupportedEncodingException usee) {
			output = new String(utf8bytes);
			basicLogger.error("Exception converting the bytes " + output + " into String ", usee);
		}
		return output;
	}
	
	public void set(PreparedStatement pstmt, Object value, int index) throws SQLException {		
		if(value instanceof String) {
			if(basicLogger.isDebugEnabled()) {
				basicLogger.debug("Converting the string " + value + " into bytes");
			}
			try {
				if(value == null) {
					pstmt.setBytes(index, null);
				}
				else{
					pstmt.setBytes(index, ((String)value).getBytes("UTF-8"));
				}
			} catch(UnsupportedEncodingException usee) {
				basicLogger.error("Exception converting the string " + value + " into bytes ", usee);
			}
		} else {
			basicLogger.error("Object value " + value + ". Expected input type is String. Actual type is " + value.getClass().getName());
		}
	}
	
	public static void main(String args[]) {
		try{
			String s1 = "CLIP18_0_1_";
			byte[] b = s1.getBytes();
			String s = new String(b,"UTF-8");
			System.out.println(s);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

