package com.wenba.comm;

import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;

public class BaseStoreUtil {

    /**
     * 目录是否可用
     *
     * @param file
     * @return
     */
    public static boolean isAvailableDirectory(File file) {
        if (file == null) {
            return false;
        }

        if (!file.exists()) {
            return false;
        }

        if (!file.isDirectory()) {
            return false;
        }

        if (file.listFiles() == null) {
            return false;
        }

        if(!file.canRead()){
            return false;
        }

        if (!file.canWrite()) {
            return false;
        }

        return true;
    }

    public static boolean isAvailableFile(File file) {
        if (file == null ) {
            return false;
        }

        if (!file.exists()) {
            return false;
        }

        if(!file.canRead()){
            return false;
        }

        if(!file.canWrite()){
            return false;
        }

        return true;
    }

    private static File makeFile(File parentFile, String childPath) {
        if (isAvailableDirectory(parentFile)) {
            File childFile = new File(parentFile, childPath);
            if (!childFile.exists()) {
                if (!childFile.mkdirs()) {
                    return null;
                }
            }
            return childFile;
        }

        return null;
    }

    /**
     * 获得应用的可用根目录
     *
     * @param ctx
     * @param childPath
     * @param external
     * @return
     */
    protected static File getRootDir(Context ctx, String childPath, boolean external) {
        File file = null;
        if (ctx != null) {

            file = makeFile(ctx.getExternalCacheDir(),childPath);
            if(file != null){
                return file;
            }

            if(!external) {//external==true，仅仅需要外部存储
                file = makeFile(ctx.getCacheDir(), childPath);
                if (file != null) {
                    return file;
                }

                file = makeFile(ctx.getFilesDir(), childPath);
                if (file != null) {
                    return file;
                }
            }
        }
        if (file == null && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = makeFile(Environment.getExternalStorageDirectory(),childPath);
        }
        return file;
    }

    public static void closeObject(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception e) {
            BBLog.w("wenba", e);
        }
    }

    public static String readTextAsset(Context context, String name) {
        InputStream stream = null;

        String content = null;
        try {
            stream = context.getResources().getAssets().open(name);

            byte[] data = new byte[stream.available()];

            stream.read(data);

            content = new String(data);
        } catch (Exception e) {
            BBLog.w("wenba", e);
        } finally {
            closeObject(stream);
        }

        return content;
    }

    /**
     * 获得当前应用的运行情况
     *
     * @return
     */
    public static String getAppRuntimeStatus() {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long useMemory = runtime.totalMemory() - runtime.freeMemory();
        long nativeUseMemory = Debug.getNativeHeapAllocatedSize();

        boolean externalStorageAvailable = isExternalStorageAvailable();
        long availableInternalMemorySize = getAvailableInternalMemorySize();
        long totalInternalMemorySize = getTotalInternalMemorySize();
        long availableExternalMemorySize = getAvailableExternalMemorySize();
        long totalExternalMemorySize = getTotalExternalMemorySize();

        long block = 1024 * 1024;

        JSONObject json = new JSONObject();
        try {
            json.put("maxMemory", maxMemory / block);
            json.put("useMemory", useMemory / block);
            json.put("nativeUseMemory", nativeUseMemory / block);
            json.put("externalStorageAvailable", externalStorageAvailable ? 1 : 0);
            json.put("availableInternalMemorySize", availableInternalMemorySize / block);
            json.put("totalInternalMemorySize", totalInternalMemorySize / block);
            json.put("availableExternalMemorySize", availableExternalMemorySize / block);
            json.put("totalExternalMemorySize", totalExternalMemorySize / block);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    /**
     * 外部存储是否可用 (存在且具有读写权限)
     *
     * @return
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return
     */

    @SuppressWarnings("deprecation")
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部空间大小
     *
     * @return
     */

    @SuppressWarnings("deprecation")
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();// Gets the Android data
        // directory
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize(); // 每个block 占字节数
        long totalBlocks = stat.getBlockCount(); // block总数
        return totalBlocks * blockSize;
    }

    /**
     * 获取手机外部可用空间大小
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getAvailableExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            File path = Environment.getExternalStorageDirectory();// 获取SDCard根目录
            StatFs stat = new StatFs(path.getPath());

            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 获取手机外部总空间大小
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getTotalExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            File path = Environment.getExternalStorageDirectory(); // 获取SDCard根目录
            StatFs stat = new StatFs(path.getPath());

            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }


}
