package co.touchlab.squeaky.db;

import android.database.sqlite.SQLiteException;

/**
 * Created by kgalligan on 11/17/15.
 */
public interface SQLiteStatement
{
	void close();

	long executeInsert() throws SQLiteException;

	long executeUpdateDelete() throws SQLiteException;

	void bindBlob(int index, byte[] value);

	void bindDouble(int index, double value);

	void bindLong(int index, long value);

	void bindNull(int index);

	void bindString(int index, String value);

	void clearBindings();
}
