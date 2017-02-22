package com.xun.holyplugindemo.plugindebug;

import com.wenba.comm.WenbaApplication;
import com.xun.holyplugindemo.pluginbase.manager.CorePageManager;
import com.xun.holyplugindemo.pluginbase.manager.PluginInitializer;
import com.xun.holyplugindemo.pluginbase.update.IPluginUpdateRequest;
import com.xun.holyplugindemo.pluginbase.update.PluginUpdateInfoListener;

import java.util.HashMap;

/**
 * Created by xunwang on 17/2/21.
 */

public class DebugApplication extends WenbaApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        CorePageManager.init(this, null, true);

        boolean validPublicKey = false;
        PluginInitializer.init(getApplicationContext(), new PluginUpdateRequest(), true, validPublicKey);
    }

    public static class PluginUpdateRequest implements IPluginUpdateRequest {
        @Override
        public void startRequest(String requestParam, PluginUpdateInfoListener pluginUpdateInfoListener) {
            HashMap<String, String> params = new HashMap<String, String>();
            if (requestParam != null) {
                params.put("param", requestParam);
            }
//            WenbaRequest request = new WenbaRequest(SoUtil.getUrl(SoMapping.PLUGIN_CHECK_UPDATE), params, new
//                    PluginRequestListener(pluginUpdateInfoListener));
//            WenbaWebLoader.startHttpLoader(request);
        }
    }

    @Override
    public boolean isDebug() {
        return true;
    }
}
