package co.touchlab.squeaky.db.sqlcipher;

import android.database.sqlite.SQLiteException;
import co.touchlab.squeaky.db.SQLiteStatement;

/**
 * Created by kgalligan on 11/17/15.
 */
public class SQLiteStatementImpl implements SQLiteStatement
{
	private final net.sqlcipher.database.SQLiteStatement stmt;

	public SQLiteStatementImpl(net.sqlcipher.database.SQLiteStatement stmt)
	{
		this.stmt = stmt;
	}

	@Override
	public void close()
	{
		stmt.close();
	}

	@Override
	public long executeInsert() throws SQLiteException
	{
		return stmt.executeInsert();
	}

	@Override
	public long executeUpdateDelete() throws SQLiteException
	{
		return stmt.executeUpdateDelete();
	}

	@Override
	public void bindBlob(int index, byte[] value)
	{
		stmt.bindBlob(index, value);
	}

	@Override
	public void bindDouble(int index, double value)
	{
		stmt.bindDouble(index, value);
	}

	@Override
	public void bindLong(int index, long value)
	{
		stmt.bindLong(index, value);
	}

	@Override
	public void bindNull(int index)
	{
		stmt.bindNull(index);
	}

	@Override
	public void bindString(int index, String value)
	{
		stmt.bindString(index, value);
	}

	@Override
	public void clearBindings()
	{
		stmt.clearBindings();
	}
}
