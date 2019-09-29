package com.lhc.webviewjsbridge;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HCWKWebViewJsBridge is used to implement communication between H5 and WKWebView.
 * <p>Please refer to the README.md for detailed usage.
 *
 * js code example, hcJsBridge is a global object:
 * <blockquote><pre>
 *     // H5 register an api for native calls
 *     hcJsBridge.registerHandler("test", function(data, callback) {
 *         callback(responseData);
 *     })
 *
 *     // H5 call a native api, api name is 'ui.alert'
 *     hcJsBridge.callHandler("ui.alert", data, function (responseData) {
 *
 *     })
 * </pre></blockquote>
 *
 * @author  lhc
 */
public class WebViewJsBridge {

    private static final String TAG = "WebViewJsBridge";

    private static final String NATIVE_JAVASCRIPT_INTERFACE = "nativeBridgeHead";
    private static final String SEND_MESSAGE_SCRIPT_FORMAT = "hcJsBridge.handleMessageFromNative(%s)";
    private static final String MESSAGE_NAME = "name";
    private static final String MESSAGE_DATA = "data";
    private static final String MESSAGE_CALLBACK_ID = "callbackId";
    private static final String MESSAGE_RESPONSE_ID = "responseId";
    private static final String MESSAGE_RESPONSE_DATA = "responseData";
    private static final String DEFAULT_NAMESPACE = "default";

    private WebView mWebView;

    private boolean mIsDebug;
    private Map<String, ResponseHandler> mResponseHandlerMap;
    private Map<String, Object> mApiHandlerMap;
    private List<String> startupMessageQueue;

    /**
     * Static constructor
     * @param webView android.webkit.WebView
     * @return WebViewJsBridge
     */
    public static WebViewJsBridge newInstance(WebView webView) {
        WebViewJsBridge bridge = new WebViewJsBridge();
        bridge.mWebView = webView;
        bridge.init();
        return bridge;
    }

    private WebViewJsBridge() {}

    private void init() {
        mResponseHandlerMap = new HashMap<>();
        mApiHandlerMap = new HashMap<>();
        startupMessageQueue = new ArrayList<>();
        mWebView.addJavascriptInterface(new MessageHandlerJsInterface(), NATIVE_JAVASCRIPT_INTERFACE);
    }

    /**
     * Whether to enable the debug log mode.
     *
     * @param debug if {@code true}, it will print some call process logs
     */
    public void enableDebugLogging(boolean debug) {
        mIsDebug = debug;
    }

    /**
     * Add an class object to HCWKWebViewJsBridge with default namespace.
     * <p> This class object is used to implement the handler method of H5 calling native.
     *
     * @param apiObject implementation object of the H5 call native.
     */
    public void addJsBridgeApiObject(Object apiObject) {
        if (apiObject == null) {
            throw new IllegalArgumentException("The apiObject cannot be null");
        }
        mApiHandlerMap.put(DEFAULT_NAMESPACE, apiObject);
    }

    /**
     * Add an class object to HCWKWebViewJsBridge with specified namespace.
     *
     * @param apiObject implementation object of the H5 call native.
     * @param name namespace
     */
    public void addJsBridgeApiObject(Object apiObject, String name) {
        if (apiObject == null) {
            throw new IllegalArgumentException("The apiObject cannot be null");
        }
        String namespace = DEFAULT_NAMESPACE;
        if (name != null) {
            namespace = name;
        }
        mApiHandlerMap.put(namespace, apiObject);
        if (mIsDebug) {
            Log.d(TAG, String.format("Already added the api object(%s) to HCWebViewJsBridge, namespace is %s",
                    apiObject.getClass(),
                    namespace));
        }
    }

