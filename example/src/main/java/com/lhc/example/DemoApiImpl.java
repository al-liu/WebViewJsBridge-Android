package com.lhc.example;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.lhc.webviewjsbridge.ResponseHandler;

import org.json.JSONObject;

/**
 * @author lhc
 */
public class DemoApiImpl {
    private static final String TAG = "DemoApiImpl";

    @JavascriptInterface
    public void test1(String data, ResponseHandler<String> responseHandler){
        Log.d(TAG,  String.format("Js native api test1, data is:%s", data));
        responseHandler.complete("native api test1'callback,");
    }

    @JavascriptInterface
    public void test2(JSONObject data){
        Log.d(TAG, String.format("Js native api:test2, data is:%s", data.toString()));
    }

    @JavascriptInterface
    public void test3(){
        Log.d(TAG, "Js native api:test3");
    }

    @JavascriptInterface
    public void test4(ResponseHandler<String> responseHandler){
        Log.d(TAG,  "Js native api test4, data is null");
        responseHandler.complete("native api test4'callback,");
    }
}
