package com.onmobile.apps.ringbacktones.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.onmobile.gprs.base.DBConnection;

/**
 * @author laxmankumar
 *  
 */
public class RBTUser
{

    public static Logger basicLogger = Logger
            .getLogger(RBTUser.class.getName());

    public static final String Q_INSERT_USER = "INSERT INTO RBT_WEB_USERS(SUBSCRIBER_ID, PASSWORD, USER_TYPE, "
            + "CORPORATE_USER, LANGUAGE, LAST_WAP_ACCESS_TIME, LAST_WEB_ACCESS_TIME, EMAILID, FIRST_NAME, LAST_NAME,"
            + " DATE_OF_BIRTH, WORK_PHONE_NO, HOME_PHONE_NO) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String Q_SELECT_USER = "SELECT SUBSCRIBER_ID, PASSWORD, USER_TYPE, CORPORATE_USER,"
            + " LANGUAGE, LAST_WAP_ACCESS_TIME, LAST_WEB_ACCESS_TIME, EMAILID, FIRST_NAME, LAST_NAME,"
            + " DATE_OF_BIRTH, WORK_PHONE_NO, HOME_PHONE_NO FROM RBT_WEB_USERS WHERE SUBSCRIBER_ID=? AND PASSWORD=? ";

    public static final String Q_IS_USER_EXISTS = "SELECT PASSWORD FROM RBT_WEB_USERS WHERE SUBSCRIBER_ID=? ";

    public static final String Q_UPDATE_USER = "UPDATE RBT_WEB_USERS SET SUBSCRIBER_ID=?, PASSWORD=?, USER_TYPE=?, "
            + "CORPORATE_USER=?, LANGUAGE=?, LAST_WAP_ACCESS_TIME=?, LAST_WEB_ACCESS_TIME=?, EMAILID=?, FIRST_NAME=?,"
            + "LAST_NAME=?, DATE_OF_BIRTH=?, WORK_PHONE_NO=?, HOME_PHONE_NO=? ";

    public static final String Q_IS_AUTH_USER = "SELECT SUBSCRIBER_ID FROM RBT_WEB_USERS WHERE "
            + "SUBSCRIBER_ID=? AND PASSWORD=?";

    public static final String Q_UPDATE_PWD = "UPDATE RBT_WEB_USERS SET PASSWORD=? WHERE SUBSCRIBER_ID=?";

    private String subscriberId = null;
    private String password = null;
    private String userType = null;
    private String corporateUser = null;
    private String language = null;
    private Timestamp lastWapAccessedTime = null;
    private Timestamp lastWebAccessedTime = null;
    private String emailId = null;
    private String firstName = null;
    private String lastName = null;
    private Timestamp dateOfBirth = null;
    private String workPhoneNo = null;
    private String homePhoneNo = null;

    /**
     * @return Returns the corporateUser.
     */
    public String getCorporateUser()
    {
        return corporateUser;
    }

    /**
     * @param corporateUser The corporateUser to set.
     */
    public void setCorporateUser(String corporateUser)
    {
        this.corporateUser = corporateUser;
    }

