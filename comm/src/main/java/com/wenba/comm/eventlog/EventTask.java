package com.wenba.comm.eventlog;

import com.wenba.comm.BBLog;
import com.wenba.comm.eventlog.bi.BiEventHandler;
import com.wenba.comm.eventlog.bi.UserEventContext;

import java.util.UUID;

/**
 * Created by Lijj on 16/11/28.
 */

public class EventTask {
    private static String TAG = "WenbaUserEvent";

    private String sessionId = "";
    /**
     * app切出/回来 subSessionId ++
     */
    private int subSessionId = 0;
    private BiEventHandler biHandler;

    public EventTask(UserEventContext eventContext,boolean isDebug){
        biHandler = new BiEventHandler(eventContext, isDebug);
        sessionId = UUID.randomUUID().toString();
        subSessionId = 0;
    }

    private void addEventInner(UserEvent log) {
        if (log == null) {
            return;
        }

        log.sequence_number = biHandler.takeSequenceNumber();
        String val = log.toString();
        BBLog.d(TAG, "add event : "+val);
        biHandler.addEvent(val);
    }

    public synchronized void increaseSubsessionId() {
        biHandler.snapshotToDisk(true);

        subSessionId++;
    }

    public void wakeUploadInner() {
        biHandler.wakeupWorker();
    }

    public void onUserLogout() {
        biHandler.onUserLogout();
    }

    public void makeSnapshot(boolean saveBeforeExit) {
        biHandler.snapshotToDisk(saveBeforeExit);
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getSubSessionId() {
        return subSessionId;
    }

    /**
     * 记录日志
     *
     * @param log
     */
    public void addEvent(UserEvent log) {
        addEventInner(log);
    }

}
