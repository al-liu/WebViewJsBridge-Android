# WebViewJsBridge-Android

WebViewJsBridge-Android 是 HTML5 和 WebView 之间用于通讯的工具库。

WebViewJsBridge-iOS：[https://github.com/al-liu/WebViewJsBridge-iOS](https://github.com/al-liu/WebViewJsBridge-iOS)

它的特点是跨平台，支持 iOS，Android，JavaScript，接口统一，简单易用。工具库的实现对 WebView 无侵入性。使用以类的方式来管理通信的接口，每个接口的实现类对应唯一的命名空间，如 ui.alert，ui 对应一个实现类的命名空间，alert 是该实现类的一个实现方法。

下面这张图帮助理解它们之间的关系：

![WebViewJsBridge-namespace.png](https://i.loli.net/2019/10/08/a8hiDPQNAUOlByo.png)

## 系统版本要求
支持 API 19，Android4.4及以上的系统版本。

## 安装

### Gradle

```java
compile 'com.lhc:webviewjsbridge:1.0.0'
```

### 引入 WebViewJsBridge 的 js 文件
在 html 中 `<script>引入 hcJsBridge.js</script>`。

## Example 的说明
example 模块中提供完整使用示例，包括基础的调用演示和进阶用法，如，调用相机拍摄一张图片，使用 `okhttp` 发起一个 GET 请求。

## 使用方法

### 初始化原生的 WebViewJsBridge 环境

```java
// WebView 要开启 JavaScriptEnabled
webSettings.setJavaScriptEnabled(true);
mJsBridge = WebViewJsBridge.newInstance(mMainWebView);
```

### 原生注册接口实现类供 HTML5 调用

```java
mJsBridge.addJsBridgeApiObject(new UIApiImpl(), "ui");
mJsBridge.addJsBridgeApiObject(new RequestApiImpl(weakReference), "request");
```

UIApiImpl 的实现类：

```java
public class UIApiImpl {
    private static final String TAG = "UIApiImpl";
    
    @JavascriptInterface
    public void alert(JSONObject data, ResponseHandler<String> responseHandler){
        responseHandler.complete("native api alert'callback.");
    }
    // 实现类支持四种方法签名：
    // 有参数，有回调
    @JavascriptInterface
    public void test1(String data, ResponseHandler<String> responseHandler){
        responseHandler.complete("response data");
    }
    // 有参数，无回调
    @JavascriptInterface
    public void test2(JSONObject data){
        Log.d(TAG, String.format("Js native api:test2, data is:%s", data.toString()));
    }
    // 无参数，无回调
    @JavascriptInterface
    public void test3(){
        Log.d(TAG, "Js native api:test3");
    }
    // 无参数，有回调
    @JavascriptInterface
    public void test4(ResponseHandler<String> responseHandler){
        responseHandler.complete("response data");
    }
}
```

### 原生调用 HTML5 接口

```java
mJsBridge.callHandler("test1", "test1 data", new ResponseHandler() {
    @Override
    public void complete(Object responseData) {
        Log.d(TAG, String.format("test1 callback data is:%s", responseData));
    }
});
```

### 初始化 HTML5 的 WebViewJsBridge 环境

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

### HTML5 注册接口供原生调用

```js
hcJsBridge.registerHandler("testCallJs", function(data, callback) {
    log('Native call js ,handlename is testCallJs, data is:', data);
    callback('callback native, handlename is testCallJs');
})
```

### HTML5 调用原生接口

```js
var data = {foo: "bar"};
hcJsBridge.callHandler('ui.alert', data, function (responseData) {
    log('Js receives the response data returned by native, response data is', responseData);
})
```

### 开启 debug 日志

开启 debug 日志，将打印一些调用信息，辅助排查问题。debug 日志默认不开启。

```java
mJsBridge.enableDebugLogging(true);
```

## License
WebViewJsBridge-Android 使用 MIT license 发布，查看 [LICENSE](./LICENSE)  详情。


