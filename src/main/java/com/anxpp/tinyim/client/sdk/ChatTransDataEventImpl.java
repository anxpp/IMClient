package com.anxpp.tinyim.client.sdk;

import com.anxpp.tinyim.client.sdk.event.ChatTransDataEvent;

/**
 * 实时消息事件回调实现类
 * Created by yangtao on 2017/3/30.
 */
public class ChatTransDataEventImpl implements ChatTransDataEvent {
    // 收到即时通讯消息通知
    @Override
    public void onTransBuffer(String fingerPrintOfProtocal, int fromUserId, String dataContent) {
        System.out.println("message from " + fromUserId + ":" + dataContent);
    }

    // 收到服务端反馈的错误信息通知
    @Override
    public void onErrorResponse(int errorCode, String errorMsg) {
        System.out.println("error message from server,errorCode=" + errorCode + ", errorMsg=" + errorMsg);
    }
}