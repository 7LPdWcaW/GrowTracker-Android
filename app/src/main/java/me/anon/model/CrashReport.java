package me.anon.model;

import java.io.Serializable;

import lombok.Data;

@Data
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
}
