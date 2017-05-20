package com.jim.ktv.ktvcontroller.http;

/**
 * Created by junjiechen on 16/4/6.
 */

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okio.BufferedSink;
import okio.Okio;

public class okHttp {


    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    private Callback mOkhttpCallbacker = null;

    public String runGetSynch(String url, String username, String Password) throws IOException {
        String credential;
        Request request;

        int timeout = client.connectTimeoutMillis();


        if(username != null) {
            if (username.equals("Bearer "))
                credential = username + Password;
            else
                credential = Credentials.basic(username, Password);


            request = new Request.Builder()
                    .header("Authorization", credential)
                    .url(url)
                    .build();
        }
        else
        {
            request = new Request.Builder()
                    .url(url)
                    .build();

        }
        // synchronous
        Response response = client.newCall(request).execute();

        return response.body().string();

        // asynchronous
        //client.newCall(request).enqueue(mOkhttpCallbacker);
        //return "OK";
    }

    public byte[] runGetSynchByte(String url, String username, String Password) throws IOException {
        String credential;
        Request request;
        if(username != null) {
            if (username.equals("Bearer "))
                credential = username + Password;
            else
                credential = Credentials.basic(username, Password);


            request = new Request.Builder()
                    .header("Authorization", credential)
                    .url(url)
                    .build();
        }
        else
        {
            request = new Request.Builder()
                    .url(url)
                    .build();

        }
        // synchronous
        Response response = client.newCall(request).execute();

        return response.body().bytes();

        // asynchronous
        //client.newCall(request).enqueue(mOkhttpCallbacker);
        //return "OK";
    }

    public String runGetAsynch(String url, String username, String Password)  {
        String credential;
        Request request;
        if(username != null) {
            if (username.equals("Bearer "))
                credential = username + Password;
            else
                credential = Credentials.basic(username, Password);

            request = new Request.Builder()
                    .header("Authorization", credential)
                    .url(url)
                    .build();
        }
        else
        {
            request = new Request.Builder()
                    .url(url)
                    .build();
        }


        // synchronous
//        Response response = client.newCall(request).execute();
//        return response.body().string();

        // asynchronous

        client.newCall(request).enqueue(mOkhttpCallbacker);

        return "OK";
    }

    public int runPost(String url) throws IOException {

        RequestBody requestBody = new RequestBody() {
            @Override public MediaType contentType() {
                return MEDIA_TYPE_MARKDOWN;
            }

            @Override public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
                //Okio.source(Socket s);
                //sink.write(Source s);
                for (int i = 2; i <= 997; i++) {
                    sink.writeUtf8(String.format(" * %s = %s\n", i, factor(i)));
                }
            }

            private String factor(int n) {
                for (int i = 2; i < n; i++) {
                    int x = n / i;
                    if (x * i == n) return factor(x) + " × " + i;
                }
                return Integer.toString(n);
            }
        };
        return 0;

    }

    public void setOkhttpCallback(final Callback new_val)
    {
        mOkhttpCallbacker = new_val;
    }
//    public static class Callback {
//        public void onResponse(Response response) throws IOException {
//            return;
//        };
//
//        public void onFailure(Request request, IOException throwable) {
//            return;
//        };
//    }




//    public static void main(String[] args) throws IOException {
//
//    }
}
