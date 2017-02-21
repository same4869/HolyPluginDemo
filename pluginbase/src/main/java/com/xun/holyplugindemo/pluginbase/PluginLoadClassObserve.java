package com.xun.holyplugindemo.pluginbase;

/**
 * Created by lixiaoyu on 2016/8/18.
 */
public interface PluginLoadClassObserve {
    public void loadClassStart();
    public void loadClassLoading(int process);
    public void loadClassSuccess(Class<?> cls);
    public void loadClassFailed(String msg);
}
