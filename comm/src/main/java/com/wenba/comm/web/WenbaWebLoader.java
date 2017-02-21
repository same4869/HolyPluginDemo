package com.wenba.comm.web;

import android.app.Application;

import com.wenba.comm.BBLog;
import com.wenba.comm.web.core.BaseHttpRequest;
import com.wenba.comm.web.core.HttpCancel;
import com.wenba.comm.web.core.WenbaResponse;
import com.wenba.comm.web.ssl.SSLContextHandler;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.cookie.CookieStoreListener;
import com.yolanda.nohttp.cookie.DiskCookieStore;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLContext;

/**
 * @Author:Lijj
 * @Date:2014-4-17上午10:34:02
 * @Todo:TODO
 */
public class WenbaWebLoader {
    public static final String COOKIE_NAME = "PHPSESSID";
    public static final String COOKIE_NAME2 = "XBSID";

    /**
     * 请求队列.
     */
    private static RequestQueue mRequestQueue;

    private static SSLContext mSSLContext;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Random mWhatRandom = new Random(Integer.MAX_VALUE);

    public static void initialize(Application application, boolean isDebug) {
        NoHttp.initialize(application);
        NoHttp.setDefaultConnectTimeout(15000);
        NoHttp.setDefaultReadTimeout(15000);
        NoHttp.setEnableCache(true);
        NoHttp.setDefaultCookieManager(new CookieManager(DiskCookieStore.INSTANCE, CookiePolicy.ACCEPT_ALL));

        Logger.setTag("NoHttp_Wenba");
        Logger.setDebug(isDebug);// 开始NoHttp的调试模式, 这样就能看到请求过程和日志
        NoHttp.setEnableCookie(true);

        DiskCookieStore cookieStore = getDiskCookieStore();
        cookieStore.setCookieStoreListener(new CookieManagerListener());
    }

    private static DiskCookieStore getDiskCookieStore() {
        CookieManager cookieManager = NoHttp.getDefaultCookieManager();
        return (DiskCookieStore) cookieManager.getCookieStore();
    }

    public static List<HttpCookie> getHttpCookies() {
        return getDiskCookieStore().getCookies();
    }



    /**
     * 判断当前cookie是否过期
     *
     * @return
     */
    public static boolean hasHttpCookieExpired(boolean logined) {
        BBLog.d(BaseHttpRequest.TAG, "start check cookie, logined = " + logined);

        if(!logined){
            return true;
        }

        boolean cookieExpired = true;

        DiskCookieStore cookieStore = getDiskCookieStore();

        List<HttpCookie> cookies = cookieStore.getCookies();

        for (HttpCookie cookie : cookies) {
            BBLog.d(BaseHttpRequest.TAG, "校验cookie :" +
                    cookie.getName() + " value = " + cookie.getValue() +
                    " domain" + " = " + cookie.getDomain() +
                    " hasExpired = " + cookie.hasExpired() +
                    " date = " + sdf.format(new Date(cookie.getMaxAge() * 1000 + System.currentTimeMillis())));

            if (!COOKIE_NAME.equals(cookie.getName()) && !COOKIE_NAME2.equals(cookie.getName())) {
                continue;
            }

            if(cookie.hasExpired() || "deleted".equals(cookie.getValue())){
                cookieExpired = true;
                break;
            }else{
                cookieExpired = false;
            }
        }
        BBLog.d(BaseHttpRequest.TAG, "cookie 校验结果 是否过期 = " + cookieExpired);
        return cookieExpired;
    }

    private static void setSSLSocketFactory(Request<?> httpsRequest) {
        httpsRequest.setSSLSocketFactory(getSSLContext().getSocketFactory());
        httpsRequest.setHostnameVerifier(SSLContextHandler.HOSTNAME_VERIFIER);
    }

    private static RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            synchronized (WenbaWebLoader.class) {
                if (mRequestQueue == null) {
                    mRequestQueue = NoHttp.newRequestQueue();
                }
            }
        }

        return mRequestQueue;
    }

    private static SSLContext getSSLContext() {
        if (mSSLContext == null) {
            synchronized (WenbaWebLoader.class) {
                if (mSSLContext == null) {
                    mSSLContext = SSLContextHandler.getSSLContext();
                }
            }
        }

        return mSSLContext;
    }

    public static HttpCancel startHttpLoader(BaseHttpRequest request) {
        return startHttpLoader(mWhatRandom.nextInt(), request);
    }

    public static HttpCancel startHttpLoader(int what, BaseHttpRequest request) {
        if(request.certificateLocalKeystore()) {
            BBLog.d(BaseHttpRequest.TAG,request.url()+" certificateLocalKeystore is true");
            setSSLSocketFactory(request);
        }

        HttpCancel cancle = new HttpCancel();
        request.setCancelSign(cancle);

        WenbaResponse response = request.getResponseListener();
        if(response != null) {
            response.setUrl(request.url());
        }

        getRequestQueue().add(what, request, response);

        return cancle;
    }

    public static void cancle(HttpCancel sign) {
        getRequestQueue().cancelBySign(sign);
    }

    public static void cancelAll() {
        getRequestQueue().cancelAll();
    }

    public static class CookieManagerListener implements CookieStoreListener {

        /**
         * When saving a Cookie callback.
         *
         * @param uri    cookie corresponding uri.
         * @param cookie {@link HttpCookie}.
         */
        @Override
        public void onSaveCookie(URI uri, HttpCookie cookie) {
            BBLog.d(BaseHttpRequest.TAG, "onSaveCookie uri = " + uri +
                    " cookie : name = " + cookie.getName() +
                    " domain = " + cookie.getDomain() +
                    " date = " + sdf.format(new Date(cookie.getMaxAge() * 1000 + System.currentTimeMillis())) +
                    " value = " + cookie.getValue());
        }

        /**
         * The callback when deleting cookies.
         *
         * @param uri    cookie corresponding uri.
         * @param cookie {@link HttpCookie}.
         */
        @Override
        public void onRemoveCookie(URI uri, HttpCookie cookie) {
            BBLog.d(BaseHttpRequest.TAG, "onRemoveCookie cookie : " + cookie);
        }
    }

}
