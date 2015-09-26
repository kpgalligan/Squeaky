package co.touchlab.squeaky.logger;

/**
 * Created by kgalligan on 9/7/15.
 */
public class OLog
{
	static
	{
		log = new AndroidLog();
	}

	private static OLogImpl log;

	/**
	 * This is not going to be thread safe.  If you're going to set a custom logger, do it really early in your lifecycle.
	 * I'd suggest Application.onCreate
	 *
	 * @param log
	 */
	public static void setLog(OLogImpl log)
	{
		OLog.log = log;
	}

	public static void d(String tag, String message)
	{
		log.d(tag, message);
	}

	public static void d(String tag, String message, Throwable t)
	{
		log.d(tag, message, t);
	}

	public static void i(String tag, String message)
	{
		log.i(tag, message);
	}

	public static void i(String tag, String message, Throwable t)
	{
		log.i(tag, message, t);
	}

	public static void w(String tag, String message)
	{
		log.w(tag, message);
	}

	public static void w(String tag, String message, Throwable t)
	{
		log.w(tag, message, t);
	}

	public static void e(String tag, String message)
	{
		log.e(tag, message);
	}

	public static void e(String tag, String message, Throwable t)
	{
		log.e(tag, message, t);
	}
}
