package com.anxpp.tinyim.client.sdk.core;

import com.anxpp.tinyim.client.ClientCoreSDK;
import com.anxpp.tinyim.client.sdk.conf.ConfigEntity;
import com.anxpp.tinyim.client.sdk.message.Message;
import com.anxpp.tinyim.client.sdk.message.MessageFactory;
import com.anxpp.tinyim.client.sdk.message.MessageType;
import com.anxpp.tinyim.client.sdk.message.server.ErrorResponse;
import com.anxpp.tinyim.client.sdk.message.server.LoginInfoResponse;
import com.anxpp.tinyim.client.sdk.utils.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 消息接收
 */
public class MessageReceiver {
    private static final String TAG = MessageReceiver.class.getSimpleName();
    private static MessageReceiver instance = null;
    private static MessageHandler messageHandler = null;
    private Thread thread = null;

    public static MessageReceiver getInstance() {
        if (instance == null) {
            instance = new MessageReceiver();
            messageHandler = new MessageHandler();
        }
        return instance;
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }
    }

    void startup() {
        stop();
        try {
            this.thread = new Thread(() -> {
                try {
                    if (ClientCoreSDK.DEBUG) {
                        Log.d(MessageReceiver.TAG, "Client started at:" + ConfigEntity.localUDPPort + "...");
                    }
                    //开始侦听
                    MessageReceiver.this.p2pListeningImpl();
                } catch (Exception eee) {
                    Log.w(MessageReceiver.TAG, "【IMCORE】本地UDP监听停止了(socket被关闭了?)," + eee.getMessage(), eee);
                }
            });
            this.thread.start();
        } catch (Exception e) {
            Log.w(TAG, "【IMCORE】本地UDPSocket监听开启时发生异常," + e.getMessage(), e);
        }
    }

    private void p2pListeningImpl() throws Exception {
        while (true) {
            byte[] data = new byte[1024];
            // 接收数据报的包
            DatagramPacket packet = new DatagramPacket(data, data.length);
            DatagramSocket localUDPSocket = SocketProvider.getInstance().getLocalUDPSocket();
            if ((localUDPSocket == null) || (localUDPSocket.isClosed())) {
                continue;
            }
            localUDPSocket.receive(packet);
            messageHandler.handleMessage(packet);
        }
    }

    /**
     * 消息处理
     */
    private static class MessageHandler {
        void handleMessage(DatagramPacket datagramPacket) {
            if (null == datagramPacket) {
                return;
            }
            try {
                Message message = MessageFactory.parse(datagramPacket.getData(), datagramPacket.getLength());
                //
                if (message.isQoS()) {
                    if (QoS4ReciveDaemon.getInstance().hasRecieved(message.getFp())) {
                        if (ClientCoreSDK.DEBUG) {
                            Log.d(MessageReceiver.TAG, "【IM CORE】【QoS机制】" + message.getFp() + "已经存在于发送列表中，这是重复包，通知应用层收到该包罗！");
                        }
                        QoS4ReciveDaemon.getInstance().addRecieved(message);
                        sendReceivedBack(message);
                        return;
                    }

                    QoS4ReciveDaemon.getInstance().addRecieved(message);
                    sendReceivedBack(message);
                }
                //消息类型
                switch (message.getType()) {
                    //其他客户端消息
                    case MessageType.Client.FROM_CLIENT_TYPE_OF_COMMON$DATA: {
                        if (ClientCoreSDK.getInstance().getChatTransDataEvent() == null)
                            break;
                        ClientCoreSDK.getInstance().getChatTransDataEvent().onTransBuffer(
                                message.getFp(), message.getFrom(), message.getDataContent());

                        break;
                    }
                    //心跳消息
                    case MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE: {
                        if (ClientCoreSDK.DEBUG) {
                            Log.p(MessageReceiver.TAG, "heart message");
                        }
                        KeepAliveDaemon.getInstance().updateGetKeepAliveResponseFromServerTimstamp();
                        break;
                    }
                    case MessageType.Client.FROM_CLIENT_TYPE_OF_RECIVED: {
                        String theFingerPrint = message.getDataContent();
                        if (ClientCoreSDK.DEBUG) {
                            Log.i(MessageReceiver.TAG, "【IM CORE】【QoS】收到" + message.getFrom() + "发过来的指纹为" + theFingerPrint + "的应答包.");
                        }

                        if (ClientCoreSDK.getInstance().getMessageQoSEvent() != null) {
                            ClientCoreSDK.getInstance().getMessageQoSEvent().messagesBeReceived(theFingerPrint);
                        }

                        QoS4SendDaemon.getInstance().remove(theFingerPrint);
                        break;
                    }
                    //登陆响应消息
                    case MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN: {
                        LoginInfoResponse loginInfoRes = MessageFactory.parsePLoginInfoResponse(message.getDataContent());

                        if (loginInfoRes.getCode() == 0) {
                            ClientCoreSDK.getInstance().setLoginHasInit(true).setCurrentUserId(loginInfoRes.getUserId());
                            AutoReLoginDaemon.getInstance().stop();
                            KeepAliveDaemon.getInstance().setNetworkConnectionLostObserver((observable, data) -> {
                                QoS4SendDaemon.getInstance().stop();
                                QoS4ReciveDaemon.getInstance().stop();
                                ClientCoreSDK.getInstance().setConnectedToServer(false);
                                ClientCoreSDK.getInstance().setCurrentUserId(-1);
                                ClientCoreSDK.getInstance().getChatBaseEvent().onLinkCloseMessage(-1);
                                AutoReLoginDaemon.getInstance().start(true);
                            });

                            KeepAliveDaemon.getInstance().start(false);
                            QoS4SendDaemon.getInstance().startup(true);
                            QoS4ReciveDaemon.getInstance().startup(true);
                            ClientCoreSDK.getInstance().setConnectedToServer(true);
                        } else {
                            ClientCoreSDK.getInstance().setConnectedToServer(false);
                            ClientCoreSDK.getInstance().setCurrentUserId(-1);
                        }

                        // TODO FOR DEBUG
                        System.out.println("【注意：：：】登陆成功，User_id()=" + loginInfoRes.getUserId() + ", getChatBaseEvent=" + ClientCoreSDK.getInstance().getChatBaseEvent());

                        if (ClientCoreSDK.getInstance().getChatBaseEvent() == null)
                            break;
                        ClientCoreSDK.getInstance().getChatBaseEvent().onLoginMessage(
                                loginInfoRes.getUserId(), loginInfoRes.getCode());
                        break;
                    }
                    case MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR: {
                        ErrorResponse errorRes = MessageFactory.parsePErrorResponse(message.getDataContent());

                        if (errorRes.getErrorCode() == 301) {
                            ClientCoreSDK.getInstance().setLoginHasInit(false);

                            Log.e(MessageReceiver.TAG, "server response of never login");

                            KeepAliveDaemon.getInstance().stop();
                            AutoReLoginDaemon.getInstance().start(false);
                        }

                        if (ClientCoreSDK.getInstance().getChatTransDataEvent() == null)
                            break;
                        ClientCoreSDK.getInstance().getChatTransDataEvent().onErrorResponse(
                                errorRes.getErrorCode(), errorRes.getErrorMsg());

                        break;
                    }
                    default:
                        Log.w(MessageReceiver.TAG, "unknown message type:" + message.getType());
                }
            } catch (Exception e) {
                Log.w(MessageReceiver.TAG, "parse message error:", e);
            }
        }

        private void sendReceivedBack(final Message pFromServer) {
            if (pFromServer.getFp() != null) {
                new MessageSender.SendCommonDataAsync(MessageFactory.createRecivedBack(pFromServer.getTo(), pFromServer.getFrom(), pFromServer.getFp())) {
                    @Override
                    protected void onPostExecute(Integer code) {
                        if (ClientCoreSDK.DEBUG)
                            Log.d(TAG, "【IM CORE】【QoS】向" + pFromServer.getFrom() + "发送" + pFromServer.getFp() + "包的应答包成功,from=" + pFromServer.getTo() + "！");
                    }
                }.execute();
            } else {
                Log.w(TAG, "【IM CORE】【QoS】收到" + pFromServer.getFrom() + "发过来需要QoS的包，但它的指纹码却为null！无法发应答包！");
            }
        }
    }
}