package com.xun.holyplugindemo;

import com.wenba.comm.WenbaApplication;
import com.xun.holyplugindemo.pluginbase.manager.CorePageManager;

/**
 * Created by xunwang on 17/2/21.
 */

public class HolyApplication extends WenbaApplication{

    @Override
    public void onCreate() {
        super.onCreate();
        CorePageManager.init(this, CorePageConfig.PAGES_MAPPING,false);
    }

    @Override
    public boolean isDebug() {
        return true;
    }
}
