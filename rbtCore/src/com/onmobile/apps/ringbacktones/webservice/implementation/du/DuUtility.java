/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.du;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 *
 */
public class DuUtility implements WebServiceConstants
{
	private static Logger logger = Logger.getLogger(DuUtility.class);

	public static String getUserLanguage(WebServiceContext task)
	{
		String language = null;

		LdapContext ldapContext = null;
		NamingEnumeration<SearchResult> namingEnumeration = null;
		try
		{
			Hashtable<String, String> env = getLdapEnvironment();
			if (env == null)
				return null;

			ldapContext = new InitialLdapContext(env, null);

			String subscriberID = task.getString(param_subscriberID);
			String subEntryDN = getEntryDN(subscriberID);

			Parameters attrIDParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_ATTRIBUTE_LIST", "lineUserServiceLanguage");
			String[] attrIDs = {attrIDParam.getValue().trim()};

			String filter = subEntryDN.substring(0, subEntryDN.indexOf(','));
			String name = subEntryDN.substring(subEntryDN.indexOf(',') + 1);

			namingEnumeration = ldapContext.search(name, filter, null);
			if (namingEnumeration.hasMoreElements())
			{
				SearchResult searchResult = namingEnumeration.nextElement();
				Attributes attributes = searchResult.getAttributes();

				String ldapLanguage = null;
				Attribute attribute = attributes.get(attrIDs[0]);
				if (attribute != null)
					ldapLanguage = (String) attribute.get();

				logger.info("RBT:: ldapLanguage = " + ldapLanguage);
				language = getRBTLanguage(ldapLanguage);
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
		}

		logger.info("RBT:: language = " + language);
		return language;
	}

	public static Hashtable<String, String> getLdapEnvironment()
	{
		Parameters providerURLParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_PROVIDER_URL", null);
		if (providerURLParam == null)
			return null;

		String providerURL = providerURLParam.getValue();

		Parameters securiryPrincipalParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_PRINCIPAL", null);
		String securiryPrincipal = securiryPrincipalParam.getValue();

		Parameters securiryCredentialsParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_SECURITY_CREDENTIALS", null);
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

	public static String getEntryDN(String subscriberID)
	{
		Parameters entryDNParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_ENTRY_DN", "serviceNumber=%SUBSCRIBER_ID%,ou=Contracts,ou=customers,dc=du,dc=ae");
		String entryDN = entryDNParam.getValue().trim();

		Parameters countryPrefixParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
		String[] countryPrefixes = countryPrefixParam.getValue().trim().split(",");

		if (countryPrefixes != null && countryPrefixes.length > 0)
		{
			boolean countryCodePrefixed = false;
			for (String countryPrefix : countryPrefixes)
			{
				if (subscriberID.startsWith(countryPrefix))
				{
					countryCodePrefixed = true;
					break;
				}
			}
			if (!countryCodePrefixed)
				subscriberID = countryPrefixes[0] + subscriberID;
		}
		String subEntryDN = entryDN.replaceAll("%SUBSCRIBER_ID%", subscriberID);

		return subEntryDN;
	}

	public static String getRBTLanguage(String ldapLanguage)
	{
		HashMap<String, String> ldapRBTLangMap = new HashMap<String, String>();
		Parameters ldapRBTLangParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "LDAP_RBT_LANG_MAP", null);
		if (ldapRBTLangParam != null)
		{
			String[] ldapRBTLangs = ldapRBTLangParam.getValue().split(",");
			for (String langMap : ldapRBTLangs)
			{
				String[] tokens = langMap.split("=");
				ldapRBTLangMap.put(tokens[0], tokens[1]);
			}
		}

		logger.info("RBT:: ldapRBTLangMap = " + ldapRBTLangMap);
		return ldapRBTLangMap.get(ldapLanguage);
	}
}
