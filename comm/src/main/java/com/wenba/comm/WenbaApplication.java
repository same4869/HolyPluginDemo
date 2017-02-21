package com.wenba.comm;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public abstract class WenbaApplication extends Application {
	protected static WenbaApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
        instance = this;
	}

	public static WenbaApplication getInstance() {
		return instance;
	}

	protected boolean inMainProcess() {
		String packageName = getPackageName();
		String processName = APPUtil.getProcessName(this);

		return packageName.equals(processName);
	}

	public SharedPreferences getWenbaSharedPreferences(String tbl) {
		return getSharedPreferences(tbl, Context.MODE_PRIVATE);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.e("ljj", "onTerminate : "+APPUtil.getProcessName(this));
	}

	public abstract boolean isDebug();

}
