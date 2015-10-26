package co.touchlab.squeaky.dao;

import co.touchlab.squeaky.field.FieldType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kgalligan on 10/15/15.
 */
public class DaoHelper
{
	/*public static class ForeignRefreshBuilder
	{
		private ForeignRefreshBuilder parent;
		private List<ForeignRefreshBuilder> fields = new ArrayList<>();
		private String field;

		ForeignRefreshBuilder(String field, ForeignRefreshBuilder parent)
		{
			this.field = field;
			this.parent = parent;
		}

		public ForeignRefreshBuilder add(String field)
		{
			fields.add(new ForeignRefreshBuilder(field, this));
			return this;
		}

		public ForeignRefreshBuilder addChildFields()
		{
			return fields.get(fields.size() - 1);
		}

		public ForeignRefreshBuilder doneChildFields()
		{
			return parent;
		}

		public Dao.ForeignRefresh[] build()
		{

		}

		private Dao.ForeignRefresh[] internalBuild()
		{

		}

	}

	public static ForeignRefreshBuilder foreignRefreshBuilder()
	{
		return new ForeignRefreshBuilder(null);
	}*/

	public static Dao.ForeignRefresh findRefresh(Dao.ForeignRefresh[] foreignRefreshs, String fieldName)
	{
		for (Dao.ForeignRefresh foreignRefresh : foreignRefreshs)
		{
			if(foreignRefresh.field.equals(fieldName))
				return foreignRefresh;
		}

		return null;
	}

	public static Dao.ForeignRefresh[] fillForeignRefreshMap(SqueakyContext squeakyContext, Class type, int count)throws SQLException
	{
		FieldType[] fieldTypes = squeakyContext.getGeneratedTableMapper(type).getTableConfig().getFieldTypes();
		return fillForeignRefreshMap(squeakyContext, fieldTypes, count);
	}

	public static Dao.ForeignRefresh[] fillForeignRefreshMap(SqueakyContext squeakyContext, FieldType[] fieldTypes, int count) throws SQLException
	{
		List<Dao.ForeignRefresh> foreignRefreshs = new ArrayList<>();

		for (FieldType fieldType : fieldTypes)
		{
			if(fieldType.isForeign() && fieldType.isForeignAutoRefresh())
			{
				foreignRefreshs.add(new Dao.ForeignRefresh(fieldType.getFieldName(), fillForeignRefreshMap(squeakyContext, fieldType, count-1)));
			}
		}

		return foreignRefreshs.size() == 0 ? null : foreignRefreshs.toArray(new Dao.ForeignRefresh[foreignRefreshs.size()]);
	}

	private static Dao.ForeignRefresh[] fillForeignRefreshMap(SqueakyContext squeakyContext, FieldType parentType, int count) throws SQLException
	{
		if(count == 0)
			return null;

		FieldType[] fieldTypes = squeakyContext.getGeneratedTableMapper(parentType.getFieldType()).getTableConfig().getFieldTypes();
		return fillForeignRefreshMap(squeakyContext, fieldTypes, count);
	}
}
