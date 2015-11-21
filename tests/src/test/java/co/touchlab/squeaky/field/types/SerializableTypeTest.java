package co.touchlab.squeaky.field.types;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.DatabaseField;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.table.DatabaseTable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SerializableTypeTest extends BaseTypeTest
{

	private static final String SERIALIZABLE_COLUMN = "serializable";
	private static final String BYTE_COLUMN = "byteField";
	private SimpleHelper helper;

	@Before
	public void before()
	{
		helper = getHelper();
	}

	@After
	public void after()
	{
		helper.close();
	}

	@Test
	public void testSerializable() throws Exception
	{
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable> dao = helper.getDao(clazz);
		Integer val = 1331333131;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
		objOutStream.writeObject(val);
		byte[] sqlArg = outStream.toByteArray();
		String valStr = val.toString();
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = val;
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	@Test
	public void testSerializableNull() throws Exception
	{
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable> dao = helper.getDao(clazz);
		LocalSerializable foo = new LocalSerializable();
		dao.create(foo);
		assertTrue(EqualsBuilder.reflectionEquals(foo, dao.queryForAll().list().get(0)));
	}

	/*@Test
	public void testSerializableNoValue() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable> dao = helper.getDao(clazz);
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = null;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt =
					conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			FieldType fieldType =
					FieldType.createFieldType(connectionSource, TABLE_NAME,
							clazz.getDeclaredField(SERIALIZABLE_COLUMN), clazz);
			assertNull(DataType.SERIALIZABLE.getDataPersister().resultToJava(fieldType, results,
					results.findColumn(SERIALIZABLE_COLUMN)));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}*/

	/*@Test(expected = SQLException.class)
	public void testSerializableInvalidResult() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray> dao = createDao(clazz, true);
		LocalByteArray foo = new LocalByteArray();
		foo.byteField = new byte[] { 1, 2, 3, 4, 5 };
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt =
					conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			FieldType fieldType =
					FieldType.createFieldType(connectionSource, TABLE_NAME,
							LocalSerializable.class.getDeclaredField(SERIALIZABLE_COLUMN), LocalSerializable.class);
			DataType.SERIALIZABLE.getDataPersister().resultToJava(fieldType, results, results.findColumn(BYTE_COLUMN));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}*/

	@Test(expected = SQLException.class)
	public void testSerializableParseDefault() throws Exception
	{
		DataType.SERIALIZABLE.getDataPersister().parseDefaultString(null, null);
	}

	/*@Test
	public void testUpdateBuilderSerializable() throws Exception {
		Dao<SerializedUpdate> dao = createDao(SerializedUpdate.class, true);
		SerializedUpdate foo = new SerializedUpdate();
		SerializedField serialized1 = new SerializedField("wow");
		foo.serialized = serialized1;
		assertEquals(1, dao.create(foo));

		SerializedUpdate result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNotNull(result.serialized);
		assertEquals(serialized1.foo, result.serialized.foo);

		// update with dao.update
		SerializedField serialized2 = new SerializedField("zip");
		foo.serialized = serialized2;
		assertEquals(1, dao.update(foo));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNotNull(result.serialized);
		assertEquals(serialized2.foo, result.serialized.foo);

		// update with UpdateBuilder
		SerializedField serialized3 = new SerializedField("crack");
		UpdateBuilder<SerializedUpdate> ub = dao.updateBuilder();
		ub.updateColumnValue(SerializedUpdate.SERIALIZED_FIELD_NAME, serialized3);
		ub.where().idEq(foo.id);
		assertEquals(1, ub.update());

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNotNull(result.serialized);
		assertEquals(serialized3.foo, result.serialized.foo);
	}*/

	@Test
	public void testCoverage()
	{
		new SerializableType(SqlType.SERIALIZABLE, new Class[0]);
	}

	/*@Test
	public void testSerializedNotSerializable() throws Exception {
		createDao(SerializedCollection.class, false);
	}*/

	/* ------------------------------------------------------------------------------------ */

	@DatabaseTable
	protected static class LocalSerializable
	{
		@DatabaseField(columnName = SERIALIZABLE_COLUMN, dataType = DataType.SERIALIZABLE)
		Integer serializable;
	}

	/*protected static class LocalSerializableType extends SerializableType {

		private static LocalSerializableType singleton;

		public LocalSerializableType() {
			super(SqlType.SERIALIZABLE, new Class<?>[0]);
		}

		public static LocalSerializableType getSingleton() {
			if (singleton == null) {
				singleton = new LocalSerializableType();
			}
			return singleton;
		}

		@Override
		public boolean isValidForField(Field field) {
			return Collection.class.isAssignableFrom(field.getType());
		}
	}*/

	private SimpleHelper getHelper()
	{
		return createHelper(
				LocalSerializable.class
		);
	}
}
