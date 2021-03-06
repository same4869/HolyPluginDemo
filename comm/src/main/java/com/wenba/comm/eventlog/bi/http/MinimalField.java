package com.wenba.comm.eventlog.bi.http;

/**
 * Created by lqp on 16-11-15.
 */

public class MinimalField {

    private final String name;
    private final String value;

    public MinimalField(final String name, final String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getBody() {
        return this.value;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(this.name);
        buffer.append(": ");
        buffer.append(this.value);
        return buffer.toString();
    }
}
