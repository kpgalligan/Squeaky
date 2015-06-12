package com.j256.ormlite.stmt.mapped;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.GeneratedTableMapper;
import com.j256.ormlite.table.TableInfo;

import java.sql.SQLException;

/**
 * Abstract mapped statement for queries which handle the creating of a new object and the row mapping functionality.
 *
 * @author graywatson
 */
public abstract class BaseMappedQuery<T, ID> extends BaseMappedStatement<T, ID> implements GenericRowMapper<T>
{
	protected BaseMappedQuery(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes)
	{
		super(tableInfo, statement, argFieldTypes);
	}

	public T mapRow(DatabaseResults results) throws SQLException
	{
		GeneratedTableMapper<T, ID> generatedTableMapper = tableInfo.getGeneratedTableMapper();
		T filledRow = generatedTableMapper.createObject();
		generatedTableMapper.fillRow(filledRow, results);

		return filledRow;
/*

		// create our instance
		T instance = tableInfo.createObject();
		// populate its fields
		Object id = null;
		boolean foreignCollections = false;*/
//		for (FieldType fieldType : resultsFieldTypes) {
//			//TODO: foreign
//			/*if (fieldType.isForeignCollection()) {
//				foreignCollections = true;
//			} else*/ {
//				Object val = fieldType.resultToJava(results, colPosMap);
//				/*
//				 * This is pretty subtle. We introduced multiple foreign fields to the same type which use the {@link
//				 * ForeignCollectionField} foreignColumnName field. The bug that was created was that all the fields
//				 * were then set with the parent class. Only the fields that have a matching id value should be set to
//				 * the parent. We had to add the val.equals logic.
//				 */
//				//TODO: foreign
//				/*if (val != null && parent != null && fieldType.getField().getType() == parent.getClass()
//						&& val.equals(parentId)) {
//					fieldType.assignField(instance, parent, true, objectCache);
//				} else*/ {
//					fieldType.assignField(instance, val, false);
//				}
//				if (fieldType == idField) {
//					id = val;
//				}
//			}
//		}
		//TODO: foreign
		/*if (foreignCollections) {
			// go back and initialize any foreign collections
			for (FieldType fieldType : resultsFieldTypes) {
				if (fieldType.isForeignCollection()) {
					BaseForeignCollection<?, ?> collection = fieldType.buildForeignCollection(instance, id);
					if (collection != null) {
						fieldType.assignField(instance, collection, false, objectCache);
					}
				}
			}
		}*/
		// if we have a cache and we have an id then add it to the cache
		/*if (objectCache != null && id != null) {
			objectCache.put(clazz, id, instance);
		}
		if (columnPositions == null) {
			columnPositions = colPosMap;
		}
		return instance;*/
	}

	/**
	 * If we have a foreign collection object then this sets the value on the foreign object in the class.
	 */
	/*public void setParentInformation(Object parent, Object parentId)
	{
		this.parent = parent;
		this.parentId = parentId;
	}*/
}
