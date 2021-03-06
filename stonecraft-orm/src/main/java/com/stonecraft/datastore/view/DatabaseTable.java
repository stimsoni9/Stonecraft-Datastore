package com.stonecraft.datastore.view;

import android.net.Uri;

import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.utils.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the base class for a abstract view of a database table. The sub
 * classes are in charge of implement the db specific syntax for creating the
 * table
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public abstract class DatabaseTable {
	public static final String CREATE_TABLE = "CREATE TABLE";
	protected static final String PREFIX_TEMP = "TEMP_";

	protected Uri myUri;
	protected String myName;
	protected Map<String, DatabaseColumn> myColumns;
	protected boolean myIsTempTable;
	protected Calendar myLastTableUpdate;

	public DatabaseTable(String name, Uri uri) {
		myName = name;
		myUri = uri;
		myColumns = new HashMap<String, DatabaseColumn>();
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}
	
	/**
	 * @return the name
	 */
	public String getDBName() {
		StringBuilder builder = new StringBuilder();
		if(isTempTable()){
			builder.append(PREFIX_TEMP);
		}
		
		builder.append(myName);
		return builder.toString();
	}

	/**
	 * This method returns the column in this table with the given name
	 * @param name
	 * @return
	 */
	public DatabaseColumn getColumn(String name) {
		return myColumns.get(name);
	}

	public Uri getUri() {
		return myUri;
	}

	/**
	 * @return the columns
	 */
	public Map<String, DatabaseColumn> getColumns() {
		return myColumns;
	}

	public void addColumn(DatabaseColumn col) {
		myColumns.put(col.getName(), col);
	}

	/**
	 * @return the isTempTable
	 */
	public boolean isTempTable() {
		return myIsTempTable;
	}

	/**
	 * @param isTempTable the isTempTable to set
	 */
	public void setTempTable(boolean isTempTable) {
		myIsTempTable = isTempTable;
	}
	
	public String getColumnNameString(){
		List<String>columns = new ArrayList<String>();
		for(DatabaseColumn column : myColumns.values()){
			columns.add(column.getName());
		}
		
		return StringUtils.convertListToDelimitedString(columns, StringUtils.COMMA);
	}

	public Calendar getLastTableUpdate() {
		return myLastTableUpdate;
	}

	public void notifyTableUpdate() {
		myLastTableUpdate = Calendar.getInstance();
	}

	/**
	 * This method returns the string that will be used in a create statement to
	 * create this table.
	 */
	public abstract String getCreateTableStmt() throws DatabaseException;

	/**
	 * This method returns whether this table has composite primary key
	 * 
	 * @return
	 */
	public abstract boolean hasCompositeKey();
}
