/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Koen Vlaswinkel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package co.touchlab.squeaky.processor;

import android.database.Cursor;

import co.touchlab.squeaky.dao.Dao;
import co.touchlab.squeaky.dao.DaoHelper;
import co.touchlab.squeaky.dao.ModelDao;
import co.touchlab.squeaky.db.SQLiteStatement;
import co.touchlab.squeaky.field.*;
import co.touchlab.squeaky.table.*;
import com.google.common.base.Joiner;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

//import com.j256.ormlite.android.squeaky.Dao;

public class AnnotationProcessor extends AbstractProcessor
{
//    private static final int DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL = ForeignCollectionField.DEFAULT_MAX_EAGER_LEVEL;

	private Types typeUtils;
	private Filer filer;
	private Messager messager;

	private List<ClassName> baseClasses;
	private List<ClassName> generatedClasses;
	private Elements elementUtils;

	enum EntityType
	{
		Table, View, Query
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		try
		{
			return safeProcess(roundEnv);
		} catch (Exception e)
		{
			StringWriter sq = new StringWriter();
			PrintWriter printWriter = new PrintWriter(sq);
			e.printStackTrace(printWriter);
			File debugOut = new File("/Users/kgalligan/temp/apterror.txt");
			try
			{
				FileWriter fileWriter = new FileWriter(debugOut);
				fileWriter.append(sq.toString());
				fileWriter.close();
			}
			catch (IOException e1)
			{
				//
			}
			messager.printMessage(Diagnostic.Kind.ERROR, "(Failed in annotation processing) "+ e.getClass().getName() + "/" + e.getMessage() + "\n\n" + sq
					.toString() + "\n\n");
			return true;
		}
	}

	private class DatabaseTableHolder
	{
		public final Element annoDatabaseTableElement;
		public final List<FieldTypeGen> fieldTypeGens;
		public final List<FieldTypeGen> finalFieldTypeGens;
		public final ExecutableElement finalConstructor;
		public final List<ForeignCollectionHolder> foreignCollectionInfos;
		public final TypeElement typeElement;
		public final String tableName;
		public final EntityType entityType;

