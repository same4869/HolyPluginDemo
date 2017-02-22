package com.xun.holyplugindemo.scanword;

import android.content.Context;

import com.xun.holyplugindemo.pluginbase.core.PluginBase;
import com.xun.holyplugindemo.scanword.ui.ScanwordMainFragment;


public class LauncherPlugin extends PluginBase {

	public static final String PLUGIN_NAME = PLUGIN_SCANWORD;

	private static String[][] mConfigs = {
			{ ScanwordMainFragment.class.getSimpleName(), ScanwordMainFragment.class.getName() },
	};

	public LauncherPlugin(Context hostContext){
		super(hostContext);
	}

	@Override
	public String[][] getConfigPages() {
		return mConfigs;
	}

	@Override
	public String getPluginName() {
		return PLUGIN_SCANWORD;
	}


}
