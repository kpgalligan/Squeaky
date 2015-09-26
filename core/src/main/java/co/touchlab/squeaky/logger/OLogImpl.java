package co.touchlab.squeaky.logger;

/**
 * Interface so we can front various log code which may or may not be in the classpath.
 *
 * @author graywatson
 */
public interface OLogImpl
{
	void d(String tag, String message);

	void d(String tag, String message, Throwable t);

	void i(String tag, String message);

	void i(String tag, String message, Throwable t);

	void w(String tag, String message);

	void w(String tag, String message, Throwable t);

	void e(String tag, String message);

	void e(String tag, String message, Throwable t);
}
