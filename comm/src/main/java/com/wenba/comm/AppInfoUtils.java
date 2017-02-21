package com.wenba.comm;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AppInfoUtils {
    private static final String TAG = AppInfoUtils.class.getSimpleName();

    private static String channel = null;
    private static int appVersionCode = -1;
    private static String appVersionName = null;
    private static String mobileDeviceId;

    // 判断是否安装了应用
    public static boolean isAppInstalled(Context context, String packagename) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            BBLog.w("wenba", e);
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 安装APK文件
     */
    public static void installApk(Context context, String filePath) {
        File apkFile = new File(filePath);
        installApk(context, apkFile);
    }

    /**
     * 安装APK文件
     */
    public static void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 获得应用的版本号
     *
     * @param context
     * @return
     */
    public static int getCurrentVersionCode(Context context) {
        if (appVersionCode != -1) {
            return appVersionCode;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            appVersionCode = pinfo.versionCode;
        } catch (NameNotFoundException e) {
            BBLog.w("wenba", e);
        }
        return appVersionCode;
    }

    /**
     * 获得应用的版本名称
     *
     * @param context
     * @return
     */
    public static String getCurrentVersionName(Context context) {
        if (appVersionName != null) {
            return appVersionName;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            appVersionName = pinfo.versionName;
        } catch (NameNotFoundException e) {
            BBLog.w("wenba", e);
        }
        return appVersionName;
    }

    /**
     * 根据meta key获得 value
     *
     * @param context
     * @param metaKey
     * @return
     * @throws NameNotFoundException
     */
    public static String getMetaValue(Context context, String metaKey) throws NameNotFoundException {
        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                PackageManager.GET_META_DATA);
        return appInfo.metaData.getString(metaKey);
    }

    /**
     * 获得channel信息
     *
     * @param context
     * @return
     */
    public static String getChannelByMeta(Context context) {
        if (channel != null) {
            return channel;
        }
        try {
            channel =  getMetaValue(context, "UMENG_CHANNEL");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (StringUtil.isBlank(channel)) {
            channel = "nochannel";
        }
        return channel;
    }

    /**
     * 获得去应用的deviceId
     *
     * @param context
     * @return
     */
    public static String getMobileDeviceInfo(Context context) {
        if (mobileDeviceId != null) {
            return mobileDeviceId;
        }

        if (context == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        BBLog.d(TAG,"app DeviceId="+tm.getDeviceId());
        sb.append(tm.getDeviceId());

        String androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        BBLog.d(TAG,"app ANDROID_ID="+androidId);
        sb.append(androidId);

        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        String macAddress = wifi.getConnectionInfo().getMacAddress();
        BBLog.d(TAG,"app MacAddress="+macAddress);
        sb.append(macAddress);

        mobileDeviceId = sb.toString();
        return mobileDeviceId;
    }

    /**
     * 获得apk签名
     *
     * @param context
     * @param apkPath
     * @return 当签名无法获得，返回null
     */
    public static String getApkSignature(Context context, String apkPath) {
        if (context == null) {
            return "";
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return "";
        }
        PackageInfo mInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES);
        if (mInfo != null) {
            if (mInfo.signatures != null && mInfo.signatures.length > 0) {
                Signature signatures = mInfo.signatures[0];
                try {
                    MessageDigest md2 = MessageDigest.getInstance("SHA1");
                    md2.update(signatures.toByteArray());
                    byte[] digest2 = md2.digest();
                    return toHexString(digest2);
                } catch (NoSuchAlgorithmException e) {
                    BBLog.w("wenba", e);
                }
            } else {
                return null;// 当为null时，无法获得签名
            }
        }
        return "";
    }

    /**
     * 是否是模拟器
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isEmulator(Context context) {

        String serial = android.os.Build.SERIAL;
        if ("unknown".equals(serial)) {
            return true;
        }

        WifiManager wifimanage = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiinfo = wifimanage.getConnectionInfo();
        if (wifiinfo.getMacAddress() == null) {
            return true;
        }

        return false;
    }

    /**
     * 获取单个应用内存限制
     *
     * @param context
     * @return
     */
    public static int getAppMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getMemoryClass();
    }

    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

}