    /**
     * @return Returns the dateOfBirth.
     */
    public Timestamp getDateOfBirth()
    {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth The dateOfBirth to set.
     */
    public void setDateOfBirth(Timestamp dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return Returns the emailId.
     */
    public String getEmailId()
    {
        return emailId;
    }

    /**
     * @param emailId The emailId to set.
     */
    public void setEmailId(String emailId)
    {
        this.emailId = emailId;
    }

    /**
     * @return Returns the firstName.
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * @param firstName The firstName to set.
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * @return Returns the homePhoneNo.
     */
    public String getHomePhoneNo()
    {
        return homePhoneNo;
    }

    /**
     * @param homePhoneNo The homePhoneNo to set.
     */
    public void setHomePhoneNo(String homePhoneNo)
    {
        this.homePhoneNo = homePhoneNo;
    }

    /**
     * @return Returns the language.
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * @param language The language to set.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * @return Returns the lastName.
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * @param lastName The lastName to set.
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * @return Returns the lastWapAccessedTime.
     */
    public Timestamp getLastWapAccessedTime()
    {
        return lastWapAccessedTime;
    }

    /**
     * @param lastWapAccessedTime The lastWapAccessedTime to set.
     */
    public void setLastWapAccessedTime(Timestamp lastWapAccessedTime)
    {
        this.lastWapAccessedTime = lastWapAccessedTime;
    }

    /**
     * @return Returns the lastWebAccessedTime.
     */
    public Timestamp getLastWebAccessedTime()
    {
        return lastWebAccessedTime;
    }

    /**
     * @param lastWebAccessedTime The lastWebAccessedTime to set.
     */
    public void setLastWebAccessedTime(Timestamp lastWebAccessedTime)
    {
        this.lastWebAccessedTime = lastWebAccessedTime;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return Returns the subscriberId.
     */
    public String getSubscriberId()
    {
        return subscriberId;
    }

    /**
     * @param subscriberId The subscriberId to set.
     */
    public void setSubscriberId(String subscriberId)
    {
        this.subscriberId = subscriberId;
    }

    /**
     * @return Returns the userType.
     */
    public String getUserType()
    {
        return userType;
    }

    /**
     * @param userType The userType to set.
     */
    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    /**
     * @return Returns the workPhoneNo.
     */
    public String getWorkPhoneNo()
    {
        return workPhoneNo;
    }

    /**
     * @param workPhoneNo The workPhoneNo to set.
     */
    public void setWorkPhoneNo(String workPhoneNo)
    {
        this.workPhoneNo = workPhoneNo;
    }

    /**
     * @return true If creating user is successful.
     *  
     */
    public boolean createUser()
    {

        //		if(isExists(getSubscriberId())) {
        //			return updateUser();
        //		}

        Connection con = null;
        PreparedStatement stmnt = null;
        boolean isCreated = false;
        try
        {
            basicLogger.info("Creating user account for " + getSubscriberId());
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_INSERT_USER);
            stmnt.setString(1, getSubscriberId());
            stmnt.setString(2, getPassword());
            stmnt.setString(3, getUserType());
            stmnt.setString(4, getCorporateUser());
            stmnt.setString(5, getLanguage());
            stmnt.setTimestamp(6, getLastWapAccessedTime());
            stmnt.setTimestamp(7, getLastWebAccessedTime());
            stmnt.setString(8, getEmailId());
            stmnt.setString(9, getFirstName());
            stmnt.setString(10, getLastName());
            stmnt.setTimestamp(11, getDateOfBirth());
            stmnt.setString(12, getWorkPhoneNo());
            stmnt.setString(13, getHomePhoneNo());
            int i = stmnt.executeUpdate();
            if (i > 0)
            {
                isCreated = true;
            }
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
            basicLogger.error("Error in creating account for user "
                    + getSubscriberId(), sqle);
        }
        return isCreated;
    }

    /**
     * @param subId Mobile number of the subscriber.
     * @param pwd Password for user account.
     * @return RBTUser User object with set of parameters.
     */
    public static RBTUser selectUser(String subId, String pwd)
    {
        Connection con = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        RBTUser user = null;
        try
        {
            basicLogger.info("Getting user " + subId + " details from DB.");
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_SELECT_USER);
            stmnt.setString(1, subId);
            stmnt.setString(2, pwd);
            rs = stmnt.executeQuery();
            basicLogger.info("before...");
            if (rs.next())
            {
                basicLogger.info("after...");
                user = new RBTUser();
                user.setSubscriberId(rs.getString("SUBSCRIBER_ID"));
                user.setPassword(rs.getString("PASSWORD"));
                user.setUserType(rs.getString("USER_TYPE"));
                user.setCorporateUser(rs.getString("CORPORATE_USER"));
                user.setLanguage(rs.getString("LANGUAGE"));
                user.setLastWapAccessedTime(rs
                        .getTimestamp("LAST_WAP_ACCESS_TIME"));
                user.setLastWebAccessedTime(rs
                        .getTimestamp("LAST_WEB_ACCESS_TIME"));
                user.setEmailId(rs.getString("EMAILID"));
                user.setFirstName(rs.getString("FIRST_NAME"));
                user.setLastName(rs.getString("LAST_NAME"));
                user.setDateOfBirth(rs.getTimestamp("DATE_OF_BIRTH"));
                user.setWorkPhoneNo(rs.getString("WORK_PHONE_NO"));
                user.setHomePhoneNo(rs.getString("HOME_PHONE_NO"));
            }
            DBConnection.closeConnStmtResultSet(con, stmnt, rs);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, rs);
            sqle.printStackTrace();
            basicLogger.error("Exception while getting user " + subId
                    + " details.", sqle);
        }
        return user;
    }

