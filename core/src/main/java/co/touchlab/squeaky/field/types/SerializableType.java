package co.touchlab.squeaky.field.types;

import android.database.Cursor;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.field.SqlType;
import co.touchlab.squeaky.misc.IOUtils;
import co.touchlab.squeaky.misc.SqlExceptionUtil;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Persists an unknown Java Object that is {@link Serializable}.
 *
 * @author graywatson
 */
public class SerializableType extends BaseDataType
{

	private static final SerializableType singleTon = new SerializableType();

	public static SerializableType getSingleton()
	{
		return singleTon;
	}

	private SerializableType()
	{
		/*
		 * NOTE: Serializable class should _not_ be in the list because _everything_ is serializable and we want to
		 * force folks to use DataType.SERIALIZABLE -- especially for forwards compatibility.
		 */
		super(SqlType.SERIALIZABLE);
	}

	/**
	 * Here for others to subclass.
	 */
	protected SerializableType(SqlType sqlType, Class<?>[] classes)
	{
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException
	{
		throw new SQLException("Default values for serializable types are not supported");
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, Cursor results, int columnPos) throws SQLException
	{
		return results.getBlob(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException
	{
		byte[] bytes = (byte[]) sqlArg;
		ObjectInputStream objInStream = null;
		try
		{
			objInStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
			return objInStream.readObject();
		}
		catch (Exception e)
		{
			throw SqlExceptionUtil.create("Could not read serialized object from byte array: " + Arrays.toString(bytes)
					+ "(len " + bytes.length + ")", e);
		}
		finally
		{
			// we do this to give GC a hand with ObjectInputStream reference maps
			IOUtils.closeQuietly(objInStream);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) throws SQLException
	{
		ObjectOutputStream objOutStream = null;
		try
		{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			objOutStream = new ObjectOutputStream(outStream);
			objOutStream.writeObject(obj);
			objOutStream.close();
			objOutStream = null;
			return outStream.toByteArray();
		}
		catch (Exception e)
		{
			throw SqlExceptionUtil.create("Could not write serialized object to byte array: " + obj, e);
		}
		finally
		{
			// we do this to give GC a hand with ObjectOutputStream reference maps
			IOUtils.closeQuietly(objOutStream);
		}
	}

	@Override
	public boolean isComparable()
	{
		return false;
	}
}
