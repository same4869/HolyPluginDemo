package com.wenba.comm.web.core;

import com.wenba.comm.APPUtil;
import com.wenba.comm.BBLog;
import com.wenba.comm.R;
import com.wenba.comm.eventlog.UserEvent;
import com.wenba.comm.eventlog.UserEventHandler;
import com.yolanda.nohttp.error.NetworkError;
import com.yolanda.nohttp.error.NotFoundCacheError;
import com.yolanda.nohttp.error.ParseError;
import com.yolanda.nohttp.error.ServerError;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.error.URLError;
import com.yolanda.nohttp.error.UnKnownHostError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Response;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ProtocolException;

/**
 * Created by Lijj on 16/9/18.
 */
public abstract class WenbaResponse<T> implements OnResponseListener<T>{

    private String mUrl;

    public void setUrl(String url){
        this.mUrl = url;
    }

    public abstract void onResponse(T response);

    public abstract void onExcepetion(String msg);

    public abstract void onStart();

    public abstract void onFinish();

    @Override
    public void onStart(int i) {
        onStart();
    }

    @Override
    public void onFinish(int i) {
        onFinish();
    }

    /**
     * Server correct response to callback when an HTTP request.
     *
     * @param what     the credit of the incoming request is used to distinguish between multiple requests.
     * @param response successful callback.
     */
    @Override
    public void onSucceed(int what, Response<T> response) {
        onResponse(response.get());
    }

    /**
     * When there was an error correction.
     *
     * @param what     the credit of the incoming request is used to distinguish between multiple requests.
     * @param response failure callback.
     */
    @Override
    public void onFailed(int what, Response<T> response) {
        String msg = null;
        int logCode = 0;

        Exception exception = response.getException();

        if (exception instanceof NetworkError) {// 网络不好
            msg = APPUtil.getString(R.string.error_wenba);
            logCode = 1;
        } else if (exception instanceof TimeoutError) {// 请求超时
            msg = APPUtil.getString(R.string.error_request_timeout);
            logCode = 9;
        } else if (exception instanceof UnKnownHostError) {// 找不到服务器
            msg = APPUtil.getString(R.string.error_request_unknown_host_error);
            logCode = 5;
        } else if (exception instanceof URLError) {// URL是错的
            msg = APPUtil.getString(R.string.error_request_url_error);
            logCode = 6;
        } else if (exception instanceof NotFoundCacheError) {// 这个异常只会在仅仅查找缓存时没有找到缓存时返回
            msg = APPUtil.getString(R.string.error_request_not_found_cache_error);
            logCode = 11;
        } else if (exception instanceof ProtocolException) {
            msg = APPUtil.getString(R.string.error_request_protocol_exception);
            logCode = 4;
        } else if (exception instanceof ParseError) {
            msg = APPUtil.getString(R.string.error_response_data);
            logCode = 7;
        } else if (exception instanceof ServerError) {
            msg = APPUtil.getString(R.string.error_request_error_server);
            logCode = 3;
        } else {
            msg = APPUtil.getString(R.string.error_network);
            logCode = 0;
        }

        BBLog.e(BaseHttpRequest.TAG,"错误：" + exception.getMessage());

        UserEvent userEvent = new UserEvent("app_exception");
        userEvent.addEventArgs("url",mUrl);
        userEvent.addEventArgs("error_code",String.valueOf(logCode));
        UserEventHandler.addEvent(userEvent);

        onExcepetion(msg);
    }

    public Class<T> getResponseClass(){
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        return (Class<T>) params[0];
    }

}
