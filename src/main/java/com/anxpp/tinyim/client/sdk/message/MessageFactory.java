package com.anxpp.tinyim.client.sdk.message;

import com.anxpp.tinyim.client.sdk.message.client.KeepAlive;
import com.anxpp.tinyim.client.sdk.message.client.LoginInfo;
import com.anxpp.tinyim.client.sdk.message.server.ErrorResponse;
import com.anxpp.tinyim.client.sdk.message.server.KeepAliveResponse;
import com.anxpp.tinyim.client.sdk.message.server.LoginInfoResponse;
import com.google.gson.Gson;

public class MessageFactory {
    private static String create(Object c) {
        return new Gson().toJson(c);
    }

    public static <T> T parse(byte[] fullProtocalJASOnBytes, int len, Class<T> clazz) {
        return parse(CharsetHelper.getString(fullProtocalJASOnBytes, len), clazz);
    }

    public static <T> T parse(String dataContentOfProtocal, Class<T> clazz) {
        return new Gson().fromJson(dataContentOfProtocal, clazz);
    }

    public static Message parse(byte[] fullProtocalJASOnBytes, int len) {
        return parse(fullProtocalJASOnBytes, len, Message.class);
    }

    public static Message createPKeepAliveResponse(int to_user_id) {
        return new Message(MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE,
                create(new KeepAliveResponse()), 0, to_user_id);
    }

    public static KeepAliveResponse parsePKeepAliveResponse(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, KeepAliveResponse.class);
    }

    public static Message createPKeepAlive(int from_user_id) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_KEEP$ALIVE,
                create(new KeepAlive()), from_user_id, 0);
    }

    public static KeepAlive parsePKeepAlive(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, KeepAlive.class);
    }

    public static Message createPErrorResponse(int errorCode, String errorMsg, int user_id) {
        return new Message(MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR,
                create(new ErrorResponse(errorCode, errorMsg)), 0, user_id);
    }

    public static ErrorResponse parsePErrorResponse(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, ErrorResponse.class);
    }

    public static Message createLogoutInfo(int user_id, String loginName) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_LOGOUT
//				, create(new PLogoutInfo(user_id, loginName))
                , null
                , user_id, 0);
    }

    public static Message createPLoginInfo(String loginName, String loginPsw, String extra) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_LOGIN
                , create(new LoginInfo(loginName, loginPsw, extra)), -1, 0);
    }

    public static LoginInfo parsePLoginInfo(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, LoginInfo.class);
    }

    public static Message createPLoginInfoResponse(int code, int user_id) {
        return new Message(MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN,
                create(new LoginInfoResponse(code, user_id)),
                0,
                user_id,
                true, Message.genFingerPrint());
    }

    public static LoginInfoResponse parsePLoginInfoResponse(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, LoginInfoResponse.class);
    }

    public static Message createCommonData(String dataContent, int from_user_id, int to_user_id, boolean QoS, String fingerPrint) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_COMMON$DATA,
                dataContent, from_user_id, to_user_id, QoS, fingerPrint);
    }

    public static Message createCommonData(String dataContent, int from_user_id, int to_user_id) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_COMMON$DATA,
                dataContent, from_user_id, to_user_id);
    }

    public static Message createRecivedBack(int from_user_id, int to_user_id, String recievedMessageFingerPrint) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_RECIVED
                , recievedMessageFingerPrint, from_user_id, to_user_id);// 该包当然不需要QoS支持！
    }
}