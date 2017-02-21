package com.au.util;

/**
 * Created by Lijj on 16/8/9.
 */
public interface ICrashUpdate {
    public void updateCrash(String crashInfo, boolean jni);

    public void sendMessage(String info);
}
