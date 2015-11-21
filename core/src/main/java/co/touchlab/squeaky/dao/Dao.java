package co.touchlab.squeaky.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Public data access interface.  Used for reading and writing objects.
 *
 * @author graywatson, kgalligan
 */
public interface Dao<T>
{
	class ForeignRefresh
	{
		public final String field;
		public final ForeignRefresh[] refreshFields;

		public ForeignRefresh(String field)
		{
			this(field, null);
		}

		public ForeignRefresh(String field, ForeignRefresh[] refreshFields)
		{
			this.field = field;
			this.refreshFields = refreshFields;
		}
	}

	interface QueryModifiers<T>
	{
		QueryModifiers<T> orderBy(String s);
		QueryModifiers<T> limit(Integer i);
		QueryModifiers<T> offset(Integer i);
		QueryModifiers<T> foreignRefreshMap(ForeignRefresh[] foreignRefreshMap);
		List<T> list() throws SQLException;
	}

	T queryForId(Object id) throws SQLException;

	QueryModifiers<T> queryForAll() throws SQLException;

	QueryModifiers<T> queryForEq(String fieldName, Object value) throws SQLException;

	QueryModifiers<T> queryForFieldValues(Map<String, Object> fieldValues) throws SQLException;

	QueryModifiers<T> query(Query where) throws SQLException;

	QueryModifiers<T> query(String where, String[] args) throws SQLException;

	void create(T data) throws SQLException;

	T createIfNotExists(T data) throws SQLException;

	void createOrUpdate(T data) throws SQLException;

	void update(T data) throws SQLException;

	int updateId(T data, Object newId) throws SQLException;

	int update(Query where, Map<String, Object> valueMap) throws SQLException;

	void refresh(T data) throws SQLException;

	void refresh(T data, ForeignRefresh[] foreignRefreshMap) throws SQLException;

	int delete(T data) throws SQLException;

	int deleteById(Object id) throws SQLException;

	int delete(Collection<T> datas) throws SQLException;

	int deleteIds(Collection<Object> ids) throws SQLException;

	int delete(Query preparedDelete) throws SQLException;

	CloseableIterator<T> iterator() throws SQLException;

	CloseableIterator<T> iterator(Query where) throws SQLException;

	/**
	 * Perform a raw query that returns a single value (usually an aggregate function like MAX or COUNT). If the query
	 * does not return a single long value then it will throw a SQLException.
	 */
	long queryRawValue(String query, String... arguments) throws SQLException;

	/**
	 * Return the string version of the object with each of the known field values shown. Useful for testing and
	 * debugging.
	 *
	 * @param data The data item for which we are returning the toString information.
	 */
	String objectToString(T data) throws SQLException;

	/**
	 * Return true if the two parameters are equal. This checks each of the fields defined in the database to see if
	 * they are equal. Useful for testing and debugging.
	 *
	 * @param data1 One of the data items that we are checking for equality.
	 * @param data2 The other data item that we are checking for equality.
	 */
	boolean objectsEqual(T data1, T data2) throws SQLException;

	/**
	 * Returns the ID from the data parameter passed in. This is used by some of the internal queries to be able to
	 * search by id.
	 */
	Object extractId(T data) throws SQLException;

	void fillForeignCollection(T data, String fieldName) throws SQLException;

	/**
	 * Returns the class of the DAO. This is used by internal query operators.
	 */
	Class<T> getDataClass();

	/**
	 * Returns true if we can call update on this class. This is used most likely by folks who are extending the base
	 * dao classes.
	 */
	boolean isUpdatable();

	long countOf() throws SQLException;

	long countOf(Query preparedQuery) throws SQLException;

	/**
	 * Returns true if an object exists that matches this ID otherwise false.
	 */
	boolean idExists(Object id) throws SQLException;

	void registerObserver(DaoObserver observer);

	void unregisterObserver(DaoObserver observer);

	/**
	 * Notify any registered {@link DaoObserver}s that the underlying data may have changed. This is done automatically
	 * when using {@link #create(Object)}, {@link #update(Object)}, or {@link #delete(Object)} type methods. Batch
	 * methods will be notified once at the end of the batch, not for every statement in the batch.
	 */
	void notifyChanges();

	/**
	 * Defines a class that can observe changes to entities managed by the DAO.
	 */
	interface DaoObserver
	{
		/**
		 * Called when entities possibly have changed in the DAO. This can be used to detect changes to the entities
		 * managed by the DAO so that views can be updated.
		 */
		void onChange();
	}

	/**
	 * Helper that matches all for query
	 * @return
	 */
	Query all();
}
