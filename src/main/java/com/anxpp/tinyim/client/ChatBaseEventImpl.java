package com.anxpp.tinyim.client;

import com.anxpp.tinyim.client.sdk.event.ChatBaseEvent;

/**
 * 框架基本事件回调实现类
 * Created by yangtao on 2017/3/30.
 */
public class ChatBaseEventImpl implements ChatBaseEvent {
    // 登陆/掉线重连结果通知
    @Override
    public void onLoginMessage(int dwUserId, int dwErrorCode) {
        if (dwErrorCode == 0)
            System.out.println("登录成功，当前分配的user_id=" + dwUserId);
        else
            System.out.println("登录失败，错误代码：" + dwErrorCode);
    }

    // 掉线事件通知
    @Override
    public void onLinkCloseMessage(int dwErrorCode) {
        System.out.println("网络连接出错关闭了，error：" + dwErrorCode);
    }
}