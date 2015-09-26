package co.touchlab.squeaky.misc;

import java.io.Closeable;
import java.io.IOException;

/**
 * Utility class
 *
 * @author graywatson
 */
public class IOUtils
{

	/**
	 * Close the closeable if not null and ignore any exceptions.
	 */
	public static void closeQuietly(Closeable closeable)
	{
		if (closeable != null)
		{
			try
			{
				closeable.close();
			}
			catch (IOException e)
			{
				// ignored
			}
		}
	}
}
