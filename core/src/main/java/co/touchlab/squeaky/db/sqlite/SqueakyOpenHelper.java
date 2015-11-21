package co.touchlab.squeaky.db.sqlite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.table.GeneratedTableMapper;

/**
 * SQLiteOpenHelper you should extend to manage data access.  Generally works the same as the standard SQLiteOpenHelper.
 *
 * @author kgalligan
 */
public abstract class SqueakyOpenHelper extends SQLiteOpenHelper implements co.touchlab.squeaky.db.SQLiteOpenHelper
{
	private final SqueakyContext squeakyContext;
	private SQLiteDatabaseImpl sqLiteDatabase;

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
	{
		super(context, name, factory, version);
		squeakyContext = new SqueakyContext(this);
	}

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler)
	{
		super(context, name, factory, version, errorHandler);
		squeakyContext = new SqueakyContext(this);
	}

	public SqueakyContext getSqueakyContext()
	{
		return squeakyContext;
	}

	public <D extends Dao<T>, T> D getDao(Class<T> clazz)
	{
		// special reflection fu is now handled internally by create dao calling the database type
		Dao<T> dao = squeakyContext.getDao(clazz);
		@SuppressWarnings("unchecked")
		D castDao = (D) dao;
		return castDao;
	}

	@Override
	public synchronized void close()
	{
		squeakyContext.close();
		super.close();
	}

	public synchronized GeneratedTableMapper getGeneratedTableMapper(Class clazz)
	{
		return squeakyContext.getGeneratedTableMapper(clazz);
	}

	@Override
	public synchronized co.touchlab.squeaky.db.SQLiteDatabase getWrappedDatabase()
	{
		if(sqLiteDatabase == null)
			sqLiteDatabase = new SQLiteDatabaseImpl(getWritableDatabase());
		return sqLiteDatabase;
	}
}
