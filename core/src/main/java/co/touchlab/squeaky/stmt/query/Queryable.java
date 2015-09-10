package co.touchlab.squeaky.stmt.query;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by kgalligan on 9/9/15.
 */
public interface Queryable<T>
{
	String getStatement() throws SQLException;

	List<T> query() throws SQLException;
	
	List<T> query(String orderBy)throws SQLException;
	
	Queryable<T> reset() ;

	Queryable<T> eq(String columnFieldName, Object value) throws SQLException;
	
	Queryable<T> gt(String columnFieldName, Object value) throws SQLException;
	
	Queryable<T> ge(String columnFieldName, Object value) throws SQLException;
	
	Queryable<T> lt(String columnFieldName, Object value) throws SQLException;
	
	Queryable<T> le(String columnFieldName, Object value) throws SQLException;
	
	Queryable<T> like(String columnFieldName, Object value) throws SQLException;
	
	Queryable<T> ne(String columnFieldName, Object value) throws SQLException;

	Queryable<T> in(String columnFieldName, Iterable<?> objects) throws SQLException;
	
	Queryable<T> notIn(String columnFieldName, Iterable<?> objects) throws SQLException;
	
	Queryable<T> in(String columnFieldName, Object... objects) throws SQLException;
	
	Queryable<T> notIn(String columnFieldName, Object... objects) throws SQLException;
	
	Queryable<T> between(String columnFieldName, Object low, Object high) throws SQLException;
	
	Queryable<T> isNull(String columnFieldName) throws SQLException;
	
	Queryable<T> isNotNull(String columnFieldName) throws SQLException;
	
	Queryable<T> and()throws SQLException;
	
	Queryable<T> or()throws SQLException;

	Queryable<T> not()throws SQLException;

	Queryable<T> end() throws SQLException;
}
