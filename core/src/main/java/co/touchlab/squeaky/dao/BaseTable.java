package co.touchlab.squeaky.dao;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kgalligan on 10/16/15.
 */
public abstract class BaseTable implements ManagedModel
{
	private SqueakyContext squeakyContext;
	private final Set<String> refreshed = new HashSet<>();

	protected void checkRefresh(String field, Object data) throws SQLException
	{
		if(!refreshed.contains(field))
		{
			squeakyContext.getDao(getClass()).refresh(data);
			refreshed.add(field);
		}
	}

	public void setContext(SqueakyContext squeakyContext)
	{
		this.squeakyContext = squeakyContext;
	}
}
