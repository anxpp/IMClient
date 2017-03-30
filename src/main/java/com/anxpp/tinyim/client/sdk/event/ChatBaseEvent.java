package com.anxpp.tinyim.client.sdk.event;

public abstract interface ChatBaseEvent {
    public abstract void onLoginMessage(int paramInt1, int paramInt2);

    public abstract void onLinkCloseMessage(int paramInt);
}