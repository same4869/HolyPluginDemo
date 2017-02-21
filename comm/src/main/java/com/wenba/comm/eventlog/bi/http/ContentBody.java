package com.wenba.comm.eventlog.bi.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by lqp on 16-11-15.
 */

public interface ContentBody extends ContentDescriptor {
    String getFilename();

    void writeTo(OutputStream out) throws IOException;
}
