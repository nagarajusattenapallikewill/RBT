package com.onmobile.apps.ringbacktones.content.database;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;

public class RobiDbMgrImpl extends GrameenDbMgrImpl {
	@Override
	public String[] getChargeClassForShuffleCatgory(String subscriberId,
			Subscriber consentSubscriber, Categories categories,
			ClipMinimal clip, boolean incrSelCount, String subscriberWavFile,
			boolean isPackSel, String packCosID, String selBy,
			HashMap<String, String> extraInfo, String nextClass,
			String classType) {
		return new RBTDBManager().getChargeClassForShuffleCatgory(subscriberId, consentSubscriber,
				categories, clip, incrSelCount, subscriberWavFile, isPackSel,
				packCosID, selBy, extraInfo, nextClass, classType);
	}
	
	public String getCosChargeClass(Subscriber subscriber, Category category, Clip clip, CosDetails cos)
	  {
	    return new RBTDBManager().getCosChargeClass(subscriber, category, clip, cos);
	  }
}
