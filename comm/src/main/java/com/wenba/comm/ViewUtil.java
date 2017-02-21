package com.wenba.comm;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ViewUtil {
	
	private static long lastClickTime;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void setBackground(View view,Drawable drawable){
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			view.setBackground(drawable);
		}else{
			view.setBackgroundDrawable(drawable);
		}
	}
	
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (timeD > 0 && timeD < 600) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
	
	public static boolean isFastDoubleClick(long whatTime) {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (timeD > 0 && timeD < whatTime) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

}
