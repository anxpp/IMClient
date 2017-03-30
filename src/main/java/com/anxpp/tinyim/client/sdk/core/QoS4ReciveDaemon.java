package com.anxpp.tinyim.client.sdk.core;

import com.anxpp.tinyim.client.ClientCoreSDK;
import com.anxpp.tinyim.client.sdk.message.Message;
import com.anxpp.tinyim.client.sdk.utils.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ConcurrentHashMap;

public class QoS4ReciveDaemon {
    public static final int CHECH_INTERVAL = 300000;
    public static final int MESSAGES_VALID_TIME = 600000;
    private static final String TAG = QoS4ReciveDaemon.class.getSimpleName();
    private static QoS4ReciveDaemon instance = null;
    private ConcurrentHashMap<String, Long> recievedMessages = new ConcurrentHashMap<String, Long>();
    private boolean running = false;
    private boolean _excuting = false;
    private Timer timer = null;

    public QoS4ReciveDaemon() {
        init();
    }

    public static QoS4ReciveDaemon getInstance() {
        if (instance == null) {
            instance = new QoS4ReciveDaemon();
        }

        return instance;
    }

    private void init() {
        this.timer = new Timer(CHECH_INTERVAL, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                QoS4ReciveDaemon.this.run();
            }
        });
    }

    public void run() {
        // 极端情况下本次循环内可能执行时间超过了时间间隔，此处是防止在前一
        // 次还没有运行完的情况下又重复过劲行，从而出现无法预知的错误
        if (!this._excuting) {
            this._excuting = true;

            if (ClientCoreSDK.DEBUG) {
                Log.d(TAG, "【IMCORE】【QoS接收方】++++++++++ START 暂存处理线程正在运行中，当前长度" + this.recievedMessages.size() + ".");
            }

            for (String key : this.recievedMessages.keySet()) {
                long delta = System.currentTimeMillis() - ((Long) this.recievedMessages.get(key)).longValue();

                if (delta < MESSAGES_VALID_TIME)
                    continue;
                if (ClientCoreSDK.DEBUG)
                    Log.d(TAG, "【IMCORE】【QoS接收方】指纹为" + key + "的包已生存" + delta +
                            "ms(最大允许" + MESSAGES_VALID_TIME + "ms), 马上将删除之.");
                this.recievedMessages.remove(key);
            }

        }

        if (ClientCoreSDK.DEBUG) {
            Log.d(TAG, "【IMCORE】【QoS接收方】++++++++++ END 暂存处理线程正在运行中，当前长度" + this.recievedMessages.size() + ".");
        }

        this._excuting = false;
    }

    public void startup(boolean immediately) {
        stop();

        if ((this.recievedMessages != null) && (this.recievedMessages.size() > 0)) {
            for (String key : this.recievedMessages.keySet()) {
                putImpl(key);
            }

        }

        if (immediately)
            this.timer.setInitialDelay(0);
        else
            this.timer.setInitialDelay(CHECH_INTERVAL);
        this.timer.start();

        this.running = true;
    }

    public void stop() {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void addRecieved(Message p) {
        if ((p != null) && (p.isQoS()))
            addRecieved(p.getFp());
    }

    public void addRecieved(String fingerPrintOfProtocal) {
        if (fingerPrintOfProtocal == null) {
            Log.w(TAG, "【IMCORE】无效的 fingerPrintOfProtocal==null!");
            return;
        }

        if (this.recievedMessages.containsKey(fingerPrintOfProtocal)) {
            Log.w(TAG, "【IMCORE】【QoS接收方】指纹为" + fingerPrintOfProtocal +
                    "的消息已经存在于接收列表中，该消息重复了（原理可能是对方因未收到应答包而错误重传导致），更新收到时间戳哦.");
        }

        putImpl(fingerPrintOfProtocal);
    }

    private void putImpl(String fingerPrintOfProtocal) {
        if (fingerPrintOfProtocal != null)
            this.recievedMessages.put(fingerPrintOfProtocal, Long.valueOf(System.currentTimeMillis()));
    }

    public boolean hasRecieved(String fingerPrintOfProtocal) {
        return this.recievedMessages.containsKey(fingerPrintOfProtocal);
    }

    public int size() {
        return this.recievedMessages.size();
    }
}