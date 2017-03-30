package com.anxpp.tinyim.client.sdk.message.server;

public class LoginInfoResponse {
    private int code = 0;
    private int userId = -1;

    public LoginInfoResponse(int code, int userId) {
        this.code = code;
        this.userId = userId;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}