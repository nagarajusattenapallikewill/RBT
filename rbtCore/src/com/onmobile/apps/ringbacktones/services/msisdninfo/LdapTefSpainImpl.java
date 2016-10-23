package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.sun.jndi.ldap.LdapPoolManager;

/**
 * @author sridhar.sindiri
 *
 */
public class LdapTefSpainImpl  implements MSISDNServiceDefinition
{
	private static Logger logger = Logger.getLogger(LdapTefSpainImpl.class);
	private static List<String> configmodes = null;
	static {
		Parameters modes = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "MODES_TO_VALIDATE_LDAP_LOPD");
		if(modes != null) {
			configmodes=ListUtils.convertToList(modes.getValue(),",");
		}
	}
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition#getSubscriberDetail(com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext)
	 */
	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext) 
	{
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());
        
		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = new HashMap<String, String>();
		if (!Utility.isValidNumber(subscriberID))
		{
			return new SubscriberDetail(subscriberID, circleID, isPrepaid,
					isValidSubscriber, subscriberDetailsMap);
		}
		
		LdapContext ldapContext = null;
		NamingEnumeration<SearchResult> namingEnumeration = null;
		try
		{
			Hashtable<String, String> env = getLdapEnvironment();

			logConnectionPoolStatus();

			ldapContext = new InitialLdapContext(env, null);
			ldapContext.addToEnvironment(Context.REFERRAL, "follow");

			String subEntryDN = getEntryDN(subscriberID);

			String[] attrIDs = {"SEGMENT-ORG", "FECHANAC", "OPERATORID","LOPD"};
			Parameters attrIDParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_ATTRIBUTE_LIST");
			if (attrIDParam != null && attrIDParam.getValue() != null)
				attrIDs = attrIDParam.getValue().trim().split(",");

			String filter = subEntryDN.substring(0, subEntryDN.indexOf(','));
			String name = subEntryDN.substring(subEntryDN.indexOf(',') + 1);
            String mode =mnpContext.getMode();
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setReturningAttributes(attrIDs);
			namingEnumeration = ldapContext.search(name, filter, searchControls);
			logger.debug("LDAP response: " + namingEnumeration);
			if (namingEnumeration.hasMoreElements())
			{
				SearchResult searchResult = namingEnumeration.nextElement();
				logger.debug("searchResult: " + searchResult);
				Attributes attributes = searchResult.getAttributes();

				Attribute segmentAttribute = attributes.get(attrIDs[0]);
				if (segmentAttribute != null)
				{
					String segmentOrg = (String) segmentAttribute.get();
					segmentOrg = segmentOrg.trim();
					subscriberDetailsMap.put("SEGMENT-ORG", segmentOrg);
				}

				Attribute dateOfBirthAttribute = attributes.get(attrIDs[1]);
				if (dateOfBirthAttribute != null)
				{
					try
					{
						String dateOfBirth = (String) dateOfBirthAttribute.get();
						dateOfBirth = dateOfBirth.trim();
						DateFormat formatter = new SimpleDateFormat("YYYYMMDD");
						Date birthDate = formatter.parse(dateOfBirth);
						long age = (System.currentTimeMillis() - birthDate.getTime())/(365 * 24 * 60 * 60 * 1000);
						subscriberDetailsMap.put("AGE", String.valueOf(age));
					}
					catch (Exception e)
					{
					}
				}

				Attribute operatorIDAttribute = attributes.get(attrIDs[2]);
				if (operatorIDAttribute != null)
				{
					String operatorID = (String) operatorIDAttribute.get();
					operatorID = operatorID.trim();

					if (operatorID.equalsIgnoreCase("001"))
						isValidSubscriber = true;
				}
				
				
				
				Attribute lopdAttribute = attributes.get(attrIDs[3]);
				boolean isModeViral= (configmodes == null ? false : configmodes.contains(mode));
				logger.debug("subscriberID:" + subscriberID + ", Attribute " + attrIDs[3] + ", mode:"+mode+", isValidSubscriber:" + isValidSubscriber + ", isModeViral:" + isModeViral +", lopdAttribute" + lopdAttribute);
				if (isValidSubscriber && isModeViral && lopdAttribute != null) {

					String lopd = (String) lopdAttribute.get();
					lopd = lopd.trim();
					logger.debug("subscriberID:" + subscriberID + ", Attribute " + attrIDs[3] + ", mode:"+mode+", isValidSubscriber:" + isValidSubscriber + ", isModeViral:" + isModeViral +", lopdAttribute" + lopdAttribute + ", lopd:" + lopd);
					if (!lopd.equalsIgnoreCase("000000000000"))
						isValidSubscriber = false;
				}
				logger.debug("subscriberID:" + subscriberID + ", Attribute " + attrIDs[3] + ", mode:"+mode+", isValidSubscriber:" + isValidSubscriber + ", isModeViral:" + isModeViral +", lopdAttribute" + lopdAttribute);
			}

		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			if (namingEnumeration != null)
			{
				try
				{
					namingEnumeration.close();
				}
				catch (NamingException e)
				{
					logger.warn(e.getMessage(), e);
				}
			}

			if (ldapContext != null)
			{
				try
				{
					ldapContext.close();
				}
				catch (NamingException e)
				{
					logger.warn(e.getMessage(), e);
				}
			}

			logConnectionPoolStatus();
		}
		
		Parameters userTypeParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "DEFAULT_USER_TYPE", "POSTPAID");
		if (userTypeParam != null)
			isPrepaid = userTypeParam.getValue().trim().equalsIgnoreCase("PREPAID");

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID, circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		if(isValidSubscriber)
		{
			SitePrefix sitePrefix = Utility.getPrefix(subscriberID);
			if (sitePrefix != null)
				subscriberDetail.setCircleID(sitePrefix.getCircleID());
		}

		logger.info("RBT:: subscriberDetail: " + subscriberDetail);
		return subscriberDetail;
	}
	
	/**
	 * @return
	 */
	private Hashtable<String, String> getLdapEnvironment()
	{

		Parameters providerURLParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_PROVIDER_URL");
		String providerURL = providerURLParam.getValue();
		Parameters securiryPrincipalParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_PRINCIPAL", "cn=yavoy");
		String securiryPrincipal = securiryPrincipalParam.getValue();
		Parameters securiryCredentialsParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_CREDENTIALS", "yavoy");
		String securiryCredentials = securiryCredentialsParam.getValue();

		Hashtable<String, String> ldapEnvironment = new Hashtable<String, String>();
		ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		ldapEnvironment.put(Context.PROVIDER_URL, providerURL);
		ldapEnvironment.put(Context.REFERRAL, "follow");

		ldapEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");
		ldapEnvironment.put(Context.SECURITY_PRINCIPAL, securiryPrincipal);
		ldapEnvironment.put(Context.SECURITY_CREDENTIALS, securiryCredentials);

		ldapEnvironment.put("com.sun.jndi.ldap.connect.pool", "true");

		logger.info("RBT:: ldapEnvironment = " + ldapEnvironment);
		return ldapEnvironment;
	}

	/**
	 * @param subscriberID
	 * @return
	 */
	private String getEntryDN(String subscriberID)
	{
		String entryDN = "MSISDN=%SUBSCRIBER_ID%,o=siux";
		Parameters entryDNParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_ENTRY_DN");
		if (entryDNParam != null)
			entryDN = entryDNParam.getValue().trim();

		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "COUNTRY_PREFIX");
		if (param != null)
		{
			String[] countryPrefixes = param.getValue().split(",");
			
			subscriberID = Utility.trimCountryPrefix(subscriberID);
			subscriberID = countryPrefixes[0] + subscriberID;
		}

		String subEntryDN = entryDN.replaceAll("%SUBSCRIBER_ID%", subscriberID);

		logger.info("RBT:: subEntryDN = " + subEntryDN);
		return subEntryDN;
	}
	
	private void logConnectionPoolStatus()
	{
		if (!logger.isDebugEnabled())
			return;
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(outputStream);
		LdapPoolManager.showStats(printStream);
		logger.debug(outputStream.toString());
	}
}
