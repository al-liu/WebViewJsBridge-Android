package com.lhc.webviewjsbridge;

import android.webkit.JavascriptInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

/**
 * @author  lhc
 */
final class WebViewJsBridgeUtils {

    private static final String CALLBACK_ID_FORMAT = "native_callback_%s";
    private static final String DEFAULT_NAMESPACE = "default";

    private WebViewJsBridgeUtils() {}

    static String generateCallbackId() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        return String.format(CALLBACK_ID_FORMAT, uuid);
    }

    static ApiName resolveMessageName(String messageName) {
        String[] splitMsgName = messageName.split("\\.");
        int splitLength = splitMsgName.length;
        String namespace, methodName;
        if (splitLength == 2) {
            namespace = splitMsgName[0];
            methodName = splitMsgName[1];
        } else if (splitLength == 1) {
            namespace = DEFAULT_NAMESPACE;
            methodName = splitMsgName[0];
        } else {
            return null;
        }
        ApiName apiName = new ApiName();
        apiName.namespace = namespace;
        apiName.methodName = methodName;
        return apiName;
    }

    static MethodInvokeResult invokeApi(ApiName apiName,
                                        Map<String, Object> apiObjectMap,
                                        Object data,
                                        String callbackId,
                                        ResponseHandler responseHandler) {
        Object targetApiObj = apiObjectMap.get(apiName.namespace);
        if (targetApiObj == null) {
            return MethodInvokeResult.NOT_FOUND_API_OBJECT;
        }
        String methodName = apiName.methodName;
        Class<?> clazz = targetApiObj.getClass();
        MethodArgumentType argumentType = decideArgumentType(data, callbackId);
        Method method = null;
        try {
            switch (argumentType) {
                case DATA_AND_CALLBACK:
                    method = clazz.getMethod(methodName, data.getClass(), ResponseHandler.class);
                    break;
                case ONLY_DATA:
                    method = clazz.getMethod(methodName, data.getClass());
                    break;
                case ONLY_CALLBACK:
                    method = clazz.getMethod(methodName, ResponseHandler.class);
                    break;

                case NO_ARGUMENT:
                    method = clazz.getMethod(methodName);
                    break;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return MethodInvokeResult.NOT_FOUND_METHOD;
        }
        JavascriptInterface jsInterfaceAnnotation = method.getAnnotation(JavascriptInterface.class);
        if (jsInterfaceAnnotation == null) {
            return MethodInvokeResult.NOT_FOUND_JAVASCRIPT_INTERFACE_ANNOTATION;
        }

        try {
            switch (argumentType) {
                case DATA_AND_CALLBACK:
                    method.invoke(targetApiObj, data, responseHandler);
                    break;
                case ONLY_DATA:
                    method.invoke(targetApiObj, data);
                    break;
                case ONLY_CALLBACK:
                    method.invoke(targetApiObj, responseHandler);
                    break;

                case NO_ARGUMENT:
                    method.invoke(targetApiObj);
                    break;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return MethodInvokeResult.METHOD_INVOKE_FAIL;
        }
        return MethodInvokeResult.METHOD_INVOKE_SUCCESS;
    }

    private static MethodArgumentType decideArgumentType(Object data, String callbackId) {
        if (data != null && callbackId != null) {
            return MethodArgumentType.DATA_AND_CALLBACK;
        } else if (data != null) {
            return MethodArgumentType.ONLY_DATA;
        } else if (callbackId != null) {
            return MethodArgumentType.ONLY_CALLBACK;
        } else {
            return MethodArgumentType.NO_ARGUMENT;
        }
    }

    public static String escape(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    static class ApiName {
        String namespace;
        String methodName;
    }

    enum MethodArgumentType {
        DATA_AND_CALLBACK,
        ONLY_DATA,
        ONLY_CALLBACK,
        NO_ARGUMENT
    }

    enum MethodInvokeResult {
        NOT_FOUND_API_OBJECT,
        NOT_FOUND_METHOD,
        NOT_FOUND_JAVASCRIPT_INTERFACE_ANNOTATION,
        METHOD_INVOKE_FAIL,
        METHOD_INVOKE_SUCCESS
    }
}
