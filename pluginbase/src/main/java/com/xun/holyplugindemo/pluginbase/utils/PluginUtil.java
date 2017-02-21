package com.xun.holyplugindemo.pluginbase.utils;

import com.xun.holyplugindemo.pluginbase.core.PluginInfo;
import com.xun.holyplugindemo.pluginbase.manager.PluginManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Lijj on 16/8/9.
 */
public class PluginUtil {
    private static volatile Executor installPluginPool;

    private static final Executor getInstallPluginExecutor() {
        if (installPluginPool != null) {
            return installPluginPool;
        }

        synchronized (PluginUtil.class) {
            if (installPluginPool == null) {
                installPluginPool = Executors.newSingleThreadExecutor();
            }
        }

        return installPluginPool;
    }

    public static String getSoMappingValue(String pluginName, String key) {
        try {
            PluginInfo pluginInfo = PluginManager.getPluginInfo(pluginName);
            if (pluginInfo != null) {
                return pluginInfo.pluginBase.getMappingValue(key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void installPluginPoolExecute(Runnable runnable){
        getInstallPluginExecutor().execute(runnable);
    }

}
