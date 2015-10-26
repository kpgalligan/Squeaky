package co.touchlab.squeaky.dao;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import co.touchlab.squeaky.table.GeneratedTableMapper;

/**
 * SQLiteOpenHelper you should extend to manage data access.  Generally works the same as the standard SQLiteOpenHelper.
 *
 * @author kgalligan
 */
public abstract class SqueakyOpenHelper extends SQLiteOpenHelper
{
	private final SqueakyContext squeakyContext;

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, Class... managingClasses)
	{
		super(context, name, factory, version);
		squeakyContext = new SqueakyContext(this, managingClasses);
	}

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler, Class[] managingClasses)
	{
		super(context, name, factory, version, errorHandler);
		squeakyContext = new SqueakyContext(this, managingClasses);
	}

	public SqueakyContext getSqueakyContext()
	{
		return squeakyContext;
	}

	public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz)
	{
		// special reflection fu is now handled internally by create dao calling the database type
		Dao<T, ?> dao = squeakyContext.getDao(clazz);
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

	public static GeneratedTableMapper loadGeneratedTableMapper(Class clazz)
	{
		try
		{
			return (GeneratedTableMapper) Class.forName(clazz.getName() + "$$Configuration").newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public Class[] getManagingClasses()
	{
		return squeakyContext.getManagingClasses();
	}
}
