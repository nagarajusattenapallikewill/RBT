package com.onmobile.apps.ringbacktones.rbtcontents.dao;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.onmobile.apps.ringbacktones.content.database.ClipStatusImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip.ClipInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipBoundary;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfo;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.RbtSocialContentPublisher;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.thread.ClipThread;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.ImageResize;
import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.exception.OnMobileException;

public class ClipsDAO {
	private static Logger basicLogger = Logger.getLogger(ClipsDAO.class);

	private static final RBTContentJarParameters RBT_CONTENT_JAR_PARAMETERS = RBTContentJarParameters
			.getInstance();
	private static final String IMAGE_RESIZE_REQUIRED_STR = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_resize_required");
	private static final String IMAGE_BASE_DIRECTORY_PATH = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_base_directory_path");
	private static final String IMAGE_WIDTH = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_width");
	private static final String IMAGE_HEIGHT = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_height");

	private static short CLIP_TYPE = 1;
	private static short CLIP_INSERT_UPDATE = 1;
	private static short CLIP_DELETE = 2;

	private static String populatePromoId =RBT_CONTENT_JAR_PARAMETERS
			.getParameter("populate_promoid");
	
	//RBT-10215
	private static String AdRBTCircleIds =null;

	/**
	 * Saves the clip in RBT_CLIPS table. If clipEndTime() is null, then the
	 * value is set as {@link Clip#DEFAULT_CLIP_END_TIME}
	 * 
	 * @param clip
	 * @return the Clip object saved in the DB.
	 * @throws DataAccessException
	 */
	public static Clip saveClip(Clip clip) throws DataAccessException {

		// check the clip end time
		if (null == clip.getClipEndTime()) {
			clip.setClipEndTime(Clip.DEFAULT_CLIP_END_TIME);
		}
		if (null == clip.getSmsStartTime()) {
			clip.setSmsStartTime(clip.getClipStartTime());
		}

		if (populatePromoId != null
				&& populatePromoId.trim().equalsIgnoreCase("FALSE")) {
			clip.setClipPromoId(null);
		}

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(clip);
			
			//RBT-10215 Added for saving ADRBT
			if(isAdRBTClip(clip))
			{
				checkAndInsertClipWithStatusADRBT(clip.getClipRbtWavFile());
			}

			RbtSocialContentPublisher publisher = new RbtSocialContentPublisher(
					CLIP_TYPE, clip.getClipId() + "", CLIP_INSERT_UPDATE);
			session.save(publisher);

			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Saved clip " + clip + " in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
		start = System.currentTimeMillis();
		try {
			ClipThread clipThread = new ClipThread("clipThread", null);
			clipThread.processRecord(clip);
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
		} catch (Exception e) {
			// safety check
			basicLogger.error("Error updating the clip in cache " + clip);
		}

		/*
		 * Re-Size the clip image from the specified configuration.
		 */
		performImageResize(clip.getClipInfoSet());

		return clip;
	}

