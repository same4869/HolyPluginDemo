package com.xun.holyplugindemo.pluginbase.update;

/**
 * Created by lixiaoyu on 2016/8/11.
 */

public interface IPluginUpdateRequest {
    public void startRequest(String requestParam, com.xun.holyplugindemo.pluginbase.update.PluginUpdateInfoListener pluginUpdateInfoListener);
}
