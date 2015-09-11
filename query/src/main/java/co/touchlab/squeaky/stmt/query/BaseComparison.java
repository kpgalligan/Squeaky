package co.touchlab.squeaky.stmt.query;

import co.touchlab.squeaky.dao.ModelDao;
import co.touchlab.squeaky.dao.SqueakyContext;
import co.touchlab.squeaky.field.FieldType;
import co.touchlab.squeaky.stmt.ArgumentHolder;
import co.touchlab.squeaky.stmt.SelectArg;
import co.touchlab.squeaky.table.GeneratedTableMapper;
import co.touchlab.squeaky.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal base class for all comparison operations.
 * 
 * @author graywatson
 */
abstract class BaseComparison implements Comparison {

	private static final String NUMBER_CHARACTERS = "0123456789.-+";
	protected final FieldType fieldType;
	private final Object value;

	protected BaseComparison(FieldType fieldType, Object value, boolean isComparison)
			throws SQLException {
		if (isComparison && fieldType != null && !fieldType.isComparable()) {
			throw new SQLException("Field '" + fieldType.getColumnName() + "' is of data type " + fieldType.getDataPersister()
					+ " which can not be compared");
		}
		this.fieldType = fieldType;
		this.value = value;
	}

	public abstract void appendOperation(StringBuilder sb);

	public void appendSql(SqueakyContext squeakyContext, String tableName, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		if (tableName != null) {
			TableUtils.appendEscapedEntityName(sb, tableName);
			sb.append('.');
		}
		TableUtils.appendEscapedEntityName(sb, fieldType.getColumnName());
		sb.append(' ');
		appendOperation(sb);
		// this needs to call appendValue (not appendArgOrValue) because it may be overridden
		appendValue(squeakyContext, sb, argList);
	}

	public String getColumnName() {
		return fieldType.getColumnName();
	}

	public void appendValue(SqueakyContext squeakyContext, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		appendArgOrValue(squeakyContext, fieldType, sb, argList, value);
	}

	/**
	 * Append to the string builder either a {@link ArgumentHolder} argument or a value object.
	 */
	protected void appendArgOrValue(SqueakyContext squeakyContext, FieldType fieldType, StringBuilder sb,
			List<ArgumentHolder> argList, Object argOrValue) throws SQLException {
		boolean appendSpace = true;
		if (argOrValue == null) {
			throw new SQLException("argument for '" + fieldType.getFieldName() + "' is null");
		} else if (argOrValue instanceof ArgumentHolder) {
			sb.append('?');
			ArgumentHolder argHolder = (ArgumentHolder) argOrValue;
			argHolder.setMetaInfo(fieldType.getColumnName(), fieldType);
			argList.add(argHolder);
		} else if (fieldType.isArgumentHolderRequired()) {
			sb.append('?');
			ArgumentHolder argHolder = new SelectArg();
			argHolder.setMetaInfo(fieldType.getColumnName(), fieldType);
			// conversion is done when the getValue() is called
			argHolder.setValue(argOrValue);
			argList.add(argHolder);
		}
		else if (fieldType.isForeign() && fieldType.getFieldType().isAssignableFrom(argOrValue.getClass())) {
			GeneratedTableMapper generatedTableMapper = ((ModelDao) squeakyContext.getDao(fieldType.getFieldType())).getGeneratedTableMapper();
			Object idVal = generatedTableMapper.extractId(argOrValue);
			FieldType idFieldType = generatedTableMapper.getTableConfig().idField;
			appendArgOrValue(squeakyContext, idFieldType, sb, argList, idVal);
			// no need for the space since it was done in the recursion
			appendSpace = false;
		} else if (fieldType.isEscapedValue()) {
			TableUtils.appendEscapedWord(sb, fieldType.convertJavaFieldToSqlArgValue(argOrValue).toString());
		} else if (fieldType.isForeign()) {
			/*
			 * I'm not entirely sure this is correct. This is trying to protect against someone trying to pass an object
			 * into a comparison with a foreign field. Typically if they pass the same field type, then ORMLite will
			 * extract the ID of the foreign.
			 */
			String value = fieldType.convertJavaFieldToSqlArgValue(argOrValue).toString();
			if (value.length() > 0) {
				if (NUMBER_CHARACTERS.indexOf(value.charAt(0)) < 0) {
					throw new SQLException("Foreign field " + fieldType
							+ " does not seem to be producing a numerical value '" + value
							+ "'. Maybe you are passing the wrong object to comparison: " + this);
				}
			}
			sb.append(value);
		} else {
			// numbers can't have quotes around them in derby
			sb.append(fieldType.convertJavaFieldToSqlArgValue(argOrValue));
		}
		if (appendSpace) {
			sb.append(' ');
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(fieldType.getColumnName()).append(' ');
		appendOperation(sb);
		sb.append(' ');
		sb.append(value);
		return sb.toString();
	}
}
