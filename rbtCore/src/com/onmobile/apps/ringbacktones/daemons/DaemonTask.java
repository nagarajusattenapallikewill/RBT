package com.onmobile.apps.ringbacktones.daemons;

public class DaemonTask
{
	int m_taskType = 0;
	Object m_obj = null;
	
	public DaemonTask(int task, Object obj)
	{
		m_taskType = task;
		m_obj = obj;
	}
	
	public int getTaskType()
	{
		return m_taskType;
	}
	
	public Object getObject()
	{
		return m_obj;
	}
}
