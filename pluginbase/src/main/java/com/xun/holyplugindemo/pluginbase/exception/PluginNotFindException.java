package com.xun.holyplugindemo.pluginbase.exception;

/**
 * Created by Lijj on 16/5/19.
 */
public class PluginNotFindException extends Exception{

    public PluginNotFindException(){
        this("plugin not find");
    }

    public PluginNotFindException(String message){
        super(message);
    }

}
