package com.xun.holyplugindemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xun.holyplugindemo.bbcomm.config.PageParam;
import com.xun.holyplugindemo.pluginbase.corepage.CorePageFragment;
import com.xun.holyplugindemo.pluginbase.corepage.core.CoreAnim;

/**
 * Created by xunwang on 17/2/21.
 */

public class MainFragment extends CorePageFragment implements View.OnClickListener {
    private Button scanwordBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, null);
        scanwordBtn = (Button) rootView.findViewById(R.id.app_scanword_btn);
        scanwordBtn.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_scanword_btn:
                openPage(PageParam.ScanwordMainFragment, new Bundle(), CoreAnim.slide, true, true);
                break;
        }
    }
}
