package me.anon.lib.helper;

import com.squareup.otto.Bus;

public final class BusHelper
{
	private final Bus BUS = new Bus();
	private static BusHelper instance = new BusHelper();

	private BusHelper()
	{

	}

	public static BusHelper getInstance()
	{
		if (instance == null)
		{
			 instance = new BusHelper();
		}

		return instance;
	}

	public void register(Object object)
	{
		try
		{
			BUS.register(object);
		}
		catch (Exception ignore)
		{
		}
	}

	public void unregister(Object object)
	{
		try
		{
			BUS.unregister(object);
		}
		catch (Exception ignore)
		{
		}
	}

	public void post(Object object)
	{
		try
		{
			BUS.post(object);
		}
		catch (Exception ignore)
		{
		}
	}
}
