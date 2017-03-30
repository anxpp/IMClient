package com.anxpp.tinyim.client.sdk.event;

public abstract interface ChatTransDataEvent
{
  public abstract void onTransBuffer(String paramString1, int paramInt, String paramString2);

  public abstract void onErrorResponse(int paramInt, String paramString);
}