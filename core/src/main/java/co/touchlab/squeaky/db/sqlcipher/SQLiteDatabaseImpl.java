package co.touchlab.squeaky.db.sqlcipher;

import android.content.ContentValues;
import android.database.Cursor;

import android.database.sqlite.SQLiteException;
import co.touchlab.squeaky.dao.Query;
import co.touchlab.squeaky.db.SQLiteDatabase;
import co.touchlab.squeaky.db.SQLiteStatement;
import net.sqlcipher.DatabaseUtils;

import java.sql.SQLException;

/**
 * Created by kgalligan on 11/17/15.
 */
public class SQLiteDatabaseImpl implements SQLiteDatabase
{
	private final net.sqlcipher.database.SQLiteDatabase db;

	public SQLiteDatabaseImpl(net.sqlcipher.database.SQLiteDatabase db)
	{
		this.db = db;
	}

	@Override
	public SQLiteStatement compileStatement(String sql) throws SQLiteException
	{
		return new SQLiteStatementImpl(db.compileStatement(sql));
	}

	@Override
	public Cursor rawQuery(String sql, String[] args)
	{
		return db.rawQuery(sql, args);
	}

	@Override
	public int update(String tableName, ContentValues vals, String where, String[] args) throws SQLiteException
	{
		return db.update(tableName, vals, where, args);
	}

	@Override
	public int delete(String tableName, String where, String[] args)
	{
		return db.delete(tableName, where, args);
	}

	@Override
	public long countOf(String tableName)
	{
		return DatabaseUtils.queryNumEntries(db, tableName);
	}

	@Override
	public long countOf(Query where) throws SQLException
	{
		return DatabaseUtils.longForQuery(db, "select count(*) from " + where.getFromStatement(true) + " where " + where.getWhereStatement(true), where.getParameters());
	}

	@Override
	public long longForQuery(String query, String... arguments)
	{
		return DatabaseUtils.longForQuery(db, query, arguments);
	}

	@Override
	public void execSQL(String sql)
	{
		db.execSQL(sql);
	}

	@Override
	public void beginTransaction()
	{
		db.beginTransaction();
	}

	@Override
	public void setTransactionSuccessful()
	{
		db.setTransactionSuccessful();
	}

	@Override
	public void endTransaction()
	{
		db.endTransaction();
	}
}
