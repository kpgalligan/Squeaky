package co.touchlab.squeaky.sql;

import co.touchlab.squeaky.dao.ModelDao;
import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.table.GeneratedTableMapper;
import co.touchlab.squeaky.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by kgalligan on 9/26/15.
 */
public class SqlHelper
{
	public static void appendWhereClauseBody(StringBuilder sb, String tablePrefix, String operation, FieldType fieldType)
	{
		sb.append(tablePrefix).append('.');
		TableUtils.appendEscapedEntityName(sb, fieldType.getColumnName());
		sb.append(' ').append(operation);
	}

	public static void appendArgOrValue(SqueakyContext squeakyContext, FieldType fieldType, List<String> params, Object argOrValue) throws SQLException
	{
		if (argOrValue == null)
		{
			throw new SQLException("argument for '" + fieldType.getFieldName() + "' is null");
		} else if (fieldType.isForeign() && fieldType.getFieldType().isAssignableFrom(argOrValue.getClass()))
		{
			GeneratedTableMapper generatedTableMapper = ((ModelDao) squeakyContext.getDao(fieldType.getFieldType())).getGeneratedTableMapper();
			Object idVal = generatedTableMapper.extractId(argOrValue);
			FieldType idFieldType = generatedTableMapper.getTableConfig().idField;
			appendArgOrValue(squeakyContext, idFieldType, params, idVal);
		} else
		{
			params.add(fieldType.convertJavaFieldToSqlArgValue(argOrValue).toString());
		}
	}
}