		public DatabaseTableHolder(Element annoDatabaseTableElement, List<FieldTypeGen> fieldTypeGens, List<ForeignCollectionHolder> foreignCollectionInfos, TypeElement typeElement, String tableName, List<FieldTypeGen> finalFieldTypeGens, ExecutableElement finalConstructor, EntityType entityType)
		{
			this.annoDatabaseTableElement = annoDatabaseTableElement;
			this.fieldTypeGens = fieldTypeGens;
			this.foreignCollectionInfos = foreignCollectionInfos;
			this.typeElement = typeElement;
			this.tableName = tableName;
			this.finalFieldTypeGens = finalFieldTypeGens;
			this.finalConstructor = finalConstructor;
			this.entityType = entityType;
		}
	}
	private boolean safeProcess(RoundEnvironment roundEnv)
	{
		baseClasses = new ArrayList<ClassName>();
		generatedClasses = new ArrayList<ClassName>();

		List<DatabaseTableHolder> tableHolders = new ArrayList<DatabaseTableHolder>();

		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(DatabaseTable.class))
		{
			if (!annotatedElement.getKind().isClass())
			{
				error(annotatedElement, "Only classes can be annotated with %s", DatabaseTable.class.getSimpleName());
				return false;
			}
			if (processTableViewQuery(tableHolders, annotatedElement, EntityType.Table)) return false;
		}

		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(DatabaseView.class))
		{
			if (!annotatedElement.getKind().isClass())
			{
				error(annotatedElement, "Only classes can be annotated with %s", DatabaseView.class.getSimpleName());
				return false;
			}
			if (processTableViewQuery(tableHolders, annotatedElement, EntityType.View)) return false;
		}

		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(DatabaseQuery.class))
		{
			if (!annotatedElement.getKind().isClass())
			{
				error(annotatedElement, "Only classes can be annotated with %s", DatabaseQuery.class.getSimpleName());
				return false;
			}
			if (processTableViewQuery(tableHolders, annotatedElement, EntityType.Query)) return false;
		}

		for (DatabaseTableHolder tableHolder : tableHolders)
		{
			JavaFile javaFile = generateClassConfigFile(tableHolders, tableHolder);

			try
			{
				javaFile.writeTo(filer);
			} catch (IOException e)
			{
				error(tableHolder.typeElement, "Code gen failed: " + e);
				return false;
			}
		}

		return false;
	}



	private boolean processTableViewQuery(List<DatabaseTableHolder> tableHolders, Element annotatedElement, EntityType entityType)
	{
		TypeElement typeElement = (TypeElement) annotatedElement;
		String fromString;

		switch (entityType)
		{
			case Table:
			case View:
				fromString = extractTableName(typeElement);
				break;
			case Query:
				fromString = "("+ typeElement.getAnnotation(DatabaseQuery.class).fromQuery() +")";
				break;
			default:
				throw new RuntimeException("No type (this will NEVER happen)");
		}

		List<FieldTypeGen> fieldTypeGens = new ArrayList<FieldTypeGen>();
		List<ForeignCollectionHolder> foreignCollectionInfos = new ArrayList<ForeignCollectionHolder>();

		// walk up the classes finding the fields
		TypeElement working = typeElement;
		while (working != null)
		{
			for (Element element : working.getEnclosedElements())
			{

				if (element.getKind().isField())
				{
					if (element.getAnnotation(DatabaseField.class) != null)
					{
						FieldTypeGen fieldTypeGen = new FieldTypeGen(annotatedElement, element, typeUtils, messager);

						fieldTypeGens.add(fieldTypeGen);

					} else if (element.getAnnotation(ForeignCollectionField.class) != null) {
						ForeignCollectionField foreignCollectionField = element.getAnnotation(ForeignCollectionField.class);
						foreignCollectionInfos.add(new ForeignCollectionHolder(foreignCollectionField, (VariableElement)element, messager));
					}
				}
			}
			if (working.getSuperclass().getKind().equals(TypeKind.NONE))
			{
				break;
			}
			working = (TypeElement) typeUtils.asElement(working.getSuperclass());
		}
		if (fieldTypeGens.isEmpty())
		{
			error(
					typeElement,
					"Every class annnotated with %s, %s, or %s must have at least 1 field annotated with %s",
					DatabaseTable.class.getSimpleName(),
					DatabaseView.class.getSimpleName(),
					DatabaseQuery.class.getSimpleName(),
					DatabaseField.class.getSimpleName()
			);
			return true;
		}

		List<FieldTypeGen> testFields = fieldTypeGens;
		List<FieldTypeGen> finalFields = new ArrayList<FieldTypeGen>();
		for (FieldTypeGen testField : testFields)
		{
			if(testField.finalField)
				finalFields.add(testField);
		}

		ExecutableElement finalConstructor = null;

		if(!finalFields.isEmpty())
		{
			List<ExecutableElement> executableElements = ElementFilter.constructorsIn(annotatedElement.getEnclosedElements());
			for (ExecutableElement executableElement : executableElements)
			{
				List<FieldTypeGen> finalFieldsCheck = new ArrayList<FieldTypeGen>(finalFields);
				List<? extends VariableElement> parameters = executableElement.getParameters();
				for (VariableElement parameter : parameters)
				{
					String fieldName = parameter.getSimpleName().toString();
					String fieldClassname = DataTypeManager.findFieldClassname(parameter);

					Iterator<FieldTypeGen> iterator = finalFieldsCheck.iterator();
					boolean found = false;
					while (iterator.hasNext())
					{
						FieldTypeGen next = iterator.next();
						if(next.fieldName.equals(fieldName) && next.dataTypeClassname.equals(fieldClassname))
						{
							found = true;
							iterator.remove();
						}
					}
					if(!found)
						break;
				}

				if(finalFieldsCheck.isEmpty())
				{
					finalConstructor = executableElement;
					break;
				}
			}

			if(finalConstructor == null)
			{
				List<String> allFinals = new ArrayList<String>();
				for (FieldTypeGen finalField : finalFields)
				{
					allFinals.add(finalField.fieldName);
				}
				error(annotatedElement, "Final fields need to be set in constructor %s", StringUtils.join(allFinals, ","));
				return true;
			}
		}

		DatabaseTableHolder tableHolder = new DatabaseTableHolder(
				annotatedElement,
				fieldTypeGens,
				foreignCollectionInfos,
				typeElement,
				fromString,
				finalFields,
				finalConstructor,
				entityType);

		tableHolders.add(tableHolder);
		return false;
	}

	private JavaFile generateClassConfigFile(List<DatabaseTableHolder> databaseTableHolders, DatabaseTableHolder tableHolder)
	{
		TypeElement element = tableHolder.typeElement;
		List<FieldTypeGen> fieldTypeGens = tableHolder.fieldTypeGens;
		List<ForeignCollectionHolder> foreignCollectionInfos = tableHolder.foreignCollectionInfos;


		ConfigureClassDefinitions configureClassDefinitions = new ConfigureClassDefinitions(databaseTableHolders, element).invoke();
		ClassName configName = configureClassDefinitions.getConfigName();
		ClassName className = configureClassDefinitions.getClassName();
		ClassName idType = configureClassDefinitions.getIdType();

		FieldSpec staticInstanceField = FieldSpec.builder(configName, "instance", Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
				.initializer("new $T()", configName)
				.build();

		TypeSpec.Builder configBuilder = TypeSpec.classBuilder(configName.simpleName())
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(FieldsEnum[].class, "fields")
				.addField(ForeignCollectionInfo[].class, "foreignConfigs")
				.addField(staticInstanceField)
				.addSuperinterface(ParameterizedTypeName.get(ClassName.get(GeneratedTableMapper.class), className))
				.addJavadoc("Generated on $L\n", new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date()));


		MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
//				.addException(SQLException.class)
				.addStatement("this.$N = $N()", "fields", "getFields")
				.addStatement("this.$N = $N()", "foreignConfigs", "getForeignConfigs")
				.build();

		configBuilder.addMethod(constructor);

		createObject(databaseTableHolders, tableHolder, className, configBuilder);
		fillRow(databaseTableHolders, foreignCollectionInfos, element, fieldTypeGens, className, idType, configBuilder);
//		assignVersion(className, configBuilder);
		assignId(fieldTypeGens, className, configBuilder);
		extractId(fieldTypeGens, className, configBuilder);
//		extractVersion(fieldTypeGens, className, configBuilder);
		buildExtractStatements(databaseTableHolders, tableHolder, fieldTypeGens, className, configBuilder, "bindVals", false);
		buildExtractStatements(databaseTableHolders, tableHolder, fieldTypeGens, className, configBuilder, "bindCreateVals", true);
		objectToString(fieldTypeGens, className, configBuilder);
		objectsEqual(fieldTypeGens, className, configBuilder);

		addForeignCollectionFillers(configureClassDefinitions, foreignCollectionInfos, className, idType, configBuilder);

		MethodSpec fieldConfigsMethod = fieldConfigs(databaseTableHolders, fieldTypeGens, tableHolder.entityType == EntityType.Table ? tableHolder.tableName : null, configBuilder);
		MethodSpec foreignConfigsMethod = foreignConfigs(foreignCollectionInfos, configBuilder);

		tableConfig(element, tableHolder.tableName, className, configBuilder, fieldConfigsMethod, foreignConfigsMethod);

		baseClasses.add(className);
		generatedClasses.add(configName);

		return JavaFile.builder(configName.packageName(), configBuilder.build()).build();
	}

	private void addForeignCollectionFillers(ConfigureClassDefinitions configureClassDefinitions, List<ForeignCollectionHolder> foreignCollectionInfos, ClassName className, ClassName idType, TypeSpec.Builder configBuilder)
	{
		ParameterizedTypeName modelDaoType = ParameterizedTypeName.get(ClassName.get(ModelDao.class), className);

		MethodSpec.Builder globalFillMethod = MethodSpec.methodBuilder("fillForeignCollection")
				.addModifiers(Modifier.PUBLIC)
				.addException(SQLException.class)
				.addAnnotation(Override.class)
				.addParameter(className, "data")
				.addParameter(modelDaoType, "modelDao")
				.addParameter(String.class, "fieldName");

		for (ForeignCollectionHolder foreignCollectionInfo : foreignCollectionInfos)
		{
			String methodName = "fill_" + foreignCollectionInfo.variableName;

			globalFillMethod.beginControlFlow("if(fieldName.equals($S))", foreignCollectionInfo.variableName);
			globalFillMethod.addStatement(methodName + "(data, modelDao)");
			globalFillMethod.addStatement("return");
			globalFillMethod.endControlFlow();

			MethodSpec.Builder fillCollectionMethod = MethodSpec.methodBuilder(methodName)
					.addModifiers(Modifier.PUBLIC)
					.addException(SQLException.class)
					.addParameter(className, "data")
					.addParameter(modelDaoType, "modelDao");

			fillCollectionMethod.addStatement("$T foreignDao = (ModelDao)modelDao.getOpenHelper().getDao($T.class)", ModelDao.class, ClassName.bestGuess(foreignCollectionInfo.foreignTypeName));
			fillCollectionMethod.addStatement("ForeignCollectionInfo foreignCollectionInfo = modelDao.findForeignCollectionInfo($S)", foreignCollectionInfo.variableName);
			fillCollectionMethod.addStatement("data.$L = foreignDao.queryForEq(foreignCollectionInfo.foreignFieldName, data).orderBy(foreignCollectionInfo.orderBy).list()", foreignCollectionInfo.variableName);
/*
			ClassName configName = ClassName.get(className.packageName(), Joiner.on('$').join(ClassName.bestGuess(foreignCollectionInfo.foreignTypeName).simpleNames()) + "$Configuration");
			fillCollectionMethod.addStatement("data.$N = foreignDao.queryForEq($L$L, $S, modelDao.extractId(data))",
					foreignCollectionInfo.variableName,
					configName, ".Fields."+ foreignCollectionInfo.variableName +".getColumnName()");//, StringUtils.trimToNull(foreignCollectionInfo.foreignCollectionField.orderBy()));
*/


//			fillCollectionMethod.addStatement("$T where = dao.createWhere().eq($S, data)", Where.class, foreignCollectionInfo.foreignCollectionField.foreignFieldName());
//			fillCollectionMethod.addStatement("data.$N = dao.findForeignCollectionValues($T.extractId(data), $S)", configureClassDefinitions.configName, foreignCollectionInfo.variableName, StringUtils.trimToNull(foreignCollectionInfo.foreignCollectionField.orderBy()));

			configBuilder.addMethod(fillCollectionMethod.build());
		}

		configBuilder.addMethod(globalFillMethod.build());
	}

	private void tableConfig(TypeElement element, String tableName, ClassName className, TypeSpec.Builder configBuilder, MethodSpec fieldConfigsMethod, MethodSpec foreignConfigsMethod)
	{
		TypeName databaseTableConfig = ParameterizedTypeName.get(ClassName.get(TableInfo.class), className);
		MethodSpec.Builder tableConfigMethodBuilder = MethodSpec.methodBuilder("getTableConfig")
				.addModifiers(Modifier.PUBLIC)
				.addException(SQLException.class)
				.returns(databaseTableConfig)
				.addStatement("$T config = new $T($T.class, $S, $N(), $N())",
						databaseTableConfig,
						databaseTableConfig,
						element,
						tableName,
						fieldConfigsMethod,
						foreignConfigsMethod
				);

		tableConfigMethodBuilder.addStatement("return config");

		configBuilder.addMethod(tableConfigMethodBuilder.build());
	}

	private MethodSpec fieldConfigs(List<DatabaseTableHolder> databaseTableHolders, List<FieldTypeGen> fieldTypeGens, String indexNameBase, TypeSpec.Builder configBuilder)
	{
		TypeSpec.Builder fieldsEnumBuilder = TypeSpec.enumBuilder("Fields")
				.addModifiers(Modifier.PUBLIC)
				.addSuperinterface(FieldsEnum.class)
				.addField(FieldType.class, "fieldType", Modifier.PRIVATE, Modifier.FINAL)
				.addMethod(MethodSpec.constructorBuilder()
						.addParameter(FieldType.class, "fieldType")
						.addStatement("this.$N = $N", "fieldType", "fieldType")
						.build())
				.addMethod(
						MethodSpec.methodBuilder("getFieldType")
								.addModifiers(Modifier.PUBLIC)
								.addAnnotation(Override.class)
								.returns(FieldType.class)
								.addStatement("return $N", "fieldType")
								.build());


		MethodSpec.Builder fieldConfigsMethodBuilder = MethodSpec.methodBuilder("getFields")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(FieldsEnum[].class);

		for (FieldTypeGen config : fieldTypeGens)
		{
			fieldsEnumBuilder
					.addEnumConstant(config.fieldName,
							TypeSpec.anonymousClassBuilder("$L", getFieldConfig(databaseTableHolders, config, config.databaseField, indexNameBase)).build()
					);
		}


		fieldConfigsMethodBuilder.addStatement("return Fields.values()");

		MethodSpec fieldConfigsMethod = fieldConfigsMethodBuilder.build();

		configBuilder.addMethod(fieldConfigsMethod);

		configBuilder.addType(fieldsEnumBuilder.build());
		return fieldConfigsMethod;
	}

	private MethodSpec foreignConfigs(List<ForeignCollectionHolder> fieldTypeGens, TypeSpec.Builder configBuilder)
	{
		TypeName listOfFieldConfigs = ParameterizedTypeName.get(List.class, ForeignCollectionInfo.class);
		TypeName arrayListOfFieldConfigs = ParameterizedTypeName.get(ArrayList.class, ForeignCollectionInfo.class);

		MethodSpec.Builder fieldConfigsMethodBuilder = MethodSpec.methodBuilder("getForeignConfigs")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//				.addException(SQLException.class)
				.returns(ForeignCollectionInfo[].class)
				.addStatement("$T list = new $T()", listOfFieldConfigs, arrayListOfFieldConfigs);

		fieldConfigsMethodBuilder.addStatement("$T config = null", ForeignCollectionInfo.class);

		for (ForeignCollectionHolder config : fieldTypeGens)
		{
			CodeBlock.Builder builder = CodeBlock.builder();


			builder.addStatement("config = new $T(" +
							"$L, " +
							"$L, " +
							"$S, " +
							"$S, " +
							"$S, " +
							"$L.class)",
					ForeignCollectionInfo.class,
					config.foreignCollectionField.eager(),
					config.foreignCollectionField.maxEagerLevel(),
					StringUtils.trimToNull(config.foreignCollectionField.orderBy()),
					StringUtils.trimToNull(config.foreignCollectionField.foreignFieldName()),
					StringUtils.trimToNull(config.variableName),
					config.foreignTypeName
					);

			fieldConfigsMethodBuilder.addCode(builder.build());

			fieldConfigsMethodBuilder.addStatement("list.add(config)");
		}

		fieldConfigsMethodBuilder.addStatement("return list.toArray(new ForeignCollectionInfo[list.size()])");

		MethodSpec fieldConfigsMethod = fieldConfigsMethodBuilder.build();

		configBuilder.addMethod(fieldConfigsMethod);
		return fieldConfigsMethod;
	}

	private void createObject(List<DatabaseTableHolder> databaseTableHolders, DatabaseTableHolder tableHolder, ClassName className, TypeSpec.Builder configBuilder)
	{
		MethodSpec.Builder javaFillMethodBuilder = MethodSpec.methodBuilder("createObject")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addException(SQLException.class)
				.addParameter(Cursor.class, "results")
//				.addParameter(SqueakyContext.class, "squeakyContext")
				.returns(className);

		if(tableHolder.finalConstructor != null)
		{
			javaFillMethodBuilder.beginControlFlow("if(results == null)")
					.addStatement("throw new $T(\"Foreign entities can't have final fields. They need to be refreshed.\")", SQLException.class)
					.endControlFlow();
			List<String> consParamList = new ArrayList<String>();

			List<? extends VariableElement> parameters = tableHolder.finalConstructor.getParameters();
			for (VariableElement parameter : parameters)
			{
				String paramName = parameter.getSimpleName().toString();
				int count = 0;

				for (FieldTypeGen fieldTypeGen : tableHolder.fieldTypeGens)
				{
					if(fieldTypeGen.fieldName.equals(paramName))
					{
						AccessDataHolder accessDataHolder = new AccessDataHolder(databaseTableHolders, null, fieldTypeGen, count).invoke();
						String accessData = accessDataHolder.getAccessData();
						boolean checkNull = accessDataHolder.isCheckNull();
						if(checkNull)
						{
							//TODO Need primitive transform for null
							consParamList.add("results.isNull("+ count +")?null:"+ accessData);
						}
						else
						{
							consParamList.add(accessData);
						}

						break;
					}

					count++;
				}
			}
			javaFillMethodBuilder.addStatement("$T data = new $T($N)", className, className, StringUtils.join(consParamList, ", "));
		}
		else
		{
			javaFillMethodBuilder.addStatement("$T data = new $T()", className, className);
		}
//		messager.printMessage(Diagnostic.Kind.ERROR, "balls: start");
		/*List<TypeMirror> mirrors = new ArrayList<>();
		listTypes(tableHolder.typeElement.asType(), mirrors);

		for (TypeMirror typeMirror : mirrors)
		{
			messager.printMessage(Diagnostic.Kind.ERROR, "balls: "+ typeMirror.toString());
		}*/
		/*javaFillMethodBuilder.beginControlFlow("if(data instanceof $T)", BaseTable.class)
				.addStatement("((BaseTable)data).setContext(squeakyContext)")
				.endControlFlow();*/
		javaFillMethodBuilder.addStatement("return data");

		configBuilder.addMethod(javaFillMethodBuilder.build());
	}

