package com.wenba.comm.eventlog.bi.http;

/**
 * Created by lqp on 16-11-15.
 */

public enum HttpMultipartMode {
    /** RFC 822, RFC 2045, RFC 2046 compliant */
    STRICT,
    /** browser-compatible mode, i.e. only write Content-Disposition; use content charset */
    BROWSER_COMPATIBLE
}
