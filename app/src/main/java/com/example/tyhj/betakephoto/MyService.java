package com.example.tyhj.betakephoto;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import java.util.Arrays;

public class MyService extends Service {
    AVIMConversation myconversation=null;
    String data;
    public MyService() {
    }
    public class Binder extends android.os.Binder{
        public void setData(String string){
            data=string;
        }
        public MyService getService(){
            return MyService.this;
        }
    }

    private  Callback callback=null;
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    public Callback getCallback() {
        return callback;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return new Binder();
    }

    @Override
    public void onCreate() {
        AVOSCloud.initialize(this, "sWU1ERjPGATpOg0d6dSzxfhB-gzGzoHsz", "zqLWPb25PoVCNHBSTIS6mQlj");
        AVIMMessageManager.registerDefaultMessageHandler(new CustomMessageHandler());
        super.onCreate();
        creatConversation();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if(callback!=null){
                        callback.onDataChange(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    public static interface Callback{
        void onDataChange(int str);
    }


    public void creatConversation() {
        // Tom 用自己的名字作为clientId，获取AVIMClient对象实例
        AVIMClient tom = AVIMClient.getInstance("13678141943");
        // 与服务器连接
        tom.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    // 创建与Jerry之间的对话
                    client.createConversation(Arrays.asList("13678141941"), "13678141943" + "& " + "13678141941", null,
                            new AVIMConversationCreatedCallback() {
                                @Override
                                public void done(AVIMConversation conversation, AVIMException e) {
                                    if (e == null) {
                                        myconversation = conversation;
                                    }
                                }
                            });
                }
            }
        });
    }
    class CustomMessageHandler extends AVIMMessageHandler {
        //接收到消息后的处理逻辑
        @Override
        public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client){
            if(message instanceof AVIMTextMessage){
                if(((AVIMTextMessage) message).getText().substring(0,11).equals("13678141941")) {
                    String str=((AVIMTextMessage) message).getText().substring(11,((AVIMTextMessage) message).getText().length());
                    if(callback!=null){
                        callback.onDataChange(1);
                    }
                }
            }
        }
        //消息被接受
        public void onMessageReceipt(AVIMMessage message,AVIMConversation conversation,AVIMClient client){

        }
    }
}