/*	private void listTypes(TypeMirror typeMirror, List<TypeMirror> mirrors)
	{
		typeUtils.
		List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(typeMirror);
		mirrors.addAll(typeMirrors);
		for (TypeMirror mirror : typeMirrors)
		{
			listTypes(mirror, mirrors);
		}
	}*/

	private void fillRow(List<DatabaseTableHolder> databaseTableHolders, List<ForeignCollectionHolder> foreignCollectionInfos,  TypeElement element, List<FieldTypeGen> fieldTypeGens, ClassName className, ClassName idType, TypeSpec.Builder configBuilder)
	{
		ParameterizedTypeName modelDaoType = ParameterizedTypeName.get(ClassName.get(ModelDao.class), className);
		MethodSpec.Builder javaFillMethodBuilder = MethodSpec.methodBuilder("fillRow")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(className, "data")
				.addParameter(Cursor.class, "results")
				.addParameter(modelDaoType, "modelDao")
				.addParameter(Dao.ForeignRefresh[].class, "foreignRefreshMap")
				.addParameter(TransientCache.class, "objectCache")
				.addException(SQLException.class)
				.addAnnotation(Override.class);

		makeCopyRows(databaseTableHolders, javaFillMethodBuilder, element, fieldTypeGens);

		for (ForeignCollectionHolder foreignCollectionInfo : foreignCollectionInfos)
		{
			if(foreignCollectionInfo.foreignCollectionField.eager())
			{
				javaFillMethodBuilder.addStatement("modelDao.fillForeignCollection(data, $S)", foreignCollectionInfo.variableName);
			}
		}
		configBuilder.addMethod(javaFillMethodBuilder.build());
	}

	private void assignId(List<FieldTypeGen> fieldTypeGens, ClassName className, TypeSpec.Builder configBuilder)
	{
		FieldTypeGen idField = findIdField(fieldTypeGens);

		ClassName helperName = ClassName.get(OrmLiteHelper.class);

		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder("assignId")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addParameter(className, "data")
				.addParameter(Object.class, "val");

		if(idField == null)
		{
			//Do nothing
		}
		else if(idField.finalField)
		{
			methodBuilder.addStatement("throw new UnsupportedOperationException(\"Can't assign id to final field "+ idField.fieldName +"\")");
		}
		else if(idField.useGetSet)
			methodBuilder.addCode(CodeBlock.builder()
							.addStatement("data.set$N(($N)$T.safeConvert($N.class, val))", StringUtils.capitalize(idField.fieldName), idField.dataTypeClassname, helperName, idField.dataTypeClassname)
							.build()
			);
		else
			methodBuilder.addCode(CodeBlock.builder()
							.addStatement("data.$N = ($N)$T.safeConvert($N.class, val)", idField.fieldName, idField.dataTypeClassname, helperName, idField.dataTypeClassname)
							.build()
			);

		configBuilder.addMethod(methodBuilder
						.build()
		);
	}

	private void extractId(List<FieldTypeGen> fieldTypeGens, ClassName className, TypeSpec.Builder configBuilder)
	{
		FieldTypeGen idField = findIdField(fieldTypeGens);

		ClassName idClassName = idField == null ? ClassName.get(Object.class) : typeForString(idField.dataTypeClassname);
		MethodSpec.Builder methodBody = MethodSpec
				.methodBuilder("extractId")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addParameter(className, "data")
				.returns(idClassName);

		if(idField == null)
		{
			methodBody.addStatement("return null");
		}
		else
		{
			methodBody.beginControlFlow("if(data == null)");
			methodBody.addStatement("return null");
			methodBody.endControlFlow();

			methodBody.addStatement("$T val = " + simpleExtractor(idField), idClassName);
			if (idField.primitiveType)
				methodBody.addStatement("val = val == 0 ? null : val");

			methodBody.addStatement("return val");


		}
		configBuilder.addMethod(methodBody
						.build()
		);
	}

	private FieldTypeGen findIdField(List<FieldTypeGen> fieldTypeGens)
	{
		FieldTypeGen idField = null;
		for (FieldTypeGen fieldTypeGen : fieldTypeGens)
		{
			if (fieldTypeGen.isId || fieldTypeGen.isGeneratedId)
			{
				idField = fieldTypeGen;
			}
		}

		return idField;
	}

	private static DataType[] STATIC_TYPES =  new DataType[]{
			DataType.BOOLEAN,
			DataType.BOOLEAN_OBJ,
			DataType.DOUBLE,
			DataType.DOUBLE_OBJ,
			DataType.FLOAT,
			DataType.FLOAT_OBJ,
			DataType.INTEGER,
			DataType.INTEGER_OBJ,
			DataType.LONG,
			DataType.LONG_OBJ,
			DataType.SHORT,
			DataType.SHORT_OBJ,
			DataType.STRING,
			DataType.BYTE_ARRAY
	};

	private static String simpleExtractor(FieldTypeGen fieldTypeGen)
	{
		StringBuilder convertBuilder = new StringBuilder();
		if (fieldTypeGen.useGetSet)
		{
			String accessPrefix = fieldTypeGen.dataType == DataType.BOOLEAN ? "is" : "get";
			convertBuilder.append("data.").append(accessPrefix).append(StringUtils.capitalize(fieldTypeGen.fieldName)).append("(").append(")");
		}
		else
		{
			convertBuilder.append("data.").append(fieldTypeGen.fieldName);
		}

		return convertBuilder.toString();
	}

	private static boolean isStaticType(DataType dataType)
	{
		for (DataType staticType : STATIC_TYPES)
		{
			if(staticType == dataType)
				return true;
		}

		return false;
	}

	//TODO
	private void objectToString(List<FieldTypeGen> fieldTypeGens, ClassName className, TypeSpec.Builder configBuilder)
	{
		MethodSpec.Builder tableConfigMethodBuilder = MethodSpec.methodBuilder("objectToString")
				.addModifiers(Modifier.PUBLIC)
				.addException(SQLException.class)
				.returns(String.class)
				.addAnnotation(Override.class)
				.addParameter(className, "data")
				.addStatement("return \"heyo\"");

		configBuilder.addMethod(tableConfigMethodBuilder.build());
	}

	//boolean objectsEqual(T d1, T d2)throws SQLException;
