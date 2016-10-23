package com.onmobile.apps.ringbacktones.content.database;

import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLXML;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.sql.Statement;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;

public class RBTResultSet implements ResultSet
{
    private ResultSet rs;

    public RBTResultSet(ResultSet rs)
    {
        this.rs = rs;
    }

    @Override
	public void close() throws SQLException
    {
        rs.close();
    }

    @Override
	public boolean next() throws SQLException
    {
        return rs.next();
    }

    @Override
	public boolean wasNull() throws SQLException
    {
        return rs.wasNull();
    }

    @Override
	public String getString(int columnIndex) throws SQLException
    {
        String str = rs.getString(columnIndex);
		if(str != null)
			str = new String(str);
        return str;
    }

    @Override
	public boolean getBoolean(int columnIndex) throws SQLException
    {
        return rs.getBoolean(columnIndex);
    }

    @Override
	public byte getByte(int columnIndex) throws SQLException
    {
        return rs.getByte(columnIndex);
    }

    @Override
	public short getShort(int columnIndex) throws SQLException
    {
        return rs.getShort(columnIndex);
    }

    @Override
	public int getInt(int columnIndex) throws SQLException
    {
        return rs.getInt(columnIndex);
    }

    @Override
	public long getLong(int columnIndex) throws SQLException
    {
        return rs.getLong(columnIndex);
    }

    @Override
	public float getFloat(int columnIndex) throws SQLException
    {
        return rs.getFloat(columnIndex);
    }

    @Override
	public double getDouble(int columnIndex) throws SQLException
    {
        return rs.getDouble(columnIndex);
    }

    @Override
    @Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws SQLException
    {
        return rs.getBigDecimal(columnIndex, scale);
    }

    @Override
	public byte[] getBytes(int columnIndex) throws SQLException
    {
        return rs.getBytes(columnIndex);
    }

    @Override
	public java.sql.Date getDate(int columnIndex) throws SQLException
    {
        return rs.getDate(columnIndex);
    }

    @Override
	public java.sql.Time getTime(int columnIndex) throws SQLException
    {
        return rs.getTime(columnIndex);
    }

