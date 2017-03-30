package com.anxpp.tinyim.client.sdk.core;

import com.anxpp.tinyim.client.ClientCoreSDK;
import com.anxpp.tinyim.client.sdk.utils.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observer;

public class KeepAliveDaemon {
    private static final String TAG = KeepAliveDaemon.class.getSimpleName();

    public static int NETWORK_CONNECTION_TIME_OUT = 10000;

    public static int KEEP_ALIVE_INTERVAL = 3000;
    private static KeepAliveDaemon instance = null;
    private boolean keepAliveRunning = false;
    // 记录最近一次服务端的心跳响应包时间
    private long lastGetKeepAliveResponseFromServerTimstamp = 0L;
    private Observer networkConnectionLostObserver = null;

    private boolean _excuting = false;

    private Timer timer = null;

    private KeepAliveDaemon() {
        init();
    }

    public static KeepAliveDaemon getInstance() {
        if (instance == null)
            instance = new KeepAliveDaemon();
        return instance;
    }

    private void init() {
        this.timer = new Timer(KEEP_ALIVE_INTERVAL, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                KeepAliveDaemon.this.run();
            }
        });
    }

    public void run() {
        // 极端情况下本次循环内可能执行时间超过了时间间隔，此处是防止在前一
        // 次还没有运行完的情况下又重复过劲行，从而出现无法预知的错误
        if (!this._excuting) {
            boolean willStop = false;

            this._excuting = true;
            if (ClientCoreSDK.DEBUG)
                Log.i(TAG, "【IMCORE】心跳线程执行中...");
            int code = LocalUDPDataSender.getInstance().sendKeepAlive();

            boolean isInitialedForKeepAlive = this.lastGetKeepAliveResponseFromServerTimstamp == 0L;
            if ((code == 0) && (this.lastGetKeepAliveResponseFromServerTimstamp == 0L)) {
                this.lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();
            }

            if (!isInitialedForKeepAlive) {
                long now = System.currentTimeMillis();

                if (now - this.lastGetKeepAliveResponseFromServerTimstamp >= NETWORK_CONNECTION_TIME_OUT) {
                    stop();

                    if (this.networkConnectionLostObserver != null) {
                        this.networkConnectionLostObserver.update(null, null);
                    }
                    willStop = true;
                }
            }

            this._excuting = false;
            if (willStop) {
                this.timer.stop();
            }
        }
    }

    public void stop() {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.keepAliveRunning = false;
        this.lastGetKeepAliveResponseFromServerTimstamp = 0L;
    }

    public void start(boolean immediately) {
        stop();

        if (immediately)
            this.timer.setInitialDelay(0);
        else
            this.timer.setInitialDelay(KEEP_ALIVE_INTERVAL);

        this.timer.start();
        this.keepAliveRunning = true;
    }

    public boolean isKeepAliveRunning() {
        return this.keepAliveRunning;
    }

    public void updateGetKeepAliveResponseFromServerTimstamp() {
        this.lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();
    }

    public void setNetworkConnectionLostObserver(Observer networkConnectionLostObserver) {
        this.networkConnectionLostObserver = networkConnectionLostObserver;
    }
}