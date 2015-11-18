package co.touchlab.squeaky.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import co.touchlab.squeaky.dao.Query;

import java.sql.SQLException;

/**
 * Created by kgalligan on 11/17/15.
 */
public interface SQLiteDatabase
{
	SQLiteStatement compileStatement(String sql)throws SQLiteException;
	Cursor rawQuery(String sql, String[] args);
	int update(String tableName, ContentValues vals, String where, String[] args)throws SQLiteException;
	int delete(String tableName, String where, String[] args);
	long countOf(String tableName);
	long countOf(Query where) throws SQLException;
	long longForQuery(String query, String... arguments);
	void execSQL(String sql);
	void beginTransaction();
	void setTransactionSuccessful();
	void endTransaction();
}
