/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.common;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class Task
{
	private String taskAction = null;
	private HashMap<String, Object> taskSession = null;

	/**
	 * 
	 */
	public Task()
	{
		this(null);
	}

	/**
	 * @param taskAction
	 */
	public Task(String taskAction)
	{
		this.taskAction = taskAction;
		taskSession = new HashMap<String, Object>();
	}

	/**
	 * @param taskAction
	 * @param taskSession
	 */
	public Task(String taskAction, HashMap<String, Object> taskSession)
	{
		this.taskAction = taskAction;
		this.taskSession = taskSession;
	}

	/**
	 * @return the taskAction
	 */
	public String getTaskAction()
	{
		return taskAction;
	}

	/**
	 * @return the taskSession
	 */
	public HashMap<String, Object> getTaskSession()
	{
		return taskSession;
	}

	/**
	 * @param key
	 * @return the object stored in taskSession
	 */
	public Object getObject(String key)
	{
		return taskSession.get(key);
	}

	/**
	 * @param key
	 * @return the string stored in taskSession
	 */
	public String getString(String key)
	{
		return ((String) taskSession.get(key));
	}

	/**
	 * @param taskAction the taskAction to set
	 */
	public void setTaskAction(String taskAction)
	{
		this.taskAction = taskAction;
	}

	/**
	 * @param taskSession the taskSession to set
	 */
	public void setTaskSession(HashMap<String, Object> taskSession)
	{
		this.taskSession = taskSession;
	}

	/**
	 * @param key
	 * @param object to store in taskSession
	 */
	public void setObject(String key, Object object)
	{
		taskSession.put(key, object);
	}
	
	public boolean containsKey(String key)
	{
		return (taskSession.containsKey(key));
	}
    public boolean remove(String key){
    	 boolean b=false;
    	 taskSession.remove(key);
    	 b=true;
    	 return b;
    }
	@Override
	public String toString()
	{
		String string = "Task{taskAction = " + taskAction + ", taskSession = "
				+ taskSession + "}";

		return string;
	}
}
