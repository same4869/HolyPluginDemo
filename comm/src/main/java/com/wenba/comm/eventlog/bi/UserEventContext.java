package com.wenba.comm.eventlog.bi;

import java.io.File;

public interface UserEventContext {
	public File getEventDataDir();
	public String getCommonArguments();
	
	public boolean isWiFiAvaliable();
	
	public void postTask(Runnable runnable);
	
	/**
	 * 
	 * @return 2G, 3G, 4G
	 */
	public String getMobileNetwork();
	
	public void logError(String tag, String msg);
	public void logInfo(String tag, String msg);
}
