package com.xun.holyplugindemo.pluginbase.exception;

/**
 * Created by Lijj on 16/5/19.
 */
public class PluginVersionBelowException extends Exception{

    public PluginVersionBelowException(){
        this("plugin version below");
    }

    public PluginVersionBelowException(String message){
        super(message);
    }

}
