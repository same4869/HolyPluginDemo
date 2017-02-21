package com.xun.holyplugindemo;

import android.os.Bundle;

import com.xun.holyplugindemo.pluginbase.corepage.CorePageActivity;
import com.xun.holyplugindemo.pluginbase.corepage.core.CoreAnim;

public class MainActivity extends CorePageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openPage(MainFragment.class.getSimpleName(), null, CoreAnim.none, true);
    }
}
