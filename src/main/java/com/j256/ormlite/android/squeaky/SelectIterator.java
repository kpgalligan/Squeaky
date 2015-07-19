package com.j256.ormlite.android.squeaky;

import android.database.Cursor;
import com.j256.ormlite.Config;
import com.j256.ormlite.table.GeneratedTableMapper;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by kgalligan on 6/15/15.
 */
public class SelectIterator<T, ID> implements CloseableIterator<T>
{
	private final Cursor cursor;
	private final ModelDao<T, ID> modelDao;
	private final GeneratedTableMapper<T, ID> generatedTableMapper;

	public SelectIterator(Cursor cursor, ModelDao<T, ID> modelDao)
	{
		this.cursor = cursor;
		this.modelDao = modelDao;
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
			//TODO
		}

		return null;
	}

	public void remove()
	{
		throw new UnsupportedOperationException("Can't remove from cursor");
	}

	private T makeData() throws SQLException
	{
		T data = generatedTableMapper.createObject(cursor);
		generatedTableMapper.fillRow(data, cursor, modelDao, Config.MAX_AUTO_REFRESH);
		return data;
	}

}
