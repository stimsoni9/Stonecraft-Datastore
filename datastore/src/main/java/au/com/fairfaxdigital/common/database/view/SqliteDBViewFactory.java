/**
 * 
 */
package au.com.fairfaxdigital.common.database.view;

/**
 * This class is the sqlite implemention of {@link DatabaseViewFactory}.
 * It returns {@link SQLiteColumn} and {@link SQLiteTable} object from it's methods 
 *
 * @author mdelaney
 * @author Author: michael.delaney
 * @created Oct 22, 2013
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class SqliteDBViewFactory implements DatabaseViewFactory {

	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.view.DatabaseViewFactory#getNewTable(java.lang.String)
	 */
	@Override
	public DatabaseTable getNewTable(String tableName) {
		return new SQLiteTable(tableName);
	}

	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.view.DatabaseViewFactory#getNewColumn(java.lang.String, int, int)
	 */
	@Override
	public DatabaseColumn getNewColumn(String name, int type, int length) {
		return new SQLiteColumn(name, type, length);
	}

	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.view.DatabaseViewFactory#getNewColumn(java.lang.String, int, int, boolean)
	 */
	@Override
	public DatabaseColumn getNewColumn(String name, int type, int length,
		boolean isPrimaryKey) {
		return new SQLiteColumn(name, type, length, isPrimaryKey);
	}

	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.view.DatabaseViewFactory#getNewColumn(java.lang.String, int, int, boolean, boolean)
	 */
	@Override
	public DatabaseColumn getNewColumn(String name, int type, int length,
		boolean isPrimaryKey, boolean isNullable) {
		return new SQLiteColumn(name, type, length, isPrimaryKey, isNullable);
	}

	/* (non-Javadoc)
	 * @see au.com.fairfaxdigital.common.database.view.DatabaseViewFactory#getNewColumn(java.lang.String, int, int, boolean, boolean, boolean)
	 */
	@Override
	public DatabaseColumn getNewColumn(String name, int type, int length,
		boolean isPrimaryKey, boolean isNullable, boolean isAutoIncrement) {
		return new SQLiteColumn(name, type, length, isPrimaryKey, isNullable, isAutoIncrement);
	}
}
