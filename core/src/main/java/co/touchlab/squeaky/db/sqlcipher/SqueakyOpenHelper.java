package co.touchlab.squeaky.db.sqlcipher;

import android.content.Context;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.table.GeneratedTableMapper;
import net.sqlcipher.DatabaseErrorHandler;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper you should extend to manage data access.  Generally works the same as the standard SQLiteOpenHelper.
 *
 * @author kgalligan
 */
public abstract class SqueakyOpenHelper extends SQLiteOpenHelper implements co.touchlab.squeaky.db.SQLiteOpenHelper
{
	private final SqueakyContext squeakyContext;
	private final PassphraseProvider passphraseProvider;
	private SQLiteDatabaseImpl sqLiteDatabase;

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, PassphraseProvider passphraseProvider)
	{
		this(context, name, factory, version, passphraseProvider, null);
	}

	public SqueakyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, PassphraseProvider passphraseProvider, SQLiteDatabaseHook databaseHook)
	{
		super(context, name, factory, version, databaseHook);
		this.passphraseProvider = passphraseProvider;
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
			sqLiteDatabase = new SQLiteDatabaseImpl(getWritableDatabase(passphraseProvider.getPassphrase()));
		return sqLiteDatabase;
	}
}
