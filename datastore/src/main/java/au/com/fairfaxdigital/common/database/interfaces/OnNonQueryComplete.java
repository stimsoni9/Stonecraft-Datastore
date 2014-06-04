package au.com.fairfaxdigital.common.database.interfaces;

import au.com.fairfaxdigital.common.database.exceptions.DatabaseException;

/**
 * This interface is used as the call back when a non query statement is
 * complete
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public interface OnNonQueryComplete {
    /**
     * This method returns the results of a query that has been made to
     * Datastore.
     *
     * @param token
     * @param updated
     * @param e
     */
	public void onNonQueryComplete(int token, int updated, DatabaseException e);
}
