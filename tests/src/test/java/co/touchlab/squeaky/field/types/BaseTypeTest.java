package co.touchlab.squeaky.field.types;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.squeaky.dao.SqueakyOpenHelper;
import co.touchlab.squeaky.table.TableUtils;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;

public abstract class BaseTypeTest
{

	public SimpleHelper createHelper(Class... c)
	{
		return new SimpleHelper(RuntimeEnvironment.application, getClass().getSimpleName() + ".db", c);
	}

	public SimpleHelper createViewHelper(Class viewClass, Class... c)
	{
		return new SimpleHelper(RuntimeEnvironment.application, viewClass, getClass().getSimpleName() + ".db", c);
	}

	public static class SimpleHelper extends SqueakyOpenHelper
	{
		private Class viewClass;

		public SimpleHelper(Context context, String name, Class... managingClasses)
		{
			super(context, name, null, 1, managingClasses);
		}

		public SimpleHelper(Context context, Class viewClass, String name, Class... managingClasses)
		{
			super(context, name, null, 1, managingClasses);
			this.viewClass = viewClass;
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase)
		{
			try
			{
				TableUtils.createTables(sqLiteDatabase, getManagingClasses());
				if (viewClass != null)
				{
					TableUtils.createViews(sqLiteDatabase, viewClass);
				}
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

			for (int i = 0; i < managingClasses.length; i++)
			{
				reversed[(managingClasses.length - i) - 1] = managingClasses[i];
			}
			try
			{
				if (viewClass != null)
				{
					TableUtils.dropViews(sqLiteDatabase, true, viewClass);
				}
				TableUtils.dropTables(sqLiteDatabase, true, reversed);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	/*protected static final String TABLE_NAME = "foo";
	protected static final FieldType[] noFieldTypes = new FieldType[0];

	protected <T, ID> void testType(Dao<T, ID> dao, T foo, Class<T> clazz, Object javaVal, Object defaultSqlVal,
			Object sqlArg, String defaultValStr, DataType dataType, String columnName, boolean isValidGeneratedType,
			boolean isAppropriateId, boolean isEscapedValue, boolean isPrimitive, boolean isSelectArgRequired,
			boolean isStreamType, boolean isComparable, boolean isConvertableId) throws Exception {
		DataPersister dataPersister = dataType.getDataPersister();
		SQLiteDatabase conn = connectionSource.getWritableDatabase();
		if (sqlArg != null) {
			assertEquals(defaultSqlVal.getClass(), sqlArg.getClass());
		}
		try {
			Cursor results = conn.rawQuery("select * from " + TABLE_NAME, null);
			assertTrue(results.moveToNext());

			int colNum = results.getColumnIndex(columnName);
			Field field = clazz.getDeclaredField(columnName);
			FieldType fieldType = FieldType.createFieldType(connectionSource, TABLE_NAME, field, clazz);
			Class<?>[] classes = fieldType.getDataPersister().getAssociatedClasses();
			if (classes.length > 0) {
				assertTrue(classes[0].isAssignableFrom(fieldType.getType()));
			}
			assertTrue(fieldType.getDataPersister().isValidForField(field));
			if (javaVal instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) javaVal,
						(byte[]) dataPersister.resultToJava(fieldType, results, colNum)));
			} else {
				Map<String, Integer> colMap = new HashMap<String, Integer>();
				colMap.put(columnName, colNum);
				Object result = fieldType.resultToJava(results, colMap);
				assertEquals(javaVal, result);
			}
			if (dataType == DataType.STRING_BYTES || dataType == DataType.BYTE_ARRAY
					|| dataType == DataType.SERIALIZABLE) {
				try {
					dataPersister.parseDefaultString(fieldType, "");
					fail("parseDefaultString should have thrown for " + dataType);
				} catch (SQLException e) {
					// expected
				}
			} else if (defaultValStr != null) {
				assertEquals(defaultSqlVal, dataPersister.parseDefaultString(fieldType, defaultValStr));
			}
			if (sqlArg == null) {
				// noop
			} else if (sqlArg instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) sqlArg, (byte[]) dataPersister.javaToSqlArg(fieldType, javaVal)));
			} else {
				assertEquals(sqlArg, dataPersister.javaToSqlArg(fieldType, javaVal));
			}
			assertEquals(isValidGeneratedType, dataPersister.isValidGeneratedType());
			assertEquals(isAppropriateId, dataPersister.isAppropriateId());
			assertEquals(isEscapedValue, dataPersister.isEscapedValue());
			assertEquals(isEscapedValue, dataPersister.isEscapedDefaultValue());
			assertEquals(isPrimitive, dataPersister.isPrimitive());
			assertEquals(isSelectArgRequired, dataPersister.isArgumentHolderRequired());
			assertEquals(isStreamType, dataPersister.isStreamType());
			assertEquals(isComparable, dataPersister.isComparable());
			if (isConvertableId) {
				assertNotNull(dataPersister.convertIdNumber(10));
			} else {
				assertNull(dataPersister.convertIdNumber(10));
			}
			List<T> list = dao.queryForAll().list();
			assertEquals(1, list.size());
			assertTrue(dao.objectsEqual(foo, list.get(0)));
			// if we have a value then look for it, floats don't find any results because of rounding issues
			if (javaVal != null && dataPersister.isComparable() && dataType != DataType.FLOAT
					&& dataType != DataType.FLOAT_OBJ) {
				// test for inline arguments
				list = dao.queryForMatching(foo);
				assertEquals(1, list.size());
				assertTrue(dao.objectsEqual(foo, list.get(0)));
				// test for SelectArg arguments
				list = dao.queryForMatchingArgs(foo);
				assertEquals(1, list.size());
				assertTrue(dao.objectsEqual(foo, list.get(0)));
			}
			if (dataType == DataType.STRING_BYTES || dataType == DataType.BYTE_ARRAY
					|| dataType == DataType.SERIALIZABLE) {
				// no converting from string to value
			} else {
				// test string conversion
				String stringVal = results.getString(colNum);
				Object convertedJavaVal = fieldType.convertStringToJavaField(stringVal, 0);
				assertEquals(javaVal, convertedJavaVal);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}

		}
	}*/

}
