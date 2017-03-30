package com.anxpp.tinyim;

import com.anxpp.tinyim.client.ChatBaseEventImpl;
import com.anxpp.tinyim.client.ClientCoreSDK;
import com.anxpp.tinyim.client.sdk.ChatTransDataEventImpl;
import com.anxpp.tinyim.client.sdk.MessageQoSEventImpl;
import com.anxpp.tinyim.client.sdk.conf.ConfigEntity;
import com.anxpp.tinyim.client.sdk.core.MessageSender;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    //客户端测试
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
        };
    }

    @Scheduled(cron = "0/10 * *  * * ? ")
    public void messageSendTask() {
        int i = 0;
        while (i++ < 100) {
            int currentUserId = ClientCoreSDK.getInstance().getCurrentUserId();
            if (currentUserId > 0)
                sendMsg(currentUserId, "消息" + i);
        }
    }

    /**
     * 登陆
     */
    private void login() {
        new MessageSender.SendLoginDataAsync("10002", "password") {
            @Override
            protected void fireAfterSendLogin(int code) {
                System.out.println(code == 0 ? "login success" : "login Failed:" + code);
            }
        }.execute();
    }

    /**
     * 发送消息
     */
    private void sendMsg(int toUserId, String message) {
        new MessageSender.SendCommonDataAsync(message, toUserId, true) {
            @Override
            protected void onPostExecute(Integer code) {
                System.out.println(code == 0 ? "send success" : "send Failed:" + code);
            }
        }.execute();
    }
}