package co.touchlab.squeaky.field;

/**
 * Created by kgalligan on 7/12/15.
 */
public @interface ForeignCollectionField
{
	public static final int DEFAULT_MAX_EAGER_LEVEL = 1;

	boolean eager() default false;

	int maxEagerLevel() default DEFAULT_MAX_EAGER_LEVEL;

	String orderBy() default "";

	String foreignFieldName();
}