	/**
	 * Gets all the clips available in the RBT_CLIPS table. As the complete
	 * table is transfered, use the API carefully.
	 * 
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static List<Clip> getAllClips() throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			List<Clip> result1 = (List<Clip>) criteria.list();
			Set clipSet = new LinkedHashSet<Clip>(result1);
			List<Clip> result = new ArrayList<Clip>(clipSet);
			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the clips in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	/**
	 * Gets all the clips artist available in the RBT_CLIPS table. As the complete
	 * table is transfered, use the API carefully.
	 * @Author Ankit.Kanchan
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getAllClipsArtist() throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class).setProjection(Projections.projectionList().add(Projections.groupProperty("artist")));
			basicLogger.info("Querying for all clips' artist....");
			List<String> resultArtists = (List<String>) criteria.list();
			Set clipArtistSet = new LinkedHashSet<String>(resultArtists);
			List<String> result = new ArrayList<String>(clipArtistSet);
			tx.commit();
			basicLogger.info("Got all the clips' artists in "+ (System.currentTimeMillis() - start) + "ms");
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the clips' artists in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * Gets all active clips. the filter is clipStartTime < current time and
	 * clipEndTime > current time
	 * 
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static List<Clip> getAllActiveClips() throws DataAccessException {

		// String query =
		// "from Clip clip, CategoryClipMap categoryClipMap where clip.clipId=categoryClipMap.clipId";

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			criteria = criteria.add(Restrictions
					.lt("clipStartTime", new Date()));
			criteria = criteria.add(Restrictions.gt("clipEndTime", new Date()));
			// do i have to add the clip id in (select distinct clip id from
			// category_clip_map)
			List<Clip> result1 = (List<Clip>) criteria.list();
			Set clipSet = new LinkedHashSet<Clip>(result1);
			List<Clip> result = new ArrayList<Clip>(clipSet);
			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the active clips in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * Updates the clip with the current clipId. Remember you should get the
	 * clip using the provided API, update the required field and use this
	 * method. Ex: Doing the following will set all the other columns to default
	 * value (NULL) except expiryDate Original clip details in DB: {clipId=1001,
	 * clipName=abc clipRbtWavFile=rbt_abc_rbt
	 * clipPreviewWavFile=rbt_abc_preview clipEndTime=2037-01-01 ...} Clip clip
	 * = new Clip(); clip.setClipId(1001); clip.setClipEndTime(currentTime);
	 * ClipDAO.updateClip(clip); the resulting clip details in DB: {clipId=1001,
	 * clipName=NULL clipRbtWavFile=NULL clipPreviewWavFile=NULL
	 * clipEndTime=2009-05-27...} The correct approach would have been Clip clip
	 * = ClipDAO.getClip(clipId); clip.setClipEndTime(currentTime);
	 * ClipDAO.updateClip(clip);
	 * 
	 * @param clip
	 * @throws DataAccessException
	 */
	public static void updateClip(Clip clip) throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		if (null == clip.getSmsStartTime()) {
			clip.setSmsStartTime(clip.getClipStartTime());
		}
		if (populatePromoId != null
				&& populatePromoId.trim().equalsIgnoreCase("FALSE")) {
			clip.setClipPromoId(null);
		}
		try {
			tx = session.beginTransaction();
			session.update(clip);
			//RBT-10215 Added for updating ADRBT
			if(isAdRBTClip(clip))
			{
				checkAndInsertClipWithStatusADRBT(clip.getClipRbtWavFile());
			}
			
			RbtSocialContentPublisher publisher = new RbtSocialContentPublisher(
					CLIP_TYPE, clip.getClipId() + "", CLIP_INSERT_UPDATE);
			session.save(publisher);
			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated clip " + clip + " in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
		start = System.currentTimeMillis();
		try {
			ClipThread clipThread = new ClipThread("clipThread", null);
			clipThread.processRecord(clip);
			// RBTCache.putClipInCache(clip);
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Updated cache in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
		} catch (Exception e) {
			// safety check
			basicLogger.error("Error updating the clip in cache " + clip, e);
		}

		/*
		 * Re-Size the clip image from the specified values.
		 */
		performImageResize(clip.getClipInfoSet());

		return;
	}

	/**
	 * Gets the clip with clipId
	 * 
	 * @param clipId
	 * @return
	 * @throws DataAccessException
	 */
	public static Clip getClip(String clipId) throws DataAccessException {
		return getClip(Integer.parseInt(clipId));
	}

	/**
	 * Gets the clip with clipId
	 * 
	 * @param clipId
	 * @return
	 * @throws DataAccessException
	 */
	public static Clip getClip(String clipId, String language)
			throws DataAccessException {
		return getClip(Integer.parseInt(clipId), language);
	}

	/**
	 * Gets the clip with clipId
	 * 
	 * @param clipId
	 * @return
	 * @throws DataAccessException
	 */
	public static Clip getClip(int clipId) throws DataAccessException {
		return getClip(clipId, null);
	}

	public static Clip getClip(int clipId, String language)
			throws DataAccessException {
		// Session session = HibernateUtil.getSession();
		// Transaction tx = null;
		// try {
		// tx = session.beginTransaction();
		// Clip clip = (Clip)session.get(Clip.class, new Integer(clipId));
		// tx.commit();
		// clip = getClipForLanguage(clip, language);
		// return clip;
		// } catch(HibernateException he) {
		// if(null != tx) {
		// tx.rollback();
		// }
		// throw new DataAccessException(he);
		// } finally {
		// session.close();
		// }
		return getClip(clipId, language, false);
	}

	public static Clip getClip(int clipId, boolean isReturnClipWithClipInfo)
			throws DataAccessException {
		return getClip(clipId, null, isReturnClipWithClipInfo);
	}

	private static Clip getClip(int clipId, String language,
			boolean isReturnClipWithClipInfo) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Clip clip = (Clip) session.get(Clip.class, new Integer(clipId));
			tx.commit();
			if (!isReturnClipWithClipInfo) {
				clip = getClipForLanguage(clip, language);
			}
			return clip;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * Gets the clipWith promoId
	 * 
	 * @param promoId
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static Clip getClipByPromoId(String promoId)
			throws DataAccessException {
		return getClipByPromoId(promoId, null);
	}

