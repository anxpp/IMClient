package com.anxpp.tinyim.client.sdk;

import com.anxpp.tinyim.client.sdk.event.MessageQoSEvent;
import com.anxpp.tinyim.client.sdk.protocal.Protocal;

import java.util.ArrayList;

/**
 * QoS相关事件回调实现类
 * Created by yangtao on 2017/3/30.
 */
public class MessageQoSEventImpl implements MessageQoSEvent {
    // 消息无法完成实时送达的通知
    @Override
    public void messagesLost(ArrayList<Protocal> lostMessages) {
        System.out.println("收到系统的未实时送达事件通知，当前共有"
                + lostMessages.size() + "个包QoS保证机制结束，判定为【无法实时送达】！");
    }

    // 对方已成功收到消息的通知
    @Override
    public void messagesBeReceived(String theFingerPrint) {
        if (theFingerPrint != null)
            System.out.println("收到对方已收到消息事件的通知，消息指纹码=" + theFingerPrint);
    }
}