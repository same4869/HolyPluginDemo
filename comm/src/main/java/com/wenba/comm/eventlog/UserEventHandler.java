
package com.wenba.comm.eventlog;

public class UserEventHandler {
	public static final int FORMAT_VERSION = 1;

	private static EventTask mEventTask;

	public static void initEventTask(EventTask eventTask){
		mEventTask = eventTask;
	}
	
	/**
	 * 上传log
	 */
	public static void wakeUploadTask() {
		if(mEventTask != null){
			mEventTask.wakeUploadInner();
		}
	}

	/**
	 * 记录日志
	 * 
	 * @param log
	 */
	public static void addEvent(UserEvent log) {
		if(mEventTask != null){
			mEventTask.addEvent(log);
		}
	}
	

	public static void onUserLogout() {
		if(mEventTask != null){
			mEventTask.onUserLogout();
			mEventTask = null;
		}
	}
	
	public static void makeSnapshot(boolean saveBeforeExit) {
		if(mEventTask != null){
			mEventTask.makeSnapshot(saveBeforeExit);
		}

	}

	public static String getSessionId(){
		if(mEventTask != null){
			return mEventTask.getSessionId();
		}
		return "";
	}

	public static int getSubSessionId() {
		if(mEventTask != null){
			return mEventTask.getSubSessionId();
		}
		return 0;
	}

	public static void increaseSubsessionId(){
		if(mEventTask != null){
			mEventTask.increaseSubsessionId();
		}
	}
}