    /**
     * @param subId Mobile number of the subscriber.
     * @return true if user with given subId is exists in DB.
     *  
     */
    public static boolean isExists(String subId)
    {
        Connection con = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        boolean isExists = false;
        try
        {
            basicLogger.info("Checking for user " + subId + " in DB.");
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_IS_USER_EXISTS);
            stmnt.setString(1, subId);
            rs = stmnt.executeQuery();
            if (rs.next())
            {
                isExists = true;
            }
            DBConnection.closeConnStmtResultSet(con, stmnt, rs);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, rs);
            sqle.printStackTrace();
            basicLogger.error("Error while checking the user " + subId
                    + " existence.", sqle);
        }
        return isExists;
    }

    /**
     * @return true if updation of user is successful.
     *  
     */
    public boolean updateUser()
    {
        Connection con = null;
        PreparedStatement stmnt = null;
        boolean isChanged = false;
        try
        {
            basicLogger
                    .info("Updating user " + getSubscriberId() + " account.");
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_UPDATE_USER);
            stmnt.setString(1, getSubscriberId());
            stmnt.setString(2, getPassword());
            stmnt.setString(3, getUserType());
            stmnt.setString(4, getCorporateUser());
            stmnt.setString(5, getLanguage());
            stmnt.setTimestamp(6, getLastWapAccessedTime());
            stmnt.setTimestamp(7, getLastWebAccessedTime());
            stmnt.setString(8, getEmailId());
            stmnt.setString(9, getFirstName());
            stmnt.setString(10, getLastName());
            stmnt.setTimestamp(11, getDateOfBirth());
            stmnt.setString(12, getWorkPhoneNo());
            stmnt.setString(13, getHomePhoneNo());
            int count = stmnt.executeUpdate();
            if (count > 0)
            {
                isChanged = true;
            }
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
            sqle.printStackTrace();
            basicLogger.error("Error in updating account of user "
                    + getSubscriberId(), sqle);
        }
        return isChanged;
    }

    /**
     * @return true if authorized user or else false.
     *  
     */
    public boolean isAuthUser()
    {
        Connection con = null;
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        boolean isAuth = false;
        try
        {
            basicLogger.info("Checking for user " + getSubscriberId()
                    + " in DB.");
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_IS_AUTH_USER);
            stmnt.setString(1, getSubscriberId());
            stmnt.setString(2, getPassword());
            rs = stmnt.executeQuery();
            if (rs.next())
            {
                isAuth = true;
            }
            DBConnection.closeConnStmtResultSet(con, stmnt, rs);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, rs);
            sqle.printStackTrace();
            basicLogger.error("Error while checking the user "
                    + getSubscriberId() + " existence.", sqle);
        }
        return isAuth;

    }

    /**
     * @return true if successfully updates password or else false.
     *  
     */
    public boolean updatePwd()
    {
        Connection con = null;
        PreparedStatement stmnt = null;
        boolean isChanged = false;
        try
        {
            basicLogger.info("Updating the pwd of user " + getSubscriberId());
            con = DBConnection.getConnection();
            stmnt = con.prepareStatement(Q_UPDATE_PWD);
            stmnt.setString(1, getPassword());
            stmnt.setString(2, getSubscriberId());
            int rows = stmnt.executeUpdate();
            if (rows > 0)
            {
                isChanged = true;
            }
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
        }
        catch (SQLException sqle)
        {
            DBConnection.closeConnStmtResultSet(con, stmnt, null);
            sqle.printStackTrace();
            basicLogger.error("Error while updating pwd of user "
                    + getSubscriberId(), sqle);
        }
        return isChanged;
    }

    /**
     * @param password
     * @param id
     */
    public RBTUser(String subscriberId, String password)
    {
        this.subscriberId = subscriberId;
        this.password = password;
    }

    /**
     * Default constructor for creating RBTUser object.
     *  
     */
    public RBTUser()
    {
        super();
    }

}