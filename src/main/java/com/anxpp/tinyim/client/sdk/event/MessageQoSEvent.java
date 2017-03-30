package com.anxpp.tinyim.client.sdk.event;

import com.anxpp.tinyim.client.sdk.message.Message;

import java.util.ArrayList;

public interface MessageQoSEvent {
    void messagesLost(ArrayList<Message> paramArrayList);

    void messagesBeReceived(String paramString);
}