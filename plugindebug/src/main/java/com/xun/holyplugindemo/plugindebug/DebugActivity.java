package com.xun.holyplugindemo.plugindebug;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.xun.holyplugindemo.pluginbase.corepage.CorePageActivity;
import com.xun.holyplugindemo.pluginbase.corepage.core.CoreAnim;

public class DebugActivity extends CorePageActivity {
    private static final String PAGE_NAME = "page_name";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getActivityMetaDataBundle(getPackageManager(), getComponentName());

        String pageName = bundle.getString(PAGE_NAME);
        openPluginPage(pageName);
    }

    private void openPluginPage(String pageName) {
        if (pageName != null) {
            openPage(pageName, null, CoreAnim.none, true);
        }
    }

    /**
     * 获取Activity中的meta-data.
     *
     * @param packageManager
     * @param component
     * @return
     */
    private Bundle getActivityMetaDataBundle(PackageManager packageManager, ComponentName component) {
        Bundle bundle = null;
        try {
            ActivityInfo ai = packageManager.getActivityInfo(component, PackageManager.GET_META_DATA);
            bundle = ai.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return bundle;
    }

}
