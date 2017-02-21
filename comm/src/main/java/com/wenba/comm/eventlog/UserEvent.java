package com.wenba.comm.eventlog;

import com.wenba.comm.BBLog;
import com.wenba.comm.DateUtil;
import com.wenba.comm.json.JSONToBeanHandler;

import java.io.Serializable;
import java.util.HashMap;

public class UserEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5829275064183279840L;
	
	private static final String LINE_TYPE_RECORD = "record";
	private static final String EVENT_NAME_PREFIX = "app.xbj.";// <一级模块名>.<二级模块名>

	public String line_type;
	public long sequence_number;
	public long event_mts;
	public String event_name;
	public HashMap<String, String> event_args;

	public UserEvent(String event_name) {
		super();
		this.line_type = LINE_TYPE_RECORD;
		this.event_mts = DateUtil.getCurWenbaTime();
		this.event_name = EVENT_NAME_PREFIX + event_name;
	}
	
	public void addEventArgs(String key, String value) {
		if (event_args == null) {
			event_args = new HashMap<String, String>();
		}
		event_args.put(key, value);
	}

	public void removeEventArgs(String key) {
		if (event_args == null) {
			return;
		}
		event_args.remove(key);
	}

	@Override
	public String toString() {
		String result = null;
		try {
			result = JSONToBeanHandler.toJsonString(this);
		} catch (Exception e) {
			BBLog.w("wenba", e);
		}
		return result;
	}

}
