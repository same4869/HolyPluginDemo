package com.xun.holyplugindemo.bbcomm.plugin;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.xun.holyplugindemo.pluginbase.corepage.CorePageFragment;
import com.xun.holyplugindemo.pluginbase.exception.PluginNotFindException;
import com.xun.holyplugindemo.pluginbase.manager.PluginManager;

public abstract class PluginFragment extends CorePageFragment {
	
	private Context mContext;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mContext = PluginManager.getPluginContext(getPluginName());
		} catch (PluginNotFindException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Context getPluginContext() {
		return mContext;
	}

	public Resources getPluginResources() {
		return mContext.getResources();
	}

    public LayoutInflater getPluginInflater() {
        return LayoutInflater.from(mContext);
    }

	protected abstract String getPluginName();

}
