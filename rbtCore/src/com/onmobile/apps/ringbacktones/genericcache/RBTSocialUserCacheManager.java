package com.onmobile.apps.ringbacktones.genericcache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUser;


public class RBTSocialUserCacheManager {
	
	private static RBTSocialUserCacheManager instance = new RBTSocialUserCacheManager();

	private static Logger logger = Logger.getLogger(RBTSocialUserCacheManager.class);



	/**
	 * @param cosID
	 * @return List<RBTSocialUser>
	 */
	@SuppressWarnings("unchecked")
	public RBTSocialUser getActiveSNGUser(String userId,int socialType)
	{

		//RBTSocialUser rbtSocialUser = null;
		RBTSocialUser rbtSocialUser = null;
		
		try {
			rbtSocialUser = RBTSocialUser.getRBTSocialUser(userId, socialType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Problem in calling rbtSocialUserDao.getAllRBTSocialUser");
			return null;

		}

		
		Date currentDate = new Date();

		if(rbtSocialUser!=null){
			
			logger.info("Rbtsocial user list "+rbtSocialUser.getMsisdn());
			if (rbtSocialUser.getEndTime() != null
					&& rbtSocialUser.getEndTime().after(currentDate)
					&& rbtSocialUser.getStartTime() != null
					&& rbtSocialUser.getStartTime().before(currentDate))
				return rbtSocialUser;
		}



		return null;

	}

	public List<RBTSocialUser> getActiveSNGUser(String msisdn)
	{

		//RBTSocialUser rbtSocialUser = null;
		List<RBTSocialUser> rbtSocialUserList = null;
		List<RBTSocialUser> activeRbtSocialUserList = new ArrayList<RBTSocialUser>() ;
		

		try {
			rbtSocialUserList = RBTSocialUser.getRBTSocialUser(msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Problem in calling rbtSocialUserDao.getAllRBTSocialUser");
			return null;
		}
		if (rbtSocialUserList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUser rbtSocialUser : rbtSocialUserList)
			{

				if (rbtSocialUser.getEndTime() != null
						&& rbtSocialUser.getEndTime().after(currentDate)
						&& rbtSocialUser.getStartTime() != null
						&& rbtSocialUser.getStartTime().before(currentDate))
					activeRbtSocialUserList.add(rbtSocialUser);

			}
		}
		return activeRbtSocialUserList;

	}


	public RBTSocialUser getDeactiveSNGUser(String userId,int socialType){
		RBTSocialUser rbtSocialUser = null;
		
		try {
			rbtSocialUser = RBTSocialUser.getRBTSocialUser(userId, socialType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Problem in calling rbtSocialUserDao.getAllRBTSocialUser");
			return null;
		}

		Date currentDate = new Date();
		if(rbtSocialUser!=null){
			if (rbtSocialUser.getEndTime() != null
					&& rbtSocialUser.getEndTime().before(currentDate)
					&& rbtSocialUser.getStartTime() != null
					&& rbtSocialUser.getStartTime().before(currentDate))
				return rbtSocialUser;
		}

		return null;
	}

	/**
	 * @return List<RBTSocialUser>
	 */
	public List<RBTSocialUser> getDeactiveSNGUser(String msisdn)
	{

		//RBTSocialUser rbtSocialUser = null;
		List<RBTSocialUser> rbtSocialUserList = null;
		List<RBTSocialUser> deactiveRbtSocialUserList = new ArrayList<RBTSocialUser>() ;
		
		try {
			rbtSocialUserList = RBTSocialUser.getRBTSocialUser(msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Problem in calling rbtSocialUserDao.getAllRBTSocialUser");
			return null;
		}
		if (rbtSocialUserList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUser rbtSocialUser : rbtSocialUserList)
			{

				if (rbtSocialUser.getEndTime() != null
						&& rbtSocialUser.getEndTime().before(currentDate)
						&& rbtSocialUser.getStartTime() != null
						&& rbtSocialUser.getStartTime().before(currentDate))
					deactiveRbtSocialUserList.add(rbtSocialUser);

			}
		}
		return deactiveRbtSocialUserList;

	}





	/**
	 * Add cosDetail to RBT_COS_DETAIL table and cache
	 * 
	 * @param rbtSocialUser
	 * @return void
	 */
	public boolean activateSNGUser(RBTSocialUser user){

		RBTSocialUser activeUserExists = getActiveSNGUser(user.getUserId(),user.getSocialType());
		RBTSocialUser deactiveUserExists = getDeactiveSNGUser(user.getUserId(),user.getSocialType());
		if(activeUserExists!=null)
			return false;

		if(deactiveUserExists!=null){
			deactiveUserExists.setMsisdn(user.getMsisdn());
			user = deactiveUserExists;
			Calendar cal = Calendar.getInstance();
			cal.set(2037, 0, 1, 0, 0, 0);
			java.sql.Timestamp startTime = new  Timestamp((new Date()).getTime());
			user.setStartTime(startTime);
			Date endDate = cal.getTime();

			java.sql.Timestamp endTime = new  Timestamp(endDate.getTime());
			user.setEndTime(endTime);	

			try {
				user.update();
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Problem in calling rbtSocialUserDao.updateRBTSocialUser");
				return false;
			}
			return true;
		}

		Calendar cal = Calendar.getInstance();
		cal.set(2037, 0, 1, 0, 0, 0);
		java.sql.Timestamp startTime = new  Timestamp((new Date()).getTime());
		user.setStartTime(startTime);
		Date endDate = cal.getTime();

		java.sql.Timestamp endTime = new  Timestamp(endDate.getTime());
		user.setEndTime(endTime);	

		try {
			user.insert();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Problem in calling rbtSocialUserDao.insertRBTSocialUser");
			return false;
		}
		return true;		


	}
	/**
	 * Add cosDetail to RBT_COS_DETAIL table and cache
	 * 
	 * @param rbtSocialUser
	 * @return void
	 */
	public boolean updateSNGUser(RBTSocialUser user){

		RBTSocialUser activeUserExists = getActiveSNGUser(user.getUserId(),user.getSocialType());
		RBTSocialUser deactiveUserExists = getDeactiveSNGUser(user.getUserId(),user.getSocialType());
		
		if(activeUserExists==null && deactiveUserExists==null){
			Calendar cal = Calendar.getInstance();
			cal.set(2037, 0, 1, 0, 0, 0);
			java.sql.Timestamp startTime = new  Timestamp((new Date()).getTime());
			user.setStartTime(startTime);
			Date endDate = cal.getTime();

			java.sql.Timestamp endTime = new  Timestamp(endDate.getTime());
			user.setEndTime(endTime);	

			try {
				user.insert();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("Problem in calling rbtSocialUserDao.insertRBTSocialUser");
				return false;
			}
			return true;		
		}
		if(activeUserExists!=null){
			activeUserExists.setMsisdn(user.getMsisdn());
			user = activeUserExists;
			try {
				user.update();
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Problem in calling rbtSocialUserDao.updateRBTSocialUser");
				return false;
			}
			return true;
		}
		if(deactiveUserExists!=null){
			deactiveUserExists.setMsisdn(user.getMsisdn());
			user = deactiveUserExists;
			Calendar cal = Calendar.getInstance();
			cal.set(2037, 0, 1, 0, 0, 0);
			java.sql.Timestamp startTime = new  Timestamp((new Date()).getTime());
			user.setStartTime(startTime);
			Date endDate = cal.getTime();

			java.sql.Timestamp endTime = new  Timestamp(endDate.getTime());
			user.setEndTime(endTime);	

			try {
				user.update();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("Problem in calling rbtSocialUserDao.updateRBTSocialUser");
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean deactivateSNGUser(String userId,int socialType){
		RBTSocialUser activeUserExists = getActiveSNGUser(userId,socialType);
		RBTSocialUser deactiveUserExists = getDeactiveSNGUser(userId,socialType);
		RBTSocialUser user;
		if(deactiveUserExists!=null)
			return false;

		if(activeUserExists!=null){
			user = activeUserExists;

			java.sql.Timestamp endTime = new  Timestamp((new Date()).getTime());
			user.setEndTime(endTime);


			try {
				user.update();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("Problem in calling rbtSocialUserDao.updateRBTSocialUser");
				return false;
			}
			return true;
		}

		return false;
	}

	public boolean activateSNGUser(String msisdn){
		List<RBTSocialUser> rbtSocialUserList = null;

		
		try {
			rbtSocialUserList = RBTSocialUser.getRBTSocialUser(msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Problem in calling rbtSocialUserDao.getAllRBTSocialUser");
			return false;
		}
		if (rbtSocialUserList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUser user : rbtSocialUserList)
			{

				if (user.getEndTime() != null
						&& user.getEndTime().before(currentDate)
						&& user.getStartTime() != null
						&& user.getStartTime().before(currentDate))
				{
					Calendar cal = Calendar.getInstance();
					cal.set(2037, 0, 1, 0, 0, 0);
					java.sql.Timestamp startTime = new  Timestamp((new Date()).getTime());
					user.setStartTime(startTime);
					Date endDate = cal.getTime();

					java.sql.Timestamp endTime = new  Timestamp(endDate.getTime());
					user.setEndTime(endTime);
					try {
						user.update();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.info("Problem in calling rbtSocialUserDao.updateRBTSocialUser");
						return false;
					}
				}
			}
		}


		return true;
	}

	public boolean deactivateSNGUser(String msisdn){
		List<RBTSocialUser> rbtSocialUserList = null;
		
		try {
			rbtSocialUserList = RBTSocialUser.getRBTSocialUser(msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Problem in calling rbtSocialUserDao.getAllRBTSocialUser");
			return false;
		}
		if (rbtSocialUserList != null)
		{
			Date currentDate = new Date();
			for (RBTSocialUser user : rbtSocialUserList)
			{

				if (user.getEndTime() != null
						&& user.getEndTime().after(currentDate)
						&& user.getStartTime() != null
						&& user.getStartTime().before(currentDate))
				{
					java.sql.Timestamp endTime = new  Timestamp((new Date()).getTime());
					user.setEndTime(endTime);	
					try {
						user.update();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.info("Problem in calling rbtSocialUserDao.updateRBTSocialUser");
						return false;
					}
				}
			}
		}

		return true;
	}

	public static RBTSocialUserCacheManager getInstance() {
		return instance;
	}



}
