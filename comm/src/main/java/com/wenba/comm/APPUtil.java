package com.wenba.comm;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class APPUtil {
	private static Toast toast = null;

	public static int dpToPx(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dipValue * scale + 0.5f);
	}

	public static int spToPx(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	// @SuppressLint({ "NewApi", "InflateParams" })
	// public static final void showToast(String text, final int duration, final
	// int topMargin) {
	// if (TextUtils.isEmpty(text)) {
	// return;
	// }
	// final Context context =
	// WenbaApplication.getInstance().getApplicationContext();
	// final View toastRoot =
	// LayoutInflater.from(context).inflate(R.layout.view_comm_toast,
	// null);
	// TextView message = (TextView) toastRoot.findViewById(R.id.toast_text);
	// if (message != null) {
	// message.setText(text);
	// }
	//
	// WenbaThreadPool.runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// Toast toastStart = getToast(context);
	// if (toastStart == null) {
	// return;
	// }
	// toastStart.setGravity(Gravity.TOP, 0,
	// (int) ((topMargin == 0 ? (50 * ScreenUtils.getScreenDensity(context)) :
	// topMargin)));
	// toastStart.setDuration(duration);
	// toastStart.setView(toastRoot);
	// toastStart.show();
	// }
	// });
	//
	// }

	private static Toast getToast(Context context) {
		if (toast != null) {
			return toast;
		}
		if (context == null) {
			return null;
		}
		toast = new Toast(context.getApplicationContext());
		return toast;
	}

	public static final void showToast(String text) {
		// showToast(context, text, Toast.LENGTH_LONG, 0);
		if (TextUtils.isEmpty(text)) {
			return;
		}
		Context context = WenbaApplication.getInstance().getApplicationContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.comm_tip_message_layout, null);
		TextView textView = (TextView) layout.findViewById(R.id.comm_tip_msg_tv);
		textView.setText(text);
		// 实例化一个Toast对象
		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(layout);
		toast.show();
	}

	public static final void showToast(String text, int duration) {
		if (TextUtils.isEmpty(text)) {
			return;
		}
		Context context = WenbaApplication.getInstance().getApplicationContext();
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.comm_tip_message_layout, null);
		TextView textView = (TextView) layout.findViewById(R.id.comm_tip_msg_tv);
		textView.setText(text);
		// 实例化一个Toast对象
		Toast toast = new Toast(context);
		toast.setDuration(duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(layout);
		toast.show();
	}

	public static final void cancelToast(Context context) {
		Toast toastStart = getToast(context);
		if (toastStart != null) {
			toastStart.cancel();
		}
	}

	/**
	 * 获得NotificationManager
	 * 
	 * @param context
	 * @return
	 */
	public static NotificationManager getNotificationManager(Context context) {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/**
	 * 获得Notification
	 * 
	 * @param context
	 * @param title
	 * @param message
	 * @param intent
	 * @param type
	 * @param flag
	 * @param voice
	 * @return
	 */
	public static Notification generateNotification(Context context, String title, String message, Intent intent,
			int type, int flag, boolean voice) {

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		PendingIntent contentIndent = null;
		if(type == 2){// 后台推过来的  要做点击统计
			contentIndent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}else {//学霸君内部发送通知
			contentIndent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		builder.setContentIntent(contentIndent).setSmallIcon(R.mipmap.ic_launcher).setWhen(System.currentTimeMillis())// 设置时间发生时间
				.setAutoCancel(true)// 设置可以清除
				.setTicker(title).setContentTitle(title)// 设置下拉列表里的标题
				.setContentText(message);// 设置上下文内容
		Notification notification = builder.build();
		notification.flags |= flag | Notification.FLAG_ONLY_ALERT_ONCE;
		if (voice) {
			notification.defaults = Notification.DEFAULT_ALL;
		}
		return notification;
	}

	/**
	 * cancel notification
	 * 
	 * @param context
	 */
	public static void cancelAllNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	/**
	 * 调起本地应用市场评论应用
	 * 
	 * @param context
	 * @param noMarket
	 */
	public static void toScore(Context context, String noMarket) {
		try {
			StringBuffer sb = new StringBuffer().append("market://details?id=").append(context.getPackageName());
			Uri uri = Uri.parse(sb.toString());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			APPUtil.showToast(noMarket);
		}
	}

	public static String getString(int stringId) {
		try {
			return WenbaApplication.getInstance().getString(stringId);
		} catch (Exception e) {
			BBLog.w("wenba", e);
			return null;
		}
	}

	/**
	 * 获得手机安装应用的所有包名
	 * 
	 * @param context
	 * @return
	 */
	public static ArrayList<String> getInstalledAppcations(Context context) {
		ArrayList<String> packageList = null;
		PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		if (installedApps != null) {
			packageList = new ArrayList<String>();
			for (ApplicationInfo appInfo : installedApps) {
				packageList.add(appInfo.packageName);
			}
		}
		return packageList;
	}

	/**
	 * 根据packageName 获得应用的名称
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getApplicationName(Context context, String packageName) {
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(packageName, 0);
			return packageManager.getApplicationLabel(applicationInfo).toString();
		} catch (PackageManager.NameNotFoundException e) {
			applicationInfo = null;
		}
		return null;
	}

	/**
	 * 获得手机当前打开的应用包名
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getTopApplication(final Context context) {
		try {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasks = am.getRunningTasks(1);
			if (tasks != null && !tasks.isEmpty()) {
				ComponentName topActivity = tasks.get(0).topActivity;
				if (topActivity != null) {
					return topActivity.getPackageName();
				}
			}
		} catch (Exception e) {
			BBLog.w("wenba", e);
		}
		return null;
	}

	public static Intent getSystemCameraIntent(Context context, String picPath) {
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);

		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

		for (ResolveInfo info : infos) {

			ApplicationInfo appInfo = info.activityInfo.applicationInfo;
			if (appInfo == null) {
				continue;
			}

			try {
				Field field = ResolveInfo.class.getDeclaredField("system");

				boolean isSystem = field.getBoolean(info);
				if (isSystem) {
					intent.setPackage(appInfo.packageName);
					break;
				}

			} catch (Exception e) {
				if (appInfo.publicSourceDir != null && appInfo.publicSourceDir.contains("/system")) {
					intent.setPackage(appInfo.packageName);
				}
			}
		}

		if (picPath != null) {
			Uri takeCameraOutUri = Uri.fromFile(new File(picPath));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, takeCameraOutUri);
		}
		return intent;
	}

	/**
	 * 获得手机型号
	 * 
	 * @return
	 */
	public static String getPhoneType() {
		return android.os.Build.BRAND + "_" + android.os.Build.MODEL;
	}

	/**
	 * 添加快捷方式
	 */
	public static void creatShortCut(Activity activity, String shortcutName, int resourceId) {
		Intent intent = new Intent();
		intent.setClass(activity, activity.getClass());
		/* 以下两句是为了在卸载应用的时候同时删除桌面快捷方式 */
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		// 不允许重复创建
		shortcutintent.putExtra("duplicate", false);
		// 需要现实的名称
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
		// 快捷图片
		Parcelable icon = Intent.ShortcutIconResource.fromContext(activity.getApplicationContext(), resourceId);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		// 点击快捷图片，运行的程序主入口
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		// 发送广播。OK
		activity.sendBroadcast(shortcutintent);
	}

	/**
	 * 删除快捷方式
	 */
	public static void deleteShortCut(Activity activity, String shortcutName) {
		Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
		// 快捷方式的名称
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
		// 在网上看到到的基本都是一下几句，测试的时候发现并不能删除快捷方式。
		// String appClass = activity.getPackageName()+"."+
		// activity.getLocalClassName();
		// ComponentName comp = new ComponentName( activity.getPackageName(),
		// appClass);
		// shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new
		// Intent(Intent.ACTION_MAIN).setComponent(comp));
		/** 改成以下方式能够成功删除，估计是删除和创建需要对应才能找到快捷方式并成功删除 **/
		Intent intent = new Intent();
		intent.setClass(activity, activity.getClass());
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		activity.sendBroadcast(shortcut);
	}

	/**
	 * 判断是否存在快捷方式
	 */
	public static boolean hasShortcut(Activity activity, String shortcutName) {
		String url = "";
		/* 大于8的时候在com.android.launcher2.settings 里查询（未测试） */
		if (android.os.Build.VERSION.SDK_INT < 8) {
			url = "content://com.android.launcher.settings/favorites?notify=true";
		} else {
			url = "content://com.android.launcher2.settings/favorites?notify=true";
		}
		ContentResolver resolver = activity.getContentResolver();
		Cursor cursor = resolver.query(Uri.parse(url), null, "title=?", new String[] { shortcutName }, null);
		if (cursor != null && cursor.moveToFirst()) {
			cursor.close();
			return true;
		}
		return false;
	}

	/**
	 * 获取当前进程名
	 * 
	 * @param context
	 * @return 进程名
	 */
	public static final String getProcessName(Context context) {
		String processName = null;

		ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));

		while (true) {
			for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
				if (info.pid == android.os.Process.myPid()) {
					processName = info.processName;
					break;
				}
			}

			// go home
			if (!TextUtils.isEmpty(processName)) {
				return processName;
			}

			// take a rest and again
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static boolean fileIsExists(String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
