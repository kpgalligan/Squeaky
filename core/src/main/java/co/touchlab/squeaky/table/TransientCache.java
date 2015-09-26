package co.touchlab.squeaky.table;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kgalligan on 8/8/15.
 */
public class TransientCache
{
	private Map<Class, Map<Object, Object>> cache = new HashMap<>();

	public void put(Class c, Object id, Object data)
	{
		Map<Object, Object> objectObjectMap = primeCache(c);
		objectObjectMap.put(id, data);
	}

	public Object get(Class c, Object id)
	{
		Map<Object, Object> objectObjectMap = primeCache(c);
		return objectObjectMap.get(id);
	}

	private Map<Object, Object> primeCache(Class c)
	{
		Map<Object, Object> objectObjectMap = cache.get(c);
		if (objectObjectMap == null)
		{
			objectObjectMap = new HashMap<>();
			cache.put(c, objectObjectMap);
		}
		return objectObjectMap;
	}
}
