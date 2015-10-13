package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.stmt.JoinAlias;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by kgalligan on 9/9/15.
 */
public interface Queryable<T>
{
	String getWhereStatement(boolean joinsAllowed) throws SQLException;

	Dao.QueryModifiers<T> query() throws SQLException;

	Queryable<T> reset();

	Queryable<T> eq(String columnFieldName, Object value) throws SQLException;

	Queryable<T> eq(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException;

	Queryable<T> gt(String columnFieldName, Object value) throws SQLException;

	Queryable<T> gt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException;

	Queryable<T> ge(String columnFieldName, Object value) throws SQLException;

	Queryable<T> ge(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException;

	Queryable<T> lt(String columnFieldName, Object value) throws SQLException;

	Queryable<T> lt(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException;

	Queryable<T> le(String columnFieldName, Object value) throws SQLException;

	Queryable<T> le(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException;

	Queryable<T> like(String columnFieldName, Object value) throws SQLException;

	Queryable<T> like(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException;

	Queryable<T> ne(String columnFieldName, Object value) throws SQLException;

	Queryable<T> ne(JoinAlias joinAlias, String columnFieldName, Object value) throws SQLException;

	Queryable<T> in(String columnFieldName, Iterable<?> objects) throws SQLException;

	Queryable<T> in(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException;

	Queryable<T> notIn(String columnFieldName, Iterable<?> objects) throws SQLException;

	Queryable<T> notIn(JoinAlias joinAlias, String columnFieldName, Iterable<?> objects) throws SQLException;

	Queryable<T> in(String columnFieldName, Object... objects) throws SQLException;

	Queryable<T> in(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException;

	Queryable<T> notIn(String columnFieldName, Object... objects) throws SQLException;

	Queryable<T> notIn(JoinAlias joinAlias, String columnFieldName, Object... objects) throws SQLException;

	Queryable<T> between(String columnFieldName, Object low, Object high) throws SQLException;

	Queryable<T> between(JoinAlias joinAlias, String columnFieldName, Object low, Object high) throws SQLException;

	Queryable<T> isNull(String columnFieldName) throws SQLException;

	Queryable<T> isNull(JoinAlias joinAlias, String columnFieldName) throws SQLException;

	Queryable<T> isNotNull(String columnFieldName) throws SQLException;

	Queryable<T> isNotNull(JoinAlias joinAlias, String columnFieldName) throws SQLException;

	Queryable<T> and() throws SQLException;

	Queryable<T> or() throws SQLException;

	Queryable<T> not() throws SQLException;

	Queryable<T> end() throws SQLException;
}
