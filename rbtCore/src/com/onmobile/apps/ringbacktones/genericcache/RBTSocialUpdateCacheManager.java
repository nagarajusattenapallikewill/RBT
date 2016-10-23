package com.onmobile.apps.ringbacktones.genericcache;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;



public class RBTSocialUpdateCacheManager {

	private static RBTSocialUpdateCacheManager instance = new RBTSocialUpdateCacheManager();
	
	public boolean publishUpdates(RBTSocialUpdate update)
	{

		Calendar cal = Calendar.getInstance();
		cal.set(2037, 0, 1, 0, 0, 0);
		java.sql.Timestamp startTime = new  Timestamp((new Date()).getTime());
		update.setStartTime(startTime);
		update.setStatus(1);
		Date endDate = cal.getTime();

		java.sql.Timestamp endTime = new  Timestamp(endDate.getTime());
		update.setEndTime(endTime);

	
		try {
			update.insert();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean deactivateUpdates(long sequenceId,String msisdn)
	{

		RBTSocialUpdate rbtSocialUpdate = null;
		

		try {
			
			rbtSocialUpdate= RBTSocialUpdate.getRBTSocialUpdate(sequenceId, msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		if (rbtSocialUpdate != null)
		{
			Date currentDate = new Date();
			
					if (rbtSocialUpdate.getEndTime() != null
							&& rbtSocialUpdate.getEndTime().after(currentDate)
							&& rbtSocialUpdate.getStartTime() != null
							&& rbtSocialUpdate.getStartTime().before(currentDate))
					{


						java.sql.Timestamp endTime = new  Timestamp((new Date()).getTime());
						rbtSocialUpdate.setEndTime(endTime);

						try {
							rbtSocialUpdate.update();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
					}

				}
			
		



		return true;		
	}
	public boolean changeUpdatestatus(long sequenceId,String msisdn,int status)
	{

		 RBTSocialUpdate rbtSocialUpdate = null;
		

		try {
			rbtSocialUpdate = RBTSocialUpdate.getRBTSocialUpdate(sequenceId,msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		if (rbtSocialUpdate != null)
		{
			
					rbtSocialUpdate.setStatus(status);
					try {
						rbtSocialUpdate.update();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}

				}
		



		return true;
	}


	public List< RBTSocialUpdate> getActiveUpdates(String msisdn)
	{
		List< RBTSocialUpdate> rbtSocialUpdateList = null;
		List<RBTSocialUpdate> activeRbtSocialUpdateList = new ArrayList<RBTSocialUpdate>() ;
		

		try {
			rbtSocialUpdateList = RBTSocialUpdate.getRBTSocialUpdate(msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (rbtSocialUpdateList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUpdate rbtSocialUpdate : rbtSocialUpdateList)
			{
				
					if (rbtSocialUpdate.getEndTime() != null
							&& rbtSocialUpdate.getEndTime().after(currentDate)
							&& rbtSocialUpdate.getStartTime() != null
							&& rbtSocialUpdate.getStartTime().before(currentDate))
						activeRbtSocialUpdateList.add(rbtSocialUpdate);
				
			}
		}
		return activeRbtSocialUpdateList;


	}
	public List< RBTSocialUpdate> getActiveUpdates(String msisdn,int status)
	{
		List< RBTSocialUpdate> rbtSocialUpdateList = null;
		List<RBTSocialUpdate> activeRbtSocialUpdateList = new ArrayList<RBTSocialUpdate>() ;
		
		try {
			rbtSocialUpdateList = RBTSocialUpdate.getRBTSocialUpdate(msisdn, status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (rbtSocialUpdateList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUpdate rbtSocialUpdate : rbtSocialUpdateList)
			{
				
					if (rbtSocialUpdate.getEndTime() != null
							&& rbtSocialUpdate.getEndTime().after(currentDate)
							&& rbtSocialUpdate.getStartTime() != null
							&& rbtSocialUpdate.getStartTime().before(currentDate))
						activeRbtSocialUpdateList.add(rbtSocialUpdate);
				
			}
		}
		return activeRbtSocialUpdateList;
	}
	public List< RBTSocialUpdate> getDeactiveUpdates(String msisdn)
	{
		List< RBTSocialUpdate> rbtSocialUpdateList = null;
		List<RBTSocialUpdate> deactiveRbtSocialUpdateList = new ArrayList<RBTSocialUpdate>() ;
		
		try {
			rbtSocialUpdateList = RBTSocialUpdate.getRBTSocialUpdate(msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (rbtSocialUpdateList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUpdate rbtSocialUpdate : rbtSocialUpdateList)
			{
				
					if (rbtSocialUpdate.getEndTime() != null
							&& rbtSocialUpdate.getEndTime().before(currentDate)
							&& rbtSocialUpdate.getStartTime() != null
							&& rbtSocialUpdate.getStartTime().before(currentDate))
						deactiveRbtSocialUpdateList.add(rbtSocialUpdate);
				
			}
		}
		return deactiveRbtSocialUpdateList;
	}
	public List< RBTSocialUpdate> getDeactiveUpdates(String msisdn,int status)
	{
		List< RBTSocialUpdate> rbtSocialUpdateList = null;
		List<RBTSocialUpdate> deactiveRbtSocialUpdateList = new ArrayList<RBTSocialUpdate>() ;
		

		try {
			rbtSocialUpdateList = RBTSocialUpdate.getRBTSocialUpdate(msisdn, status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (rbtSocialUpdateList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUpdate rbtSocialUpdate : rbtSocialUpdateList)
			{
				
					if (rbtSocialUpdate.getEndTime() != null
							&& rbtSocialUpdate.getEndTime().before(currentDate)
							&& rbtSocialUpdate.getStartTime() != null
							&& rbtSocialUpdate.getStartTime().before(currentDate))
						deactiveRbtSocialUpdateList.add(rbtSocialUpdate);
				
			}
		}
		return deactiveRbtSocialUpdateList;
	}
	public static RBTSocialUpdateCacheManager getInstance() {
		return instance;
	}
	
	public String getUpdateQueryString(int status, long eventtype, int fetchSize, long presentSequenceId) {
		String hql = RBTSocialUpdate.getUpdateQueryString(status, eventtype, fetchSize, presentSequenceId);
		return hql;
	}


}
