package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class FileDetailsImpl extends RBTPrimitive {

	private static Logger LOG = Logger.getLogger(FileDetailsImpl.class);

	private final static String TABLE_NAME = "RBT_FILE_DETAILS";
	private final static String NAME = "NAME";

	private String name;

	static boolean insert(Connection conn, String name) {

		StringBuffer query = new StringBuffer(
				"INSERT INTO RBT_FILE_DETAILS (name, created_time) VALUES ");
		Statement stmt = null;
		boolean isInserted = false;
		try {
			stmt = conn.createStatement();
			query.append(" (");
			query.append(sqlString(name)).append(", ");
			query.append("SYSDATE() ").append(" )");
			
			LOG.debug("Executing Query: " + query);
			int i = stmt.executeUpdate(query.toString());
			if (i > 0) {
				LOG.debug("Successfully inserted file: " + name);
				isInserted = true;
			} else {
				LOG.debug("No records inserted");
			}
		} catch (Exception e) {
			LOG.error("Failed to insert file: " + name + ". Exception: "
					+ e.getMessage(), e);
		}

		return isInserted;
	}

	static FileDetailsImpl retrieve(Connection conn, String name) {

		String query = "SELECT * FROM " + TABLE_NAME;
		StringBuffer dquery = new StringBuffer();
		Statement stmt = null;
		ResultSet results = null;
		FileDetailsImpl fd = null;

		try {
			stmt = conn.createStatement();
			if (null != name) {
				dquery.append(" WHERE ");
				dquery.append(NAME).append(" = ").append(sqlString(name));
			}

			dquery.insert(0, query);
			LOG.debug("Executing Query: " + dquery.toString());
			results = stmt.executeQuery(dquery.toString());
			if (results.first()) {
				fd = new FileDetailsImpl();
				fd.setName(results.getString(1));
				LOG.debug("Got FileDetailsImpl results from database. fileDetailsImpl: "
								+ fd);
			} else {
				LOG.debug("No result found. fileDetailsImpl: " + fd);
			}
		} catch (Exception e) {
			LOG.error("Failed to insert. Exception: " + e.getMessage(), e);
		}
		return fd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	@Override
	public String toString() {
		return "FileDetailsImpl [name=" + name + "]";
	}
}
