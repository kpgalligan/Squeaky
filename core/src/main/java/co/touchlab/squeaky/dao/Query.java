package co.touchlab.squeaky.dao;

import java.sql.SQLException;

/**
 * Created by kgalligan on 9/10/15.
 */
public interface Query
{
	String getFromStatement(boolean joinsAllowed) throws SQLException;

	String getWhereStatement(boolean joinsAllowed) throws SQLException;

	String[] getParameters() throws SQLException;
}
