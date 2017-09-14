package com.imudges.web.CloudAddressBook.Util;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SendMessage {
    private String checkCode;


    private void createCheckCode(){
        checkCode="";
        String base[] = {"0","1","2","3","4","5","6","7","8","9"};
        for (int i = 0; i < 6; i++) {
            int RandNum=new Random().nextInt(base.length);
            this.checkCode+=base[RandNum];
        }
    }
    private String getCheckCode(){
        return checkCode;
    }

    private OkHttpClient client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    }).build();
    public String sendMessage(String phoneNum){
        if(phoneNum == null || phoneNum.equals("")){
            return null;
        }
        createCheckCode();
        String content = "【宠爱】您的验证码为" + getCheckCode() + ",在10分钟内有效";
        String USERNAME = Config.SMS_USERNAME;
        String PASSWORD = MD5.getMd5(Config.SMS_PASSWORD);
        String CONTENT = Toolkit.encodeUrlString(content,"UTF-8");

        Request request = new Request.Builder()
                .url(Config.SMS_BASE_URL + "/sms" + "?u=" + USERNAME + "&p=" + PASSWORD + "&m=" + phoneNum + "&c=" + CONTENT)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response);
            }
        });
        return checkCode;
    }
}
