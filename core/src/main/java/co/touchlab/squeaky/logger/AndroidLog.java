package co.touchlab.squeaky.logger;

import android.util.Log;

/**
 * Created by kgalligan on 9/7/15.
 */
public class AndroidLog implements OLogImpl
{
	@Override
	public void d(String tag, String message)
	{
		Log.d(tag, message);
	}

	@Override
	public void d(String tag, String message, Throwable t)
	{
		Log.d(tag, message, t);
	}

	@Override
	public void i(String tag, String message)
	{
		Log.i(tag, message);
	}

	@Override
	public void i(String tag, String message, Throwable t)
	{
		Log.i(tag, message, t);
	}

	@Override
	public void w(String tag, String message)
	{
		Log.w(tag, message);
	}

	@Override
	public void w(String tag, String message, Throwable t)
	{
		Log.w(tag, message, t);
	}

	@Override
	public void e(String tag, String message)
	{
		Log.e(tag, message);
	}

	@Override
	public void e(String tag, String message, Throwable t)
	{
		Log.e(tag, message, t);
	}
}
