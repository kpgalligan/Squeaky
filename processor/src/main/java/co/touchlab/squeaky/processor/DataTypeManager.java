package co.touchlab.squeaky.processor;

import co.touchlab.squeaky.field.DataPersister;
import co.touchlab.squeaky.field.DataType;
import co.touchlab.squeaky.field.types.EnumStringType;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kgalligan on 5/26/15.
 */
public class DataTypeManager
{
	private static final DataPersister DEFAULT_ENUM_PERSISTER = EnumStringType.getSingleton();

	private static final Map<String, DataType> builtInMap;
//	private static List<DataPersister> registeredPersisters = null;

	static {
		// add our built-in persisters
		builtInMap = new HashMap<String, DataType>();
		for (DataType dataType : DataType.values()) {
			DataPersister persister = dataType.getDataPersister();
			if (persister != null) {
				for (Class<?> clazz : persister.getAssociatedClasses()) {
					builtInMap.put(clazz.getName(), dataType);
				}
				String[] associatedClassNames = persister.getAssociatedClassNames();
				if (associatedClassNames != null) {
					for (String className : persister.getAssociatedClassNames()) {
						builtInMap.put(className, dataType);
					}
				}
			}
		}
	}

	private static final String JAVA_LANG_ENUM = "java.lang.Enum<?>";

	private static String getCanonicalTypeName(DeclaredType declaredType) {
		List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
		if (!typeArguments.isEmpty()) {
			StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
			typeString.append('<');
			for (int i = 0; i < typeArguments.size(); i++) {
				if (i > 0) {
					typeString.append(',');
				}
				typeString.append('?');
			}
			typeString.append('>');

			return typeString.toString();
		} else {
			return declaredType.toString();
		}
	}

	/**
	 * Lookup the data-type associated with the class.
	 *
	 * @return The associated data-type interface or null if none found.
	 */
	public static DataType lookupForField(Element variableElement, Messager messager, Types types) {

		//Check if is enum
		/*if(variableElement instanceof DeclaredType)
		{
			((DeclaredType)variableElement).getEnclosingType().
		}*/
		Element fieldTypeElement = types.asElement(variableElement.asType());

		boolean isTypeElement = false;
		if (fieldTypeElement instanceof TypeElement) {
			isTypeElement = true;
			TypeElement typeElement = (TypeElement) fieldTypeElement;
			TypeMirror superclass = typeElement.getSuperclass();
			if (superclass instanceof DeclaredType) {
				DeclaredType superclassDeclaredType = (DeclaredType) superclass;

				if (JAVA_LANG_ENUM.equals(getCanonicalTypeName(superclassDeclaredType))) {
					return DataType.ENUM_STRING;
				}
			}
		}

		String fieldClassName = findFieldClassname(variableElement);

		// look it up in our built-in map by class
		DataType dataType = builtInMap.get(fieldClassName);
		if (dataType != null) {
			return dataType;
		}

		/*
		 * Special case for enum types. We can't put this in the registered persisters because we want people to be able
		 * to override it.
		 */
		/*if (field.getType().isEnum()) {
			return DEFAULT_ENUM_PERSISTER;
		} else {
			*//*
			 * Serializable classes return null here because we don't want them to be automatically configured for
			 * forwards compatibility with future field types that happen to be Serializable.
			 *//*
			return null;
		}*/
		throw new RuntimeException("Couldn't find data type for "+ fieldClassName +"/"+ variableElement.getKind() +"/isTypeElement: "+ isTypeElement);
	}

	public static boolean isPrimitive(Element fieldElement)
	{
		return fieldElement.asType().getKind().isPrimitive();
	}

	public static String findFieldClassname(Element fieldElement)
	{
		TypeMirror typeMirror = fieldElement.asType();
		TypeKind kind = typeMirror.getKind();
		if(kind.isPrimitive())
		{
			Class primitiveClass = null;
			switch (kind)
			{
				case BOOLEAN:
					primitiveClass = boolean.class;
					break;
				case DOUBLE:
					primitiveClass = double.class;
					break;
				case FLOAT:
					primitiveClass = float.class;
					break;
				case INT:
					primitiveClass = int.class;
					break;
				case LONG:
					primitiveClass = long.class;
					break;
				case SHORT:
					primitiveClass = short.class;
					break;
				case BYTE:
					primitiveClass = byte.class;
					break;
				case CHAR:
					primitiveClass = char.class;
					break;
				default:
					throw new UnsupportedOperationException("Don't recognize type: "+ kind);
			}

			return primitiveClass.toString();
		}
		else
		{
			if(typeMirror instanceof ArrayType)
			{
//				ArrayType arrayType = (ArrayType) typeMirror;
//				arrayType.getComponentType().
				//TODO: See if this neds to be fixed.  Its bytes, though.
				return "byte[]";
			}
			else
			{
				DeclaredType declaredType = (DeclaredType) typeMirror;
				return ((TypeElement) declaredType.asElement()).getQualifiedName().toString();
			}
		}
	}
}
