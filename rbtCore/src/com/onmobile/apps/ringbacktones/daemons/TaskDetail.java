package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Calendar;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class TaskDetail implements iRBTConstant
{	
	protected ArrayList m_arrayList;
	private Calendar m_cal;
	private String m_taskName = "TaskDetail";
	private int m_minutes;
	
	private int m_iCount;
	private int m_fCount;
	private int m_pCount;
	
	private static int m_sleepMinutes = -1;
	private ArrayList m_processedInHourList;
	private int m_processedInHourCounter;
	
	public TaskDetail(int taskType, int minutes, int sleepMinutes)
	{
		m_taskName = TATARBTDaemonController.getTaskName(taskType);
		m_minutes = minutes;
		
		m_arrayList = new ArrayList();
		m_cal = Calendar.getInstance();
		m_iCount = 0;
		m_fCount = 0;
		m_pCount = 0;
		
		if(m_sleepMinutes < 0)
			m_sleepMinutes = sleepMinutes;
		
		m_processedInHourList = new ArrayList();
		initializeProcessedHourList();
		m_processedInHourCounter = 0;
	}

	public int getInitialCount()
	{
		return m_iCount;
	}

	public int getFinalCount()
	{
		return m_fCount;
	}

	public int getPreviousFinalCount()
	{
		return m_pCount;
	}
	
	public String getName()
	{
		return m_taskName;
	}

	public int getIncrementedCount()
	{
		return (m_fCount - m_iCount);
	}
	
	public int getProcessedCount()
	{
		return (m_pCount - m_iCount);
	}
	
	public int size()
	{
		return m_arrayList.size();
	}
	
	public void incrementCalendar()
	{
		m_cal.add(Calendar.MINUTE, m_minutes);
	}
	
	public boolean canAddToList(Calendar calPresent)
	{
		return (m_arrayList.size() <= 0 && (m_cal.before(calPresent)));
	}
	
	public void updateCounts (int iCount, int fCount) 
	{
		m_pCount = m_fCount;
		m_iCount = iCount;
		m_fCount = fCount;
		updateProcessedInHour();
	}
	
	public void updateCounts()
	{
		m_pCount = m_fCount;
		m_fCount = m_iCount = size();
		updateProcessedInHour();
	}
	
	public void initializeProcessedHourList()
	{
		for(int i = 0; i < 60/m_sleepMinutes; i++)
		{
			m_processedInHourList.add(i, new Integer(0));
		}
	}
	
	public void updateProcessedInHour()
	{
		m_processedInHourCounter  = (++m_processedInHourCounter)%(60/m_sleepMinutes);
		m_processedInHourList.set(m_processedInHourCounter, new Integer(getProcessedCount()));
	}
	
	public int getProcessedInHourCount()
	{
		int count = 0;
		
		for(int i = 0; i < m_processedInHourList.size(); i++)
		{
			count += ((Integer)m_processedInHourList.get(i)).intValue();
		}
		
		return count;
	}
}
