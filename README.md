# WebViewJsBridge-Android

[![](https://img.shields.io/badge/build-pass-green)](https://github.com/wendux/DSBridge-Android) [![](https://img.shields.io/badge/language-Java-brightgreen)](https://github.com/wendux/DSBridge-Android) [![](https://img.shields.io/badge/minSdkVersion-19-orange)](https://github.com/wendux/DSBridge-Android) [![](https://img.shields.io/github/license/al-liu/WebViewJsBridge-Android)](./LICENSE)

WebViewJsBridge-Android 是 JavaScript 与 Android WebView 通信的桥接库。

配套的 iOS 版本：[https://github.com/al-liu/WebViewJsBridge-iOS](https://github.com/al-liu/WebViewJsBridge-iOS)

本桥接库的特点是跨平台，支持 iOS，Android。Js 对 iOS，Android 的接口调用统一，简单易用。支持将客户端接口划分到多个实现类中进行管理，用命名空间加以区分，如 ui.alert，ui 对应一个实现类的命名空间，alert 是该实现类的一个实现方法。

## 系统版本要求
支持 API 19，Android4.4及以上的系统版本。

## 安装

### Gradle

```java
compile 'com.lhc:webviewjsbridge:1.1.1'
```

### 手动安装
下载 webviewjsbridge 的源代码，并添加到自己的项目中即可使用。

### 引入 hcJsBridge.min.js 文件
在 html 文件中引用 js： `<script>./hcJsBridge.min.js</script>` 。

#### 在 vue 项目中引用 hcJsBridge.min.js
引入方法：`import './hcJsBridge.min.js'`
import js 文件后，即可用 `window.hcJsBridge` 调用客户端接口。
如果不想用 `window.hcJsBridge` 的方式调用，可以使用 vue 插件。

```js
var jsBridge = {}
jsBridge.install = function (Vue, options) {
  Vue.prototype.$hcJsBridge = window.hcJsBridge
}
export default jsBridge
```

然后在 main.js 中：

```js
import JsBridge from './plugins/jsBridge'
Vue.use(JsBridge)
```

在 vue 实例中使用：

```js
this.$hcJsBridge.callHandler('ui.alert', data, function (responseData) {
    if (responseData === 'ok') {
      affirm()
    } else {
      cancel()
    }
})
```

## Example 的说明
example 模块中提供完整使用示例，包括基础的调用演示和进阶用法，如，调用相机拍摄一张图片，使用 `okhttp` 发起一个 GET 请求。

## 使用方法

### 初始化原生的 WebViewJsBridge 环境

```java
// WebView 要开启 JavaScriptEnabled
webSettings.setJavaScriptEnabled(true);
mJsBridge = WebViewJsBridge.newInstance(mMainWebView);
```

**如果 H5 不引入 hcJsBridge.js，则还需要在 onPageFinished 中调用 injectWebViewJavascript 方法**

```java
mMainWebView.setWebViewClient(new WebViewClient() {
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mJsBridge.injectWebViewJavascript();
    }
});
```

### 原生注册接口供 HTML5 调用

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
    // 实现类支持四种方法签名（Js 在调用时要遵循对应的方法签名）：
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

初始化环境需要引入  `hcJsBridge.min.js` 文件，引用方式前面有介绍。

**如果 H5 不引入 hcJsBridge.min.js，则需要使用下面的方式注册接口。**

```js
// 在这个 window._hcJsBridgeInitFinished 全局函数中等待 bridge 初始化完成，然后注册接口，初始调用。
window._hcJsBridgeInitFinished = function(bridge) {
    // 注册接口给原生调用
    bridge.registerHandler("test1", function(data, callback) {
        callback('callback native,handlename is test1');
    })
    
    // 调用原生接口
    bridge.callHandler('ui.test3');
}
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