    /**
     * Native call H5's already registered handler
     *
     * @param handlerName   specified handler name
     * @param data  send data
     * @param responseHandler   callback
     */
    public void callHandler(String handlerName, Object data, ResponseHandler responseHandler) {
        if (handlerName == null || handlerName.isEmpty()) {
            throw new IllegalArgumentException("The handlerName cannot be null or empty");
        }
        JSONObject msgJsonObject = new JSONObject();
        try {
            msgJsonObject.put(MESSAGE_NAME, handlerName);
            if (data != null) {
                msgJsonObject.put(MESSAGE_DATA, data);
            }
            if (responseHandler != null) {
                String callbackId = WebViewJsBridgeUtils.generateCallbackId();
                mResponseHandlerMap.put(callbackId, responseHandler);
                msgJsonObject.put(MESSAGE_CALLBACK_ID, callbackId);
            }
            String messageJson = msgJsonObject.toString();
            if (startupMessageQueue != null) {
                startupMessageQueue.add(messageJson);
            } else {
                sendMessage(messageJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Method(callHandler) call failed, because " + e.getMessage());
        }
        if (mIsDebug) {
            Log.d(TAG, String.format("Native calling api of js(%s), data is %s, responseCallback is %s",
                    handlerName, data, responseHandler));
        }
    }

    /**
     * Native call H5's already registered handler
     *
     * @param handlerName   specified handler name
     */
    public void callHandler(String handlerName) {
        callHandler(handlerName, null, null);
    }

    /**
     * Native call H5's already registered handler
     *
     * @param handlerName   specified handler name
     * @param data  send data
     */
    public void callHandler(String handlerName, Object data) {
        callHandler(handlerName, data, null);
    }

    /**
     * Native call H5's already registered handler
     *
     * @param handlerName   specified handler name
     * @param responseHandler callback
     */
    public void callHandler(String handlerName, ResponseHandler responseHandler) {
        callHandler(handlerName, null, responseHandler);
    }

    private class MessageHandlerJsInterface {

        @JavascriptInterface
        public void handleMessage(String message) {
            try {
                JSONObject jsonObject = new JSONObject(message);
                String name = jsonObject.getString(MESSAGE_NAME);
                Object data = null;
                if (jsonObject.has(MESSAGE_DATA)) {
                    if (!jsonObject.isNull(MESSAGE_DATA)) {
                        data = jsonObject.get(MESSAGE_DATA);
                    }
                }
                if (mIsDebug) {
                    Log.d(TAG, String.format("Js calling api of native(%s), data is %s", name, data));
                }
                String callbackId = null;
                if (jsonObject.has(MESSAGE_CALLBACK_ID)) {
                    if (!jsonObject.isNull(MESSAGE_CALLBACK_ID)) {
                        callbackId = jsonObject.getString(MESSAGE_CALLBACK_ID);
                    }
                }
                WebViewJsBridgeUtils.ApiName apiName = WebViewJsBridgeUtils.resolveMessageName(name);
                if (apiName == null) {
                    Log.e(TAG, String.format("Namespace parsing failed，please check message name(%s)", name));
                    return;
                }
                final String responseId = callbackId;
                ResponseHandler responseHandler = new ResponseHandler() {
                    @Override
                    public void complete(Object responseData) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(MESSAGE_RESPONSE_ID, responseId);
                            jsonObject.put(MESSAGE_RESPONSE_DATA, responseData);
                            String messageJson = jsonObject.toString();
                            sendMessage(messageJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                WebViewJsBridgeUtils
                        .MethodInvokeResult result =  WebViewJsBridgeUtils.invokeApi(
                                apiName, mApiHandlerMap, data, callbackId, responseHandler);
                switch (result) {
                    case NOT_FOUND_API_OBJECT:
                        Log.e(TAG, String.format("Api Method call failed, because api object not found, please check the message name(%s)", name));
                        break;
                    case NOT_FOUND_METHOD:
                        Log.e(TAG, String.format("Api Method call failed, because method not found, please check the message name(%s)", name));
                        break;
                    case NOT_FOUND_JAVASCRIPT_INTERFACE_ANNOTATION:
                        Log.e(TAG, String.format("Api Method call failed, because javascript interface annotation not found, please check the api object(%s)", name));
                        break;
                    case METHOD_INVOKE_FAIL:
                        Log.e(TAG, String.format("Api Method call failed, please check the message name(%s)", name));
                        break;
                    case METHOD_INVOKE_SUCCESS:
                        if (mIsDebug) {
                            Log.d(TAG, String.format("Js call api of native(%s) succeeded", name));
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void handleResponseMessage(String message) {
            try {
                JSONObject jsonObject = new JSONObject(message);
                String responseId = jsonObject.getString(MESSAGE_RESPONSE_ID);
                final Object responseData = jsonObject.getString(MESSAGE_RESPONSE_DATA);
                if (responseId == null) {
                    Log.e(TAG, "Native callback call failed, native may not require a callback");
                    return;
                }
                final ResponseHandler responseHandler = mResponseHandlerMap.get(responseId);
                if (responseHandler == null) {
                    Log.e(TAG, "Native callback call failed, native may not require a callback");
                    return;
                }
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        responseHandler.complete(responseData);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, String.format("Native callback call failed, native may not require a callback, JSONException:%s", e.getMessage()));
            }
        }

        @JavascriptInterface
        public void handleStartupMessage() {
            if (startupMessageQueue != null) {
                for (String messageJson :
                        startupMessageQueue) {
                    sendMessage(messageJson);
                }
                if (mIsDebug) {
                    Log.d(TAG, "Already called all handler of startup");
                }
                startupMessageQueue = null;
            }
        }
    }

    private void sendMessage(final String messageJson) {
        final String script = String.format(SEND_MESSAGE_SCRIPT_FORMAT, messageJson);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.evaluateJavascript(script, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                    }
                });
            }
        });
    }

}
