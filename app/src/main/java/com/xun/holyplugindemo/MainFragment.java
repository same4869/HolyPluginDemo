package com.xun.holyplugindemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xun.holyplugindemo.pluginbase.corepage.CorePageFragment;

/**
 * Created by xunwang on 17/2/21.
 */

public class MainFragment extends CorePageFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, null);
        return rootView;
    }
}
