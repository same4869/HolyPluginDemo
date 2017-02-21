package com.wenba.comm.eventlog.bi.http;

/**
 * Created by lqp on 16-11-15.
 */

public class BasicHeader {
    private final String name;
    private final String value;

    public BasicHeader(String name, String value) {
        if(name == null) {
            throw new IllegalArgumentException("Name may not be null");
        } else {
            this.name = name;
            this.value = value;
        }
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}
