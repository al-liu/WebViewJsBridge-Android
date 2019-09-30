package com.lhc.example;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.lhc.example.utils.SelectPhotoUtils;
import com.lhc.webviewjsbridge.ResponseHandler;
import com.lhc.webviewjsbridge.WebViewJsBridge;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * @author lhc
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, TestApiImpl.AlertService {

    private static final String TAG = "MainActivity";

    private TestApiImpl.ObtainPhoto obtainPhoto;
    public WebView mMainWebView;
    private WebViewJsBridge mJsBridge;
    private Button test1Btn, test2Btn, test3Btn, test4Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test1Btn = findViewById(R.id.test1);
        test2Btn = findViewById(R.id.test2);
        test3Btn = findViewById(R.id.test3);
        test4Btn = findViewById(R.id.test4);
        test1Btn.setOnClickListener(this);
        test2Btn.setOnClickListener(this);
        test3Btn.setOnClickListener(this);
        test4Btn.setOnClickListener(this);

        mMainWebView = findViewById(R.id.mainWebView);
        WebSettings webSettings = mMainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        WebView.setWebContentsDebuggingEnabled(true);

        WeakReference<MainActivity> weakReference = new WeakReference<>(this);
        mJsBridge = WebViewJsBridge.newInstance(mMainWebView);
        mJsBridge.enableDebugLogging(true);
        mJsBridge.addJsBridgeApiObject(new DemoApiImpl(), "ui");
        mJsBridge.addJsBridgeApiObject(new TestApiImpl(weakReference), "test");
        // startup call js
        mJsBridge.callHandler("test1", "test1 data", new ResponseHandler() {
            @Override
            public void complete(Object responseData) {
                Log.d(TAG, String.format("test1 callback data is:%s", responseData));
            }
        });

        mMainWebView.loadUrl("file:///android_asset/test.html");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test1:
                mJsBridge.callHandler("test1", "test1 data", new ResponseHandler() {
                    @Override
                    public void complete(Object responseData) {
                        Log.d(TAG, String.format("test1 callback data is:%s", responseData));
                    }
                });
                break;
            case R.id.test2:
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("hello", "world");
                    mJsBridge.callHandler("test2", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.test3:
                mJsBridge.callHandler("test3");
                break;
            case R.id.test4:
                mJsBridge.callHandler("test1", new ResponseHandler() {
                    @Override
                    public void complete(Object responseData) {
                        Log.d(TAG, String.format("test1 callback data is:%s", responseData));
                    }
                });
                break;
        }
    }

    public void takePhoto(TestApiImpl.ObtainPhoto obtainPhoto) {
        this.obtainPhoto = obtainPhoto;
        SelectPhotoUtils.takePhoto(this);
    }

    public void selectPhoto(TestApiImpl.ObtainPhoto obtainPhoto) {
        this.obtainPhoto = obtainPhoto;
        SelectPhotoUtils.openAlbum(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        switch (requestCode) {
            case SelectPhotoUtils.REQUEST_CODE_TAKE_PHOTO:
                bitmap = SelectPhotoUtils.getTaksPhoto(data, this);
                break;
            case SelectPhotoUtils.REQUEST_CODE_ALBUM_PHOTO:
                bitmap = SelectPhotoUtils.getAlbumPhoto(data, this);
                break;
        }
        if (bitmap != null) {
            String imageBase64 = SelectPhotoUtils.bitmapToBase64(bitmap);
            if (this.obtainPhoto != null) {
                this.obtainPhoto.getPhotoBase64(imageBase64);
            }
        }
    }

    @Override
    public String alertCancelResponseData() {
        return "Good blessings from WebViewJsBridge.";
    }
}
