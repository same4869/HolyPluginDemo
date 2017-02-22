package com.xun.holyplugindemo.scanword.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xun.holyplugindemo.bbcomm.plugin.PluginFragment;
import com.xun.holyplugindemo.scanword.LauncherPlugin;
import com.xun.holyplugindemo.scanword.R;

/**
 * Created by xunwang on 17/2/22.
 */

public class ScanwordMainFragment extends PluginFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View rootView = getPluginInflater().inflate(R.layout.fragment_scanword_main, null);
        return rootView;
    }

    @Override
    protected String getPluginName() {
        return LauncherPlugin.PLUGIN_NAME;
    }
}
