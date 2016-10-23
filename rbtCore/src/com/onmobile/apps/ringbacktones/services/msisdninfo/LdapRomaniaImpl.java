package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class LdapRomaniaImpl implements MSISDNServiceDefinition
{
	private static Logger logger = Logger.getLogger(LdapRomaniaImpl.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition#getSubscriberDetail(java.lang.String)
	 */
	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext)
	{
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());

		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = null;
		if (!Utility.isValidNumber(subscriberID))
		{
			return new SubscriberDetail(subscriberID, circleID, isPrepaid,
					isValidSubscriber, subscriberDetailsMap);
		}

		LdapContext ldapContext = null;
		try
		{
			Hashtable<String, String> env = getLdapEnvironment();
			ldapContext = new InitialLdapContext(env, null);

			String subEntryDN = getEntryDN(subscriberID);

			String[] attrIDs = {"vfProvider"};
			Parameters attrIDParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_ATTRIBUTE_LIST");
			if (attrIDParam != null)
				attrIDs[0] = attrIDParam.getValue().trim();

			Attributes attributes = ldapContext.getAttributes(subEntryDN, attrIDs);
			isValidSubscriber = true;

			Attribute attribute = attributes.get(attrIDs[0]);
			if (attribute != null)
			{
				String vfProvider = (String) attribute.get();
				logger.info("RBT:: vfProvider = " + vfProvider);
				vfProvider = vfProvider.trim();
				isPrepaid = !(vfProvider.equalsIgnoreCase("ABP")); // If vfProvider = ABP mean POSTPAID
			}
			else
			{
				logger.info("RBT:: Attribute vfProvider does not exist");
				isPrepaid = true;
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
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
		}

		if(!isValidSubscriber)
			return new SubscriberDetail(subscriberID,
					circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		
		SitePrefix sitePrefix = Utility.getPrefix(subscriberID);
		if (sitePrefix != null)
			circleID = sitePrefix.getCircleID();
		else
			logger.info("Could not get circle for " + subscriberID);

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}

	private Hashtable<String, String> getLdapEnvironment()
	{

		Parameters providerURLParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_PROVIDER_URL");
		String providerURL = providerURLParam.getValue();
		Parameters securiryPrincipalParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_PRINCIPAL");
		String securiryPrincipal = securiryPrincipalParam.getValue();
		Parameters securiryCredentialsParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_CREDENTIALS");
		String securiryCredentials = securiryCredentialsParam.getValue();

		Hashtable<String, String> ldapEnvironment = new Hashtable<String, String>();
		ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		ldapEnvironment.put(Context.PROVIDER_URL, providerURL);

		ldapEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");
		ldapEnvironment.put(Context.SECURITY_PRINCIPAL, securiryPrincipal);
		ldapEnvironment.put(Context.SECURITY_CREDENTIALS, securiryCredentials);

		ldapEnvironment.put("com.sun.jndi.ldap.connect.pool", "true");

		logger.info("RBT:: ldapEnvironment = " + ldapEnvironment);
		return ldapEnvironment;
	}

	private String getEntryDN(String subscriberID)
	{
		String entryDN = "vfTelephoneNumber=%SUBSCRIBER_ID%,ou=GSM,ou=subscriber,ou=vodafonero,c=ro,o=vodafone";
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
}
