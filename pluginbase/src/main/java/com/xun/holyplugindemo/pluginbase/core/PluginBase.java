package com.xun.holyplugindemo.pluginbase.core;

import android.content.Context;

import com.wenba.comm.BBLog;
import com.wenba.comm.EncryptUtil;
import com.xun.holyplugindemo.pluginbase.corepage.core.CorePage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public abstract class PluginBase {
    public static final String PLUGIN_CLASSLIVE = "p_classlive";
    public static final String PLUGIN_COLLECT = "p_collect";
    public static final String PLUGIN_COMP = "p_comp";
    public static final String PLUGIN_EXERCISE = "p_exercise";
    public static final String PLUGIN_GUWEN = "p_guwen";
    public static final String PLUGIN_KEFU = "p_kefu";
    public static final String PLUGIN_LIVE = "p_live";
    public static final String PLUGIN_MESSAGE = "p_message";
    public static final String PLUGIN_PHOTOSELECTOR = "p_photoselector";
    public static final String PLUGIN_SCANWORD = "p_scanword";
    public static final String PLUGIN_ZXING = "p_zxing";
    public static final String PLUGIN_HISTORY = "p_history";
    public static final String PLUGIN_USER = "p_user";
    public static final String PLUGIN_CREDIT = "p_credit";

    private Context mContext;

    private JSONObject readOnlyData;

    private Map<String, CorePage> mPageMap = new HashMap<String, CorePage>();

    protected abstract String[][] getConfigPages();

    protected abstract String getPluginName();

    public PluginBase(Context context) {
        mContext = context;
        readBaseConfig(getConfigPages());
        initEncrypt();
    }

    public String getMappingValue(String key) {
        if (readOnlyData != null) {
            try {
                return readOnlyData.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void initEncrypt() {
        byte[] encryptByte = getFromAssets(mContext, getPluginName() + ".dat");
        if (encryptByte == null) {
            return;
        }
        byte[] decryptByte = EncryptUtil.soDecryptValue(encryptByte);
        if (decryptByte == null) {
            return;
        }
        String decryptStr = new String(decryptByte);

        try {
            readOnlyData = new JSONObject(decryptStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static boolean isFileInAssets(Context context, String fileName) {
        try {
            String[] listName = context.getAssets().list("");
            for (int i = 0; i < listName.length; i++) {
                if (fileName.endsWith(listName[i])) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static byte[] getFromAssets(Context context, String fileName) {
        if (context == null || fileName == null) {
            return null;
        }

        if (!isFileInAssets(context, fileName)) {
            return null;
        }

        byte[] dataBuffer = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(fileName);
            dataBuffer = new byte[inputStream.available()];

            inputStream.read(dataBuffer);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return dataBuffer;
    }


    private void readBaseConfig(String[][] config) {
        if (config != null && config.length > 0) {
            for (int i = 0; i < config.length; i++) {
                CorePage corePage = new CorePage(config[i][0], config[i][1], null, getPluginName());
                BBLog.d("plugin", corePage.toString());
                mPageMap.put(config[i][0], corePage);
            }
        }
    }

    public CorePage getCorePage(String pageName) {
        if (mPageMap.containsKey(pageName)) {
            return mPageMap.get(pageName);
        }
        return null;
    }

}