	/**
	 * Gets the clipWith promoId in specific language
	 * 
	 * @param promoId
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static Clip getClipByPromoId(String promoId, String language)
			throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Clip clip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			// criteria = criteria.add(Restrictions.eq("clipPromoId", promoId));
			criteria = criteria.add(Restrictions.like("clipPromoId", promoId,
					MatchMode.ANYWHERE));
			List<Clip> result = (List<Clip>) criteria.list();
			if (null != result && result.size() > 0) {
				for (Clip tempClip : result) {
					String[] clipPromoCode = tempClip.getClipPromoId().split(
							",");
					for (int index = 0; index < clipPromoCode.length; index++) {
						if (clipPromoCode[index].equalsIgnoreCase(promoId)) {
							clip = tempClip;
							break;
						}
					}
				}
				// clip = result.get(0);
			}
			tx.commit();
			clip = getClipForLanguage(clip, language);
			return clip;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * Gets the clip with clipRbtWavFile
	 * 
	 * @param clipRbtWavFile
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static Clip getClipByRbtWavFileName(String clipRbtWavFile)
			throws DataAccessException {
		return getClipByRbtWavFileName(clipRbtWavFile, null);
	}

	/**
	 * Gets the clip with clipRbtWavFile in specific language
	 * 
	 * @param clipRbtWavFile
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static Clip getClipByRbtWavFileName(String clipRbtWavFile,
			String language) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Clip clip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			criteria = criteria.add(Restrictions.eq("clipRbtWavFile",
					clipRbtWavFile));
			List<Clip> result = (List<Clip>) criteria.list();
			if (null != result && result.size() > 0) {
				clip = result.get(0);
			}
			tx.commit();
			clip = getClipForLanguage(clip, language);
			return clip;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * Gets the clip with clipSmsAlias
	 * 
	 * @param clipSmsAlias
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static Clip getClipBySMSAlias(String clipSmsAlias)
			throws DataAccessException {
		return getClipBySMSAlias(clipSmsAlias, null);
	}

	/**
	 * In database, SmsAlias can be configured as 'meet,meeting,meets'. The
	 * record fetching happens on like restriction. Here, ilike will work for
	 * case insensitive.
	 * 
	 * @param clipSmsAlias
	 * @param language
	 * @return
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public static Clip getClipBySMSAlias(String clipSmsAlias, String language)
			throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Clip clip = null;
		StringBuffer sb = new StringBuffer("%%");
		sb.insert(1, clipSmsAlias);
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			criteria = criteria.add(Restrictions.ilike("clipSmsAlias",
					sb.toString()));
			List<Clip> result = (List<Clip>) criteria.list();
			if (null != result && result.size() > 0) {
				clip = result.get(0);
			}
			tx.commit();
			clip = getClipForLanguage(clip, language);
			return clip;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	public static List<Clip> getClipsByAlbum(String subscriberId)
			throws DataAccessException {
		return getClipsByAlbum(subscriberId, null);
	}

	public static List<Clip> getClipsByAlbum(String subscriberId,
			String language) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			criteria = criteria.add(Restrictions.eq("album", subscriberId));
			List<Clip> result1 = (List<Clip>) criteria.list();
			Set clipSet = new LinkedHashSet<Clip>(result1);
			List<Clip> result = new ArrayList<Clip>(clipSet);
			tx.commit();
			for (int i = 0; i < result.size(); i++) {
				Clip clip = result.get(i);
				clip = getClipForLanguage(clip, language);
				result.remove(i);
				result.add(i, clip);
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	public static List<Clip> getClipsInCategory(int categoryId)
			throws DataAccessException {
		return getClipsInCategory(categoryId, null);
	}

	public static List<Clip> getClipsInCategory(int categoryId, String language)
			throws DataAccessException {
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		List<Clip> result = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			Query query = session
					.createQuery("SELECT clip FROM Clip AS clip, CategoryClipMap AS clipMap WHERE clipMap.categoryId = "
							+ categoryId
							+ " AND clipMap.clipId = clip.clipId AND clip.clipStartTime < :sysdate ORDER BY clipMap.clipInList DESC, clipMap.clipIndex");
			query.setTimestamp("sysdate", new Date());

			List<Clip> result1 = query.list();

			Set clipSet = new LinkedHashSet<Clip>(result1);
			result = new ArrayList<Clip>(clipSet);

			tx.commit();
			for (int i = 0; i < result.size(); i++) {
				Clip clip = result.get(i);
				clip = getClipForLanguage(clip, language);
				result.remove(i);
				result.add(i, clip);
			}
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got clips in category " + categoryId
						+ " in " + (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	public static List<Clip> getActiveClipsInCategory(int categoryId)
			throws DataAccessException {
		return getActiveClipsInCategory(categoryId, null);
	}

	public static List<Clip> getActiveClipsInCategory(int categoryId,
			String language) throws DataAccessException {
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		List<Clip> result = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			Query query = session
					.createQuery("SELECT clip FROM Clip AS clip, CategoryClipMap AS clipMap WHERE clipMap.categoryId = "
							+ categoryId
							+ " AND clipMap.clipId = clip.clipId AND clip.clipStartTime < :sysdate AND clip.clipEndTime > :sysdate"
							+ " ORDER BY clipMap.clipInList DESC, clipMap.clipIndex");
			query.setTimestamp("sysdate", new Date());

			List<Clip> result1 = query.list();

			Set clipSet = new LinkedHashSet<Clip>(result1);
			result = new ArrayList<Clip>(clipSet);
			tx.commit();
			for (int i = 0; i < result.size(); i++) {
				Clip clip = result.get(i);
				clip = getClipForLanguage(clip, language);
				result.remove(i);
				result.add(i, clip);
			}
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got clips in category " + categoryId
						+ " in " + (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	private static Clip getClipForLanguage(Clip clip, String language) {
		if (null == clip)
			return null;
		String defaultLanguage = RBTContentJarParameters.getInstance()
				.getParameter("default_language");
		String[] supportedLanguages = null;
		if (RBTContentJarParameters.getInstance().getParameter(
				"supported_languages") != null
				&& !(RBTContentJarParameters.getInstance().getParameter(
						"supported_languages").equals(""))) {
			supportedLanguages = RBTContentJarParameters.getInstance()
					.getParameter("supported_languages").split(",");
			// System.out.println(" supported lang length "+supportedLanguages.length);
		}
		clip.setClipLanguage(defaultLanguage);
		// -------- Set a temp variable for clipInfoSet
		Set<ClipInfo> tempClipInfoSet = clip.getClipInfoSet();
		if (tempClipInfoSet == null
				&& (language == null || language.equals("")
						|| language.equalsIgnoreCase(defaultLanguage) || (supportedLanguages != null
						&& supportedLanguages.length > 0 && !RBTContentJarParameters
						.getInstance().getParameter("supported_languages")
						.contains(language)))) {
			clip.setClipInfoSet(null);
			return clip;
		}
		ArrayList<String> keysList = new ArrayList<String>();
		String origClipName = clip.getClipName();
		String origGrammar = clip.getClipGrammar();
		String origSMSAlias = clip.getClipSmsAlias();
		String origArtist = clip.getArtist();
		String origAlbum = clip.getAlbum();
		String origLanguage = clip.getLanguage();
		String origClipInfo = clip.getClipInfo();
		String origImgPath = null;
		String origKey = null;
		ClipInfoKeys[] keys = ClipInfoKeys.values();
		for (int i = 0; i < keys.length; i++) {
			keysList.add(keys[i].toString());

		}

		/*
		 * Set for all languages as well as particular language For all
		 * languages the key will be clipId_<CLIP_ID>_ALL, in this case the set
		 * will be null For particular language the key will be
		 * clipId_<CLIP_ID>_LANG, in this case the map will be null
		 */
		clip.setClipInfoSet(tempClipInfoSet);
		// ------Populate the ClipInfoMap to get all language info
		if (clip.getClipInfoSet() != null && clip.getClipInfoSet().size() > 0) {
			Iterator<ClipInfo> itr = clip.getClipInfoSet().iterator();
			while (itr.hasNext()) {
				ClipInfo clipInfo = itr.next();
				Map<String, String> clipInfoMap = clip.getClipInfoMap();
				if (clipInfoMap == null)
					clipInfoMap = new HashMap<String, String>();
				clipInfoMap.put(clipInfo.getName(), clipInfo.getValue());
				clip.setClipInfoMap(clipInfoMap);
			}
			Map<String, String> clipInfoMap = clip.getClipInfoMap();
			Map<String, String> clipMap = new HashMap<String, String>();
			if (clipInfoMap != null) {
				for (int j = 0; j < keysList.size(); j++) {
					if (clipInfoMap.containsKey(keysList.get(j))) {
						origImgPath = clipInfoMap.get(keysList.get(j));
						origKey = keysList.get(j);
						if (origKey != null && origImgPath != null) {
							basicLogger.info("adding into the map " + origKey
									+ " " + origImgPath);
							clipMap.put(origKey, origImgPath);
						}
					}
				}
			}

			/*
			 * Set for all languages ClipInfoset is not required in case of all
			 * languages
			 */
			clip.setClipInfoSet(null);
			if (language != null && language.equalsIgnoreCase("ALL")) {
				return clip;
			}
			if (language == null
					|| language.equals("")
					|| language.equalsIgnoreCase(defaultLanguage)
					|| language.equalsIgnoreCase("ALL")
					|| (supportedLanguages != null
							&& supportedLanguages.length > 0 && !RBTContentJarParameters
							.getInstance().getParameter("supported_languages")
							.contains(language))) {
				return clip;

			}
			// -------- Again set the clipInfoSet
			clip.setClipInfoSet(tempClipInfoSet);
			// --------- ClipInfoMap is not required in case of specific
			// languages
			clip.setClipInfoMap(clipMap);
			// ------- Set for supported languages
			if (supportedLanguages != null && supportedLanguages.length > 0) {
				for (int i = 0; i < supportedLanguages.length; i++) {
					Iterator<ClipInfo> clipInfoitr = clip.getClipInfoSet()
							.iterator();
					// -------- Set information on the clip bean for the passed
					// language from the clipInfoSet
					// System.out.println(" sup lang "+supportedLanguages[i]);
					clip.setClipName(null);
					clip.setClipGrammar(null);
					clip.setArtist(null);
					clip.setAlbum(null);
					clip.setClipSmsAlias(null);
					clip.setClipInfo(null);
					clip.setLanguage(null);
					clip.setClipLanguage(defaultLanguage);
					while (clipInfoitr.hasNext()) {
						ClipInfo clipInfo = clipInfoitr.next();
						// System.out.println(" clip name "+clipInfo.getName());
						// if(!clipInfo.getName().endsWith(supportedLanguages[i].trim().toUpperCase())){
						// continue;
						// }
						if (clipInfo
								.getName()
								.equalsIgnoreCase(
										RBTCacheKey
												.getClipNameLanguageKey(supportedLanguages[i]
														.toUpperCase()))) {
							clip.setClipName(clipInfo.getValue());

						}
						if (clipInfo
								.getName()
								.equalsIgnoreCase(
										RBTCacheKey
												.getClipGrammarLanguageKey(supportedLanguages[i]
														.toUpperCase()))) {
							clip.setClipGrammar(clipInfo.getValue());
						}
						if (clipInfo
								.getName()
								.equalsIgnoreCase(
										RBTCacheKey
												.getClipArtistLanguageKey(supportedLanguages[i]
														.toUpperCase()))) {
							clip.setArtist(clipInfo.getValue());
						}
						if (clipInfo
								.getName()
								.equalsIgnoreCase(
										RBTCacheKey
												.getClipAlbumLanguageKey(supportedLanguages[i]
														.toUpperCase()))) {
							clip.setAlbum(clipInfo.getValue());
						}
						if (clipInfo
								.getName()
								.equalsIgnoreCase(
										RBTCacheKey
												.getClipSMSAliasLanguageKey(supportedLanguages[i]
														.toUpperCase()))) {
							clip.setClipSmsAlias(clipInfo.getValue());
						}
						if (clipInfo
								.getName()
								.equalsIgnoreCase(
										RBTCacheKey
												.getClipInfoLanguageKey(supportedLanguages[i]
														.toUpperCase()))) {
							clip.setClipInfo(clipInfo.getValue());
						}
						if (clipInfo
								.getName()
								.equalsIgnoreCase(
										RBTCacheKey
												.getClipLanguageLanguageKey(supportedLanguages[i]
														.toUpperCase()))) {
							clip.setLanguage(clipInfo.getValue());
						}
					}

					boolean isSupportLanguageAvail = true;

					if (clip.getClipName() == null
							|| clip.getClipName().equals("")) {
						clip.setClipName(origClipName);
						isSupportLanguageAvail = false;
					} else
						isSupportLanguageAvail = true;
					if (clip.getArtist() == null || clip.getArtist().equals("")) {
						clip.setArtist(origArtist);
						isSupportLanguageAvail = isSupportLanguageAvail ? true
								: false;
					} else
						isSupportLanguageAvail = true;
					if (clip.getAlbum() == null || clip.getAlbum().equals("")) {
						clip.setAlbum(origAlbum);
						isSupportLanguageAvail = isSupportLanguageAvail ? true
								: false;
					} else
						isSupportLanguageAvail = true;
					if (clip.getLanguage() == null
							|| clip.getLanguage().equals(""))
						clip.setLanguage(origLanguage);
					if (clip.getClipInfo() == null
							|| clip.getClipInfo().equals(""))
						clip.setClipInfo(origClipInfo);
					if (clip.getClipGrammar() == null
							|| clip.getClipGrammar().equals(""))
						clip.setClipGrammar(origGrammar);
					if (clip.getClipSmsAlias() == null
							|| clip.getClipSmsAlias().equals(""))
						clip.setClipSmsAlias(origSMSAlias);

					if (language != null
							&& language.equalsIgnoreCase(supportedLanguages[i])) {
						if (isSupportLanguageAvail) {
							clip.setClipLanguage(supportedLanguages[i]);
						}
						return clip;
					}

				}
			}
		}
		return clip;
	}

	public static Clip[] saveOrUpdateClip(Clip[] clips)
			throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for (int i = 0; i < clips.length; i++) {
				Clip clip = clips[i];
				// check the clip end time
				if (null == clip.getClipEndTime()) {
					clip.setClipEndTime(Clip.DEFAULT_CLIP_END_TIME);
				}
				if (null == clip.getSmsStartTime()) {
					clip.setSmsStartTime(clip.getClipStartTime());
				}
				if (populatePromoId != null
						&& populatePromoId.trim().equalsIgnoreCase("FALSE")) {
					clip.setClipPromoId(null);
				}
				session.saveOrUpdate(clip);
				//RBT-10215 Added for saving/updating ADRBT
				if(isAdRBTClip(clip))
				{
					checkAndInsertClipWithStatusADRBT(clip.getClipRbtWavFile());
				}
				
				RbtSocialContentPublisher publisher = new RbtSocialContentPublisher(
						CLIP_TYPE, clip.getClipId() + "", CLIP_INSERT_UPDATE);
				session.save(publisher);
				if (basicLogger.isInfoEnabled()) {
					basicLogger.info("Successfully Saved or updated clip "
							+ clip.toString());
				}
				if ((i % 100) == 0) {
					session.flush();
					session.clear();
				}

				/*
				 * Re-Size the clip image from the specified values.
				 */
				performImageResize(clip.getClipInfoSet());
			}
			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Successfully Saved or updated clips  in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
		return clips;
	}

	public static List<Clip> getAllClipsA(int startIndex, int endIndex)
			throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		String hql = "from Clip";
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(hql);
			// System.out.println(query.getQueryString());
			query.setFirstResult(startIndex);
			query.setMaxResults(endIndex);

			List<Clip> result1 = (List<Clip>) query.list();
			Set clipSet = new HashSet<Clip>(result1);
			List<Clip> result = new ArrayList<Clip>(clipSet);
			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the clips in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	public static List<Clip> getClips(List<Integer> clipIds)
			throws DataAccessException {
		return getClips(clipIds, null);
	}

	public static List<Clip> getClips(List<Integer> clipIds, String language)
			throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session
					.createQuery("FROM Clip WHERE clipId IN (:clipId)");
			query.setParameterList("clipId", clipIds);
			List<Clip> result1 = (List<Clip>) query.list();
			Set clipSet = new LinkedHashSet<Clip>(result1);
			List<Clip> rsList = new ArrayList<Clip>(clipSet);
			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got all the clips in "
						+ (System.currentTimeMillis() - start));
			}
			Map<Integer, Clip> clipMap = new HashMap<Integer, Clip>();
			int size = rsList.size();
			for (int i = 0; i < size; i++) {
				Clip clip = rsList.get(i);
				clipMap.put(clip.getClipId(), clip);
			}
			size = clipIds.size();
			List<Clip> result = new ArrayList<Clip>();
			for (int i = 0; i < size; i++) {
				if (clipMap.containsKey(clipIds.get(i))) {
					result.add(i, clipMap.get(clipIds.get(i)));
				} else {
					result.add(i, null);
				}
			}
			for (int i = 0; i < result.size(); i++) {
				Clip clip = result.get(i);
				clip = getClipForLanguage(clip, language);
				result.remove(i);
				result.add(i, clip);
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * To get clip-id boundaries. A boundary is defined such that the number of
	 * clips in between those boundaries should not be greater than
	 * <code>noOfClips</code>.
	 * 
	 * @param noOfClips
	 * @return
	 * @throws DataAccessException
	 */
	public static TreeSet<ClipBoundary> getClipBoundariesUsingBinaryAlg(
			int noOfClips) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SQLQuery boundaryQuery = session
					.createSQLQuery("SELECT MIN(CLIP_ID), MAX(CLIP_ID), COUNT(*) FROM RBT_CLIPS");
			Object[] values = (Object[]) boundaryQuery.list().get(0);
			int minIndex = ((Number) values[0]).intValue();
			int maxIndex = ((Number) values[1]).intValue();
			int total = ((Number) values[2]).intValue();
			basicLogger.info("Total Clips " + total + " MinClipId = "
					+ minIndex + " MaxClipId = " + maxIndex);
			// to include max clip-id also
			maxIndex++;
			TreeSet<ClipBoundary> result = new TreeSet<ClipBoundary>(
					ClipBoundary.getClipBoundaryComparator());
			if (total > noOfClips) {
				binarySplit(result, session, minIndex, maxIndex, noOfClips);
			} else {
				result.add(new ClipBoundary(minIndex, maxIndex, total));
			}
			tx.commit();
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * Uses an algorithm it is like binary search, to split records between
	 * <code>minClipId</code> and <code>maxClipId</code>. The condition here to
	 * follow is clip range in a boundary should not be greater than
	 * <code>size</code>.
	 * 
	 * @param session
	 * @param minClipId
	 * @param maxClipId
	 * @param size
	 * @return
	 */
	private static void binarySplit(TreeSet<ClipBoundary> clipBoundarySet,
			Session session, int minClipId, int maxClipId, int size) {
		// System.out.println(minClipId + " - " + maxClipId);
		SQLQuery countquery = session
				.createSQLQuery("SELECT COUNT(*) FROM RBT_CLIPS WHERE CLIP_ID >= :startClipId AND CLIP_ID < :endClipId");
		int middle = (minClipId + maxClipId) / 2;
		countquery.setParameter("startClipId", minClipId);
		countquery.setParameter("endClipId", middle);
		int count = ((Number) countquery.list().get(0)).intValue();
		if (count <= size) {
			basicLogger.info("boundary " + minClipId + "-" + middle + " -> "
					+ count);
			clipBoundarySet.add(new ClipBoundary(minClipId, middle, count));
		} else {
			binarySplit(clipBoundarySet, session, minClipId, middle, size);
		}
		countquery.setParameter("startClipId", middle);
		countquery.setParameter("endClipId", maxClipId);
		count = ((Number) countquery.list().get(0)).intValue();
		if (count <= size) {
			basicLogger.info("boundary " + middle + "-" + maxClipId + " -> "
					+ count);
			clipBoundarySet.add(new ClipBoundary(middle, maxClipId, count));
		} else {
			binarySplit(clipBoundarySet, session, middle, maxClipId, size);
		}
	}

	/**
	 * Get clips whose clip-ids are in between <code>startClipId</code> and
	 * <code>endClipId</code>.
	 * 
	 * @param startClipId
	 * @param endClipId
	 * @return List of Clip objects.
	 * @throws DataAccessException
	 */
	public static List<Clip> getClipsInBetween(int startClipId, int endClipId)
			throws DataAccessException {
		return getClipsInBetween(startClipId, endClipId, null);
	}

	/**
	 * Get clips whose clip-ids are in between <code>startClipId</code> and
	 * <code>endClipId</code>.
	 * 
	 * @param startClipId
	 * @param endClipId
	 * @return List of Clip objects.
	 * @throws DataAccessException
	 */
	public static List<Clip> getClipsInBetween(int startClipId, int endClipId,
			String language) throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		// String hql =
		// "from Clip as clip where clip.clipId >= :startClipId and clip.clipId < :endClipId";
		// String hql =
		// "select * from RBT_CLIPS as clip LEFT OUTER JOIN RBT_CLIP_INFO as clipInfo on clip.clip_id=clipInfo.clip_id where clip.clip_id>= ? and clip.clip_id< ?";

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			criteria.add(Restrictions.between("clipId", startClipId, endClipId));
			/*
			 * Query query = session.createQuery(hql);
			 * query.setParameter("startClipId", startClipId);
			 * query.setParameter("endClipId", endClipId);
			 */
			List<Clip> result1 = (List<Clip>) criteria.list();
			Set clipSet = new LinkedHashSet<Clip>(result1);
			List<Clip> result = new ArrayList<Clip>(clipSet);
			tx.commit();
			if (language != null && !language.equals("")) {
				for (int i = 0; i < result.size(); i++) {
					// System.out.println("i " + i);
					Clip clip = result.get(i);
					clip = getClipForLanguage(clip, language);
					result.remove(i);
					result.add(i, clip);
				}
			}
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got " + result.size() + " clips between "
						+ startClipId + "-" + endClipId + " in "
						+ (System.currentTimeMillis() - start) + "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * Returns the clips matching the given HQL
	 * 
	 * @param hql
	 * @return
	 * @throws DataAccessException
	 */
	public static List<Clip> getClips(String hql) throws DataAccessException {

		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(hql);
			List<Clip> result1 = (List<Clip>) query.list();
			Set clipSet = new LinkedHashSet<Clip>(result1);
			List<Clip> result = new ArrayList<Clip>(clipSet);
			tx.commit();
			if (basicLogger.isDebugEnabled()) {
				basicLogger.debug("Got " + result.size() + " clips for " + hql
						+ " - in " + (System.currentTimeMillis() - start)
						+ "ms");
			}
			return result;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}

	/**
	 * re-size the image based on the configured values. Source and destination
	 * folders and file names are same.
	 * 
	 * @param clipInfoSet
	 */
	private static void performImageResize(Set<ClipInfo> clipInfoSet) {
		boolean imageResizeRequired = Boolean
				.valueOf(IMAGE_RESIZE_REQUIRED_STR);
		if (!imageResizeRequired || clipInfoSet == null) {
			basicLogger.warn(" imageResizeRequired: " + imageResizeRequired
					+ ", ClipInfoMap: " + clipInfoSet);
			return;
		}
		String imageName = null;
		for (ClipInfo clipinfo : clipInfoSet) {
			String name = clipinfo.getName();
			if (name.equals(Clip.ClipInfoKeys.IMG_URL.toString())) {
				imageName = clipinfo.getValue();
				break;
			}

		}
		if (imageName == null) {
			basicLogger.warn(" IMG_URL is null in clipInfo");
			return;
		}

		String slash = "/";
		int height = Integer.parseInt(IMAGE_HEIGHT);
		int width = Integer.parseInt(IMAGE_WIDTH);
		StringBuffer imagePath = new StringBuffer(IMAGE_BASE_DIRECTORY_PATH);
		/*
		 * Check for slash at the end of base directory path. If not, append it.
		 */
		if (!IMAGE_BASE_DIRECTORY_PATH.endsWith(slash)) {
			imagePath = imagePath.append(slash);
		}
		try {
			imagePath.append(imageName);
			basicLogger.debug("performing resizing image: " + imagePath);
			File file = new File(imagePath.toString());
			ImageResize.resizeImage(file, height, width);
			basicLogger.debug("Image resize is completed");
		} catch (Exception e) {
			basicLogger.error(
					"Exception when resizing image : " + e.getMessage(), e);
		}
	}

	public static Clip getClipByVcode(String vcode, String language) throws DataAccessException {
		Session session = HibernateUtil.getSession();
		Transaction tx = null;
		Clip clip = null;
		try {
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Clip.class);
			// criteria = criteria.add(Restrictions.eq("clipPromoId", promoId));
			criteria = criteria.add(Restrictions.like("clipVcode", vcode,
					MatchMode.ANYWHERE));
			List<Clip> result = (List<Clip>) criteria.list();
			if (null != result && result.size() > 0) {
				for (Clip tempClip : result) {
					String[] clipVcode = tempClip.getClipVcode().split(
							",");
					for (int index = 0; index < clipVcode.length; index++) {
						if (clipVcode[index].equalsIgnoreCase(vcode)) {
							clip = tempClip;
							break;
						}
					}
				}
				// clip = result.get(0);
			}
			tx.commit();
			clip = getClipForLanguage(clip, language);
			return clip;
		} catch (HibernateException he) {
			basicLogger.error("", he);
			if (null != tx) {
				tx.rollback();
			}
			throw new DataAccessException(he);
		} finally {
			session.close();
		}
	}
	
	//RBT-10215 for checking clip is ADRBT or not 
	private static boolean isAdRBTClip(Clip clip)
	{
		boolean isAdRBTclip=false;
		if(AdRBTCircleIds==null)
			AdRBTCircleIds = RBTContentJarParameters.getInstance().getParameter("AdRBT_circleIDs");
		
		if(AdRBTCircleIds!=null)
		{
			if(clip!=null && clip.getClipInfo()!=null && clip.getClipInfo().indexOf("ADRBT")!=-1)
			{
				isAdRBTclip=true;
				basicLogger.info("Returning isAdRBTClip :"+isAdRBTclip+" for AdRBTCircleIds: "+AdRBTCircleIds);
			}
		}
		return isAdRBTclip;
	}
	//RBT-10215 for inserting into DB
	private static boolean checkAndInsertClipWithStatusADRBT(String rbtWavFile) {
		Connection conn = getConnection();

		if (conn == null)
			return false;

		try {
			return ClipStatusImpl.checkAndInsertClipWithStatusADRBT(conn, rbtWavFile,
					AdRBTCircleIds);
			
		} catch (Throwable e) {
			basicLogger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}

		return false;

	}
	//RBT-10215 for releasing connection
	public static boolean releaseConnection(Connection conn) {
		try {
			conn.close();
			return true;
		} catch (Exception e) {
			basicLogger.error("Exception while releasing connection", e);
		}
		return false;
	}
	
	//RBT-10215 for getting connection
	public static Connection getConnection() {

		try {
			return HibernateUtil.getSession().connection();
//			return OnMobileDBServices.getDBConnection();
		} catch (Throwable e) {
			basicLogger.error("Exception while getting connection", e);
		}
		return null;
	}
	
	

}
