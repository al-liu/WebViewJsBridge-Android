package com.lhc.example;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.lhc.webviewjsbridge.ResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lhc on 2019/9/29.
 */

public class TestApiImpl {

    private static final String TAG = "TestApiImpl";
    private MainActivity mContext;

    public TestApiImpl(WeakReference<MainActivity> weakReference) {
        this.mContext = weakReference.get();
    }

    private TestApiImpl() {};

    @JavascriptInterface
    public void alert(JSONObject data, final ResponseHandler<String> responseHandler) {
        try {
            String title = data.getString("title");
            String message = data.getString("desc");
            new AlertDialog.Builder(this.mContext)
                    .setTitle(title)
                    .setMessage(message)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String responseData = mContext.alertCancelResponseData();
                            if (responseData != null) {
                                responseHandler.complete(responseData);
                            } else {
                                responseHandler.complete("cancel action finished.");
                            }
                        }
                    }).show();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public interface AlertService {
        String alertCancelResponseData();
    }

    public interface ObtainPhoto {
        void getPhotoBase64(String image);
    }

    @JavascriptInterface
    public void selectPhoto(final ResponseHandler<JSONObject> responseHandler) {
        new AlertDialog.Builder(this.mContext)
                .setTitle("Select Photo")
                .setMessage("Camera or Album")
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mContext.takePhoto(new ObtainPhoto() {
                            @Override
                            public void getPhotoBase64(String image) {
                                final JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("image", image);
                                    responseHandler.complete(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Album", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mContext.selectPhoto(new ObtainPhoto() {
                            @Override
                            public void getPhotoBase64(String image) {
                                final JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("image", image);
                                    responseHandler.complete(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).show();
    }

    @JavascriptInterface
    public void getRequest(JSONObject data, final ResponseHandler<String> responseHandler) {
        String url = null;
        JSONObject params = null;
        try {
            url = data.getString("url");
            params = data.getJSONObject("params");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (url == null) {
            Log.d(TAG, "url can'not be null");
            return;
        }
        final String requestUrl = appendQueryParams(params, url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String result = e.getMessage();
                responseHandler.complete(result);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                responseHandler.complete(result);
            }
        });
    }

    private String appendQueryParams(JSONObject params, String url) {
        if (params == null) {
            return url;
        }
        StringBuilder urlSB = new StringBuilder(url);
        try {
            Iterator<String> iterator = params.keys();
            urlSB.append("?");
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = params.getString(key);
                urlSB.append(key).append("=").append(URLEncoder.encode(value, "utf-8"));
                if (iterator.hasNext()) {
                    urlSB.append("&");
                }
            }
            return urlSB.toString();
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
}