    @Override
	public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        return rs.getTimestamp(columnIndex);
    }

    @Override
	public java.io.InputStream getAsciiStream(int columnIndex)
            throws SQLException
    {
        return rs.getAsciiStream(columnIndex);
    }

    @Override
    @Deprecated
	public java.io.InputStream getUnicodeStream(int columnIndex)
            throws SQLException
    {
        return rs.getUnicodeStream(columnIndex);
    }

    @Override
	public java.io.InputStream getBinaryStream(int columnIndex)
            throws SQLException
    {
        return rs.getBinaryStream(columnIndex);
    }

    @Override
	public String getString(String columnName) throws SQLException
    {
        String str = rs.getString(columnName);
		if(str != null)
			str = new String(str);
        return str;
    }

    @Override
	public boolean getBoolean(String columnName) throws SQLException
    {
        return rs.getBoolean(columnName);
    }

    @Override
	public byte getByte(String columnName) throws SQLException
    {
        return rs.getByte(columnName);
    }

    @Override
	public short getShort(String columnName) throws SQLException
    {
        return rs.getShort(columnName);
    }

    @Override
	public int getInt(String columnName) throws SQLException
    {
        return rs.getInt(columnName);
    }

    @Override
	public long getLong(String columnName) throws SQLException
    {
        return rs.getLong(columnName);
    }

    @Override
	public float getFloat(String columnName) throws SQLException
    {
        return rs.getFloat(columnName);
    }

    @Override
	public double getDouble(String columnName) throws SQLException
    {
        return rs.getDouble(columnName);
    }

    @Override
    @Deprecated
	public BigDecimal getBigDecimal(String columnName, int scale)
            throws SQLException
    {
        return rs.getBigDecimal(columnName, scale);
    }

    @Override
	public byte[] getBytes(String columnName) throws SQLException
    {
        return rs.getBytes(columnName);
    }

    @Override
	public java.sql.Date getDate(String columnName) throws SQLException
    {
        return rs.getDate(columnName);
    }

    @Override
	public java.sql.Time getTime(String columnName) throws SQLException
    {
        return rs.getTime(columnName);
    }

    @Override
	public java.sql.Timestamp getTimestamp(String columnName)
            throws SQLException
    {
        return rs.getTimestamp(columnName);
    }

    @Override
	public java.io.InputStream getAsciiStream(String columnName)
            throws SQLException
    {
        return rs.getAsciiStream(columnName);
    }

    @Override
    @Deprecated
	public java.io.InputStream getUnicodeStream(String columnName)
            throws SQLException
    {
        return rs.getUnicodeStream(columnName);
    }

    @Override
	public java.io.InputStream getBinaryStream(String columnName)
            throws SQLException
    {
        return rs.getBinaryStream(columnName);
    }

    @Override
	public SQLWarning getWarnings() throws SQLException
    {
        return rs.getWarnings();
    }

    @Override
	public void clearWarnings() throws SQLException
    {
        rs.clearWarnings();
    }

    @Override
	public String getCursorName() throws SQLException
    {
        return rs.getCursorName();
    }

    @Override
	public ResultSetMetaData getMetaData() throws SQLException
    {
        return rs.getMetaData();
    }

    @Override
	public Object getObject(int columnIndex) throws SQLException
    {
        return rs.getObject(columnIndex);
    }

    @Override
	public Object getObject(String columnName) throws SQLException
    {
        return rs.getObject(columnName);
    }

    @Override
	public int findColumn(String columnName) throws SQLException
    {
        return rs.findColumn(columnName);
    }

    @Override
	public java.io.Reader getCharacterStream(int columnIndex)
            throws SQLException
    {
        return rs.getCharacterStream(columnIndex);
    }

    @Override
	public java.io.Reader getCharacterStream(String columnName)
            throws SQLException
    {
        return rs.getCharacterStream(columnName);
    }

    @Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        return rs.getBigDecimal(columnIndex);
    }

    @Override
	public BigDecimal getBigDecimal(String columnName) throws SQLException
    {
        return rs.getBigDecimal(columnName);
    }

    @Override
	public boolean isBeforeFirst() throws SQLException
    {
        return rs.isBeforeFirst();
    }

    @Override
	public boolean isAfterLast() throws SQLException
    {
        return rs.isAfterLast();
    }

    @Override
	public boolean isFirst() throws SQLException
    {
        return rs.isFirst();
    }

    @Override
	public boolean isLast() throws SQLException
    {
        return rs.isLast();
    }

    @Override
	public void beforeFirst() throws SQLException
    {
        rs.beforeFirst();
    }

    @Override
	public void afterLast() throws SQLException
    {
        rs.afterLast();
    }

    @Override
	public boolean first() throws SQLException
    {
        return rs.first();
    }

    @Override
	public boolean last() throws SQLException
    {
        return rs.last();
    }

    @Override
	public int getRow() throws SQLException
    {
        return rs.getRow();
    }

    @Override
	public boolean absolute(int row) throws SQLException
    {
        return rs.absolute(row);
    }

    @Override
	public boolean relative(int rows) throws SQLException
    {
        return rs.relative(rows);
    }

    @Override
	public boolean previous() throws SQLException
    {
        return rs.previous();
    }

    @Override
	public void setFetchDirection(int direction) throws SQLException
    {
        rs.setFetchDirection(direction);
    }

    @Override
	public int getFetchDirection() throws SQLException
    {
        return rs.getFetchDirection();
    }

    @Override
	public void setFetchSize(int rows) throws SQLException
    {
        rs.setFetchSize(rows);
    }

    @Override
	public int getFetchSize() throws SQLException
    {
        return rs.getFetchSize();
    }

    @Override
	public int getType() throws SQLException
    {
        return rs.getType();
    }

    @Override
	public int getConcurrency() throws SQLException
    {
        return rs.getConcurrency();
    }

    @Override
	public boolean rowUpdated() throws SQLException
    {
        return rs.rowUpdated();
    }

    @Override
	public boolean rowInserted() throws SQLException
    {
        return rs.rowInserted();
    }

    @Override
	public boolean rowDeleted() throws SQLException
    {
        return rs.rowDeleted();
    }

    @Override
	public void updateNull(int columnIndex) throws SQLException
    {
        rs.updateNull(columnIndex);
    }

    @Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException
    {
        rs.updateBoolean(columnIndex, x);
    }

    @Override
	public void updateByte(int columnIndex, byte x) throws SQLException
    {
        rs.updateByte(columnIndex, x);
    }

    @Override
	public void updateShort(int columnIndex, short x) throws SQLException
    {
        rs.updateShort(columnIndex, x);
    }

    @Override
	public void updateInt(int columnIndex, int x) throws SQLException
    {
        rs.updateInt(columnIndex, x);
    }

    @Override
	public void updateLong(int columnIndex, long x) throws SQLException
    {
        rs.updateLong(columnIndex, x);
    }

    @Override
	public void updateFloat(int columnIndex, float x) throws SQLException
    {
        rs.updateFloat(columnIndex, x);
    }

    @Override
	public void updateDouble(int columnIndex, double x) throws SQLException
    {
        rs.updateDouble(columnIndex, x);
    }

    @Override
	public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException
    {
        rs.updateBigDecimal(columnIndex, x);
    }

    @Override
	public void updateString(int columnIndex, String x) throws SQLException
    {
        rs.updateString(columnIndex, x);
    }

    @Override
	public void updateBytes(int columnIndex, byte x[]) throws SQLException
    {
        rs.updateBytes(columnIndex, x);
    }

    @Override
	public void updateDate(int columnIndex, java.sql.Date x)
            throws SQLException
    {
        rs.updateDate(columnIndex, x);
    }

    @Override
	public void updateTime(int columnIndex, java.sql.Time x)
            throws SQLException
    {
        rs.updateTime(columnIndex, x);
    }

    @Override
	public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
            throws SQLException
    {
        rs.updateTimestamp(columnIndex, x);
    }

    @Override
	public void updateAsciiStream(int columnIndex, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateAsciiStream(columnIndex, x, length);
    }

    @Override
	public void updateBinaryStream(int columnIndex, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateBinaryStream(columnIndex, x, length);
    }

    @Override
	public void updateCharacterStream(int columnIndex, java.io.Reader x,
            int length) throws SQLException
    {
        rs.updateCharacterStream(columnIndex, x, length);
    }

    @Override
	public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException
    {
        rs.updateObject(columnIndex, x, scale);
    }

    @Override
	public void updateObject(int columnIndex, Object x) throws SQLException
    {
        rs.updateObject(columnIndex, x);
    }

    @Override
	public void updateNull(String columnName) throws SQLException
    {
        rs.updateNull(columnName);
    }

    @Override
	public void updateBoolean(String columnName, boolean x) throws SQLException
    {
        rs.updateBoolean(columnName, x);
    }

    @Override
	public void updateByte(String columnName, byte x) throws SQLException
    {
        rs.updateByte(columnName, x);
    }

    @Override
	public void updateShort(String columnName, short x) throws SQLException
    {
        rs.updateShort(columnName, x);
    }

    @Override
	public void updateInt(String columnName, int x) throws SQLException
    {
        rs.updateInt(columnName, x);
    }

    @Override
	public void updateLong(String columnName, long x) throws SQLException
    {
        rs.updateLong(columnName, x);
    }

    @Override
	public void updateFloat(String columnName, float x) throws SQLException
    {
        rs.updateFloat(columnName, x);
    }

    @Override
	public void updateDouble(String columnName, double x) throws SQLException
    {
        rs.updateDouble(columnName, x);
    }

    @Override
	public void updateBigDecimal(String columnName, BigDecimal x)
            throws SQLException
    {
        rs.updateBigDecimal(columnName, x);
    }

    @Override
	public void updateString(String columnName, String x) throws SQLException
    {
        rs.updateString(columnName, x);
    }

    @Override
	public void updateBytes(String columnName, byte x[]) throws SQLException
    {
        rs.updateBytes(columnName, x);
    }

    @Override
	public void updateDate(String columnName, java.sql.Date x)
            throws SQLException
    {
        rs.updateDate(columnName, x);
    }

    @Override
	public void updateTime(String columnName, java.sql.Time x)
            throws SQLException
    {
        rs.updateTime(columnName, x);
    }

    @Override
	public void updateTimestamp(String columnName, java.sql.Timestamp x)
            throws SQLException
    {
        rs.updateTimestamp(columnName, x);
    }

    @Override
	public void updateAsciiStream(String columnName, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateAsciiStream(columnName, x, length);
    }

    @Override
	public void updateBinaryStream(String columnName, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateBinaryStream(columnName, x, length);
    }

    @Override
	public void updateCharacterStream(String columnName, java.io.Reader reader,
            int length) throws SQLException
    {
        rs.updateCharacterStream(columnName, reader, length);
    }

    @Override
	public void updateObject(String columnName, Object x, int scale)
            throws SQLException
    {
        rs.updateObject(columnName, x, scale);
    }

    @Override
	public void updateObject(String columnName, Object x) throws SQLException
    {
        rs.updateObject(columnName, x);
    }

    @Override
	public void insertRow() throws SQLException
    {
        rs.insertRow();
    }

    @Override
	public void updateRow() throws SQLException
    {
        rs.updateRow();
    }

    @Override
	public void deleteRow() throws SQLException
    {
        rs.deleteRow();
    }

    @Override
	public void refreshRow() throws SQLException
    {
        rs.refreshRow();
    }

    @Override
	public void cancelRowUpdates() throws SQLException
    {
        rs.cancelRowUpdates();
    }

    @Override
	public void moveToInsertRow() throws SQLException
    {
        rs.moveToInsertRow();
    }

    @Override
	public void moveToCurrentRow() throws SQLException
    {
        rs.moveToCurrentRow();
    }

    @Override
	public Statement getStatement() throws SQLException
    {
        return rs.getStatement();
    }

    @Override
	public Ref getRef(int i) throws SQLException
    {
        return rs.getRef(i);
    }

    @Override
	public Blob getBlob(int i) throws SQLException
    {
        return rs.getBlob(i);
    }

    @Override
	public Clob getClob(int i) throws SQLException
    {
        return rs.getClob(i);
    }

    @Override
	public Array getArray(int i) throws SQLException
    {
        return rs.getArray(i);
    }

    @Override
	public Ref getRef(String colName) throws SQLException
    {
        return rs.getRef(colName);
    }

    @Override
	public Blob getBlob(String colName) throws SQLException
    {
        return rs.getBlob(colName);
    }

    @Override
	public Clob getClob(String colName) throws SQLException
    {
        return rs.getClob(colName);
    }

    @Override
	public Array getArray(String colName) throws SQLException
    {
        return rs.getArray(colName);
    }

    @Override
	public java.sql.Date getDate(int columnIndex, Calendar cal)
            throws SQLException
    {
        return rs.getDate(columnIndex, cal);
    }

    @Override
	public java.sql.Date getDate(String columnName, Calendar cal)
            throws SQLException
    {
        return rs.getDate(columnName, cal);
    }

    @Override
	public java.sql.Time getTime(int columnIndex, Calendar cal)
            throws SQLException
    {
        return rs.getTime(columnIndex, cal);
    }

    @Override
	public java.sql.Time getTime(String columnName, Calendar cal)
            throws SQLException
    {
        return rs.getTime(columnName, cal);
    }

    @Override
	public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException
    {
        return rs.getTimestamp(columnIndex, cal);
    }

    @Override
	public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException
    {
        return rs.getTimestamp(columnName, cal);
    }

    @Override
	public URL getURL(int columnIndex) throws SQLException
    {
        return rs.getURL(columnIndex);
    }

    // JDBC 3.0 features.
    @Override
	public URL getURL(String columnName) throws SQLException
    {
        return rs.getURL(columnName);
    }

    @Override
	public void updateRef(int columnIndex, Ref x) throws SQLException
    {
        rs.updateRef(columnIndex, x);
    }

    @Override
	public void updateRef(String columnName, Ref x) throws SQLException
    {
        rs.updateRef(columnName, x);
    }

    @Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException
    {
        rs.updateBlob(columnIndex, x);
    }

    @Override
	public void updateBlob(String columnName, Blob x) throws SQLException
    {
        rs.updateBlob(columnName, x);
    }

    @Override
	public void updateClob(int columnIndex, Clob x) throws SQLException
    {
        rs.updateClob(columnIndex, x);
    }

    @Override
	public void updateClob(String columnName, Clob x) throws SQLException
    {
        rs.updateClob(columnName, x);
    }

    @Override
	public void updateArray(int columnIndex, Array x) throws SQLException
    {
        rs.updateArray(columnIndex, x);
    }

    @Override
	public void updateArray(String columnName, Array x) throws SQLException
    {
        rs.updateArray(columnName, x);
    }

	/* (non-Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		return rs.unwrap(iface);
	}

	/* (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return rs.isWrapperFor(iface);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException
	{
		return rs.getObject(columnIndex, map);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException
	{
		return rs.getObject(columnLabel, map);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(int)
	 */
	@Override
	public RowId getRowId(int columnIndex) throws SQLException
	{
		return rs.getRowId(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(java.lang.String)
	 */
	@Override
	public RowId getRowId(String columnLabel) throws SQLException
	{
		return rs.getRowId(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)
	 */
	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException
	{
		rs.updateRowId(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)
	 */
	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException
	{
		rs.updateRowId(columnLabel, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException
	{
		return rs.getHoldability();
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException
	{
		return rs.isClosed();
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
	 */
	@Override
	public void updateNString(int columnIndex, String nString)
			throws SQLException
	{
		rs.updateNString(columnIndex, nString);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateNString(String columnLabel, String nString)
			throws SQLException
	{
		rs.updateNString(columnLabel, nString);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
	 */
	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException
	{
		rs.updateNClob(columnIndex, nClob);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException
	{
		rs.updateNClob(columnLabel, nClob);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNClob(int)
	 */
	@Override
	public NClob getNClob(int columnIndex) throws SQLException
	{
		return rs.getNClob(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNClob(java.lang.String)
	 */
	@Override
	public NClob getNClob(String columnLabel) throws SQLException
	{
		return rs.getNClob(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException
	{
		return rs.getSQLXML(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException
	{
		return rs.getSQLXML(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException
	{
		rs.updateSQLXML(columnIndex, xmlObject);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException
	{
		rs.updateSQLXML(columnLabel, xmlObject);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNString(int)
	 */
	@Override
	public String getNString(int columnIndex) throws SQLException
	{
		return rs.getNString(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNString(java.lang.String)
	 */
	@Override
	public String getNString(String columnLabel) throws SQLException
	{
		return rs.getNString(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNCharacterStream(int)
	 */
	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException
	{
		return rs.getCharacterStream(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException
	{
		return rs.getNCharacterStream(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException
	{
		rs.updateNCharacterStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException
	{
		rs.updateNCharacterStream(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException
	{
		rs.updateAsciiStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException
	{
		rs.updateBinaryStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException
	{
		rs.updateCharacterStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException
	{
		rs.updateAsciiStream(columnLabel, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException
	{
		rs.updateBinaryStream(columnLabel, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException
	{
		rs.updateCharacterStream(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException
	{
		rs.updateBlob(columnIndex, inputStream, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException
	{
		rs.updateBlob(columnLabel, inputStream, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException
	{
		rs.updateClob(columnIndex, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException
	{
		rs.updateClob(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException
	{
		rs.updateNClob(columnIndex, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException
	{
		rs.updateNClob(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException
	{
		rs.updateNCharacterStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateNCharacterStream(columnLabel, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException
	{
		rs.updateAsciiStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException
	{
		rs.updateBinaryStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException
	{
		rs.updateCharacterStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException
	{
		rs.updateAsciiStream(columnLabel, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException
	{
		rs.updateBinaryStream(columnLabel, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateCharacterStream(columnLabel, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException
	{
		rs.updateBlob(columnIndex, inputStream);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException
	{
		rs.updateBlob(columnLabel, inputStream);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException
	{
		rs.updateClob(columnIndex, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateClob(columnLabel, reader);
		
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException
	{
		rs.updateNClob(columnIndex, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateNClob(columnLabel, reader);
		
	}

	public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}