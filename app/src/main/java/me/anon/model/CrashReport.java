package me.anon.model;

import java.io.Serializable;

public class CrashReport implements Serializable
{
	// App information
	private String version = "unknown";
	private String packageName = "unknown";
	private String versionCode = "0";

	// Device information
	private String model = "unknown";
	private String manufacturer = "unknown";
	private String osVersion = "unknown";

	private Throwable exception;
	private String additionalMessage = "";
	private long timestamp = 0L;

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public String getVersionCode()
	{
		return versionCode;
	}

	public void setVersionCode(String versionCode)
	{
		this.versionCode = versionCode;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel(String model)
	{
		this.model = model;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public void setManufacturer(String manufacturer)
	{
		this.manufacturer = manufacturer;
	}

	public String getOsVersion()
	{
		return osVersion;
	}

	public void setOsVersion(String osVersion)
	{
		this.osVersion = osVersion;
	}

	public Throwable getException()
	{
		return exception;
	}

	public void setException(Throwable exception)
	{
		this.exception = exception;
	}

	public String getAdditionalMessage()
	{
		return additionalMessage;
	}

	public void setAdditionalMessage(String additionalMessage)
	{
		this.additionalMessage = additionalMessage;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
}
