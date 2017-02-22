package com.xun.holyplugindemo.bbcomm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.xun.holyplugindemo.bbcomm.R;


/**
 * Created by xunwang on 17/2/22.
 */

public class CommDiyView extends FrameLayout {
    public CommDiyView(Context context) {
        super(context);
        init(context);
    }

    public CommDiyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommDiyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_comm_diy, this);
    }
}
