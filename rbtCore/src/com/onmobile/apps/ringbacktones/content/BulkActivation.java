/**
 * 
 */
package com.onmobile.apps.ringbacktones.content;

import java.sql.Timestamp;

/**
 * @author vinayasimha.patil
 *
 */
public interface BulkActivation
{
	public String fileName();
	public String bulkPromoID();
	public Timestamp inDate();
	public String status();
	public String promoID();
	public int categoryType();
}
