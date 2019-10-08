# WebViewJsBridge-Android

WebViewJsBridge-Android is a tool library for communication between HTML5 and UIWebView & WKWebView.

WebViewJsBridge-iOS：[https://github.com/al-liu/WebViewJsBridge-iOS](https://github.com/al-liu/WebViewJsBridge-iOS)
[Chinese-Document 中文文档](./README-CH.md)

It is cross-platform supports iOS, Android, JavaScript and easy to use. It is non-intrusive to WebView. Support the use of classes to manage apis, each implementation class corresponds to a unique namespace, such as ui.alert, ui is a namespace, and alert is an implementation method.

Refer to the following diagram:

![WebViewJsBridge-namespace.png](https://i.loli.net/2019/10/08/a8hiDPQNAUOlByo.png)

## Requirements
Support for API 19, Android 4.4 and above.

## Installation

### Gradle

```java
compile 'com.lhc:webviewjsbridge:1.0.0'
```

### Install HCWebViewJsBridge in HTML5
`<script>hcJsBridge.js</script>` in html.

## Example 的说明

The full example is provided in the example module, including basic demos and advanced usage, such as calling the camera to take a picture and using `okhttp` to make a GET request.

## Usage

### Initialize WebViewJsBridge in native

```java
// WebView to enable JavaScript
webSettings.setJavaScriptEnabled(true);
mJsBridge = WebViewJsBridge.newInstance(mMainWebView);
```

### Register implementation class in native

```java
mJsBridge.addJsBridgeApiObject(new UIApiImpl(), "ui");
mJsBridge.addJsBridgeApiObject(new RequestApiImpl(weakReference), "request");
```

#### UIJsApi implementation class

```java
public class UIApiImpl {
    private static final String TAG = "UIApiImpl";
    
    @JavascriptInterface
    public void alert(JSONObject data, ResponseHandler<String> responseHandler){
        responseHandler.complete("native api alert'callback.");
    }
    // The implementation class supports four method signatures：
    // 1. With parameters, with callbacks
    @JavascriptInterface
    public void test1(String data, ResponseHandler<String> responseHandler){
        responseHandler.complete("response data");
    }
    // 2. With parameters, no callbacks
    @JavascriptInterface
    public void test2(JSONObject data){
        Log.d(TAG, String.format("Js native api:test2, data is:%s", data.toString()));
    }
    // 3. No parameters, no callbacks
    @JavascriptInterface
    public void test3(){
        Log.d(TAG, "Js native api:test3");
    }
    // 4. No parameters, with callbacks
    @JavascriptInterface
    public void test4(ResponseHandler<String> responseHandler){
        responseHandler.complete("response data");
    }
}
```

### Calls the HTML5 api in native

```java
mJsBridge.callHandler("test1", "test1 data", new ResponseHandler() {
    @Override
    public void complete(Object responseData) {
        Log.d(TAG, String.format("test1 callback data is:%s", responseData));
    }
});
```

### Initialize WebViewJsBridge in HTML5

```js
<!DOCTYPE html>
<html>
    <head>
        ...
        <script src="./hcJsBridge.js"> </script>
    </head>
    ...
</html>
```

### Register apis for native call in HTML5

```js
hcJsBridge.registerHandler("testCallJs", function(data, callback) {
    log('Native call js ,handlename is testCallJs, data is:', data);
    callback('callback native, handlename is testCallJs');
})
```

### Calls native api in HTML5

```js
var data = {foo: "bar"};
hcJsBridge.callHandler('ui.alert', data, function (responseData) {
    log('Js receives the response data returned by native, response data is', responseData);
})
```

### Turn on the debug log

Turn on the debug log and print some call information to help troubleshoot the issue. The debug log is not enabled by default.

```java
mJsBridge.enableDebugLogging(true);
```

## License
WebViewJsBridge-Android is released under the MIT license. See [LICENSE](./LICENSE) for details.


