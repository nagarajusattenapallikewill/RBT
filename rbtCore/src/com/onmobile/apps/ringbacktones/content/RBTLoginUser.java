/**
 * 
 */
package com.onmobile.apps.ringbacktones.content;

import java.util.Date;
import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public interface RBTLoginUser
{
	public String userID();
	public String password();
	public String subscriberID();
	public String type();
	public HashMap<String, String> userInfo();
	public Date creationTime();
	public Date updateTime();
}
