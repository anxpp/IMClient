package com.anxpp.tinyim.client.sdk.core;

import com.anxpp.tinyim.client.ClientCoreSDK;
import com.anxpp.tinyim.client.sdk.utils.Log;

import javax.swing.*;

public class AutoReLoginDaemon {
    private static final String TAG = AutoReLoginDaemon.class.getSimpleName();

    private static int AUTO_RE$LOGIN_INTERVAL = 2000;
    private static AutoReLoginDaemon instance = null;
    private boolean autoReLoginRunning = false;
    private boolean _excuting = false;
    private Timer timer = null;

    private AutoReLoginDaemon() {
        init();
    }

    public static AutoReLoginDaemon getInstance() {
        if (instance == null)
            instance = new AutoReLoginDaemon();
        return instance;
    }

    private void init() {
        this.timer = new Timer(AUTO_RE$LOGIN_INTERVAL, e -> AutoReLoginDaemon.this.run());
    }

    public void run() {
        if (!this._excuting) {
            this._excuting = true;
            if (ClientCoreSDK.DEBUG)
                Log.d(TAG, "【IMCORE】自动重新登陆线程执行中, autoReLogin?" + ClientCoreSDK.autoReLogin + "...");
            int code = -1;
            // 是否允许自动重新登陆哦
            if (ClientCoreSDK.autoReLogin) {
                LocalUDPSocketProvider.getInstance().closeLocalUDPSocket();

                // 发送重登陆请求
                code = LocalUDPDataSender.getInstance().sendLogin(
                        ClientCoreSDK.getInstance().getCurrentLoginName()
                        , ClientCoreSDK.getInstance().getCurrentLoginPsw()
                        , ClientCoreSDK.getInstance().getCurrentLoginExtra());
            }

            if (code == 0) {
                LocalUDPDataReceiver.getInstance().startup();
            }

            this._excuting = false;
        }
    }

    public void stop() {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.autoReLoginRunning = false;
    }

    public void start(boolean immediately) {
        stop();

        if (immediately)
            this.timer.setInitialDelay(0);
        else
            this.timer.setInitialDelay(AUTO_RE$LOGIN_INTERVAL);
        this.timer.start();

        this.autoReLoginRunning = true;
    }

    public boolean isautoReLoginRunning() {
        return this.autoReLoginRunning;
    }
}