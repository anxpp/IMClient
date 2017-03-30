package com.anxpp.tinyim;

import com.anxpp.tinyim.client.ChatBaseEventImpl;
import com.anxpp.tinyim.client.ClientCoreSDK;
import com.anxpp.tinyim.client.sdk.ChatTransDataEventImpl;
import com.anxpp.tinyim.client.sdk.MessageQoSEventImpl;
import com.anxpp.tinyim.client.sdk.conf.ConfigEntity;
import com.anxpp.tinyim.client.sdk.core.LocalUDPDataSender;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    CommandLineRunner start() {
        return args -> {
            // 设置AppKey
            ConfigEntity.appKey = "tinyim";
            ConfigEntity.serverIP = "127.0.0.1";
            ConfigEntity.serverUDPPort = 1114;
            //
            ClientCoreSDK.getInstance().setChatBaseEvent(new ChatBaseEventImpl());
            ClientCoreSDK.getInstance().setChatTransDataEvent(new ChatTransDataEventImpl());
            ClientCoreSDK.getInstance().setMessageQoSEvent(new MessageQoSEventImpl());
            login();
            sendMsg();
            sendMsg();
            sendMsg();
            sendMsg();
        };
    }

    /**
     * 登陆
     */
    private void login() {
        new LocalUDPDataSender.SendLoginDataAsync("10003", "password") {
            @Override
            protected void fireAfterSendLogin(int code) {
                System.out.println(code == 0 ? "login success" : "login Failed:" + code);
            }
        }.execute();
    }

    /**
     * 发送消息
     */
    private void sendMsg() {
        new LocalUDPDataSender.SendCommonDataAsync("88888888888888888888888888888888888888888888888888888888888", 10001, true) {
            @Override
            protected void onPostExecute(Integer code) {
                System.out.println(code == 0 ? "send success" : "send Failed:" + code);
            }
        }.execute();
    }
}