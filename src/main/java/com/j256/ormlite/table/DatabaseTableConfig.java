package com.j256.ormlite.table;

import com.j256.ormlite.field.FieldType;

import java.lang.reflect.Constructor;

/**
 * Database table configuration information either supplied by Spring or direct Java wiring or from a
 * {@link DatabaseTable} annotation.
 * 
 * @author graywatson
 */
public class DatabaseTableConfig<T> {

	private final Class<T> dataClass;
	private final String tableName;
	private final FieldType[] fieldTypes;
	private Constructor<T> constructor;

	/**
	 * Setup a table config associated with the dataClass, table-name, and field configurations.
	 */
	public DatabaseTableConfig(Class<T> dataClass, String tableName, FieldType[] fieldTypes) {
		this.dataClass = dataClass;
		this.tableName = tableName;
		this.fieldTypes = fieldTypes;
	}

	public Class<T> getDataClass() {
		return dataClass;
	}

	public String getTableName() {
		return tableName;
	}

	/**
	 * Return the field types associated with this configuration.
	 */
	public FieldType[] getFieldTypes() {
		return fieldTypes;
	}

	/**
	 * Return the constructor for this class. If not constructor has been set on the class then it will be found on the
	 * class through reflection.
	 */
	public Constructor<T> getConstructor() {
		if (constructor == null) {
			constructor = findNoArgConstructor(dataClass);
		}
		return constructor;
	}
/*
	public static <T> String extractTableName(Class<T> clazz) {
		DatabaseTable databaseTable = clazz.getAnnotation(DatabaseTable.class);
		String name = null;
		if (databaseTable != null && databaseTable.tableName() != null && databaseTable.tableName().length() > 0) {
			name = databaseTable.tableName();
		}
		if (name == null && javaxPersistenceConfigurer != null) {
			name = javaxPersistenceConfigurer.getEntityName(clazz);
		}
		if (name == null) {
			// if the name isn't specified, it is the class name lowercased
			name = clazz.getSimpleName().toLowerCase();
		}
		return name;
	}*/

	/**
	 * Locate the no arg constructor for the class.
	 */
	public static <T> Constructor<T> findNoArgConstructor(Class<T> dataClass) {
		Constructor<T>[] constructors;
		try {
			@SuppressWarnings("unchecked")
			Constructor<T>[] consts = (Constructor<T>[]) dataClass.getDeclaredConstructors();
			// i do this [grossness] to be able to move the Suppress inside the method
			constructors = consts;
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't lookup declared constructors for " + dataClass, e);
		}
		for (Constructor<T> con : constructors) {
			if (con.getParameterTypes().length == 0) {
				if (!con.isAccessible()) {
					try {
						con.setAccessible(true);
					} catch (SecurityException e) {
						throw new IllegalArgumentException("Could not open access to constructor for " + dataClass);
					}
				}
				return con;
			}
		}
		if (dataClass.getEnclosingClass() == null) {
			throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass);
		} else {
			throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass
					+ ".  Missing static on inner class?");
		}
	}
}
