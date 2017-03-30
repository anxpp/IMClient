package com.anxpp.tinyim.client.sdk.event;

import com.anxpp.tinyim.client.sdk.protocal.Protocal;

import java.util.ArrayList;

public abstract interface MessageQoSEvent {
    public abstract void messagesLost(ArrayList<Protocal> paramArrayList);

    public abstract void messagesBeReceived(String paramString);
}