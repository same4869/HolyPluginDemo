package com.wenba.comm.eventlog.bi.http;

/**
 * Created by lqp on 16-11-15.
 */

public abstract  class AbstractContentBody implements ContentBody {

    private final String mimeType;
    private final String mediaType;
    private final String subType;

    public AbstractContentBody(final String mimeType) {
        super();
        if (mimeType == null) {
            throw new IllegalArgumentException("MIME type may not be null");
        }
        this.mimeType = mimeType;
        int i = mimeType.indexOf('/');
        if (i != -1) {
            this.mediaType = mimeType.substring(0, i);
            this.subType = mimeType.substring(i + 1);
        } else {
            this.mediaType = mimeType;
            this.subType = null;
        }
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public String getSubType() {
        return this.subType;
    }
}
