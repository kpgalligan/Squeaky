package co.touchlab.squeaky.dao;

import android.database.Cursor;
import co.touchlab.squeaky.table.GeneratedTableMapper;
import co.touchlab.squeaky.table.TransientCache;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author graywatson, kgalligan
 */
public class SelectIterator<T> implements CloseableIterator<T>
{
	private final Cursor cursor;
	private final ModelDao<T> modelDao;
	private final GeneratedTableMapper<T> generatedTableMapper;
	private final TransientCache objectCache = new TransientCache();
	private final Dao.ForeignRefresh[] foreignRefreshMap;

	public SelectIterator(Cursor cursor, ModelDao<T> modelDao, Dao.ForeignRefresh[] foreignRefreshMap)
	{
		this.cursor = cursor;
		this.modelDao = modelDao;
		this.foreignRefreshMap = foreignRefreshMap;
		this.generatedTableMapper = modelDao.getGeneratedTableMapper();
	}

	public void closeQuietly()
	{
		try
		{
			close();
		}
		catch (Exception e)
		{
			//TODO
		}
	}

	public void moveToNext()
	{
		cursor.moveToNext();
	}

	public T first() throws SQLException
	{
		return cursor.moveToFirst() ? makeData() : null;
	}

	public T previous() throws SQLException
	{
		return cursor.moveToPrevious() ? makeData() : null;
	}

	public T current() throws SQLException
	{
		return makeData();
	}

	public T nextThrow() throws SQLException
	{
		return cursor.moveToNext() ? makeData() : null;
	}

	public T moveRelative(int offset) throws SQLException
	{
		return cursor.move(offset) ? makeData() : null;
	}

	public void close() throws IOException
	{
		cursor.close();
	}

	public boolean hasNext()
	{
		boolean next = cursor.moveToNext();
		cursor.moveToPrevious();
		return next;
	}

	public T next()
	{
		try
		{
			return nextThrow();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void remove()
	{
		throw new UnsupportedOperationException("Can't remove from cursor");
	}

	private T makeData() throws SQLException
	{
		T data = generatedTableMapper.createObject(cursor);
		generatedTableMapper.fillRow(data, cursor, modelDao, foreignRefreshMap, objectCache);
		return data;
	}

}
