package co.touchlab.squeaky.dao;

import co.touchlab.squeaky.field.FieldType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kgalligan on 10/15/15.
 */
public class DaoHelper
{
	//parent[child[grandDad]],parentLazy
	public static Dao.ForeignRefresh[] refresh(String refreshTree)
	{
		List<String> parsedFields = new ArrayList<>();

		int bracketCount = 0;
		int lastBreak = 0;
		for(int i=0; i<refreshTree.length(); i++)
		{
			if(bracketCount == 0 && refreshTree.charAt(i) == ',')
			{
				String fieldTree = refreshTree.substring(lastBreak, i);
				parsedFields.add(fieldTree.trim());
				lastBreak = i+1;
			}
			else if(refreshTree.charAt(i) == '[')
			{
				bracketCount++;
			}
			else if(refreshTree.charAt(i) == ']')
			{
				bracketCount--;
			}
		}

		if(bracketCount != 0)
			throw new RuntimeException("Bad refresh format "+ refreshTree);

		parsedFields.add(refreshTree.substring(lastBreak).trim());

		List<Dao.ForeignRefresh> refreshs = new ArrayList<>(parsedFields.size());
		for (String parsedField : parsedFields)
		{
			if(parsedField.contains("["))
			{
				int startIndex = parsedField.indexOf('[');
				refreshs.add(new Dao.ForeignRefresh(parsedField.substring(0, startIndex),
						refresh(parsedField.substring(startIndex + 1, parsedField.length() - 1))
				));
			}
			else
			{
				refreshs.add(new Dao.ForeignRefresh(parsedField));
			}
		}

		return refreshs.toArray(new Dao.ForeignRefresh[refreshs.size()]);
	}

	public static class ValBuilder
	{
		Map<String, Object> vals = new HashMap<>();
		public ValBuilder add(String col, Object val)
		{
			vals.put(col, val);
			return this;
		}

		public Map<String, Object> build()
		{
			return vals;
		}
	}

	public static ValBuilder vals()
	{
		return new ValBuilder();
	}

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
