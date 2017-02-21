package com.wenba.comm.web.core;

import com.wenba.comm.BBLog;
import com.yolanda.nohttp.BasicBinary;
import com.yolanda.nohttp.FileBinary;
import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.RestRequest;
import com.yolanda.nohttp.tools.MultiValueMap;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Lijj on 16/9/22.
 */

public abstract class BaseHttpRequest<T> extends RestRequest<T> {
    public static final String TAG = "wenba_req";

    private static final String CACHE_TIME = "cache_time";
    private static final SimpleDateFormat monthSDF = new SimpleDateFormat("yyyy-MMM");
    private static final SimpleDateFormat weekSDF = new SimpleDateFormat("yyyy-ww");
    private static final SimpleDateFormat daySDF = new SimpleDateFormat("yyyy-mm-dd");
    private static final SimpleDateFormat hourSDF = new SimpleDateFormat("yyyy-mm-dd HH");

    private WenbaResponse mWenbaResponse;

    public enum CacheTimeType {
        MONTH,
        WEEK,
        DAY,
        HOUR,
        NEVER
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        setCacheMode(cacheMode, CacheTimeType.NEVER);
    }

    public void setCacheMode(CacheMode cacheMode, CacheTimeType cacheTimeType) {
        setCacheMode(cacheMode, cacheTimeType, null);
    }

    public void setCacheMode(CacheMode cacheMode, CacheTimeType cacheTimeType, String[] ignoreCacheParams) {
        super.setCacheMode(cacheMode);
        switch (cacheTimeType) {
            case MONTH:
                add(CACHE_TIME, monthSDF.format(new Date(System.currentTimeMillis())));
                break;
            case WEEK:
                add(CACHE_TIME, weekSDF.format(new Date(System.currentTimeMillis())));
                break;
            case DAY:
                add(CACHE_TIME, daySDF.format(new Date(System.currentTimeMillis())));
                break;
            case HOUR:
                add(CACHE_TIME, hourSDF.format(new Date(System.currentTimeMillis())));
                break;
            case NEVER:
            default:
                break;
        }

        setCacheKey(buildCacheKey(getParamKeyValues(), ignoreCacheParams));

        BBLog.e("ljj", "cachekey = " + getCacheKey());
    }

    /**
     * 根据请求参数获得缓存key
     * @param paramMap
     * @param ignoreCacheParams
     * @return
     */
    private String buildCacheKey(MultiValueMap<String, Object> paramMap, String[] ignoreCacheParams) {
        StringBuffer paramBuffer = new StringBuffer();
        Set<String> keySet = paramMap.keySet();
        for (String key : keySet) {
            if (ignoreCacheParams != null && ignoreCacheParams.length > 0) {//过滤掉不参与缓存key设置的参数
                boolean flag = false;
                for (int i = 0; i < ignoreCacheParams.length; i++) {
                    if (key.equals(ignoreCacheParams[i])) {
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    continue;
                }
            }


            List<Object> values = paramMap.getValues(key);
            for (Object value : values) {
                if (value != null && value instanceof CharSequence) {
                    paramBuffer.append("&");
                    try {
                        paramBuffer.append(URLEncoder.encode(key, getParamsEncoding()));
                        paramBuffer.append("=");
                        paramBuffer.append(URLEncoder.encode(value.toString(), getParamsEncoding()));
                    } catch (UnsupportedEncodingException e) {
                        paramBuffer.append(key);
                        paramBuffer.append("=");
                        paramBuffer.append(value.toString());
                    }
                }
            }
        }

        paramBuffer.insert(0, "?");
        paramBuffer.insert(0, url());

        if (paramBuffer.length() > 0) {
            paramBuffer.deleteCharAt(0);
        }
        return paramBuffer.toString();
    }

    public BaseHttpRequest(String url, Map<String, String> params, WenbaResponse responseListener) {
        this(url, RequestMethod.POST, params, responseListener);
    }

    public BaseHttpRequest(String url, RequestMethod requestMethod, Map<String, String> params, WenbaResponse
            wenbaResponse) {
        super(url, requestMethod);
        mWenbaResponse = wenbaResponse;
        setAccept(Headers.HEAD_VALUE_ACCEPT_APPLICATION_JSON);

        add(params);
    }

    public BaseHttpRequest(String url, Map<String, String> params, Map<String, String> fileParams,
                           WenbaUploadResponse wenbaResponse) {
        super(url, RequestMethod.POST);
        mWenbaResponse = wenbaResponse;
        setAccept(Headers.HEAD_VALUE_ACCEPT_APPLICATION_JSON);

        add(params);

        if (fileParams != null && !fileParams.isEmpty()) {
            for (Map.Entry<String, String> stringEntry : fileParams.entrySet()) {
                if (stringEntry.getKey() == null || stringEntry.getValue() == null) {
                    continue;
                }
                BasicBinary binary = new FileBinary(new File(stringEntry.getValue()));
                binary.setUploadListener(0, wenbaResponse);
                add(stringEntry.getKey(), binary);
            }
        }
    }

    public WenbaResponse getResponseListener() {
        return mWenbaResponse;
    }

    public abstract boolean certificateLocalKeystore();
}