//TODO
	private void objectsEqual(List<FieldTypeGen> fieldTypeGens, ClassName className, TypeSpec.Builder configBuilder)
	{
		MethodSpec.Builder tableConfigMethodBuilder = MethodSpec.methodBuilder("objectsEqual")
				.addModifiers(Modifier.PUBLIC)
				.addException(SQLException.class)
				.returns(boolean.class)
				.addAnnotation(Override.class)
				.addParameter(className, "d1")
				.addParameter(className, "d2")
				.addStatement("return false");

		configBuilder.addMethod(tableConfigMethodBuilder.build());
	}

	private void buildExtractStatements(List<DatabaseTableHolder> databaseTableHolders, DatabaseTableHolder tableHolder, List<FieldTypeGen> fieldTypeGens, ClassName className, TypeSpec.Builder configBuilder, String methodName, boolean createVals)
	{
		MethodSpec.Builder returns = MethodSpec
				.methodBuilder(methodName)
				.addModifiers(Modifier.PUBLIC)
				.addException(SQLException.class)
				.addAnnotation(Override.class)
				.addParameter(SQLiteStatement.class, "stmt")
				.addParameter(className, "data")
				;

		if(tableHolder.entityType == EntityType.Table)
		{
			int assignCount = 0;
			int configCount = 0;
			for (FieldTypeGen fieldTypeGen : fieldTypeGens)
			{
				if((createVals && !fieldTypeGen.isGeneratedId) || (!createVals && !fieldTypeGen.isGeneratedId && !fieldTypeGen.isId))
				{
					buildExtractStatement(databaseTableHolders, fieldTypeGen, returns, configCount, assignCount);
					assignCount++;
				}

				configCount++;
			}

			if(!createVals)
			{
				CodeBlock.Builder whereBlock = CodeBlock.builder();
				for (FieldTypeGen fieldTypeGen : fieldTypeGens)
				{
					if (fieldTypeGen.isGeneratedId || fieldTypeGen.isId)
					{
						String idTypeSuffix;
						if (fieldTypeGen.dataTypeClassname.contains("String"))
						{
							idTypeSuffix = "String";
						} else
						{
							idTypeSuffix = "Long";
						}
						whereBlock.addStatement("stmt.bind" + idTypeSuffix + "($L, " + simpleExtractor(fieldTypeGen) + ")", assignCount + 1);
					}
				}

				returns.addCode(whereBlock.build());
			}
		}
		else
		{
			returns.addStatement("throw new UnsupportedOperationException(\"Views can't be updated\")");
		}

		configBuilder.addMethod(returns.build());
	}
	private void buildExtractStatement(List<DatabaseTableHolder> databaseTableHolders, FieldTypeGen fieldTypeGen, MethodSpec.Builder methodBuilder, int configCount, int assignCount)
	{
		CodeBlock.Builder assignBlock = CodeBlock.builder();

		if(fieldTypeGen.foreign)
		{
			ConfigureClassDefinitions configureClassDefinitions = new ConfigureClassDefinitions(databaseTableHolders, (TypeElement) ((DeclaredType)fieldTypeGen.fieldElement.asType()).asElement()).invoke();
			boolean stringId = configureClassDefinitions.idType.simpleName().contains("String");

			String idTypeSuffix;
			if(stringId)
			{
				idTypeSuffix = "String";
			}
			else
			{
				idTypeSuffix = "Long";
			}
			assignBlock.addStatement("$T val$L = " + simpleExtractor(fieldTypeGen), configureClassDefinitions.className, assignCount + 1);

			assignBlock.add("if(val$L == null){\n", assignCount + 1);
			assignBlock.addStatement("stmt.bindNull($L)", assignCount + 1);
			assignBlock.add("}else{\n");
			assignBlock.addStatement("stmt.bind" + idTypeSuffix + "($L, $T.instance.extractId(val$L))", assignCount + 1, configureClassDefinitions.configName, assignCount + 1);
			assignBlock.add("}\n");

//			assignBlock.addStatement("stmt.bind"+ idTypeSuffix +"($L, $T.instance.extractId(" + simpleExtractor(fieldTypeGen) + "))", assignCount+1, configureClassDefinitions.configName);
		}
		else
		{
			boolean softConvert = !isStaticType(fieldTypeGen.dataType);
			if (softConvert)
			{
				boolean blobType = blobType(fieldTypeGen.dataType.getDataPersister().getSqlType());

				assignBlock.addStatement("Object val$L = " + simpleExtractor(fieldTypeGen), assignCount + 1);
				assignBlock.add("if(val$L == null){\n", assignCount + 1);
				assignBlock.addStatement("stmt.bindNull($L)", assignCount + 1);
				assignBlock.add("}else{\n");
				String stmt = blobType ?
						"stmt.bind$L($L, (byte[])fields[$L].getFieldType().getDataPersister().javaToSqlArg(fields[$L].getFieldType(), val$L))" :
						"stmt.bind$L($L, fields[$L].getFieldType().getDataPersister().javaToSqlArg(fields[$L].getFieldType(), val$L).toString())";
				assignBlock.addStatement(stmt, blobType ? "Blob" : "String", assignCount+1, configCount, configCount, assignCount+1);
				assignBlock.add("}\n");

			}
			else
			{
				String type = null;
				switch (fieldTypeGen.dataType)
				{
					case BOOLEAN:
						assignBlock.addStatement("stmt.bindLong($L, "+ simpleExtractor(fieldTypeGen) +"?1:0)", assignCount+1);
						break;
					case BOOLEAN_OBJ:
						assignBlock.addStatement("Boolean val$L = " + simpleExtractor(fieldTypeGen), assignCount+1);
						assignBlock.add("if(val$L == null){\n", assignCount+1);
						assignBlock.addStatement("stmt.bindNull($L)", assignCount+1);
						assignBlock.add("}else{\n");
						assignBlock.addStatement("stmt.bindLong($L, val$L ? 1 : 0)", assignCount+1, assignCount+1);
						assignBlock.add("}\n");
						break;
					case FLOAT:
					case DOUBLE:
						assignBlock.addStatement("stmt.bindDouble($L, "+ simpleExtractor(fieldTypeGen) +")", assignCount+1);
						break;
					case FLOAT_OBJ:
						type = "Float";
					case DOUBLE_OBJ:
						type = type == null ? "Double" : type;
						assignBlock.addStatement("$L val$L = " + simpleExtractor(fieldTypeGen), type, assignCount+1);
						assignBlock.add("if(val$L == null){\n", assignCount+1);
						assignBlock.addStatement("stmt.bindNull($L)", assignCount+1);
						assignBlock.add("}else{\n");
						assignBlock.addStatement("stmt.bindDouble($L, val$L.doubleValue())", assignCount+1, assignCount+1);
						assignBlock.add("}\n");
						break;
					case SHORT:
					case INTEGER:
					case LONG:
						assignBlock.addStatement("stmt.bindLong($L, "+ simpleExtractor(fieldTypeGen) +")", assignCount+1);
						break;
					case SHORT_OBJ:
						type = "Short";
					case INTEGER_OBJ:
						type = type == null ? "Integer" : type;
					case LONG_OBJ:
						type = type == null ? "Long" : type;
						assignBlock.addStatement("$L val$L = " + simpleExtractor(fieldTypeGen), type, assignCount+1);
						assignBlock.add("if(val$L == null){\n", assignCount+1);
						assignBlock.addStatement("stmt.bindNull($L)", assignCount+1);
						assignBlock.add("}else{\n");
						assignBlock.addStatement("stmt.bindLong($L, val$L.longValue())", assignCount+1, assignCount+1);
						assignBlock.add("}\n");
						break;
					case STRING:
						assignBlock.addStatement("$L val$L = " + simpleExtractor(fieldTypeGen), "String", assignCount+1);
						assignBlock.add("if(val$L == null){\n", assignCount + 1);
						assignBlock.addStatement("stmt.bindNull($L)", assignCount+1);
						assignBlock.add("}else{\n");
						assignBlock.addStatement("stmt.bindString($L, "+ simpleExtractor(fieldTypeGen) +")", assignCount+1);
						assignBlock.add("}\n");
						break;
					case BYTE_ARRAY:
						assignBlock.addStatement("byte[] val$L = " + simpleExtractor(fieldTypeGen), assignCount+1);
						assignBlock.add("if(val$L == null){\n", assignCount+1);
						assignBlock.addStatement("stmt.bindNull($L)", assignCount+1);
						assignBlock.add("}else{\n");
						assignBlock.addStatement("stmt.bindBlob($L, val$L)", assignCount+1, assignCount+1);
						assignBlock.add("}\n");
						break;
					default:
						throw new IllegalArgumentException("Need to figure out fialure");
				}
			}
		}

		methodBuilder.addCode(assignBlock.build());
	}

	private boolean blobType(SqlType sqlType)
	{
		return (sqlType == SqlType.BLOB || sqlType == SqlType.BYTE_ARRAY || sqlType == SqlType.SERIALIZABLE);
	}

	private void makeCopyRows(List<DatabaseTableHolder> databaseTableHolders, MethodSpec.Builder methodBuilder, TypeElement element, List<FieldTypeGen> fieldConfigs)
	{
		CodeBlock.Builder builder = CodeBlock.builder();
		int count = 0;
		for (FieldTypeGen fieldConfig : fieldConfigs)
		{
			if(!fieldConfig.finalField)
				makeCopyRow(databaseTableHolders, element, fieldConfig, builder, count);

			count++;
		}
		methodBuilder.addCode(builder.build());
	}

	private void makeCopyRow(List<DatabaseTableHolder> databaseTableHolders, TypeElement fieldElement, FieldTypeGen config, CodeBlock.Builder builder, int count)
	{
		{
			AccessDataHolder accessDataHolder = new AccessDataHolder(databaseTableHolders, fieldElement, config, count).invoke();
			String accessData = accessDataHolder.getAccessData();
			boolean checkNull = accessDataHolder.isCheckNull();

			if (accessData != null)
			{
				StringBuilder sb = new StringBuilder();

				if(config.foreign)
				{
					ConfigureClassDefinitions configureClassDefinitions = new ConfigureClassDefinitions(databaseTableHolders, (TypeElement) ((DeclaredType)config.fieldElement.asType()).asElement()).invoke();
					ClassName className = ClassName.get(fieldElement);

					CodeBlock.Builder foreignBuilder = CodeBlock.builder();
					foreignBuilder.add("if(!results.isNull(" + count + ")){");
					foreignBuilder.addStatement("$T __$N = $T.instance.createObject(null)", configureClassDefinitions.className, config.fieldName, configureClassDefinitions.configName);
					foreignBuilder.addStatement("$T.instance.assignId(__$N, $N)", configureClassDefinitions.configName, config.fieldName, accessData);

					foreignBuilder.beginControlFlow("if(foreignRefreshMap != null && $T.findRefresh(foreignRefreshMap, $S) != null)", DaoHelper.class, config.fieldName);
					foreignBuilder.addStatement("modelDao.getOpenHelper().getDao($T.class).refresh(__$N, $T.findRefresh(foreignRefreshMap, $S).refreshFields)", configureClassDefinitions.className, config.fieldName, DaoHelper.class, config.fieldName);
					foreignBuilder.endControlFlow();

					if (config.useGetSet)
					{
						sb.append("data.set").append(StringUtils.capitalize(config.fieldName)).append("(")
								.append("__"+ config.fieldName).append(")");
					} else
					{
						sb.append("data.").append(config.fieldName).append(" = ")
								.append("__"+ config.fieldName);
					}

					foreignBuilder.addStatement(sb.toString());
					foreignBuilder.add("}");
					builder.add(foreignBuilder.build());

				}
				else
				{
					if(checkNull)
						sb.append("if(!results.isNull("+ count+"))");

					if (config.useGetSet)
					{
						sb.append("data.set").append(StringUtils.capitalize(config.fieldName)).append("(")
								.append(accessData).append(")");
					} else
					{
						sb.append("data.").append(config.fieldName).append(" = ")
								.append(accessData);
					}

					builder.addStatement(sb.toString());
				}
			}
		}
	}

	private DataType findFieldDataType(List<DatabaseTableHolder> databaseTableHolders, TypeElement fieldElement, FieldTypeGen config)
	{
		DataType dataType = null;

		if (config.foreign)
		{
			for (DatabaseTableHolder databaseTableHolder : databaseTableHolders)
			{
				System.out.println("Find foreign: "+ databaseTableHolder.typeElement.getQualifiedName() + "/" + fieldElement.getQualifiedName());
				if(databaseTableHolder.typeElement.getQualifiedName().equals(fieldElement.getQualifiedName()))
				{
					for (FieldTypeGen fieldTypeGen : databaseTableHolder.fieldTypeGens)
					{
						if(fieldTypeGen.isId || fieldTypeGen.isGeneratedId)
						{
							dataType = fieldTypeGen.dataType;
						}
					}
				}
			}
		} else
		{
			dataType = config.dataType;
		}
		return dataType;
	}

	private CodeBlock getFieldConfig(List<DatabaseTableHolder> databaseTableHolders, FieldTypeGen config, DatabaseField databaseField, String indexNameBase)
	{
		DataType dataType = findFieldDataType(databaseTableHolders, (TypeElement) config.databaseElement, config);


		CodeBlock.Builder builder = CodeBlock.builder();


		builder.add("new $T( " +
						"$S," +
						"$S," +
						"$S," +
						"$L," + //isId
						"$L," +
						"$L," +
						"$T.$L," +
						"$T.class," +
						"$L," + //canBeNull
						"$S," +
						"$L," +
						"$L," +
						"$L," +
						"$L," + //uniqueIndex
						"$S," +
						"$S," +
						"$S," +
						"$L)",
				FieldType.class,
				indexNameBase,
				config.fieldName,
				config.columnName,
				config.isId,
				config.isGeneratedId,
				config.foreign,
				DataType.class,
				dataType,
				ClassName.get(config.dataTypeMirror),
				databaseField.canBeNull(),
				StringUtils.trimToNull(databaseField.format()),
				databaseField.unique(),
				databaseField.uniqueCombo(),
				databaseField.index(),
				databaseField.uniqueIndex(),
				StringUtils.trimToNull(databaseField.indexName()),
				StringUtils.trimToNull(databaseField.uniqueIndexName()),
				config.defaultValue,
				config.foreignAutoRefresh
		);

		return builder.build();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
		Set<String> annotations = new LinkedHashSet<String>();
		annotations.add(DatabaseTable.class.getCanonicalName());
		annotations.add(DatabaseView.class.getCanonicalName());
		annotations.add(DatabaseQuery.class.getCanonicalName());
		return annotations;
	}

	@Override
	public SourceVersion getSupportedSourceVersion()
	{
		return SourceVersion.latestSupported();
	}

	private void error(Element e, String msg, Object... args)
	{
		messager.printMessage(
				Diagnostic.Kind.ERROR,
				String.format(msg, args),
				e
		);
	}

	private static String extractTableName(TypeElement element)
	{
		DatabaseTable databaseTable = element.getAnnotation(DatabaseTable.class);
		DatabaseView databaseView = element.getAnnotation(DatabaseView.class);
		if (databaseTable != null && StringUtils.isNotEmpty(databaseTable.tableName()))
		{
			return databaseTable.tableName();
		}
		else if(databaseView != null && StringUtils.isNotEmpty(databaseView.viewName()))
		{
			return databaseView.viewName();
		}
		else
		{
			// if the name isn't specified, it is the class name lowercased
			return element.getSimpleName().toString().toLowerCase();
		}
	}

	private class ConfigureClassDefinitions
	{
		private List<DatabaseTableHolder> databaseTableHolders;
		private TypeElement element;
		private ClassName className;
		private ClassName idType;
		private ClassName configName;

		public ConfigureClassDefinitions(List<DatabaseTableHolder> databaseTableHolders, TypeElement element)
		{
			this.databaseTableHolders = databaseTableHolders;
			this.element = element;
		}

		public ClassName getClassName()
		{
			return className;
		}

		public ClassName getIdType()
		{
			return idType;
		}

		public ClassName getConfigName()
		{
			return configName;
		}

		public ConfigureClassDefinitions invoke()
		{
			DatabaseTableHolder myTableHolder = null;

			for (DatabaseTableHolder databaseTableHolder : databaseTableHolders)
			{
				if(databaseTableHolder.typeElement.getQualifiedName().equals(element.getQualifiedName()))
				{
					myTableHolder = databaseTableHolder;
					break;
				}
			}

			FieldTypeGen idFieldGen = null;
			for (FieldTypeGen fieldTypeGen : myTableHolder.fieldTypeGens)
			{
				if(fieldTypeGen.isId || fieldTypeGen.isGeneratedId)
				{
					idFieldGen = fieldTypeGen;
					break;
				}
			}
			className = ClassName.get(element);
			if(idFieldGen != null)
			{
				idType = typeForString(idFieldGen.dataTypeClassname);
			}
			configName = ClassName.get(className.packageName(), Joiner.on('$').join(className.simpleNames()) + "$Configuration");
			return this;
		}
	}

	ClassName typeForString(String idTypeClassname)
	{
		if(idTypeClassname.equals("long"))
			idTypeClassname = Long.class.getName();
		else if(idTypeClassname.equals("int"))
			idTypeClassname = Integer.class.getName();
		else if(idTypeClassname.equals("short"))
			idTypeClassname = Short.class.getName();
		else if(idTypeClassname.equals("byte"))
			idTypeClassname = Byte.class.getName();
		else if(idTypeClassname.equals("float"))
			idTypeClassname = Float.class.getName();
		else if(idTypeClassname.equals("double"))
			idTypeClassname = Double.class.getName();

		return ClassName.bestGuess(idTypeClassname);
	}

	private class AccessDataHolder
	{
		private List<DatabaseTableHolder> databaseTableHolders;
		private TypeElement fieldElement;
		private FieldTypeGen config;
		private int count;
		private String accessData;
		private boolean checkNull;

		public AccessDataHolder(List<DatabaseTableHolder> databaseTableHolders, TypeElement fieldElement, FieldTypeGen config, int count)
		{
			this.databaseTableHolders = databaseTableHolders;
			this.fieldElement = fieldElement;
			this.config = config;
			this.count = count;
		}

		public String getAccessData()
		{
			return accessData;
		}

		public boolean isCheckNull()
		{
			return checkNull;
		}

		public AccessDataHolder invoke()
		{
			accessData = null;
			checkNull = config.databaseField.canBeNull();
			DataType dataType = findFieldDataType(databaseTableHolders, fieldElement, config);

			switch (dataType)
			{
				case BOOLEAN:
				case BOOLEAN_OBJ:
					accessData = "results.getShort(" + count + ") != 0";
					break;
				case DOUBLE:
				case DOUBLE_OBJ:
					accessData = "results.getDouble(" + count + ")";
					break;
				case FLOAT:
				case FLOAT_OBJ:
					accessData = "results.getFloat(" + count + ")";
					break;
				case INTEGER:
				case INTEGER_OBJ:
					accessData = "results.getInt(" + count + ")";
					break;
				case LONG:
				case LONG_OBJ:
					accessData = "results.getLong(" + count + ")";
					break;
				case SHORT:
				case SHORT_OBJ:
					accessData = "results.getShort(" + count + ")";
					break;
				case STRING:
					accessData = "results.getString(" + count + ")";
					checkNull = false;
					break;
				case BYTE_ARRAY:
					accessData = "results.getBlob(" + count + ")";
					break;
				default:
					accessData = "(" + config.dataTypeClassname + ")fields[" + count + "].getFieldType().getDataPersister().resultToJava(fields[" + count + "].getFieldType(), results, " + count + ")";
			}
			return this;
		}
	}
}
