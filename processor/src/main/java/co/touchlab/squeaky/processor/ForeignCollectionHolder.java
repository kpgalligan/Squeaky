package co.touchlab.squeaky.processor;


import co.touchlab.squeaky.field.ForeignCollectionField;

import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * Created by kgalligan on 7/26/15.
 */
public class ForeignCollectionHolder
{
	public final ForeignCollectionField foreignCollectionField;
	public final VariableElement variableElement;
	public final String variableName;
	public final String collectionTypeName;
	public final String foreignTypeName;

	public ForeignCollectionHolder(ForeignCollectionField foreignCollectionField, VariableElement variableElement, Messager messager)
	{
		this.foreignCollectionField = foreignCollectionField;
		this.variableElement = variableElement;
		variableName = variableElement.getSimpleName().toString();

		DeclaredType collectionType = (DeclaredType) variableElement.asType();

		collectionTypeName = collectionType.asElement().toString();

		List<? extends TypeMirror> genericTypes = collectionType.getTypeArguments();

		foreignTypeName = ((DeclaredType) genericTypes.get(0)).asElement().toString();

//		messager.printMessage(Diagnostic.Kind.ERROR, "foreign collection type: "+ DataTypeManager.findFieldClassname(variableElement));
	}
}
