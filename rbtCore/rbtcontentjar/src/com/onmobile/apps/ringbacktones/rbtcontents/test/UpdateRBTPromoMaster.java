package com.onmobile.apps.ringbacktones.rbtcontents.test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.onmobile.apps.ringbacktones.rbtcontents.utils.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;


public class UpdateRBTPromoMaster {

	public static void updateClipId(Connection connection) throws SQLException,Exception{		
		PreparedStatement updatePromoMasterPstmt = null;
		Statement stmt = null;
		ResultSet selectPromoIDRs = null;
		long start = System.currentTimeMillis();
		try{
			updatePromoMasterPstmt = connection.prepareStatement("UPDATE RBT_PROMO_MASTER SET CLIP_ID = ? WHERE CLIP_PROMO_ID = ?");
			stmt = connection.createStatement();
			selectPromoIDRs = stmt.executeQuery("SELECT CLIP_ID,CLIP_PROMO_ID FROM RBT_CLIPS");
			int j = 0;
			while(selectPromoIDRs.next()){
				int clip_id = selectPromoIDRs.getInt("CLIP_ID");
				String clip_promo_id = selectPromoIDRs.getString("CLIP_PROMO_ID");
				updatePromoMasterPstmt.setInt(1, clip_id);
				updatePromoMasterPstmt.setString(2, clip_promo_id);
				if(updatePromoMasterPstmt.executeUpdate()>0){
					j++;
				}				
			}
			System.out.println("Successfully updated " + j);
		}
		catch(Exception e){
			throw e;
		}
		finally{
			if(null != selectPromoIDRs){
				selectPromoIDRs.close();
			}
			if(null != stmt){
				stmt.close();
			}
			if(null != updatePromoMasterPstmt){
				updatePromoMasterPstmt.close();
			}
		}
		System.out.println("Time Taken " + (System.currentTimeMillis() - start) + "ms");
	}
	
	public static void main(String args[]) throws Exception{
		
		Session session = null;
		Connection connection = null;
		Transaction transaction = null;
		try{
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			SQLQuery query = null;
			try{
				query = session.createSQLQuery("ALTER TABLE RBT_PROMO_MASTER ADD CLIP_ID INT(11)");
				query.executeUpdate();
			}
			catch(Exception e){
				query = session.createSQLQuery("ALTER TABLE RBT_PROMO_MASTER ADD CLIP_ID NUMBER(10)");
				query.executeUpdate();				
			}
			System.out.println("CLIP_ID column has been added in RBT_PROMO_MASTER table");
			try{
				query = session.createSQLQuery("ALTER TABLE RBT_PROMO_MASTER ADD CONSTRAINT FOREIGN KEY(CLIP_ID) REFERENCES RBT_CLIPS(CLIP_ID)");
				query.executeUpdate();
				System.out.println("Successfully added the Foreign key to CLIP_ID");
			}
			catch(Exception e){
				try{
					query = session.createSQLQuery("ALTER TABLE RBT_PROMO_MASTER ADD FOREIGN KEY(CLIP_ID) REFERENCES RBT_CLIPS(CLIP_ID)");
					query.executeUpdate();
					System.out.println("Successfully added the Foreign key to CLIP_ID");
				}
				catch(Exception e1){}
			}
			query = session.createSQLQuery("CREATE INDEX INX_CLIP_PROMO_ID_PROMO_MASTER ON RBT_PROMO_MASTER(CLIP_PROMO_ID)");
			query.executeUpdate();
			System.out.println("Successfully created the index to CLIP_PROMO_ID");
			connection = session.connection();
			updateClipId(connection);
			System.out.println("Successfully updated the clip_id");
//			query = session.createSQLQuery("ALTER TABLE RBT_PROMO_MASTER DROP COLUMN CLIP_PROMO_ID");
//			query.executeUpdate();
//			System.out.println("Successfully removed the CLIP_PROMO_ID from RBT_PROMO_MASTER");
			transaction.commit();
			System.out.println("Done");
		}
		catch(HibernateException he) {
			if(null != transaction) {
				transaction.rollback();
			}
			throw new DataAccessException(he);
		}
		finally{
			if(connection != null){
				connection.close();
			}
			session.close();
		}		
	}
}
