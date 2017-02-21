package com.xun.holyplugindemo.pluginbase.exception;

/**
 * Created by Lijj on 16/5/19.
 */
public class PluginNotLoadException extends Exception{

    public PluginNotLoadException(){
        this("plugin not load");
    }

    public PluginNotLoadException(String message){
        super(message);
    }

}
