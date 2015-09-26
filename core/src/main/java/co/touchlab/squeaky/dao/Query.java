package co.touchlab.squeaky.dao;

import java.sql.SQLException;

/**
 * Created by kgalligan on 9/10/15.
 */
public interface Query
{
	String getFromStatement()throws SQLException;
	String getWhereStatement()throws SQLException;
	String[] getParameters()throws SQLException;
}
