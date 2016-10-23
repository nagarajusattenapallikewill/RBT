/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tatagsm;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.content.RBTContentProviderFactory;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicXMLElementGenerator;

/**
 * @author vinayasimha.patil
 *
 */
public class TataGSMXMLElementGenerator extends BasicXMLElementGenerator
{
	public static Element generateCallDetailsElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber,
			Subscriber subscriber)
	{
		Element element = document.createElement(CALL_DETAILS);

		Element languagesElem = generateLanguagesElement(document, webServiceSubscriber);
		element.appendChild(languagesElem);

		Element mmRequestElem = generateMultiModelRequestElement(document, task, webServiceSubscriber);
		if (mmRequestElem != null)
			element.appendChild(mmRequestElem);

		if (mmRequestElem == null)
		{
			Element mmConetentElem = generateContentCallBackElement(document, task, webServiceSubscriber);
			if (mmConetentElem != null)
				element.appendChild(mmConetentElem);
		}

		Element[] pickOfTheDayElems = generatePickOfTheDayElements(document, task, webServiceSubscriber);
		if (pickOfTheDayElems != null)
		{
			for (Element pickOfTheDayElem : pickOfTheDayElems)
				element.appendChild(pickOfTheDayElem);
		}

		Element hotSongElem = generateHotSongElement(document, task, webServiceSubscriber.getCircleID());
		if (hotSongElem != null)
			element.appendChild(hotSongElem);

		CosDetails cos = DataUtils.getCos(task, subscriber);
		Element cosElement = BasicXMLElementGenerator.generateCosElement(document, cos);
		element.appendChild(cosElement);

		return element;
	}

	public static Element[] generatePickOfTheDayElements(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		ArrayList<Element> elementList = new ArrayList<Element>();

		String circleID = webServiceSubscriber.getCircleID();
		PickOfTheDay[] pickOfTheDays = DataUtils.getPickOfTheDays(task, circleID, 'b');
		if (pickOfTheDays != null && pickOfTheDays.length > 0)
		{
			String browsingLanguage = task.getString(param_browsingLanguage);

			for (PickOfTheDay pickOfTheDay : pickOfTheDays)
			{
				String profile = pickOfTheDay.profile();
				if (profile != null && profile.equalsIgnoreCase("EXIT_SMS"))
					continue;

				Clip songOfTheDay = rbtCacheManager.getClip(pickOfTheDay.clipID(), browsingLanguage);
				Category category = rbtCacheManager.getCategory(pickOfTheDay.categoryID(), browsingLanguage);

				if (songOfTheDay != null && category != null)
				{
					Element element = document.createElement(PICK_OF_THE_DAY + "_" + (profile != null ? profile : ALL));

					Element contentsElem = document.createElement(CONTENTS);
					element.appendChild(contentsElem);

					Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, category, songOfTheDay);

					Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(pickOfTheDay.categoryID()));

					contentsElem.appendChild(contentElem);

					elementList.add(element);
				}
			}
		}

		if (elementList.size() > 0)
			return elementList.toArray(new Element[0]);

		return null;
	}
}
