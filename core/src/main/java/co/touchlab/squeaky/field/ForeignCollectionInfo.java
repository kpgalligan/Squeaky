package co.touchlab.squeaky.field;

/**
 * Created by kgalligan on 7/26/15.
 */
public class ForeignCollectionInfo
{
	public final boolean eager;
	public final int maxEagerLevel;
	public final String orderBy;
	public final String foreignFieldName;
	public final String variableName;
	public final Class foreignFieldType;

	public ForeignCollectionInfo(boolean eager, int maxEagerLevel, String orderBy, String foreignFieldName, String variableName, Class foreignFieldType)
	{
		this.eager = eager;
		this.maxEagerLevel = maxEagerLevel;
		this.orderBy = orderBy;
		this.foreignFieldName = foreignFieldName;
		this.variableName = variableName;
		this.foreignFieldType = foreignFieldType;
	}
}
