package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.provisioning.bean.RBTRto;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.PropertyConfig;

public class RBTRtoExecutor extends Thread {

	private static Logger logger = Logger.getLogger(RBTRtoExecutor.class);
	PropertyConfig config = (PropertyConfig) ConfigUtil
			.getBean(BeanConstant.PROPERTY_CONFIG);
	private static boolean runDaemon = true;
	private static ThreadPoolExecutor executor;
	static int threadPoolCount = 5;
	static int threadSleepTime = 5;
	int rtoCount = 0;
	private int hitCount = 0;
	private int reqTime = 0;
	ArrayList<Future<String>> futureList;
	private RBTDaemonManager m_mainDaemonThread;

	public RBTRtoExecutor(RBTDaemonManager mainDaemonThread) {
		this.m_mainDaemonThread = mainDaemonThread;
		init();
	}

	public void init() {
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		logger.info("Inside RTO Executor==>");
		threadPoolCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"RTO_REQUEST_EXECUTOR_POOL_SIZE", 5);
		threadSleepTime = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"RTO_REQUEST_THREAD_SLEEP_TIME", 5);
		hitCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"RTO_REQUEST_HIT_COUNT", 15);
		executor = new ThreadPoolExecutor(threadPoolCount, threadPoolCount, 5,
				TimeUnit.MINUTES, queue);
		reqTime = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
				"RTO_REQUEST_HIT_TIME_INTERVAL", 5);
	}

	public RBTRtoExecutor() {
		init();
	}

	public static void main(String args[]) {
		RBTRtoExecutor rtoDaemon = new RBTRtoExecutor();
		rtoDaemon.start();
	}

	public void run() {
		try {
			while (runDaemon) {
				try {
					getRtoData();
					if (rtoCount == 0)
						sleepNow();
					else {
						processRtoData();
						sleepNow();
					}
				} catch (Exception e) {
					sleepNow();
				}
			}
		} catch (Exception e) {
			logger.info("Exception thrown in while processing" + e);
			stopThread();
		}
	}

	/**
	 * stops the daemon thread
	 */
	public void stopThread() {
		runDaemon = false;
	}

	/**
	 * starts the daemon thread
	 */
	public void startThread() {
		runDaemon = true;
	}

	private void processRtoData() {
		for (int i = 0; i < futureList.size(); i++) {
			try {
				futureList.get(i).get();
			} catch (Exception e) {
			}
		}
	}

	private void sleepNow() {
		try {
			Thread.sleep(threadSleepTime * 1000);
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	private void getRtoData() {
		Transaction tx = null;
		try {
			Session session = null;
			session = HibernateUtil.getSession();
			Query query = null;
			query = session
					.createQuery("FROM RBTRto where retry_time <= :currentDate");
			query.setParameter("currentDate", new Date());
			List<RBTRto> rtoList = query.setMaxResults(hitCount).list();
			logger.info("Query string for rto object is: "
					+ query.getQueryString());
			if (rtoList != null && !rtoList.isEmpty()) {
				rtoCount = rtoList.size();
				futureList = new ArrayList<Future<String>>();
				for (int i = 0; i < rtoList.size(); i++) {
					RBTRto rtoObj = new RBTRto();
					rtoObj = rtoList.get(i);
					Calendar cal = Calendar.getInstance();
					cal.setTime(rtoObj.getRetryTime());
					cal.add(Calendar.MINUTE, reqTime);
					rtoObj.setRetryTime(cal.getTime());
					tx = session.beginTransaction();
					session.saveOrUpdate(rtoObj);
					tx.commit();
					session.flush();
					logger.info("retry time is updated with "
							+ rtoObj.getRetryTime() + " for subscriberID "
							+ rtoObj.getSubscriberId());
					RBTRtoWorkerThread workerthread = new RBTRtoWorkerThread(
							rtoObj);
					futureList.add(executor.submit(workerthread));
				}
			} else {
				logger.info("returning from executor thread since the rtolist is empty");
			}
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
		}
	}
}