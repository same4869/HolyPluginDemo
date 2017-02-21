package com.wenba.comm.web.core;

import com.yolanda.nohttp.OnUploadListener;

/**
 * Created by Lijj on 16/9/20.
 */
public abstract class WenbaUploadResponse<T> extends WenbaResponse<T> implements OnUploadListener{

    public abstract void onCancle();

    public abstract void onProgress(int progress);

    @Override
    public void onCancel(int what) {
        onCancle();
    }

    @Override
    public void onProgress(int what, int progress) {
        onProgress(progress);
    }

    @Override
    public void onError(int what, Exception exception) {
        onExcepetion(null);
    }

}
