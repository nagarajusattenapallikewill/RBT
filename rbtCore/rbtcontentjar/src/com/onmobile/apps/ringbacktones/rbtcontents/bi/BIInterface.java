package com.onmobile.apps.ringbacktones.rbtcontents.bi;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfoAction;

public interface BIInterface {

	public Object[] process(Category category, String subscriberId, String circleId, boolean doReturnActiveClips, String language, String appName, boolean isFromCategory , ClipInfoAction clipInfoAction);
	
	public boolean processHitBIForPurchase(String subscriberId, String refId, String mode, String toneId );
	
	public String BI_URL_GENERE = "genere";
	public String BI_URL_CATEGORY = "category";
	public String BI_URL_CLIP = "clip";
	public String BI_URL_CLIP_CAT_ID = "clipCatId";
}
