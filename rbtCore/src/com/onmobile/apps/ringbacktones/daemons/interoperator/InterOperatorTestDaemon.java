package com.onmobile.apps.ringbacktones.daemons.interoperator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.dao.InterOperatorCopyRequestDao;
import com.onmobile.apps.ringbacktones.daemons.interoperator.threads.InterOperatorDBFetcher;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorHibernateUtils;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;

public class InterOperatorTestDaemon
{
	static private Logger logger = Logger.getLogger(InterOperatorTestDaemon.class);
	private static ArrayList<Integer> operatorList = new ArrayList<Integer>();
	
	
	public static void main(String[] args)
	{
		PropertyConfigurator.configure("D:/Coding/RBT/Apache-Tomcat-5.5/shared/classes/interoprlog4j.properties");
		System.out.println("hey!!");
		Calendar cal = Calendar.getInstance();
		for(int i = 0; i < 10000; i++)
		{
			InterOperatorCopyRequestBean copyBean = new InterOperatorCopyRequestBean();
			copyBean.setStatus(4);
			copyBean.setCopierMdn(9000000000L+i);
			copyBean.setCopierOperatorId(2);
			copyBean.setTargetContentId(i+"");
			copyBean.setRequestTime(cal.getTime());
			copyBean.setCopieeMdn(9900000000L+i);
			copyBean.setCopieeOperatorId(3);
			copyBean.setSourceContentId(i+"");
			if(i%3 == 0)
			{
				copyBean.setCopyType("COPY");
				copyBean.setKeyPressed("s1"+i);
				copyBean.setSourceSongName("Sound of Music "+i);
			}
			else if (i%3 ==1)
			{
				copyBean.setCopyType("COPYSTAR");
				copyBean.setKeyPressed("s9"+i);
				copyBean.setSourceMode("PRESS");
			}
			else
			{
				copyBean.setKeyPressed("s"+i);
				copyBean.setSourcePromoCode("pc"+i);
			}
				
			copyBean.setSourceContentDetails(i+":rbt_"+i+"_rbt");
			InterOperatorCopyRequestDao.save(copyBean);
		}
		
		
		//List<InterOperatorCopyRequestBean> copyRequests = InterOperatorCopyRequestDao.list();
		//System.out.println(copyRequests);
		//System.out.println(copyRequests.size());
	}
	
	public void insertInDB()
	{
		Session session = InterOperatorHibernateUtils.getSessionFactory().openSession();
		Transaction transaction = null;
		Long copyId = null;
		try
		{
			transaction = session.beginTransaction();
			InterOperatorCopyRequestBean copyRequest = new InterOperatorCopyRequestBean();
			copyRequest.setCopierMdn(9898989898L);
			copyRequest.setCopieeMdn(9797979797L);
			copyRequest.setCopieeOperatorId(3);
			copyRequest.setSourceContentId("334433");
			copyRequest.setRequestTime(Calendar.getInstance().getTime());
			copyId = (Long) session.save(copyRequest);
			transaction.commit();
		}
		catch (HibernateException e)
		{
			transaction.rollback();
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}

	}

}
