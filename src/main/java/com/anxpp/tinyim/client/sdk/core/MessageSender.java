package com.anxpp.tinyim.client.sdk.core;

import com.anxpp.tinyim.client.ClientCoreSDK;
import com.anxpp.tinyim.client.sdk.conf.ConfigEntity;
import com.anxpp.tinyim.client.sdk.message.CharsetHelper;
import com.anxpp.tinyim.client.sdk.message.StatusCode;
import com.anxpp.tinyim.client.sdk.message.Message;
import com.anxpp.tinyim.client.sdk.message.MessageFactory;
import com.anxpp.tinyim.client.sdk.utils.Log;
import com.anxpp.tinyim.client.sdk.utils.UDPUtils;

import javax.swing.*;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageSender {
    private static final String TAG = MessageSender.class.getSimpleName();
    private static MessageSender instance = null;

    public static MessageSender getInstance() {
        if (instance == null)
            instance = new MessageSender();
        return instance;
    }

    /**
     * 登陆
     *
     * @param username 用户名
     * @param password 密码
     * @param extra    额外信息
     * @return 状态吗
     */
    int sendLogin(String username, String password, String extra) {
        byte[] bytes = MessageFactory.createPLoginInfo(username, password, extra).toBytes();
        int code = send(bytes, bytes.length);
        // 登陆信息成功发出时就把登陆名存下来
        if (code == 0) {
            ClientCoreSDK.getInstance().setCurrentLoginName(username);
            ClientCoreSDK.getInstance().setCurrentLoginPsw(password);
            ClientCoreSDK.getInstance().setCurrentLoginExtra(extra);
        }
        return code;
    }

    /**
     * 登出
     *
     * @return 状态
     */
    public int sendLoginout() {
        int code = StatusCode.COMMON_CODE_OK;
        if (ClientCoreSDK.getInstance().isLoginHasInit()) {
            byte[] b = MessageFactory.createLogoutInfo(ClientCoreSDK.getInstance().getCurrentUserId(), ClientCoreSDK.getInstance().getCurrentLoginName()).toBytes();
            code = send(b, b.length);
            // 登出信息成功发出时
            if (code == 0) {
                //			// 发出退出登陆的消息同时也关闭心跳线程
                //			KeepAliveDaemon.getInstance(context).stop();
                //			// 重置登陆标识
                //			ClientCoreSDK.getInstance().setLoginHasInit(false);
            }
        }
        // 释放SDK资源
        ClientCoreSDK.getInstance().release();
        return code;
    }

    /**
     * 心跳消息
     *
     * @return 状态
     */
    int sendKeepAlive() {
        byte[] b = MessageFactory.createPKeepAlive(ClientCoreSDK.getInstance().getCurrentUserId()).toBytes();
        return send(b, b.length);
    }

    public int sendCommonData(byte[] dataContent, int dataLen, int to_user_id) {
        return sendCommonData(CharsetHelper.getString(dataContent, dataLen), to_user_id, false, null);
    }

    public int sendCommonData(byte[] dataContent, int dataLen, int to_user_id, boolean QoS, String fingerPrint) {
        return sendCommonData(CharsetHelper.getString(dataContent, dataLen), to_user_id, QoS, fingerPrint);
    }

    public int sendCommonData(String dataContentWidthStr, int to_user_id) {
        return sendCommonData(MessageFactory.createCommonData(dataContentWidthStr,
                ClientCoreSDK.getInstance().getCurrentUserId(), to_user_id));
    }

    public int sendCommonData(String dataContentWidthStr, int to_user_id, boolean QoS, String fingerPrint) {
        return sendCommonData(MessageFactory.createCommonData(dataContentWidthStr, ClientCoreSDK.getInstance().getCurrentUserId(), to_user_id, QoS, fingerPrint));
    }

    public int sendCommonData(Message p) {
        if (p != null) {
            byte[] b = p.toBytes();
            int code = send(b, b.length);
            if (code == 0) {
                if ((p.isQoS()) && (!QoS4SendDaemon.getInstance().exist(p.getFp())))
                    QoS4SendDaemon.getInstance().put(p);
            }
            return code;
        }

        return StatusCode.COMMON_INVALID_PROTOCAL;
    }

    private int send(byte[] fullProtocalBytes, int dataLen) {
        if (!ClientCoreSDK.getInstance().isInitialed())
            return StatusCode.ForC.CLIENT_SDK_NO_INITIALED;
        DatagramSocket ds = SocketProvider.getInstance().getLocalUDPSocket();
        // 如果Socket没有连接上服务端
        if (ds != null && !ds.isConnected()) {
            try {
                if (ConfigEntity.serverIP == null) {
                    Log.w(TAG, "【IMCORE】send数据没有继续，原因是ConfigEntity.server_ip==null!");
                    return StatusCode.ForC.TO_SERVER_NET_INFO_NOT_SETUP;
                }

                ds.connect(InetAddress.getByName(ConfigEntity.serverIP), ConfigEntity.serverUDPPort);
            } catch (Exception e) {
                Log.w(TAG, "【IMCORE】send时出错，原因是：" + e.getMessage(), e);
                return StatusCode.ForC.BAD_CONNECT_TO_SERVER;
            }
        }
        return UDPUtils.send(ds, fullProtocalBytes, dataLen) ? StatusCode.COMMON_CODE_OK : StatusCode.COMMON_DATA_SEND_FAILD;
    }

    public static abstract class SendCommonDataAsync extends SwingWorker<Integer, Object> {
        protected Message p = null;

        public SendCommonDataAsync(byte[] dataContent, int dataLen, int to_user_id) {
            this(CharsetHelper.getString(dataContent, dataLen), to_user_id);
        }

        public SendCommonDataAsync(String dataContentWidthStr, int to_user_id, boolean QoS) {
            this(dataContentWidthStr, to_user_id, QoS, null);
        }

        public SendCommonDataAsync(String dataContentWidthStr, int to_user_id, boolean QoS, String fingerPrint) {
            this(MessageFactory.createCommonData(dataContentWidthStr,
                    ClientCoreSDK.getInstance().getCurrentUserId(), to_user_id, QoS, fingerPrint));
        }

        public SendCommonDataAsync(String dataContentWidthStr, int to_user_id) {
            this(MessageFactory.createCommonData(dataContentWidthStr,
                    ClientCoreSDK.getInstance().getCurrentUserId(), to_user_id));
        }

        public SendCommonDataAsync(Message p) {
            if (p == null) {
                Log.w(MessageSender.TAG, "【IMCORE】无效的参数p==null!");
                return;
            }
            this.p = p;
        }

        protected Integer doInBackground() {
            if (this.p != null)
                return Integer.valueOf(MessageSender.getInstance().sendCommonData(this.p));
            return Integer.valueOf(-1);
        }

        protected void done() {
            int code = -1;
            try {
                code = ((Integer) get()).intValue();
            } catch (Exception e) {
                Log.w(MessageSender.TAG, e.getMessage());
            }

            onPostExecute(Integer.valueOf(code));
        }

        protected abstract void onPostExecute(Integer paramInteger);
    }

    public static class SendLoginDataAsync extends SwingWorker<Integer, Object> {
        protected String loginName = null;
        protected String loginPsw = null;
        protected String extra = null;

        public SendLoginDataAsync(String loginName, String loginPsw) {
            this(loginName, loginPsw, null);
        }

        public SendLoginDataAsync(String loginName, String loginPsw, String extra) {
            this.loginName = loginName;
            this.loginPsw = loginPsw;
            this.extra = extra;

            ClientCoreSDK.getInstance().init();
        }

        protected Integer doInBackground() {
            int code = MessageSender.getInstance().sendLogin(this.loginName, this.loginPsw, this.extra);
            return code;
        }

        protected void done() {
            int code = -1;
            try {
                code = get();
            } catch (Exception e) {
                Log.w(MessageSender.TAG, e.getMessage());
            }

            onPostExecute(code);
        }

        protected void onPostExecute(Integer code) {
            if (code.intValue() == 0) {
                MessageReceiver.getInstance().startup();
            } else {
                Log.d(MessageSender.TAG, "【IMCORE】数据发送失败, 错误码是：" + code + "！");
            }

            fireAfterSendLogin(code.intValue());
        }

        protected void fireAfterSendLogin(int code) {
            // default do nothing
        }
    }
}