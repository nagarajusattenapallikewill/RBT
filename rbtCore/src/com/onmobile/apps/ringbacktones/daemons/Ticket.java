package com.onmobile.rbt;

import java.util.Date;

public class Ticket 
{
	private int id;
	private String source;
	private String ip;
	private String location;
	private String name;
	private String severity;
	private Date create_ts;
	private String summary;
	private String groups;
	private String items;
	private String timestamp;
	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Ticket(int id, String source, String ip, String location,
			String name, String severity, Date create_ts, String summary,
			String groups, String items) {
		super();
		this.id = id;
		this.source = source;
		this.ip = ip;
		this.location = location;
		this.name = name;
		this.severity = severity;
		this.create_ts = create_ts;
		this.summary = summary;
		this.groups = groups;
		this.items = items;
	}
	
	//Added by Parul
	public Ticket(int id,  String ip, String location,
			String name, String severity, String timestamp, String summary,
			String groups, String items) {
		super();
		this.id = id;
		this.ip = ip;
		this.location = location;
		this.name = name;
		this.severity = severity;
		this.timestamp = timestamp;
		this.summary = summary;
		this.groups = groups;
		this.items = items;
		
	}
	
	//Added by Parul
	public Ticket(int id,  String ip, String location,
			String name, String severity, Date create_ts, String summary,
			String groups, String items) {
		super();
		this.id = id;
		this.ip = ip;
		this.location = location;
		this.name = name;
		this.severity = severity;
		this.create_ts = create_ts;
		this.summary = summary;
		this.groups = groups;
		this.items = items;
		
	}

	public Ticket() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public String getGroups() {
		return groups;
	}
	public void setGroups(String groups) {
		this.groups = groups;
	}
	public String getIp() 
	{
		return ip;
	}
	public void setIp(String ip) 
	{
		this.ip = ip;
	}
	public String getSeverity() 
	{
		return severity;
	}
	public void setSeverity(String severity) 
	{
		this.severity = severity;
	}
	public int getId() 
	{
		return id;
	}
	public void setId(int id) 
	{
		this.id = id;
	}
	public String getSource() 
	{
		return source;
	}
	public void setSource(String source) 
	{
		this.source = source;
	}
	public String getSummary() 
	{
		return summary;
	}
	public void setSummary(String summary) 
	{
		this.summary = summary;
	}
	public String getName() 
	{
		return name;
	}
	public void setName(String name) 
	{
		this.name = name;
	}
	public String getLocation() 
	{
		return location;
	}
	public void setCreate_ts(Date create_ts) 
	{
		this.create_ts = create_ts;
	}
	public Date getCreate_ts() 
	{
		return create_ts;
	}
	public void setLocation(String location) 
	{
		this.location = location;
	}
}
