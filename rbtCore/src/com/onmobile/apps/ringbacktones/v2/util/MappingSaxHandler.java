package com.onmobile.apps.ringbacktones.v2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.PropertyConfig;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping;
import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping.WavFileCompositeKey;
import com.onmobile.apps.ringbacktones.v2.dao.constants.DistributorConstants;

public class MappingSaxHandler extends DefaultHandler {

	private List<WavFileMapping> waveFileMapingLst = null;
	private WavFileMapping waveFileMapingObject = null;
	private static PropertyConfig config = (PropertyConfig) ConfigUtil
			.getBean(BeanConstant.PROPERTY_CONFIG);
	private static String operatorNamesToBeParsed = null;
	private static String[] operators = null;
	private String rbtClipId = null;
	private static Logger logger = Logger.getLogger(MappingSaxHandler.class);
	private final static String RBT_CLIP_FILENAME = "RBTCLIPFILENAME";
	private String attributeValue = "";
	private HashMap<String, String> attributeValues = new HashMap<String, String>();
	static {
		operatorNamesToBeParsed = config
				.getValueFromResourceBundle(DistributorConstants.RSS_WAV_FILE_MAPPING_OPERATOR_NAMES);
		if (null != operatorNamesToBeParsed
				&& !operatorNamesToBeParsed.isEmpty()) {
			operators = operatorNamesToBeParsed.toUpperCase().split(",");
			logger.info("configured operators are : " + operators);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		attributeValue = "";
		if (qName.equalsIgnoreCase("ONMOBILEINDIARBTCLIPID")) {
			logger.info("attributeValue for ONMOBILEINDIARBTCLIPID in startElement method: "
					+ attributeValue);
		}
		String operatorWavFileAttributeName = null;
		for (String operatorName : operators) {
			operatorWavFileAttributeName = operatorName + RBT_CLIP_FILENAME;
			if (qName.equalsIgnoreCase(operatorWavFileAttributeName)) {
				logger.info("attributeValue for "
						+ operatorWavFileAttributeName
						+ " in startElement method: " + attributeValue);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equalsIgnoreCase("ONMOBILEINDIARBTCLIPID")) {
			logger.info("attributeValue in endElement method: "
					+ attributeValue);
			rbtClipId = attributeValue;
			logger.info("rbtClipId : " + rbtClipId);
		}
		String operatorWavFileAttributeName = null;
		for (String operatorName : operators) {
			operatorWavFileAttributeName = operatorName + RBT_CLIP_FILENAME;
			if (qName.equalsIgnoreCase(operatorWavFileAttributeName)) {
				logger.info("attributeValue for "
						+ operatorWavFileAttributeName
						+ " in endElement method: " + attributeValue);
				attributeValues.put(operatorName, attributeValue);
			}
		}
		if (qName.equalsIgnoreCase("row")) {
			logger.info("attributeValues in endElement method: "
					+ attributeValues);
			Clip clip = RBTCacheManager.getInstance().getClip(rbtClipId);
			if (clip != null) {
				for (String operatorName : operators) {
					String mappedWaveFile = attributeValues.get(operatorName
							.toUpperCase());
					if (null != mappedWaveFile && !mappedWaveFile.isEmpty()) {
						waveFileMapingObject = new WavFileMapping();
						WavFileCompositeKey wavfileCompositeKeyObj = new WavFileCompositeKey();
						waveFileMapingObject.setWavFileVerOne(mappedWaveFile);
						wavfileCompositeKeyObj.setWavFileVerTwo(clip.getClipRbtWavFile());
						wavfileCompositeKeyObj.setOperatorName(operatorName);
						logger.info("operatorName : " + operatorName
								+ " ,wavfileRBT1.0" + clip.getClipRbtWavFile()
								+ " ,wavfileRBT2.0 " + mappedWaveFile);
						waveFileMapingObject
								.setWavFileCompositeKey(wavfileCompositeKeyObj);
						if (waveFileMapingLst == null)
							waveFileMapingLst = new ArrayList<WavFileMapping>();
						waveFileMapingLst.add(waveFileMapingObject);
						logger.info("waveFileMapingLst in endElement method: "
								+ waveFileMapingLst.toString());
					}
				}
			}
		}
	}

	public List<WavFileMapping> getWaveFileMapingLst() {
		return waveFileMapingLst;
	}

	public void setWaveFileMapingLst(List<WavFileMapping> waveFileMapingLst) {
		this.waveFileMapingLst = waveFileMapingLst;
	}

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		attributeValue = new String(ch, start, length);
	}
}
