package com.j256.ormlite;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.squeaky.Dao;
import com.j256.ormlite.android.squeaky.SqueakyOpenHelper;
import com.j256.ormlite.table.AndroidDatabaseType;
import org.junit.After;
import org.junit.Before;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.TableUtils;
import org.robolectric.RuntimeEnvironment;

public abstract class BaseCoreTest {

	protected AndroidDatabaseType databaseType = new AndroidDatabaseType();
	protected SimpleHelper connectionSource;

	@Before
	public void before() throws Exception {
		connectionSource = new SimpleHelper(RuntimeEnvironment.application, getClass().getName()+".db", Foo.class, Foreign.class);
	}

	@After
	public void after() throws Exception {
		connectionSource.close();
		connectionSource = null;
	}

	protected static class Foo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String STRING_COLUMN_NAME = "string";
		@DatabaseField(generatedId = true, columnName = ID_COLUMN_NAME)
		public int id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		public int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		public int equal;
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		public String stringField;
		public Foo() {
		}
		@Override
		public String toString() {
			return "Foo:" + id;
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id == ((Foo) other).id;
		}
		@Override
		public int hashCode() {
			return id;
		}
	}

	protected static class Foreign {
		public static final String FOO_COLUMN_NAME = "foo_id";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, columnName = FOO_COLUMN_NAME)
		public Foo foo;
		public Foreign() {
		}
	}

	protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}

		@SuppressWarnings("unchecked") Dao<T, ID> dao = connectionSource.getDao(clazz);

		return dao;
	}

	protected <T> void dropTable(Class<T> clazz, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTables(connectionSource.getWritableDatabase(), ignoreErrors, clazz);
	}

	public static class SimpleHelper extends SqueakyOpenHelper
	{

		public SimpleHelper(Context context, String name, Class... managingClasses)
		{
			super(context, name, null, 1, managingClasses);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase)
		{
			try
			{
				TableUtils.createTables(sqLiteDatabase, getManagingClasses());
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
		{
			Class[] managingClasses = getManagingClasses();
			Class[] reversed = new Class[managingClasses.length];

			for(int i=0; i<managingClasses.length; i++)
			{
				reversed[(managingClasses.length - i) - 1] = managingClasses[i];
			}
			try
			{
				TableUtils.dropTables(sqLiteDatabase, true, reversed);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
